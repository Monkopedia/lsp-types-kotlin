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

import com.monkopedia.lsp.Diagnostic
import com.monkopedia.lsp.DidOpenTextDocumentParams
import com.monkopedia.lsp.IntOrString
import com.monkopedia.lsp.Position
import com.monkopedia.lsp.ProgressParams
import com.monkopedia.lsp.PublishDiagnosticsParams
import com.monkopedia.lsp.Range
import com.monkopedia.lsp.WorkDoneProgressBegin
import com.monkopedia.lsp.ksrpc.fixtures.ConformanceLanguageServer
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.eclipse.lsp4j.ApplyWorkspaceEditParams
import org.eclipse.lsp4j.ApplyWorkspaceEditResponse
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.ConfigurationParams
import org.eclipse.lsp4j.DeclarationParams
import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams as Lsp4jDidOpenTextDocumentParams
import org.eclipse.lsp4j.DocumentSymbolParams
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.ImplementationParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializedParams
import org.eclipse.lsp4j.LogTraceParams
import org.eclipse.lsp4j.MessageActionItem
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.Position as Lsp4jPosition
import org.eclipse.lsp4j.PublishDiagnosticsParams as Lsp4jPublishDiagnosticsParams
import org.eclipse.lsp4j.ReferenceContext
import org.eclipse.lsp4j.ReferenceParams
import org.eclipse.lsp4j.RegistrationParams
import org.eclipse.lsp4j.ShowDocumentParams
import org.eclipse.lsp4j.ShowDocumentResult
import org.eclipse.lsp4j.ShowMessageRequestParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.TypeDefinitionParams
import org.eclipse.lsp4j.UnregistrationParams
import org.eclipse.lsp4j.WorkDoneProgressCreateParams
import org.eclipse.lsp4j.WorkDoneProgressNotification
import org.eclipse.lsp4j.WorkspaceFolder
import org.eclipse.lsp4j.launch.LSPLauncher
import org.eclipse.lsp4j.services.LanguageClient

/**
 * Direction 1 of the lsp4j interop matrix (#45): a real Eclipse lsp4j
 * [org.eclipse.lsp4j.services.LanguageServer] proxy drives our
 * [ConformanceLanguageServer] over an in-process `Content-Length`-framed pipe.
 * For every typed-union result the fixture distinguishes (see the fixture's
 * contract table), we pick inputs that hit each branch and assert that lsp4j —
 * a battle-tested third-party client — parses our wire output into the expected
 * concrete shape.
 *
 * Branch coverage exercised here:
 * - hover: MarkupContent (`contents.right`) vs marked-string list (`contents.left`)
 * - definition / declaration / typeDefinition / implementation: `Location[]`
 *   (`Either.left`) vs `LocationLink[]` (`Either.right`)
 * - completion: `CompletionList` (`Either.right`) vs `CompletionItem[]` (`Either.left`)
 * - documentSymbol: hierarchical `DocumentSymbol[]` vs flat `SymbolInformation[]`
 * - references: `Location[]`
 *
 * Plus server → client pushes: `textDocument/publishDiagnostics` and `$/progress`
 * (emitted by the [DiagnosticsEmittingServer] subclass on `didOpen`) are received
 * and parsed by the real lsp4j client.
 *
 * Every blocking round-trip is bounded by [TIMEOUT_S] so a stall fails fast.
 */
class Lsp4jConformanceInteropTest {

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var serverJob: Job? = null
    private val pipes = mutableListOf<java.io.Closeable>()

    @AfterTest
    fun tearDown() {
        serverJob?.cancel()
        pipes.forEach { runCatching { it.close() } }
        scope.cancel()
    }

