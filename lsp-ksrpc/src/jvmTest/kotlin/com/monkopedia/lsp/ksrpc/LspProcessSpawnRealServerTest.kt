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
import java.io.File
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

/**
 * JVM real-server integration for the UNIFIED [spawnLspServer]: spawn an actual external
 * `clangd` via the cross-platform [spawnLspServer] entry point (the same `expect`/`actual`
 * declaration the native `LspProcessRealServerTest` drives), run a realistic
 * initialize -> initialized -> didOpen -> definition sequence, and tear the child down via
 * the unified [LspServerProcess.kill] / [LspServerProcess.close] handle — no reliance on a
 * clean shutdown round-trip.
 *
 * This mirrors the native `LspProcessRealServerTest` so the two platforms exercise the
 * identical unified surface, and it is the proof that the JVM `spawnLspServer` actual
 * carries a real third-party LSP workload and tears down deterministically (the JVM
 * finally now gets a real process handle, not just a connection).
 *
 * ## Gating & boundedness
 *
 * clangd isn't guaranteed installed everywhere, so by default a missing clangd SKIPS
 * cleanly via [requireRealServerOrSkip]. With `-Plsp.requireRealServers=true` (the
 * dedicated real-server job) a missing clangd is a hard failure instead.
 *
 * Every wire call is wrapped in [withTimeout]; the child is unconditionally killed and
 * its streams closed in a `finally` ([LspServerProcess.close]); the drive runs in a
 * detached [SupervisorJob] scope so a wedged ksrpc pump can't keep the worker JVM alive
 * (issue #79); and the jvmTest task carries a 5-minute Gradle timeout. A wedge fails
 * bounded rather than hanging the build.
 */
class LspProcessSpawnRealServerTest : JvmIntegrationTestBase() {

    @Test
    fun `clangd unified spawnLspServer client-role sequence`() {
        requireRealServerOrSkip("clangd not on PATH", isOnPath("clangd"))
        runBlocking(Dispatchers.IO) { driveBounded() }
    }

    /**
     * Drive in a scope detached from the enclosing `runBlocking`, awaiting only a
     * [CompletableDeferred] the drive completes — never joining the detached scope whose
     * children include the (possibly wedged) ksrpc stdout pump (issue #79).
     */
    private suspend fun driveBounded() {
        val driveScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val done = CompletableDeferred<Unit>()
        driveScope.launch {
            try {
                drive()
                done.complete(Unit)
            } catch (t: Throwable) {
                done.completeExceptionally(t)
            }
        }
        try {
            done.await()
        } finally {
            driveScope.cancel()
        }
    }

    private suspend fun drive() {
        val sourceFile = fixtureFile("fixtures/c/main.c")
        assertTrue(sourceFile.exists(), "fixture missing: ${sourceFile.path}")
        val uri = fileUri(sourceFile)
        val rootUri = fileUri(sourceFile.parentFile).trimEnd('/')

        val client = object : DefaultLanguageClient() {
            override suspend fun windowLogMessage(params: LogMessageParams) = Unit
            override suspend fun windowShowMessage(params: ShowMessageParams) = Unit
            override suspend fun textDocumentPublishDiagnostics(params: PublishDiagnosticsParams) =
                Unit
        }

        val proc = spawnLspServer(listOf("clangd", "--log=error"))
        assertTrue(proc.pid > 0, "spawnLspServer must report a real pid; got ${proc.pid}")
        try {
            val server = proc.connection.connectAsLspClient(client)

            withTimeout(BUDGET_MS) {
                val init = server.initialize(
                    InitializeParams(
                        capabilities = ClientCapabilities(),
                        processId = ProcessHandle.current().pid().toInt(),
                        rootUri = rootUri
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
                            text = sourceFile.readText()
                        )
                    )
                )

                // `    int total = add(2, 3);` — the "add" call on line 8; definition
                // must resolve to the declaration on line 3.
                val definition = retry {
                    server.textDocumentDefinition(
                        DefinitionParams(
                            textDocument = TextDocumentIdentifier(uri),
                            position = Position(line = 8u, character = 17u)
                        )
                    ).takeIf { it.locations().isNotEmpty() }
                }
                assertNotNull(definition, "clangd: definition returned no locations")
                val lines = definition.locations().map { it.range.start.line }
                assertTrue(
                    lines.any { it == 3u },
                    "clangd: definition did not resolve to declaration line 3; got $lines"
                )
            }
        } finally {
            // Bounded teardown via the unified handle: kill the child + close streams.
            proc.close()
        }
    }

    private companion object {
        const val BUDGET_MS = 60_000L
    }
}

private fun fixtureFile(relative: String): File {
    val candidates = listOf(
        "lsp-ksrpc/build/resources/jvmTest/$relative",
        "lsp-ksrpc/src/jvmTest/resources/$relative",
        "build/resources/jvmTest/$relative",
        "src/jvmTest/resources/$relative"
    )
    for (path in candidates) {
        val f = File(path)
        if (f.exists()) return f.canonicalFile
    }
    error("fixture '$relative' not found in any of: ${candidates.joinToString()}")
}

private fun fileUri(file: File): String = URI("file", "", file.absoluteFile.path, null).toString()

private fun isOnPath(binary: String): Boolean {
    val path = System.getenv("PATH") ?: return false
    return path.split(File.pathSeparator).asSequence()
        .map { File(it, binary) }
        .any { it.canExecute() }
}

private fun TextDocumentDefinitionResult.locations(): List<Location> = when (this) {
    is TextDocumentDefinitionResult.DefinitionValue -> when (val v = value) {
        is SingleOrArray.Single -> listOf(v.value)
        is SingleOrArray.Multiple -> v.value
    }

    is TextDocumentDefinitionResult.DefinitionLinkArray ->
        value.map { Location(uri = it.targetUri, range = it.targetRange) }
}

private suspend fun <T : Any> retry(attempts: Int = 8, block: suspend () -> T?): T? {
    repeat(attempts) {
        runCatching { block() }.getOrNull()?.let { return it }
        delay(1_000)
    }
    return runCatching { block() }.getOrNull()
}
