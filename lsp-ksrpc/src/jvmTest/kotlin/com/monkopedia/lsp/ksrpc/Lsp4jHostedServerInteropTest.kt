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
import com.monkopedia.lsp.CompletionParams
import com.monkopedia.lsp.DeclarationParams
import com.monkopedia.lsp.DefaultLanguageClient
import com.monkopedia.lsp.DefinitionParams
import com.monkopedia.lsp.DidOpenTextDocumentParams
import com.monkopedia.lsp.DocumentSymbolParams
import com.monkopedia.lsp.HoverContents
import com.monkopedia.lsp.HoverParams
import com.monkopedia.lsp.ImplementationParams
import com.monkopedia.lsp.InitializeParams
import com.monkopedia.lsp.InitializedParams
import com.monkopedia.lsp.Position
import com.monkopedia.lsp.ReferenceContext
import com.monkopedia.lsp.ReferenceParams
import com.monkopedia.lsp.SingleOrArray
import com.monkopedia.lsp.TextDocumentCompletionResult
import com.monkopedia.lsp.TextDocumentDeclarationResult
import com.monkopedia.lsp.TextDocumentDefinitionResult
import com.monkopedia.lsp.TextDocumentDocumentSymbolResult
import com.monkopedia.lsp.TextDocumentIdentifier
import com.monkopedia.lsp.TextDocumentImplementationResult
import com.monkopedia.lsp.TextDocumentItem
import com.monkopedia.lsp.TextDocumentTypeDefinitionResult
import com.monkopedia.lsp.TypeDefinitionParams
import com.monkopedia.lsp.ksrpc.fixtures.ConformanceLanguageServer
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.CompletableFuture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.CompletionList
import org.eclipse.lsp4j.DocumentSymbol
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.LocationLink
import org.eclipse.lsp4j.MarkedString
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.Position as Lsp4jPosition
import org.eclipse.lsp4j.Range as Lsp4jRange
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.ServerInfo
import org.eclipse.lsp4j.SymbolInformation
import org.eclipse.lsp4j.SymbolKind
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.TextDocumentService
import org.eclipse.lsp4j.services.WorkspaceService

/**
 * Direction 2 of the lsp4j interop matrix (#45): a real Eclipse lsp4j
 * [LanguageServer] (driven through [DaemonLsp4jLauncher.createServerLauncher]) hosts
 * canned responses, and OUR [connectAsLspClient] stub drives it over an
 * in-process `Content-Length`-framed pipe. For each typed-union result we assert
 * that OUR generated types parse what lsp4j emits into the expected concrete
 * branch.
 *
 * The lsp4j server uses the same `position.line` → branch contract as
 * [ConformanceLanguageServer] so the two directions stay symmetric:
 * - line 0 → single `Location`            → [SingleOrArray.Single]
 * - line 1 → `Location[]`                 → [SingleOrArray.Multiple]
 * - line 2 → `LocationLink[]`             → `*.DefinitionLinkArray`
 * - completion line 0 → `CompletionList`, line 1 → `CompletionItem[]`
 * - documentSymbol `#hierarchical` → `DocumentSymbol[]`, `#flat` → `SymbolInformation[]`
 *
 * Every call is bounded by a [withTimeout] so a stall fails fast.
 */
class Lsp4jHostedServerInteropTest : JvmIntegrationTestBase() {