    @Test
    fun `real lsp4j client parses every union branch our server emits`() {
        // clientToServer: lsp4j writes requests here, our server reads them.
        val clientToServerSink = PipedOutputStream()
        val clientToServerSource = PipedInputStream(clientToServerSink, PIPE_BUFFER)
        // serverToClient: our server writes responses here, lsp4j reads them.
        val serverToClientSink = PipedOutputStream()
        val serverToClientSource = PipedInputStream(serverToClientSink, PIPE_BUFFER)
        pipes += listOf(
            clientToServerSink,
            clientToServerSource,
            serverToClientSink,
            serverToClientSource
        )

        val fixture = DiagnosticsEmittingServer()
        serverJob = scope.launch {
            val connection = (clientToServerSource to serverToClientSink).asLspConnection()
            // The client stub lets the fixture push diagnostics/progress back.
            fixture.client = connection.connectAsLspServer(fixture)
        }

        val client = RecordingClient()
        val launcher = LSPLauncher.createClientLauncher(
            client,
            serverToClientSource,
            clientToServerSink
        )
        val listening = launcher.startListening()
        val server = launcher.remoteProxy

        try {
            val initResult = server.initialize(InitializeParams()).get(TIMEOUT_S, TimeUnit.SECONDS)
            assertNotNull(initResult, "initialize must return a result")
            assertEquals(true, initResult.capabilities.hoverProvider?.left)
            assertNotNull(initResult.capabilities.definitionProvider)
            assertNotNull(initResult.capabilities.completionProvider)
            assertEquals("ConformanceLanguageServer", initResult.serverInfo?.name)

            server.initialized(InitializedParams())

            val td = server.textDocumentService

            // didOpen triggers the fixture to push a diagnostic + progress.
            td.didOpen(
                Lsp4jDidOpenTextDocumentParams(
                    TextDocumentItem(ConformanceLanguageServer.Uri.MAIN, "kotlin", 1, "fun x() {}")
                )
            )

            assertHoverBranches(td)
            assertLocationOrLinkBranches(td)
            assertCompletionBranches(td)
            assertDocumentSymbolBranches(td)
            assertReferences(td)

            // Server → client pushes: wait for the diagnostic and progress the
            // fixture emitted on didOpen.
            val diagnostics = client.diagnostics.poll(TIMEOUT_S, TimeUnit.SECONDS)
            assertNotNull(diagnostics, "expected publishDiagnostics push from server")
            assertEquals(ConformanceLanguageServer.Uri.MAIN, diagnostics.uri)
            assertTrue(diagnostics.diagnostics.isNotEmpty(), "expected at least one diagnostic")

            val progress = client.progress.poll(TIMEOUT_S, TimeUnit.SECONDS)
            assertNotNull(progress, "expected \$/progress push from server")
            val begin: WorkDoneProgressNotification? = progress.value.left
            assertNotNull(begin, "progress value should parse as a WorkDoneProgress notification")

            server.shutdown().get(TIMEOUT_S, TimeUnit.SECONDS)
            server.exit()
        } finally {
            listening.cancel(true)
        }
    }

