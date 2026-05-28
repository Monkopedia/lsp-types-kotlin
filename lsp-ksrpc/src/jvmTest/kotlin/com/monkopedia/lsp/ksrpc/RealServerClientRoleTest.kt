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

import com.monkopedia.lsp.BooleanOr
import com.monkopedia.lsp.ClientCapabilities
import com.monkopedia.lsp.DefaultLanguageClient
import com.monkopedia.lsp.DefinitionParams
import com.monkopedia.lsp.DidOpenTextDocumentParams
import com.monkopedia.lsp.DocumentSymbolParams
import com.monkopedia.lsp.HoverParams
import com.monkopedia.lsp.InitializeParams
import com.monkopedia.lsp.InitializeResult
import com.monkopedia.lsp.InitializedParams
import com.monkopedia.lsp.KsrpcLanguageServer
import com.monkopedia.lsp.Location
import com.monkopedia.lsp.LogMessageParams
import com.monkopedia.lsp.Position
import com.monkopedia.lsp.PublishDiagnosticsParams
import com.monkopedia.lsp.ReferenceContext
import com.monkopedia.lsp.ReferenceParams
import com.monkopedia.lsp.ShowMessageParams
import com.monkopedia.lsp.SingleOrArray
import com.monkopedia.lsp.TextDocumentDefinitionResult
import com.monkopedia.lsp.TextDocumentDocumentSymbolResult
import com.monkopedia.lsp.TextDocumentIdentifier
import com.monkopedia.lsp.TextDocumentItem
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

/**
 * Client-role integration matrix: drive REAL external language servers through a
 * realistic initialize → initialized → didOpen → query → shutdown → exit
 * sequence and assert structural expectations (e.g. a symbol's definition
 * resolves to its declaration line; hover is non-null).
 *
 * Every fixture under `src/jvmTest/resources/fixtures/<lang>/` declares the same
 * shape: an `add(a, b)` function declared near the top and called from `main`,
 * so `definition` from the call site must land on the declaration line and
 * `references` must report at least the declaration plus the call site.
 *
 * These tests are gated behind [requireRealServerOrSkip] (the
 * `-Plsp.requireRealServers` flag), which is INDEPENDENT of the
 * `-Plsp.requireIntegrationTests` gate. With `requireRealServers` off, a server
 * that isn't on PATH skips cleanly via `assumeTrue` — so per-PR CI (which sets
 * `requireIntegrationTests=true` but NOT `requireRealServers`) stays green even
 * without these external servers installed. The dedicated real-server job sets
 * `requireRealServers=true` to turn a missing server into a hard failure.
 *
 * Assertions are deliberately resilient to version drift: we never assert exact
 * hover text or completion contents, only structural facts.
 */
class RealServerClientRoleTest {

    @Test
    fun `clangd client-role sequence`() {
        runServerMatrix(
            ServerSpec(
                binary = "clangd",
                command = listOf("clangd", "--log=error"),
                languageId = "c",
                fixtureDir = "fixtures/c",
                fixtureFile = "main.c",
                // `    int total = add(2, 3);` — the "add" identifier at the call.
                callSite = Position(line = 8u, character = 17u),
                declarationLine = 3u
            )
        )
    }

    @Test
    fun `pyright client-role sequence`() {
        runServerMatrix(
            ServerSpec(
                binary = "pyright-langserver",
                command = listOf("pyright-langserver", "--stdio"),
                languageId = "python",
                fixtureDir = "fixtures/py",
                fixtureFile = "main.py",
                // `    total = add(2, 3)` — the "add" identifier at the call.
                callSite = Position(line = 8u, character = 12u),
                declarationLine = 3u
            )
        )
    }

    @Test
    fun `typescript-language-server client-role sequence`() {
        runServerMatrix(
            ServerSpec(
                binary = "typescript-language-server",
                command = listOf("typescript-language-server", "--stdio"),
                languageId = "typescript",
                fixtureDir = "fixtures/ts",
                fixtureFile = "main.ts",
                // `    const total = add(2, 3);` — the "add" identifier at the call.
                callSite = Position(line = 7u, character = 18u),
                declarationLine = 2u
            )
        )
    }

    @Test
    fun `gopls client-role sequence`() {
        runServerMatrix(
            ServerSpec(
                binary = "gopls",
                command = listOf("gopls"),
                languageId = "go",
                fixtureDir = "fixtures/go",
                fixtureFile = "main.go",
                // `\ttotal := add(2, 3)` — the "add" identifier at the call.
                callSite = Position(line = 8u, character = 11u),
                declarationLine = 3u,
                // gopls runs `go list`/indexes the module before answering.
                budgetMillis = 90_000
            )
        )
    }