    @Test
    fun `our client parses every union branch a real lsp4j server emits`() = runBlocking(
        Dispatchers.IO
    ) {
        // clientToServer: our client writes here, lsp4j server reads.
        val clientToServerSink = PipedOutputStream()
        val clientToServerSource = PipedInputStream(clientToServerSink, PIPE_BUFFER)
        // serverToClient: lsp4j server writes here, our client reads.
        val serverToClientSink = PipedOutputStream()
        val serverToClientSource = PipedInputStream(serverToClientSink, PIPE_BUFFER)

        val lsp4jServer = CannedLsp4jServer()
        // Daemon-backed launcher so the lsp4j reader thread (blocked on a
        // non-interruptible pipe read) can never keep the worker JVM alive after
        // the test; we also shut its executor down in `finally` (issue #79).
        val launcher = DaemonLsp4jLauncher.createServerLauncher(
            lsp4jServer,
            clientToServerSource,
            serverToClientSink
        )
        val listening = launcher.startListening()

        // Host the connection (and its stdout-pump coroutine) in a scope that is
        // NOT a child of this runBlocking, so the pump can't keep runBlocking
        // parked after the body finishes. We tear it down explicitly in `finally`.
        // The stub is published via a Deferred; the hosting `launch` stays alive
        // (its pump child keeps it running) until we cancel the scope.
        val connectionScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val serverStub = CompletableDeferred<com.monkopedia.lsp.KsrpcLanguageServer>()
        connectionScope.launch {
            val connection = (serverToClientSource to clientToServerSink).asLspConnection()
            serverStub.complete(connection.connectAsLspClient(DefaultLanguageClient()))
        }

        try {
            val server = serverStub.await()

            withTimeout(TIMEOUT_MS) {
                val init = server.initialize(
                    InitializeParams(
                        capabilities = ClientCapabilities(),
                        processId = null,
                        rootUri = null
                    )
                )
                assertNotNull(init.capabilities.hoverProvider, "server advertises hover")
                assertNotNull(init.capabilities.definitionProvider, "server advertises definition")
                assertEquals("lsp4j-canned-server", init.serverInfo?.name)

                server.initialized(InitializedParams())
                server.textDocumentDidOpen(
                    DidOpenTextDocumentParams(
                        textDocument = TextDocumentItem(
                            uri = ConformanceLanguageServer.Uri.MAIN,
                            languageId = "kotlin",
                            version = 1,
                            text = "fun x() {}"
                        )
                    )
                )

                assertHoverBranches(server)
                assertDefinitionFamily(server)
                assertCompletionBranches(server)
                assertDocumentSymbolBranches(server)
                assertReferences(server)

                assertEquals(null, server.shutdown())
                server.exit()
            }
        } finally {
            // Tear down the connection scope (kills the stdout-pump), stop the
            // lsp4j listener, then close the pipes. Cancelling the scope is what
            // lets the test terminate rather than hang on the pump.
            connectionScope.cancel()
            listening.cancel(true)
            launcher.shutdown()
            runCatching { clientToServerSink.close() }
            runCatching { clientToServerSource.close() }
            runCatching { serverToClientSink.close() }
            runCatching { serverToClientSource.close() }
        }
    }

    private suspend fun assertHoverBranches(server: com.monkopedia.lsp.KsrpcLanguageServer) {
        val markup = server.textDocumentHover(hover(ConformanceLanguageServer.Lines.SINGLE))
        assertNotNull(markup, "lsp4j hover (SINGLE) should not be null")
        assertTrue(
            markup.contents is HoverContents.MarkupContentValue,
            "lsp4j MarkupContent should parse as MarkupContentValue: ${markup.contents}"
        )
        // lsp4j emits a single marked string as a bare object → MarkedStringValue.
        val marked = server.textDocumentHover(hover(ConformanceLanguageServer.Lines.ARRAY))
        assertNotNull(marked, "lsp4j hover (ARRAY) should not be null")
        assertTrue(
            marked.contents is HoverContents.MarkedStringValue ||
                marked.contents is HoverContents.MarkedStringArray,
            "lsp4j marked string should parse as a marked-string branch: ${marked.contents}"
        )
        val markedArray = server.textDocumentHover(hover(ConformanceLanguageServer.Lines.LINK))
        assertNotNull(markedArray, "lsp4j hover (LINK) should not be null")
        assertTrue(
            markedArray.contents is HoverContents.MarkedStringArray,
            "lsp4j marked-string array should parse as MarkedStringArray: ${markedArray.contents}"
        )
    }