    @Test
    fun `real lsp4j client receives every server-initiated call the fixture triggers`() {
        // clientToServer: lsp4j writes requests here, our server reads them.
        val clientToServerSink = PipedOutputStream()
        val clientToServerSource = PipedInputStream(clientToServerSink, PIPE_BUFFER)
        // serverToClient: our server writes responses + pushes here, lsp4j reads them.
        val serverToClientSink = PipedOutputStream()
        val serverToClientSource = PipedInputStream(serverToClientSink, PIPE_BUFFER)
        pipes += listOf(
            clientToServerSink,
            clientToServerSource,
            serverToClientSink,
            serverToClientSource
        )

        val fixture = ConformanceLanguageServer()
        serverJob = scope.launch {
            val connection = (clientToServerSource to serverToClientSink).asLspConnection()
            // The client stub lets the fixture call back into lsp4j.
            fixture.client = connection.connectAsLspServer(fixture)
        }

        val client = RecordingClient()
        val launcher = LSPLauncher.createClientLauncher(
            client,
            serverToClientSource,
            clientToServerSink
        )
        val listening = launcher.startListening()
        val server = launcher.remoteProxy

        try {
            server.initialize(InitializeParams()).get(TIMEOUT_S, TimeUnit.SECONDS)
            server.initialized(InitializedParams())

            // didOpen on the trigger URI drives ConformanceLanguageServer.emitAllClientTriggers.
            server.textDocumentService.didOpen(
                Lsp4jDidOpenTextDocumentParams(
                    TextDocumentItem(
                        ConformanceLanguageServer.Triggers.ALL,
                        "kotlin",
                        1,
                        "// trigger"
                    )
                )
            )

            // For each server-initiated client method we expect, drain one entry off
            // the corresponding lsp4j RecordingClient queue with a bounded timeout.
            // Each poll asserts the wire actually carried that method back.
            assertNotNull(
                client.configurationRequests.poll(TIMEOUT_S, TimeUnit.SECONDS),
                "expected workspace/configuration over the wire"
            )
            assertNotNull(
                client.workspaceFoldersRequests.poll(TIMEOUT_S, TimeUnit.SECONDS),
                "expected workspace/workspaceFolders over the wire"
            )
            assertNotNull(
                client.applyEditRequests.poll(TIMEOUT_S, TimeUnit.SECONDS),
                "expected workspace/applyEdit over the wire"
            )
            assertNotNull(
                client.showMessageRequests.poll(TIMEOUT_S, TimeUnit.SECONDS),
                "expected window/showMessageRequest over the wire"
            )
            assertNotNull(
                client.showDocumentRequests.poll(TIMEOUT_S, TimeUnit.SECONDS),
                "expected window/showDocument over the wire"
            )
            assertNotNull(
                client.registerCapabilityRequests.poll(TIMEOUT_S, TimeUnit.SECONDS),
                "expected client/registerCapability over the wire"
            )
            assertNotNull(
                client.unregisterCapabilityRequests.poll(TIMEOUT_S, TimeUnit.SECONDS),
                "expected client/unregisterCapability over the wire"
            )
            assertNotNull(
                client.workDoneProgressCreateRequests.poll(TIMEOUT_S, TimeUnit.SECONDS),
                "expected window/workDoneProgress/create over the wire"
            )
            val progress = client.progress.poll(TIMEOUT_S, TimeUnit.SECONDS)
            assertNotNull(progress, "expected \$/progress over the wire")
            val begin: WorkDoneProgressNotification? = progress.value.left
            assertNotNull(begin, "progress value should parse as a WorkDoneProgress notification")

            // refresh family — drain six entries in any order, all six method
            // names must appear so we know each crossed the wire.
            val refreshSeen = mutableSetOf<String>()
            repeat(EXPECTED_REFRESH_COUNT) {
                val name = client.refreshCalls.poll(TIMEOUT_S, TimeUnit.SECONDS)
                    ?: error("missing refresh call #$it; saw so far: $refreshSeen")
                refreshSeen += name
            }
            assertEquals(
                setOf(
                    "workspace/codeLens/refresh",
                    "workspace/semanticTokens/refresh",
                    "workspace/inlayHint/refresh",
                    "workspace/inlineValue/refresh",
                    "workspace/diagnostic/refresh",
                    "workspace/foldingRange/refresh"
                ),
                refreshSeen,
                "every workspace/<feature>/refresh must round-trip"
            )

            assertNotNull(
                client.telemetry.poll(TIMEOUT_S, TimeUnit.SECONDS),
                "expected telemetry/event over the wire"
            )
            assertNotNull(
                client.logTraces.poll(TIMEOUT_S, TimeUnit.SECONDS),
                "expected \$/logTrace over the wire"
            )
            assertNotNull(
                client.showMessages.poll(TIMEOUT_S, TimeUnit.SECONDS),
                "expected window/showMessage over the wire"
            )
            assertNotNull(
                client.logMessages.poll(TIMEOUT_S, TimeUnit.SECONDS),
                "expected window/logMessage over the wire"
            )

            // The fixture's own record of the calls it issued must reflect what
            // we observed on the wire — every method name in ClientMethods.ALL.
            assertEquals(
                ConformanceLanguageServer.ClientMethods.ALL.toSet(),
                fixture.issuedClientCalls.toSet(),
                "fixture should record every issued server-initiated call"
            )

            server.shutdown().get(TIMEOUT_S, TimeUnit.SECONDS)
            server.exit()
        } finally {
            listening.cancel(true)
        }
    }

    private fun assertHoverBranches(td: org.eclipse.lsp4j.services.TextDocumentService) {
        // line 0 → MarkupContent (Either.right)
        val markup = td.hover(hover(ConformanceLanguageServer.Lines.SINGLE))
            .get(TIMEOUT_S, TimeUnit.SECONDS)
        assertNotNull(
            markup.contents.right,
            "line 0 hover should be MarkupContent: ${markup.contents}"
        )
        assertEquals("markdown", markup.contents.right.kind)

        // line 1 → single marked string, lsp4j normalises to a one-element list (Either.left)
        val single = td.hover(hover(ConformanceLanguageServer.Lines.ARRAY))
            .get(TIMEOUT_S, TimeUnit.SECONDS)
        val singleList = single.contents.left
        assertNotNull(singleList, "line 1 hover should be a marked-string list: ${single.contents}")
        assertEquals(1, singleList.size)

        // line 2 → marked string array (Either.left, two entries)
        val array = td.hover(hover(ConformanceLanguageServer.Lines.LINK))
            .get(TIMEOUT_S, TimeUnit.SECONDS)
        val arrayList = array.contents.left
        assertNotNull(arrayList, "line 2 hover should be a marked-string list: ${array.contents}")
        assertEquals(2, arrayList.size)
    }

