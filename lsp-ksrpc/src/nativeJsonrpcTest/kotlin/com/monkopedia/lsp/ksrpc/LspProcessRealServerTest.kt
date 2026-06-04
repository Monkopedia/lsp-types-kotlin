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
@file:OptIn(ExperimentalForeignApi::class)

package com.monkopedia.lsp.ksrpc

import com.monkopedia.lsp.ClientCapabilities
import com.monkopedia.lsp.DefaultLanguageClient
import com.monkopedia.lsp.DefinitionParams
import com.monkopedia.lsp.DidOpenTextDocumentParams
import com.monkopedia.lsp.InitializeParams
import com.monkopedia.lsp.InitializedParams
import com.monkopedia.lsp.Location
import com.monkopedia.lsp.LogMessageParams
import com.monkopedia.lsp.Position
import com.monkopedia.lsp.PublishDiagnosticsParams
import com.monkopedia.lsp.ShowMessageParams
import com.monkopedia.lsp.SingleOrArray
import com.monkopedia.lsp.TextDocumentDefinitionResult
import com.monkopedia.lsp.TextDocumentIdentifier
import com.monkopedia.lsp.TextDocumentItem
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import platform.posix.F_OK
import platform.posix.X_OK
import platform.posix.access
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite
import platform.posix.getenv
import platform.posix.getpid

/**
 * Native (Kotlin/Native: linuxX64 + macOS) real-server integration: spawn an actual
 * external `clangd` over a posix-stdio pipe via [spawnLspServer] (the native analog of
 * the JVM `ProcessBuilder.asLspConnection`), drive a realistic
 * initialize -> initialized -> didOpen -> definition/hover sequence, and tear the child
 * down by KILLING the process (no reliance on a clean shutdown round-trip).
 *
 * This is the native counterpart of the JVM `RealServerClientRoleTest` and the real
 * external-process leg that #47 deferred. It is the proof that the posix-stdio transport
 * carries a real third-party LSP workload on native and tears down without hanging
 * (relies on the ksrpc >= 1.1.0 #201 fix: the posix reader thread breaks on EOF).
 *
 * ## Gating & boundedness
 *
 * clangd isn't guaranteed installed everywhere, so by default a missing clangd SKIPS
 * cleanly (the test returns green). When `LSP_REQUIRE_REAL_SERVERS=true` is set in the
 * environment (the build forwards `-Plsp.requireRealServers` to native test tasks as
 * that env var), a missing clangd is a hard failure instead — same contract as the JVM
 * `requireRealServerOrSkip` gate.
 *
 * Every wire call is wrapped in [withTimeout], and the child is unconditionally killed
 * and its pipe fds closed in a `finally` ([LspServerProcess.close]). The native test
 * task additionally carries a 5-minute Gradle timeout. So a wedge fails bounded rather
 * than hanging the build.
 */
class LspProcessRealServerTest {

