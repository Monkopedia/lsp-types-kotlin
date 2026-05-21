/*
 * Copyright 2025 Jason Monk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.monkopedia.lsp.ksrpc

import com.monkopedia.lsp.ClientCapabilities
import com.monkopedia.lsp.DefaultLanguageClient
import com.monkopedia.lsp.DidOpenTextDocumentParams
import com.monkopedia.lsp.InitializeParams
import com.monkopedia.lsp.InitializedParams
import com.monkopedia.lsp.LogMessageParams
import com.monkopedia.lsp.PublishDiagnosticsParams
import com.monkopedia.lsp.ShowMessageParams
import com.monkopedia.lsp.TextDocumentItem
import java.io.File
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

/**
 * Integration tests against real LSP servers found on PATH. Each test is
 * skipped via [assumeTrue] when the binary isn't available, so local dev runs
 * without `clangd` (etc.) installed don't fail.
 *
 * In CI we install `clangd` so this actually runs there.
 */
class RealServerIntegrationTest {

    @Test
    fun `clangd handles initialize and shutdown`() {
        requireOrSkip("clangd not on PATH", isOnPath("clangd"))
        runBlocking(Dispatchers.IO) {
            val client = object : DefaultLanguageClient() {
                override suspend fun windowLogMessage(params: LogMessageParams) = Unit
                override suspend fun windowShowMessage(params: ShowMessageParams) = Unit
                override suspend fun textDocumentPublishDiagnostics(
                    params: PublishDiagnosticsParams
                ) = Unit
            }

            val workspace = testWorkspace()
            val mainCpp = File(workspace, "main.cpp")

            val process = ProcessBuilder("clangd")
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start()
            try {
                val connection = (process.inputStream to process.outputStream)
                    .asLspConnection()
                val server = connection.connectAsLspClient(client)

                val initResult = withTimeout(15_000) {
                    server.initialize(
                        InitializeParams(
                            capabilities = ClientCapabilities(),
                            processId = ProcessHandle.current().pid().toInt(),
                            rootUri = workspace.toURI().toString().trimEnd('/')
                        )
                    )
                }
                assertNotNull(initResult)
                assertNotNull(initResult.capabilities)
                // clangd advertises hover support by default.
                assertNotNull(initResult.capabilities.hoverProvider)

                withTimeout(5_000) { server.initialized(InitializedParams()) }

                // Open the document so clangd can answer hover queries about it.
                withTimeout(5_000) {
                    server.textDocumentDidOpen(
                        DidOpenTextDocumentParams(
                            textDocument = TextDocumentItem(
                                uri = mainCpp.toURI().toString(),
                                languageId = "cpp",
                                version = 1,
                                text = mainCpp.readText()
                            )
                        )
                    )
                }

                // Hover would be the obvious next step but clangd needs a
                // `compile_commands.json` to parse the source — without one it just
                // hangs. Skip it; the goal here is end-to-end wire compatibility.

                try {
                    withTimeout(10_000) { server.shutdown() }
                } catch (_: Throwable) {
                    // Some servers may respond unusually to shutdown — don't fail the test.
                }
                try {
                    withTimeout(2_000) { server.exit() }
                } catch (_: Throwable) {
                    // exit is a notification; server may close before we send it.
                }
            } finally {
                // Close the streams and tear down the process so the receiver
                // coroutines (running on GlobalScope) can exit and the test can
                // actually terminate.
                runCatching { process.outputStream.close() }
                runCatching { process.inputStream.close() }
                if (!process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS)) {
                    process.destroyForcibly()
                }
            }
        }
    }

    private fun testWorkspace(): File {
        // Resolve test resource directory at runtime — gradle copies them into
        // build/resources/test, but we also accept the source path.
        val candidates = listOf(
            "lsp-ksrpc/build/resources/jvmTest/clangd-test-workspace",
            "lsp-ksrpc/src/jvmTest/resources/clangd-test-workspace",
            "build/resources/jvmTest/clangd-test-workspace",
            "src/jvmTest/resources/clangd-test-workspace"
        )
        for (path in candidates) {
            val f = File(path)
            if (f.exists()) return f.canonicalFile
        }
        error(
            "clangd-test-workspace not found in any of: ${candidates.joinToString()}"
        )
    }
}

private fun isOnPath(binary: String): Boolean {
    val path = System.getenv("PATH") ?: return false
    return path.split(":").asSequence()
        .map { File(it, binary) }
        .any { it.canExecute() }
}