    private fun assertLocationOrLinkBranches(td: org.eclipse.lsp4j.services.TextDocumentService) {
        // definition
        td.definition(DefinitionParams(doc(), pos(ConformanceLanguageServer.Lines.SINGLE)))
            .get(TIMEOUT_S, TimeUnit.SECONDS).let { either ->
                assertTrue(either.isLeft, "single Location should parse as Location[] (left)")
                assertEquals(1, either.left.size)
            }
        td.definition(DefinitionParams(doc(), pos(ConformanceLanguageServer.Lines.ARRAY)))
            .get(TIMEOUT_S, TimeUnit.SECONDS).let { either ->
                assertTrue(either.isLeft, "Location[] should parse as Location[] (left)")
                assertEquals(2, either.left.size)
            }
        td.definition(DefinitionParams(doc(), pos(ConformanceLanguageServer.Lines.LINK)))
            .get(TIMEOUT_S, TimeUnit.SECONDS).let { either ->
                assertTrue(either.isRight, "LocationLink[] should parse as LocationLink[] (right)")
                assertEquals(2, either.right.size)
            }

        // declaration
        td.declaration(DeclarationParams(doc(), pos(ConformanceLanguageServer.Lines.SINGLE)))
            .get(TIMEOUT_S, TimeUnit.SECONDS).let { assertTrue(it.isLeft) }
        td.declaration(DeclarationParams(doc(), pos(ConformanceLanguageServer.Lines.LINK)))
            .get(TIMEOUT_S, TimeUnit.SECONDS).let { assertTrue(it.isRight) }

        // typeDefinition
        td.typeDefinition(TypeDefinitionParams(doc(), pos(ConformanceLanguageServer.Lines.ARRAY)))
            .get(TIMEOUT_S, TimeUnit.SECONDS).let { assertTrue(it.isLeft) }
        td.typeDefinition(TypeDefinitionParams(doc(), pos(ConformanceLanguageServer.Lines.LINK)))
            .get(TIMEOUT_S, TimeUnit.SECONDS).let { assertTrue(it.isRight) }

        // implementation
        td.implementation(ImplementationParams(doc(), pos(ConformanceLanguageServer.Lines.SINGLE)))
            .get(TIMEOUT_S, TimeUnit.SECONDS).let { assertTrue(it.isLeft) }
        td.implementation(ImplementationParams(doc(), pos(ConformanceLanguageServer.Lines.LINK)))
            .get(TIMEOUT_S, TimeUnit.SECONDS).let { assertTrue(it.isRight) }
    }

    private fun assertCompletionBranches(td: org.eclipse.lsp4j.services.TextDocumentService) {
        // line 0 → CompletionList (Either.right)
        td.completion(CompletionParams(doc(), pos(ConformanceLanguageServer.Lines.SINGLE)))
            .get(TIMEOUT_S, TimeUnit.SECONDS).let { either ->
                assertTrue(either.isRight, "should parse as CompletionList (right): $either")
                assertEquals(1, either.right.items.size)
            }
        // line 1 → CompletionItem[] (Either.left)
        td.completion(CompletionParams(doc(), pos(ConformanceLanguageServer.Lines.ARRAY)))
            .get(TIMEOUT_S, TimeUnit.SECONDS).let { either ->
                assertTrue(either.isLeft, "should parse as CompletionItem[] (left): $either")
                assertEquals(2, either.left.size)
            }
    }