    private suspend fun assertDefinitionFamily(server: com.monkopedia.lsp.KsrpcLanguageServer) {
        // lsp4j's server-side result type is Either<List<Location>, List<LocationLink>>,
        // so it always emits a JSON array for the Location branch — even a single
        // location is serialised as `[ {...} ]`. Our `SingleOrArray` parser therefore
        // sees an array and yields `Multiple`. The lsp4j-distinguishable contract here
        // is Location-list (`DefinitionValue` → `Multiple`) vs LocationLink-list
        // (`DefinitionLinkArray`); the bare-single-Location branch is exercised in the
        // reverse direction (our server → lsp4j client) in Lsp4jConformanceInteropTest.
        (
            server.textDocumentDefinition(definition(ConformanceLanguageServer.Lines.SINGLE))
                as TextDocumentDefinitionResult.DefinitionValue
            ).value.let { assertTrue(it is SingleOrArray.Multiple, "Location list → Multiple") }
        (
            server.textDocumentDefinition(definition(ConformanceLanguageServer.Lines.ARRAY))
                as TextDocumentDefinitionResult.DefinitionValue
            ).value.let { assertTrue(it is SingleOrArray.Multiple, "Location[] → Multiple") }
        server.textDocumentDefinition(definition(ConformanceLanguageServer.Lines.LINK)).let {
            assertTrue(
                it is TextDocumentDefinitionResult.DefinitionLinkArray,
                "LocationLink[] → DefinitionLinkArray: $it"
            )
            assertEquals(2, it.value.size)
        }

        // declaration: Location-list vs LocationLink-list
        assertTrue(
            server.textDocumentDeclaration(
                DeclarationParams(doc(), pos(ConformanceLanguageServer.Lines.SINGLE))
            ) is TextDocumentDeclarationResult.DeclarationValue
        )
        assertTrue(
            server.textDocumentDeclaration(
                DeclarationParams(doc(), pos(ConformanceLanguageServer.Lines.LINK))
            ) is TextDocumentDeclarationResult.DeclarationLinkArray
        )

        // typeDefinition
        assertTrue(
            server.textDocumentTypeDefinition(
                TypeDefinitionParams(doc(), pos(ConformanceLanguageServer.Lines.ARRAY))
            ) is TextDocumentTypeDefinitionResult.DefinitionValue
        )
        assertTrue(
            server.textDocumentTypeDefinition(
                TypeDefinitionParams(doc(), pos(ConformanceLanguageServer.Lines.LINK))
            ) is TextDocumentTypeDefinitionResult.DefinitionLinkArray
        )

        // implementation
        assertTrue(
            server.textDocumentImplementation(
                ImplementationParams(doc(), pos(ConformanceLanguageServer.Lines.SINGLE))
            ) is TextDocumentImplementationResult.DefinitionValue
        )
        assertTrue(
            server.textDocumentImplementation(
                ImplementationParams(doc(), pos(ConformanceLanguageServer.Lines.LINK))
            ) is TextDocumentImplementationResult.DefinitionLinkArray
        )
    }

    private suspend fun assertCompletionBranches(server: com.monkopedia.lsp.KsrpcLanguageServer) {
        server.textDocumentCompletion(
            CompletionParams(doc(), pos(ConformanceLanguageServer.Lines.SINGLE))
        ).let {
            assertTrue(
                it is TextDocumentCompletionResult.CompletionListValue,
                "lsp4j CompletionList → CompletionListValue: $it"
            )
        }
        server.textDocumentCompletion(
            CompletionParams(doc(), pos(ConformanceLanguageServer.Lines.ARRAY))
        ).let {
            assertTrue(
                it is TextDocumentCompletionResult.CompletionItemArray,
                "lsp4j CompletionItem[] → CompletionItemArray: $it"
            )
            assertEquals(2, it.value.size)
        }
    }