    @Test
    fun `rust-analyzer client-role sequence`() {
        runServerMatrix(
            ServerSpec(
                binary = "rust-analyzer",
                command = listOf("rust-analyzer"),
                languageId = "rust",
                fixtureDir = "fixtures/rs",
                fixtureFile = "src/main.rs",
                // `    let total = add(2, 3);` — the "add" identifier at the call.
                callSite = Position(line = 7u, character = 16u),
                declarationLine = 2u,
                // rust-analyzer runs `cargo metadata` and a full index pass
                // before it can answer semantic queries.
                budgetMillis = 90_000
            )
        )
    }

    private fun runServerMatrix(spec: ServerSpec) {
        requireRealServerOrSkip("${spec.binary} not on PATH", isOnPath(spec.binary))
        // A server can be on PATH yet non-functional in this environment (e.g.
        // rust-analyzer with no toolchain/network, gopls with no module cache).
        // With `requireRealServers` OFF this is best-effort, so a drive failure
        // (timeout, dead server, missing structure) skips cleanly. With the flag
        // ON — the dedicated job where servers are guaranteed usable — it's a
        // hard failure.
        val failure = runCatching { runBlocking(Dispatchers.IO) { driveServer(spec) } }
            .exceptionOrNull()
        if (failure != null) {
            requireRealServerOrSkip(
                "${spec.binary} did not complete the client-role drive: $failure",
                condition = false
            )
        }
    }

    private suspend fun driveServer(spec: ServerSpec) {
        val client = object : DefaultLanguageClient() {
            override suspend fun windowLogMessage(params: LogMessageParams) = Unit
            override suspend fun windowShowMessage(params: ShowMessageParams) = Unit
            override suspend fun textDocumentPublishDiagnostics(params: PublishDiagnosticsParams) =
                Unit
        }

        val workspace = fixtureDir(spec.fixtureDir)
        val sourceFile = File(workspace, spec.fixtureFile)
        assertTrue(sourceFile.exists(), "fixture missing: ${sourceFile.path}")
        val uri = sourceFile.toURI().toString()

        val process = ProcessBuilder(spec.command)
            .redirectInput(ProcessBuilder.Redirect.PIPE)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .start()
        try {
            // When `-Plsp.captureCorpus=true` is set, tee every observed LSP
            // wire frame to disk so CapturedCorpusReplayTest has real-world
            // payloads to round-trip-check against. The tee is byte-level and
            // synchronous — if it ever throws or stalls, the wrapped streams
            // still flush the original bytes through, so the underlying drive
            // can't be broken by a recorder failure.
            val recorder = if (isCaptureEnabled()) {
                CorpusRecorder(server = spec.binary, corpusRoot = captureCorpusRoot())
            } else {
                null
            }
            val inStream = recorder?.wrapInbound(process.inputStream) ?: process.inputStream
            val outStream = recorder?.wrapOutbound(process.outputStream) ?: process.outputStream

            val connection = (inStream to outStream).asLspConnection()
            val server = connection.connectAsLspClient(client)

            // Hard upper bound on the initialize→query drive so a server that
            // indexes very slowly (or never answers) fails THIS test fast
            // instead of hanging the suite.
            withTimeout(spec.budgetMillis) {
                val initResult = server.initialize(
                    InitializeParams(
                        capabilities = ClientCapabilities(),
                        processId = ProcessHandle.current().pid().toInt(),
                        rootUri = workspace.toURI().toString().trimEnd('/')
                    )
                )
                assertNotNull(initResult, "${spec.binary}: null initialize result")
                assertNotNull(
                    initResult.capabilities,
                    "${spec.binary}: null server capabilities"
                )

                server.initialized(InitializedParams())

                server.textDocumentDidOpen(
                    DidOpenTextDocumentParams(
                        textDocument = TextDocumentItem(
                            uri = uri,
                            languageId = spec.languageId,
                            version = 1,
                            text = sourceFile.readText()
                        )
                    )
                )

                // Structural expectations; each retries to let servers that
                // index lazily settle. Assertions are resilient to version
                // drift (no exact hover text / completion contents).
                assertDefinition(server, spec, uri, initResult)
                assertHover(server, spec, uri, initResult)
                assertDocumentSymbol(server, spec, uri, initResult)
                assertReferences(server, spec, uri, initResult)
            }

            // shutdown/exit are best-effort and bounded separately so a server
            // that's slow to wind down can't fail an otherwise green drive.
            runCatching { withTimeout(10_000) { server.shutdown() } }
            runCatching { withTimeout(2_000) { server.exit() } }
        } finally {
            runCatching { process.outputStream.close() }
            runCatching { process.inputStream.close() }
            if (!process.waitFor(2, TimeUnit.SECONDS)) {
                process.destroyForcibly()
            }
        }
    }