    // runBlocking (not runTest): this drives a REAL external clangd over a posix-stdio
    // pipe, so it needs real wall-clock time and a real dispatcher — runTest's virtual
    // clock would skip the delays/timeouts and never actually await the child's replies.
    @Test
    fun `clangd native client-role sequence over posix stdio`() = runBlocking(Dispatchers.Default) {
        val clangd = "clangd"
        if (!isOnPath(clangd)) {
            requireRealServerOrSkipNative("clangd not on PATH")
            return@runBlocking
        }

        run {
            val sourcePath = writeFixture()
            val uri = "file://$sourcePath"

            val client = object : DefaultLanguageClient() {
                override suspend fun windowLogMessage(params: LogMessageParams) = Unit
                override suspend fun windowShowMessage(params: ShowMessageParams) = Unit
                override suspend fun textDocumentPublishDiagnostics(
                    params: PublishDiagnosticsParams
                ) = Unit
            }

            val proc = spawnLspServer(listOf(clangd, "--log=error"))
            try {
                val server = proc.connection.connectAsLspClient(client)

                withTimeout(BUDGET_MS) {
                    val init = server.initialize(
                        InitializeParams(
                            capabilities = ClientCapabilities(),
                            processId = getpid(),
                            rootUri = "file://${tmpDir()}"
                        )
                    )
                    assertNotNull(init.capabilities, "clangd: null server capabilities")

                    server.initialized(InitializedParams())

                    server.textDocumentDidOpen(
                        DidOpenTextDocumentParams(
                            textDocument = TextDocumentItem(
                                uri = uri,
                                languageId = "c",
                                version = 1,
                                text = FIXTURE_C
                            )
                        )
                    )

                    // `    int total = add(2, 3);` — the "add" call on line 7;
                    // definition must resolve to the declaration on line 2.
                    val definition = retry {
                        server.textDocumentDefinition(
                            DefinitionParams(
                                textDocument = TextDocumentIdentifier(uri),
                                position = Position(line = 7u, character = 16u)
                            )
                        )?.takeIf { it.locations().isNotEmpty() }
                    }
                    assertNotNull(definition, "clangd: definition returned no locations")
                    val lines = definition.locations().map { it.range.start.line }
                    assertTrue(
                        lines.any { it == 2u },
                        "clangd: definition did not resolve to declaration line 2; got $lines"
                    )
                }
            } finally {
                // Bounded teardown: kill the child + close pipe fds. Never a clean
                // shutdown round-trip — that depends on the server cooperating, which
                // a hang test must not rely on.
                proc.close()
            }
        }
    }

    private fun writeFixture(): String {
        val path = "${tmpDir()}/lsp_native_clangd_${getpid()}.c"
        val file = fopen(path, "w") ?: error("could not create fixture at $path")
        try {
            val bytes = FIXTURE_C.encodeToByteArray()
            if (bytes.isNotEmpty()) {
                bytes.usePinned { pinned ->
                    fwrite(pinned.addressOf(0), 1u, bytes.size.toULong(), file)
                }
            }
        } finally {
            fclose(file)
        }
        check(access(path, F_OK) == 0) { "fixture not written: $path" }
        return path
    }

    private companion object {
        const val BUDGET_MS = 60_000L

        val FIXTURE_C = """
            /* Tiny self-contained fixture for clangd native client-role integration. */

            int add(int a, int b) {
                return a + b;
            }

            int main(void) {
                int total = add(2, 3);
                return total;
            }
        """.trimIndent()
    }
}

/**
 * Native gate analog of `requireRealServerOrSkip`: when `LSP_REQUIRE_REAL_SERVERS=true`
 * a missing precondition is a hard failure; otherwise it's a clean skip (the caller
 * returns without asserting).
 */
private fun requireRealServerOrSkipNative(message: String) {
    if (getenv("LSP_REQUIRE_REAL_SERVERS")?.toKString() == "true") {
        error("Real-server precondition not met (LSP_REQUIRE_REAL_SERVERS): $message")
    }
    // Otherwise skip cleanly — the caller returns green.
}

private fun tmpDir(): String = getenv("TMPDIR")?.toKString()?.trimEnd('/') ?: "/tmp"

private fun isOnPath(binary: String): Boolean {
    val path = getenv("PATH")?.toKString() ?: return false
    return path.split(':').any { dir ->
        dir.isNotEmpty() && access("$dir/$binary", X_OK) == 0
    }
}

private fun TextDocumentDefinitionResult.locations() = when (this) {
    is TextDocumentDefinitionResult.DefinitionValue -> when (val v = value) {
        is SingleOrArray.Single -> listOf(v.value)
        is SingleOrArray.Multiple -> v.value
    }

    is TextDocumentDefinitionResult.DefinitionLinkArray ->
        value.map { Location(uri = it.targetUri, range = it.targetRange) }
}

/** Retry a flaky query to let clangd settle (it parses lazily on first query). */
private suspend fun <T : Any> retry(attempts: Int = 8, block: suspend () -> T?): T? {
    repeat(attempts) {
        runCatching { block() }.getOrNull()?.let { return it }
        delay(1_000)
    }
    return runCatching { block() }.getOrNull()
}