    private suspend fun assertDocumentSymbolBranches(
        server: com.monkopedia.lsp.KsrpcLanguageServer
    ) {
        server.textDocumentDocumentSymbol(
            DocumentSymbolParams(
                textDocument = TextDocumentIdentifier(
                    ConformanceLanguageServer.Uri.HIERARCHICAL_SYMBOLS
                )
            )
        ).let {
            assertTrue(
                it is TextDocumentDocumentSymbolResult.DocumentSymbolArray,
                "lsp4j DocumentSymbol[] → DocumentSymbolArray: $it"
            )
            assertTrue(it.value.first().children?.isNotEmpty() == true, "children preserved")
        }
        server.textDocumentDocumentSymbol(
            DocumentSymbolParams(
                textDocument = TextDocumentIdentifier(ConformanceLanguageServer.Uri.FLAT_SYMBOLS)
            )
        ).let {
            assertTrue(
                it is TextDocumentDocumentSymbolResult.SymbolInformationArray,
                "lsp4j SymbolInformation[] → SymbolInformationArray: $it"
            )
        }
    }

    private suspend fun assertReferences(server: com.monkopedia.lsp.KsrpcLanguageServer) {
        val refs = server.textDocumentReferences(
            ReferenceParams(
                textDocument = doc(),
                position = pos(ConformanceLanguageServer.Lines.SINGLE),
                context = ReferenceContext(includeDeclaration = true)
            )
        )
        assertNotNull(refs, "references should not be null")
        assertEquals(3, refs.size)
    }

    private fun doc() = TextDocumentIdentifier(ConformanceLanguageServer.Uri.MAIN)
    private fun pos(line: Int) = Position(line = line.toUInt(), character = 0u)
    private fun hover(line: Int) = HoverParams(doc(), pos(line))
    private fun definition(line: Int) = DefinitionParams(doc(), pos(line))