    private fun assertDocumentSymbolBranches(td: org.eclipse.lsp4j.services.TextDocumentService) {
        // hierarchical → DocumentSymbol (Either.right of each list entry)
        td.documentSymbol(
            DocumentSymbolParams(
                TextDocumentIdentifier(ConformanceLanguageServer.Uri.HIERARCHICAL_SYMBOLS)
            )
        ).get(TIMEOUT_S, TimeUnit.SECONDS).let { list ->
            assertTrue(list.isNotEmpty(), "expected a symbol")
            assertTrue(
                list.first().isRight,
                "hierarchical should be DocumentSymbol (right): ${list.first()}"
            )
            assertEquals("hierarchicalSymbol", list.first().right.name)
            assertTrue(
                list.first().right.children.isNotEmpty(),
                "DocumentSymbol should carry children"
            )
        }
        // flat → SymbolInformation (Either.left of each list entry)
        td.documentSymbol(
            DocumentSymbolParams(
                TextDocumentIdentifier(ConformanceLanguageServer.Uri.FLAT_SYMBOLS)
            )
        ).get(TIMEOUT_S, TimeUnit.SECONDS).let { list ->
            assertTrue(list.isNotEmpty(), "expected a symbol")
            assertTrue(
                list.first().isLeft,
                "flat should be SymbolInformation (left): ${list.first()}"
            )
            assertEquals("flatSymbol", list.first().left.name)
        }
    }

    private fun assertReferences(td: org.eclipse.lsp4j.services.TextDocumentService) {
        val refs = td.references(
            ReferenceParams(
                doc(),
                pos(ConformanceLanguageServer.Lines.SINGLE),
                ReferenceContext(true)
            )
        ).get(TIMEOUT_S, TimeUnit.SECONDS)
        assertEquals(3, refs.size, "references should be a Location[3]")
    }

    private fun doc() = TextDocumentIdentifier(ConformanceLanguageServer.Uri.MAIN)
    private fun pos(line: Int) = Lsp4jPosition(line, 0)
    private fun hover(line: Int) = HoverParams(doc(), pos(line))

    /**
     * Fixture subclass that, on `didOpen`, pushes a `publishDiagnostics` and a
     * `$/progress` back through the client stub — exercising the server → client
     * direction over the real lsp4j wire. The base [ConformanceLanguageServer]
     * answers requests but emits no pushes, so this lives test-side.
     */
    private class DiagnosticsEmittingServer : ConformanceLanguageServer() {

        override suspend fun textDocumentDidOpen(params: DidOpenTextDocumentParams) {
            super.textDocumentDidOpen(params)
            val range = Range(
                start = Position(line = 0u, character = 0u),
                end = Position(line = 0u, character = 4u)
            )
            client?.textDocumentPublishDiagnostics(
                PublishDiagnosticsParams(
                    uri = params.textDocument.uri,
                    diagnostics = listOf(
                        Diagnostic(range = range, message = "canned conformance diagnostic")
                    )
                )
            )
            val begin = LSP_JSON.encodeToJsonElement(
                WorkDoneProgressBegin.serializer(),
                WorkDoneProgressBegin(kind = "begin", title = "Conformance work")
            )
            client?.progress(
                ProgressParams(
                    token = IntOrString.StringValue("conformance-progress"),
                    value = begin
                )
            )
        }
    }

    /**
     * Lsp4j-side recording client. Records every server-initiated call so the
     * trigger test ([drives every server-initiated client call over the lsp4j
     * wire][`the lsp4j wire`]) can assert which methods the server issued. lsp4j's
     * client surface is `CompletableFuture`-shaped; we hand back deterministic
     * canned values (`applyEdit` → applied=true, `configuration` → list of nulls,
     * `showMessageRequest` → the first action, `workspaceFolders` → a fixed
     * folder, refresh family → null Void) — the same contract our common
     * [com.monkopedia.lsp.ksrpc.fixtures.ConformanceLanguageClient] honours.
     */
    private class RecordingClient : LanguageClient {
        val diagnostics = LinkedBlockingQueue<Lsp4jPublishDiagnosticsParams>()
        val progress = LinkedBlockingQueue<org.eclipse.lsp4j.ProgressParams>()
        val showMessages = LinkedBlockingQueue<MessageParams>()
        val logMessages = LinkedBlockingQueue<MessageParams>()
        val telemetry = LinkedBlockingQueue<Any?>()
        val logTraces = LinkedBlockingQueue<LogTraceParams>()
        val configurationRequests = LinkedBlockingQueue<ConfigurationParams>()
        val applyEditRequests = LinkedBlockingQueue<ApplyWorkspaceEditParams>()
        val showMessageRequests = LinkedBlockingQueue<ShowMessageRequestParams>()
        val showDocumentRequests = LinkedBlockingQueue<ShowDocumentParams>()
        val registerCapabilityRequests = LinkedBlockingQueue<RegistrationParams>()
        val unregisterCapabilityRequests = LinkedBlockingQueue<UnregistrationParams>()
        val workDoneProgressCreateRequests =
            LinkedBlockingQueue<WorkDoneProgressCreateParams>()
        val workspaceFoldersRequests = LinkedBlockingQueue<Unit>()
        val refreshCalls = LinkedBlockingQueue<String>()