    private suspend fun assertDefinition(
        server: KsrpcLanguageServer,
        spec: ServerSpec,
        uri: String,
        init: InitializeResult
    ) {
        if (init.capabilities.definitionProvider.isDisabled()) return
        val result = retry {
            server.textDocumentDefinition(
                DefinitionParams(
                    textDocument = TextDocumentIdentifier(uri),
                    position = spec.callSite
                )
            ).takeIf { it.locations().isNotEmpty() }
        }
        assertNotNull(result, "${spec.binary}: definition returned no result")
        val locations = result.locations()
        assertTrue(
            locations.isNotEmpty(),
            "${spec.binary}: definition resolved to no locations"
        )
        assertTrue(
            locations.any { it.range.start.line == spec.declarationLine },
            "${spec.binary}: definition did not resolve to declaration line " +
                "${spec.declarationLine}; got ${locations.map { it.range.start.line }}"
        )
    }

    private suspend fun assertHover(
        server: KsrpcLanguageServer,
        spec: ServerSpec,
        uri: String,
        init: InitializeResult
    ) {
        if (init.capabilities.hoverProvider.isDisabled()) return
        // Hover content varies wildly between servers/versions, so we only
        // assert it resolves to a non-null response for a known symbol.
        val hover = retry {
            runCatching {
                server.textDocumentHover(
                    HoverParams(
                        textDocument = TextDocumentIdentifier(uri),
                        position = spec.callSite
                    )
                )
            }.getOrNull()
        }
        assertNotNull(hover, "${spec.binary}: hover returned null for a known symbol")
    }

    private suspend fun assertDocumentSymbol(
        server: KsrpcLanguageServer,
        spec: ServerSpec,
        uri: String,
        init: InitializeResult
    ) {
        if (init.capabilities.documentSymbolProvider.isDisabled()) return
        val names = retry {
            server.textDocumentDocumentSymbol(
                DocumentSymbolParams(textDocument = TextDocumentIdentifier(uri))
            ).symbolNames().takeIf { it.isNotEmpty() }
        }
        assertNotNull(names, "${spec.binary}: documentSymbol returned no symbols")
        assertTrue(
            names.any { it.contains("add") },
            "${spec.binary}: documentSymbol missing 'add'; got $names"
        )
    }

    private suspend fun assertReferences(
        server: KsrpcLanguageServer,
        spec: ServerSpec,
        uri: String,
        init: InitializeResult
    ) {
        if (init.capabilities.referencesProvider.isDisabled()) return
        val refs = retry {
            server.textDocumentReferences(
                ReferenceParams(
                    textDocument = TextDocumentIdentifier(uri),
                    position = spec.callSite,
                    context = ReferenceContext(includeDeclaration = true)
                )
            ).takeIf { it.isNotEmpty() }
        }
        assertNotNull(refs, "${spec.binary}: references returned no result")
        // Declaration + at least one call site.
        assertTrue(
            refs.size >= 2,
            "${spec.binary}: expected >= 2 references (decl + call); got ${refs.size}"
        )
        assertTrue(
            refs.any { it.range.start.line == spec.declarationLine },
            "${spec.binary}: references missing the declaration line " +
                "${spec.declarationLine}; got ${refs.map { it.range.start.line }}"
        )
    }
}

private data class ServerSpec(
    val binary: String,
    val command: List<String>,
    val languageId: String,
    val fixtureDir: String,
    val fixtureFile: String,
    val callSite: Position,
    val declarationLine: UInt,
    /**
     * Hard upper bound for the entire initialize→query→shutdown drive. Heavy
     * project-aware servers (gopls, rust-analyzer) index before they answer, so
     * they get a larger budget; lightweight per-file servers finish far sooner.
     */
    val budgetMillis: Long = 60_000
)

/** Retry a flaky query to let lazily-indexing servers settle. */
private suspend fun <T : Any> retry(attempts: Int = 8, block: suspend () -> T?): T? {
    repeat(attempts) {
        runCatching { block() }.getOrNull()?.let { return it }
        delay(1_000)
    }
    return runCatching { block() }.getOrNull()
}

private fun BooleanOr<*>?.isDisabled(): Boolean =
    this == null || (this is BooleanOr.BooleanValue && !value)

private fun TextDocumentDefinitionResult.locations(): List<Location> = when (this) {
    is TextDocumentDefinitionResult.DefinitionValue -> when (val v = value) {
        is SingleOrArray.Single -> listOf(v.value)
        is SingleOrArray.Multiple -> v.value
    }

    is TextDocumentDefinitionResult.DefinitionLinkArray ->
        value.map { Location(uri = it.targetUri, range = it.targetRange) }
}

private fun TextDocumentDocumentSymbolResult.symbolNames(): List<String> = when (this) {
    is TextDocumentDocumentSymbolResult.SymbolInformationArray -> value.map { it.name }
    is TextDocumentDocumentSymbolResult.DocumentSymbolArray -> value.map { it.name }
}

private fun fixtureDir(relative: String): File {
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

private fun isOnPath(binary: String): Boolean {
    val path = System.getenv("PATH") ?: return false
    return path.split(File.pathSeparator).asSequence()
        .map { File(it, binary) }
        .any { it.canExecute() }
}