    /**
     * A real lsp4j [LanguageServer] returning canned values keyed off
     * `position.line`, mirroring the [ConformanceLanguageServer] contract so our
     * client can assert the parsed branch.
     */
    private class CannedLsp4jServer :
        LanguageServer,
        TextDocumentService,
        WorkspaceService {
        override fun initialize(
            params: org.eclipse.lsp4j.InitializeParams?
        ): CompletableFuture<InitializeResult> {
            val caps = ServerCapabilities().apply {
                setHoverProvider(true)
                setDefinitionProvider(true)
                setDeclarationProvider(true)
                setTypeDefinitionProvider(true)
                setImplementationProvider(true)
                setReferencesProvider(true)
                setDocumentSymbolProvider(true)
                completionProvider = org.eclipse.lsp4j.CompletionOptions()
            }
            return CompletableFuture.completedFuture(
                InitializeResult(caps, ServerInfo("lsp4j-canned-server", "1.0.0"))
            )
        }

        override fun shutdown(): CompletableFuture<Any> = CompletableFuture.completedFuture(null)

        override fun exit() = Unit
        override fun getTextDocumentService(): TextDocumentService = this
        override fun getWorkspaceService(): WorkspaceService = this

        // WorkspaceService no-ops
        override fun didChangeConfiguration(
            params: org.eclipse.lsp4j.DidChangeConfigurationParams?
        ) = Unit
        override fun didChangeWatchedFiles(params: org.eclipse.lsp4j.DidChangeWatchedFilesParams?) =
            Unit

        // TextDocumentService notifications no-op
        override fun didOpen(params: org.eclipse.lsp4j.DidOpenTextDocumentParams?) = Unit
        override fun didChange(params: org.eclipse.lsp4j.DidChangeTextDocumentParams?) = Unit
        override fun didClose(params: org.eclipse.lsp4j.DidCloseTextDocumentParams?) = Unit
        override fun didSave(params: org.eclipse.lsp4j.DidSaveTextDocumentParams?) = Unit

        override fun hover(params: org.eclipse.lsp4j.HoverParams?): CompletableFuture<Hover> {
            val line = params!!.position.line
            val hover = when (line) {
                0 -> Hover(MarkupContent("markdown", "**markup** hover"))

                1 -> Hover(Either.forRight<String, MarkedString>(MarkedString("kotlin", "marked")))

                else -> Hover(
                    listOf(
                        Either.forLeft<String, MarkedString>("marked one"),
                        Either.forLeft<String, MarkedString>("marked two")
                    )
                )
            }
            return CompletableFuture.completedFuture(hover)
        }

        override fun definition(
            params: org.eclipse.lsp4j.DefinitionParams?
        ): CompletableFuture<Either<MutableList<out Location>, MutableList<out LocationLink>>> =
            CompletableFuture.completedFuture(locationOrLink(params!!.position.line))

        override fun declaration(
            params: org.eclipse.lsp4j.DeclarationParams?
        ): CompletableFuture<Either<MutableList<out Location>, MutableList<out LocationLink>>> =
            CompletableFuture.completedFuture(locationOrLink(params!!.position.line))

        override fun typeDefinition(
            params: org.eclipse.lsp4j.TypeDefinitionParams?
        ): CompletableFuture<Either<MutableList<out Location>, MutableList<out LocationLink>>> =
            CompletableFuture.completedFuture(locationOrLink(params!!.position.line))

        override fun implementation(
            params: org.eclipse.lsp4j.ImplementationParams?
        ): CompletableFuture<Either<MutableList<out Location>, MutableList<out LocationLink>>> =
            CompletableFuture.completedFuture(locationOrLink(params!!.position.line))

        override fun references(
            params: org.eclipse.lsp4j.ReferenceParams?
        ): CompletableFuture<MutableList<out Location>> = CompletableFuture.completedFuture(
            mutableListOf(loc(0), loc(1), loc(2))
        )

        override fun completion(
            params: org.eclipse.lsp4j.CompletionParams?
        ): CompletableFuture<Either<MutableList<CompletionItem>, CompletionList>> {
            val either = if (params!!.position.line == 0) {
                Either.forRight<MutableList<CompletionItem>, CompletionList>(
                    CompletionList(false, mutableListOf(item("fromList")))
                )
            } else {
                Either.forLeft<MutableList<CompletionItem>, CompletionList>(
                    mutableListOf(item("fromArrayA"), item("fromArrayB"))
                )
            }
            return CompletableFuture.completedFuture(either)
        }

        override fun documentSymbol(
            params: org.eclipse.lsp4j.DocumentSymbolParams?
        ): CompletableFuture<MutableList<Either<SymbolInformation, DocumentSymbol>>> {
            val list: MutableList<Either<SymbolInformation, DocumentSymbol>> =
                if (params!!.textDocument.uri.endsWith("#flat")) {
                    mutableListOf(
                        Either.forLeft(
                            SymbolInformation("flatSymbol", SymbolKind.Function, loc(0))
                        )
                    )
                } else {
                    val child = DocumentSymbol(
                        "childMethod",
                        SymbolKind.Method,
                        range(1),
                        range(1)
                    )
                    val parent = DocumentSymbol(
                        "hierarchicalSymbol",
                        SymbolKind.Class,
                        range(0),
                        range(0)
                    ).apply { children = mutableListOf(child) }
                    mutableListOf(Either.forRight(parent))
                }
            return CompletableFuture.completedFuture(list)
        }

        private fun locationOrLink(
            line: Int
        ): Either<MutableList<out Location>, MutableList<out LocationLink>> = when (line) {
            0 -> Either.forLeft(mutableListOf(loc(0)))
            1 -> Either.forLeft(mutableListOf(loc(0), loc(1)))
            else -> Either.forRight(mutableListOf(link(0), link(1)))
        }

        private fun item(label: String) = CompletionItem(label).apply {
            kind = CompletionItemKind.Function
        }

        private fun pos(line: Int) = Lsp4jPosition(line, 0)
        private fun range(line: Int) = Lsp4jRange(pos(line), Lsp4jPosition(line, 4))
        private fun loc(line: Int) = Location(ConformanceLanguageServer.Uri.MAIN, range(line))
        private fun link(line: Int) = LocationLink(
            ConformanceLanguageServer.Uri.MAIN,
            range(line + 10),
            range(line + 10),
            range(line)
        )
    }

    private companion object {
        const val TIMEOUT_MS = 15_000L
        const val PIPE_BUFFER = 1 shl 20
    }
}
