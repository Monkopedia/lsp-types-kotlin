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
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

/**
 * Drives real clangd through the published convenience entry point
 * [ProcessBuilder.asLspConnection] — the shape its own KDoc advertises — rather than the
 * `Pair` overload every other clangd test uses (issue #164).
 *
 * `initialize` alone would pass against any LSP-speaking child, so the test also asks
 * for a `textDocument/definition` whose answer only a real C front end can produce: the
 * `add` call on line 8 of `fixtures/c/main.c` must resolve to its declaration on line 3.
 *
 * ## Teardown
 *
 * This entry point returns a connection and no process handle, so the child is found
 * test-side: the `clangd` children of this JVM that appear across the call. The
 * `finally` destroys exactly those, waits a bounded time for each to exit, and asserts
 * none of them is still alive.
 *
 * ## Gating
 *
 * Behind [requireOrSkip], like [RealServerIntegrationTest]: a missing clangd skips
 * locally and hard-fails under `-Plsp.requireIntegrationTests=true`, which the per-PR
 * build job sets on the step that runs `jvmTest`.
 *
 * No `CoroutineScope` is constructed here: both overloads of `asLspConnection` already
 * host their pumps in connection-owned scopes, so a pump parked on a dead pipe is not a
 * child of this `runBlocking`. Every wire call is bounded by [withTimeout].
 */
class ProcessBuilderRealServerTest : JvmIntegrationTestBase() {

    @Test
    fun `clangd answers definition over ProcessBuilder asLspConnection`() {
        requireOrSkip("clangd not on PATH", isOnPath("clangd"))
        val before = clangdChildren()
        val spawned = mutableSetOf<ProcessHandle>()
        try {
            runBlocking(Dispatchers.IO) { drive(before, spawned) }
        } finally {
            spawned.forEach { it.destroy() }
            spawned.forEach { child ->
                runCatching { child.onExit().get(GRACE_S, TimeUnit.SECONDS) }.onFailure {
                    child.destroyForcibly()
                    child.onExit().get(GRACE_S, TimeUnit.SECONDS)
                }
            }
        }
        // Check only the child THIS test spawned. Comparing the whole set of clangd children
        // against `before` would fail spuriously if an earlier test's clangd, still exiting
        // when `before` was taken, finished exiting during this test.
        assertTrue(
            spawned.none { it.isAlive },
            "clangd child left running after teardown: ${spawned.filter { it.isAlive }}"
        )
    }

    private suspend fun drive(before: Set<ProcessHandle>, spawned: MutableSet<ProcessHandle>) {
        val sourceFile = fixtureFile("fixtures/c/main.c")
        val uri = fileUri(sourceFile)
        val rootUri = fileUri(sourceFile.parentFile).trimEnd('/')

        val connection = ProcessBuilder("clangd", "--log=error")
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .asLspConnection()
        spawned += clangdChildren() - before
        assertEquals(1, spawned.size, "expected exactly one new clangd child; got $spawned")

        val client = object : DefaultLanguageClient() {
            override suspend fun windowLogMessage(params: LogMessageParams) = Unit
            override suspend fun windowShowMessage(params: ShowMessageParams) = Unit
            override suspend fun textDocumentPublishDiagnostics(params: PublishDiagnosticsParams) =
                Unit
        }
        val server = connection.connectAsLspClient(client)

        withTimeout(BUDGET_MS) {
            val init = server.initialize(
                InitializeParams(
                    capabilities = ClientCapabilities(),
                    processId = ProcessHandle.current().pid().toInt(),
                    rootUri = rootUri
                )
            )
            assertNotNull(init.capabilities.definitionProvider, "clangd: no definitionProvider")
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

            // `    int total = add(2, 3);` (line 8): the `add` call resolves to line 3.
            val definition = retry {
                server.textDocumentDefinition(
                    DefinitionParams(
                        textDocument = TextDocumentIdentifier(uri),
                        position = Position(line = 8u, character = 17u)
                    )
                )?.locations()?.takeIf { it.isNotEmpty() }
            }
            assertNotNull(definition, "clangd: definition returned no locations")
            val lines = definition.map { it.range.start.line }
            assertTrue(
                lines.any { it == 3u },
                "clangd: definition did not resolve to declaration line 3; got $lines"
            )
        }
    }

    private fun clangdChildren(): Set<ProcessHandle> = ProcessHandle.current().children()
        .filter { child ->
            child.info().command().map { File(it).name == "clangd" }.orElse(false)
        }
        .toList()
        .toSet()

    private companion object {
        const val BUDGET_MS = 60_000L
        const val GRACE_S = 5L
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