        override fun telemetryEvent(`object`: Any?) {
            telemetry.add(`object`)
        }
        override fun publishDiagnostics(diagnostics: Lsp4jPublishDiagnosticsParams?) {
            diagnostics?.let { this.diagnostics.add(it) }
        }
        override fun showMessage(messageParams: MessageParams?) {
            messageParams?.let { showMessages.add(it) }
        }
        override fun showMessageRequest(
            requestParams: ShowMessageRequestParams?
        ): CompletableFuture<MessageActionItem> {
            requestParams?.let { showMessageRequests.add(it) }
            val response = requestParams?.actions?.firstOrNull()
                ?: MessageActionItem("OK")
            return CompletableFuture.completedFuture(response)
        }
        override fun logMessage(message: MessageParams?) {
            message?.let { logMessages.add(it) }
        }
        override fun notifyProgress(params: org.eclipse.lsp4j.ProgressParams?) {
            params?.let { progress.add(it) }
        }
        override fun logTrace(params: LogTraceParams?) {
            params?.let { logTraces.add(it) }
        }
        override fun applyEdit(
            params: ApplyWorkspaceEditParams?
        ): CompletableFuture<ApplyWorkspaceEditResponse> {
            params?.let { applyEditRequests.add(it) }
            return CompletableFuture.completedFuture(ApplyWorkspaceEditResponse(true))
        }
        override fun configuration(
            params: ConfigurationParams?
        ): CompletableFuture<MutableList<Any>> {
            params?.let { configurationRequests.add(it) }
            // Return one null per requested item (lsp4j accepts a list of Object,
            // null values represent unset configuration).
            val nulls: MutableList<Any> = ArrayList<Any>(params?.items?.size ?: 0).apply {
                repeat(params?.items?.size ?: 0) {
                    @Suppress("UNCHECKED_CAST")
                    (this as MutableList<Any?>).add(null)
                }
            }
            return CompletableFuture.completedFuture(nulls)
        }
        override fun showDocument(
            params: ShowDocumentParams?
        ): CompletableFuture<ShowDocumentResult> {
            params?.let { showDocumentRequests.add(it) }
            return CompletableFuture.completedFuture(ShowDocumentResult(true))
        }
        override fun registerCapability(params: RegistrationParams?): CompletableFuture<Void> {
            params?.let { registerCapabilityRequests.add(it) }
            return CompletableFuture.completedFuture(null)
        }
        override fun unregisterCapability(params: UnregistrationParams?): CompletableFuture<Void> {
            params?.let { unregisterCapabilityRequests.add(it) }
            return CompletableFuture.completedFuture(null)
        }
        override fun createProgress(
            params: WorkDoneProgressCreateParams?
        ): CompletableFuture<Void> {
            params?.let { workDoneProgressCreateRequests.add(it) }
            return CompletableFuture.completedFuture(null)
        }
        override fun workspaceFolders(): CompletableFuture<MutableList<WorkspaceFolder>> {
            workspaceFoldersRequests.add(Unit)
            return CompletableFuture.completedFuture(
                mutableListOf(WorkspaceFolder("file:///conformance", "conformance-root"))
            )
        }
        override fun refreshSemanticTokens(): CompletableFuture<Void> =
            recordRefresh("workspace/semanticTokens/refresh")
        override fun refreshCodeLenses(): CompletableFuture<Void> =
            recordRefresh("workspace/codeLens/refresh")
        override fun refreshInlayHints(): CompletableFuture<Void> =
            recordRefresh("workspace/inlayHint/refresh")
        override fun refreshInlineValues(): CompletableFuture<Void> =
            recordRefresh("workspace/inlineValue/refresh")
        override fun refreshDiagnostics(): CompletableFuture<Void> =
            recordRefresh("workspace/diagnostic/refresh")
        override fun refreshFoldingRanges(): CompletableFuture<Void> =
            recordRefresh("workspace/foldingRange/refresh")

        private fun recordRefresh(name: String): CompletableFuture<Void> {
            refreshCalls.add(name)
            return CompletableFuture.completedFuture(null)
        }
    }

    private companion object {
        const val TIMEOUT_S = 15L
        const val PIPE_BUFFER = 1 shl 20
        const val EXPECTED_REFRESH_COUNT = 6
    }
}
