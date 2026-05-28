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
package com.monkopedia.lsp.ksrpc.fixtures

import com.monkopedia.ksrpc.serialized
import com.monkopedia.ksrpc.toStub
import com.monkopedia.lsp.CallHierarchyIncomingCallsParams
import com.monkopedia.lsp.CallHierarchyOutgoingCallsParams
import com.monkopedia.lsp.CallHierarchyPrepareParams
import com.monkopedia.lsp.ClientCapabilities
import com.monkopedia.lsp.CodeAction
import com.monkopedia.lsp.CodeActionContext
import com.monkopedia.lsp.CodeActionParams
import com.monkopedia.lsp.CodeLens
import com.monkopedia.lsp.CodeLensParams
import com.monkopedia.lsp.Color
import com.monkopedia.lsp.ColorPresentationParams
import com.monkopedia.lsp.CompletionItem
import com.monkopedia.lsp.CompletionParams
import com.monkopedia.lsp.CreateFilesParams
import com.monkopedia.lsp.DeclarationParams
import com.monkopedia.lsp.DefinitionParams
import com.monkopedia.lsp.DeleteFilesParams
import com.monkopedia.lsp.DidChangeConfigurationParams
import com.monkopedia.lsp.DidChangeNotebookDocumentParams
import com.monkopedia.lsp.DidChangeTextDocumentParams
import com.monkopedia.lsp.DidChangeWatchedFilesParams
import com.monkopedia.lsp.DidChangeWorkspaceFoldersParams
import com.monkopedia.lsp.DidCloseNotebookDocumentParams
import com.monkopedia.lsp.DidCloseTextDocumentParams
import com.monkopedia.lsp.DidOpenNotebookDocumentParams
import com.monkopedia.lsp.DidOpenTextDocumentParams
import com.monkopedia.lsp.DidSaveNotebookDocumentParams
import com.monkopedia.lsp.DidSaveTextDocumentParams
import com.monkopedia.lsp.DocumentColorParams
import com.monkopedia.lsp.DocumentDiagnosticParams
import com.monkopedia.lsp.DocumentFormattingParams
import com.monkopedia.lsp.DocumentHighlightParams
import com.monkopedia.lsp.DocumentLink
import com.monkopedia.lsp.DocumentLinkParams
import com.monkopedia.lsp.DocumentOnTypeFormattingParams
import com.monkopedia.lsp.DocumentRangeFormattingParams
import com.monkopedia.lsp.DocumentRangesFormattingParams
import com.monkopedia.lsp.DocumentSymbolParams
import com.monkopedia.lsp.ExecuteCommandParams
import com.monkopedia.lsp.FileChangeType
import com.monkopedia.lsp.FileCreate
import com.monkopedia.lsp.FileDelete
import com.monkopedia.lsp.FileEvent
import com.monkopedia.lsp.FileRename
import com.monkopedia.lsp.FoldingRangeParams
import com.monkopedia.lsp.FormattingOptions
import com.monkopedia.lsp.HoverContents
import com.monkopedia.lsp.HoverParams
import com.monkopedia.lsp.ImplementationParams
import com.monkopedia.lsp.InitializeParams
import com.monkopedia.lsp.InitializedParams
import com.monkopedia.lsp.InlayHint
import com.monkopedia.lsp.InlayHintParams
import com.monkopedia.lsp.InlineCompletionContext
import com.monkopedia.lsp.InlineCompletionParams
import com.monkopedia.lsp.InlineCompletionTriggerKind
import com.monkopedia.lsp.InlineValueContext
import com.monkopedia.lsp.InlineValueParams
import com.monkopedia.lsp.IntOrString
import com.monkopedia.lsp.KsrpcLanguageServer
import com.monkopedia.lsp.LinkedEditingRangeParams
import com.monkopedia.lsp.MonikerParams
import com.monkopedia.lsp.NotebookCell
import com.monkopedia.lsp.NotebookCellKind
import com.monkopedia.lsp.NotebookDocument
import com.monkopedia.lsp.NotebookDocumentIdentifier
import com.monkopedia.lsp.Position
import com.monkopedia.lsp.PrepareRenameParams
import com.monkopedia.lsp.ProgressParams
import com.monkopedia.lsp.Range
import com.monkopedia.lsp.ReferenceContext
import com.monkopedia.lsp.ReferenceParams
import com.monkopedia.lsp.RenameFilesParams
import com.monkopedia.lsp.RenameParams
import com.monkopedia.lsp.SelectionRangeParams
import com.monkopedia.lsp.SemanticTokensDeltaParams
import com.monkopedia.lsp.SemanticTokensParams
import com.monkopedia.lsp.SemanticTokensRangeParams
import com.monkopedia.lsp.SetTraceParams
import com.monkopedia.lsp.SignatureHelpParams
import com.monkopedia.lsp.SingleOrArray
import com.monkopedia.lsp.StringOr
import com.monkopedia.lsp.SymbolKind
import com.monkopedia.lsp.TextDocumentCompletionResult
import com.monkopedia.lsp.TextDocumentContentChangeEvent
import com.monkopedia.lsp.TextDocumentDefinitionResult
import com.monkopedia.lsp.TextDocumentDocumentSymbolResult
import com.monkopedia.lsp.TextDocumentIdentifier
import com.monkopedia.lsp.TextDocumentItem
import com.monkopedia.lsp.TextDocumentSaveReason
import com.monkopedia.lsp.TraceValues
import com.monkopedia.lsp.TypeDefinitionParams
import com.monkopedia.lsp.TypeHierarchyItem
import com.monkopedia.lsp.TypeHierarchyPrepareParams
import com.monkopedia.lsp.TypeHierarchySubtypesParams
import com.monkopedia.lsp.TypeHierarchySupertypesParams
import com.monkopedia.lsp.VersionedNotebookDocumentIdentifier
import com.monkopedia.lsp.VersionedTextDocumentIdentifier
import com.monkopedia.lsp.WillSaveTextDocumentParams
import com.monkopedia.lsp.WorkDoneProgressCancelParams
import com.monkopedia.lsp.WorkspaceDiagnosticParams
import com.monkopedia.lsp.WorkspaceFolder
import com.monkopedia.lsp.WorkspaceFoldersChangeEvent
import com.monkopedia.lsp.WorkspaceSymbol
import com.monkopedia.lsp.WorkspaceSymbolParams
import com.monkopedia.lsp.ksrpc.asLspConnection
import com.monkopedia.lsp.ksrpc.connectAsLspClient
import com.monkopedia.lsp.ksrpc.connectAsLspServer
import com.monkopedia.lsp.ksrpc.lspKsrpcEnvironment
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.close
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.JsonPrimitive
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Transport matrix round-trips (issue #46, epic #41).
 *
 * Runs the *same* lifecycle plus a representative set of typed methods
 * ([assertLifecycleAndMethods]) against a [ConformanceLanguageServer] over every
 * JVM transport, asserting that the union branches the server picks (keyed off
 * request `position.line` / document URI per the fixture's contract table) come
 * back identical regardless of which transport carried them. Parameterizing the
 * single assertion body across transports is the whole point: a transport that
 * silently corrupts a union discriminator, drops a notification, or mangles
 * framing fails here.
 *
 * ## Transports covered
 *
 * | name        | wiring                                                                 | process model |
 * |-------------|------------------------------------------------------------------------|---------------|
 * | `in-memory` | paired ktor [ByteChannel]s + LSP `Content-Length` jsonrpc framing      | in-process    |
 * | `tcp`       | ktor-network loopback sockets + LSP jsonrpc framing                    | in-process (real OS sockets) |
 * | `stdio`     | paired [PipedInputStream]/[PipedOutputStream] + LSP jsonrpc framing    | in-process (see note) |
 * | `relay`     | ksrpc [serialized]/[toStub] relay — serve the [KsrpcLanguageServer] over a ksrpc channel and consume it through the relay (kodemirror's model) | in-process |
 *
 * ### A note on the `stdio` transport
 *
 * The true child-process stdio path is exercised by [RawClientServerTest], which
 * spawns the `samples/echo-server` distribution and drives it with raw framed
 * bytes. That sample server does not implement the full conformance method set,
 * so it cannot back this matrix. Here `stdio` instead drives the *same JVM
 * stream-based* [asLspConnection] code path (`(InputStream, OutputStream) ->`
 * connection — identical to what a spawned child process uses) over an in-process
 * piped stream pair. The distinction: real sub-process isolation is not tested
 * here, but the stdio framing/adapter layer is.
 *
 * Every round-trip is bounded by [TIMEOUT_MS]; each transport's
 * [Transport.withSession] tears down with a bounded join so a stuck receive loop
 * cannot hang CI.
 */
@RunWith(Parameterized::class)
class TransportMatrixIntegrationTest(
    @Suppress("unused") private val transportName: String,
    private val transport: Transport
) {

    @org.junit.Test
    fun `lifecycle and representative typed methods round-trip identically`() =
        runBlocking(Dispatchers.IO) {
            transport.withSession { remote ->
                withTimeout(TIMEOUT_MS) {
                    assertLifecycleAndMethods(remote)
                }
            }
        }

    /**
     * Issue #65 — drive every server method (request + notification) through the
     * ksrpc stub on each transport, asserting that each request returns the
     * fixture's canned response and that each notification is recorded on the
     * fixture side.
     *
     * Lifecycle is run once; we then issue one representative call per remaining
     * method. Notifications are sent, the test waits a bounded window via
     * `withTimeout` for the fixture's notification log to contain the expected
     * methods.
     */
    @org.junit.Test
    fun `every server method round-trips via the ksrpc stub`() = runBlocking(Dispatchers.IO) {
        transport.withSessionAndFixture { remote, fixture ->
            withTimeout(TIMEOUT_MS) {
                AllMethodsDriver(remote, fixture).runAll()
            }
        }
    }

    /**
     * Server → client surface (issue #64): open the
     * [ConformanceLanguageServer.Triggers.ALL] URI to drive the fixture's
     * `emitAllClientTriggers`. Assert that every method the fixture issues appears
     * on the local [ConformanceLanguageClient]'s recording lists (i.e. the wire
     * actually carried it). Skipped for transports that do not carry
     * server-initiated traffic (the in-process ksrpc relay).
     */
    @org.junit.Test
    fun `every server-initiated client call round-trips over the wire`() =
        runBlocking(Dispatchers.IO) {
            org.junit.Assume.assumeTrue(
                "$transportName does not carry server-initiated client traffic",
                transport.supportsServerInitiated
            )
            transport.withDuplexSession { session ->
                withTimeout(TIMEOUT_MS) {
                    assertServerInitiatedTriggers(session)
                }
            }
        }

    private suspend fun assertServerInitiatedTriggers(session: DuplexSession) {
        val remote = session.remote
        val server = session.serverFixture
        val client = session.clientFixture

        remote.initialize(
            InitializeParams(
                capabilities = ClientCapabilities(),
                processId = 4242,
                rootUri = "file:///conformance"
            )
        )
        remote.initialized(InitializedParams())

        // didOpen on the trigger URI drives emitAllClientTriggers.
        remote.textDocumentDidOpen(
            DidOpenTextDocumentParams(
                textDocument = TextDocumentItem(
                    uri = ConformanceLanguageServer.Triggers.ALL,
                    languageId = "kotlin",
                    version = 1,
                    text = "// trigger"
                )
            )
        )

        // Wait for the server's own record to fill — every issued method.
        val expected = ConformanceLanguageServer.ClientMethods.ALL.toSet()
        val startNs = kotlin.time.TimeSource.Monotonic.markNow()
        while (server.issuedClientCalls.toSet() != expected) {
            if (startNs.elapsedNow().inWholeMilliseconds > TRIGGER_ASSERT_TIMEOUT_MS) {
                kotlin.test.fail(
                    "server did not issue all triggers in $TRIGGER_ASSERT_TIMEOUT_MS ms; " +
                        "saw ${server.issuedClientCalls}"
                )
            }
            kotlinx.coroutines.delay(50)
        }
        kotlin.test.assertEquals(
            expected,
            server.issuedClientCalls.toSet(),
            "server fixture must record every issued server-initiated call"
        )

        // Client side: every method we expect must have arrived over the wire.
        kotlin.test.assertEquals(
            1,
            client.configurationRequests.size,
            "configuration request"
        )
        kotlin.test.assertEquals(
            1,
            client.workspaceFoldersRequestCount,
            "workspaceFolders request"
        )
        kotlin.test.assertEquals(1, client.applyEditRequests.size, "applyEdit request")
        kotlin.test.assertEquals(
            1,
            client.showMessageRequests.size,
            "showMessageRequest"
        )
        kotlin.test.assertEquals(
            1,
            client.showDocumentRequests.size,
            "showDocument request"
        )
        kotlin.test.assertEquals(
            1,
            client.registerCapabilityRequests.size,
            "registerCapability"
        )
        kotlin.test.assertEquals(
            1,
            client.unregisterCapabilityRequests.size,
            "unregisterCapability"
        )
        kotlin.test.assertEquals(
            1,
            client.workDoneProgressCreateRequests.size,
            "workDoneProgress/create"
        )
        kotlin.test.assertTrue(
            client.progressNotifications.isNotEmpty(),
            "progress notification"
        )
        kotlin.test.assertEquals(
            setOf(
                "workspace/codeLens/refresh",
                "workspace/semanticTokens/refresh",
                "workspace/inlayHint/refresh",
                "workspace/inlineValue/refresh",
                "workspace/diagnostic/refresh",
                "workspace/foldingRange/refresh"
            ),
            client.refreshCalls.toSet(),
            "every workspace/<feature>/refresh must round-trip"
        )
        kotlin.test.assertTrue(client.telemetryEvents.isNotEmpty(), "telemetry/event")
        kotlin.test.assertTrue(client.logTraces.isNotEmpty(), "logTrace")
        kotlin.test.assertTrue(
            client.showMessages.isNotEmpty(),
            "window/showMessage notification"
        )
        kotlin.test.assertTrue(
            client.logMessages.isNotEmpty(),
            "window/logMessage notification"
        )

        kotlin.test.assertEquals(null, remote.shutdown())
        remote.exit()
    }

    private suspend fun assertLifecycleAndMethods(remote: KsrpcLanguageServer) {
        // ---- lifecycle: initialize ----
        val init = remote.initialize(
            InitializeParams(
                capabilities = ClientCapabilities(),
                processId = 4242,
                rootUri = "file:///conformance"
            )
        )
        assertNotNull(init.capabilities.hoverProvider, "server must advertise hover")
        assertNotNull(init.capabilities.definitionProvider, "server must advertise definition")
        assertEquals("ConformanceLanguageServer", init.serverInfo?.name)
        remote.initialized(com.monkopedia.lsp.InitializedParams())

        assertHoverBranches(remote)
        assertDefinitionBranches(remote)
        assertCompletionBranches(remote)
        assertDocumentSymbolBranches(remote)

        // ---- lifecycle: shutdown / exit ----
        assertEquals(null, remote.shutdown(), "shutdown result is null per LSP spec")
        remote.exit()
    }

    private suspend fun assertHoverBranches(remote: KsrpcLanguageServer) {
        val markup = remote.textDocumentHover(hover(ConformanceLanguageServer.Lines.SINGLE))
        assertTrue(
            markup.contents is HoverContents.MarkupContentValue,
            "line 0 hover should be MarkupContentValue, was ${markup.contents}"
        )
        val marked = remote.textDocumentHover(hover(ConformanceLanguageServer.Lines.ARRAY))
        assertTrue(
            marked.contents is HoverContents.MarkedStringValue,
            "line 1 hover should be MarkedStringValue, was ${marked.contents}"
        )
        val markedArray = remote.textDocumentHover(hover(ConformanceLanguageServer.Lines.LINK))
        assertTrue(
            markedArray.contents is HoverContents.MarkedStringArray,
            "line 2 hover should be MarkedStringArray, was ${markedArray.contents}"
        )
    }

    private suspend fun assertDefinitionBranches(remote: KsrpcLanguageServer) {
        val single = remote.textDocumentDefinition(
            definition(ConformanceLanguageServer.Lines.SINGLE)
        )
        val singleValue = (single as TextDocumentDefinitionResult.DefinitionValue).value
        assertTrue(
            singleValue is SingleOrArray.Single,
            "line 0 definition should be a single Location"
        )

        val array = remote.textDocumentDefinition(definition(ConformanceLanguageServer.Lines.ARRAY))
        val arrayValue = (array as TextDocumentDefinitionResult.DefinitionValue).value
        assertTrue(arrayValue is SingleOrArray.Multiple, "line 1 definition should be a Location[]")

        val link = remote.textDocumentDefinition(definition(ConformanceLanguageServer.Lines.LINK))
        assertTrue(
            link is TextDocumentDefinitionResult.DefinitionLinkArray,
            "line 2 definition should be a LocationLink[]"
        )
        assertEquals(2, link.value.size)
    }

    private suspend fun assertCompletionBranches(remote: KsrpcLanguageServer) {
        val list = remote.textDocumentCompletion(completion(ConformanceLanguageServer.Lines.SINGLE))
        assertTrue(
            list is TextDocumentCompletionResult.CompletionListValue,
            "line 0 completion should be a CompletionList"
        )
        assertEquals(1, list.value.items.size)

        val array = remote.textDocumentCompletion(completion(ConformanceLanguageServer.Lines.ARRAY))
        assertTrue(
            array is TextDocumentCompletionResult.CompletionItemArray,
            "line 1 completion should be a CompletionItem[]"
        )
        assertEquals(2, array.value.size)
    }

    private suspend fun assertDocumentSymbolBranches(remote: KsrpcLanguageServer) {
        val hierarchical = remote.textDocumentDocumentSymbol(
            DocumentSymbolParams(
                textDocument = TextDocumentIdentifier(
                    ConformanceLanguageServer.Uri.HIERARCHICAL_SYMBOLS
                )
            )
        )
        assertTrue(
            hierarchical is TextDocumentDocumentSymbolResult.DocumentSymbolArray,
            "#hierarchical should yield DocumentSymbol[]"
        )
        assertEquals(1, hierarchical.value.first().children?.size)

        val flat = remote.textDocumentDocumentSymbol(
            DocumentSymbolParams(
                textDocument = TextDocumentIdentifier(ConformanceLanguageServer.Uri.FLAT_SYMBOLS)
            )
        )
        assertTrue(
            flat is TextDocumentDocumentSymbolResult.SymbolInformationArray,
            "#flat should yield SymbolInformation[]"
        )
    }

    private fun pos(line: Int) = Position(line = line.toUInt(), character = 0u)

    private fun hover(line: Int) = HoverParams(
        textDocument = TextDocumentIdentifier(ConformanceLanguageServer.Uri.MAIN),
        position = pos(line)
    )

    private fun definition(line: Int) = DefinitionParams(
        textDocument = TextDocumentIdentifier(ConformanceLanguageServer.Uri.MAIN),
        position = pos(line)
    )

    private fun completion(line: Int) = CompletionParams(
        textDocument = TextDocumentIdentifier(ConformanceLanguageServer.Uri.MAIN),
        position = pos(line)
    )

    companion object {
        const val TIMEOUT_MS = 15_000L

        /** Bound on each transport teardown so a stuck receive loop cannot hang CI. */
        const val TEARDOWN_MS = 5_000L

        /**
         * Bound on the polling loop that waits for the server fixture's issued-call
         * record to fill — well under the per-test [TIMEOUT_MS] so a missed trigger
         * surfaces as a clear assertion rather than a 15s wait.
         */
        const val TRIGGER_ASSERT_TIMEOUT_MS = 10_000L

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun transports(): List<Array<Any>> = listOf(
            arrayOf("in-memory", InMemoryTransport),
            arrayOf("tcp", TcpTransport),
            arrayOf("stdio", StdioPipeTransport),
            arrayOf("relay", RelayTransport)
        )
    }
}

/**
 * A transport under test. [withSession] stands up a fresh
 * [ConformanceLanguageServer] reachable through the transport, hands a remote
 * stub to [block], then tears the transport down within a bounded window.
 *
 * Transports that carry server → client traffic over a real wire (i.e. every
 * transport except the in-process [RelayTransport]) implement [withDuplexSession]
 * to additionally expose the local [ConformanceLanguageServer] fixture (so the
 * test can read its `issuedClientCalls` recording) and the local
 * [ConformanceLanguageClient] (so the test can read what the client recorded).
 */
sealed interface Transport {
    suspend fun withSession(block: suspend (KsrpcLanguageServer) -> Unit) =
        withSessionAndFixture { remote, _ -> block(remote) }

    /**
     * Like [withSession] but also hands the test the live fixture instance, so
     * notifications recorded on the server side can be asserted. Implementations
     * MUST stand up the fixture in the same process; for relay/stdio transports
     * that share the same JVM this is just a direct reference.
     */
    suspend fun withSessionAndFixture(
        block: suspend (KsrpcLanguageServer, ConformanceLanguageServer) -> Unit
    )

    /**
     * Whether this transport supports the server → client trigger test
     * ([Lsp4jConformanceInteropTest]-style). Returns `false` for transports
     * (e.g. relay) whose model doesn't carry a server-initiated client wire.
     */
    val supportsServerInitiated: Boolean get() = true

    /**
     * Stand up a session and expose both ends so the caller can drive a
     * `textDocument/didOpen` against [ConformanceLanguageServer.Triggers.ALL] and
     * assert the local fixtures recorded what the wire actually carried.
     * Default implementation throws — only the wire-framed transports override.
     */
    suspend fun withDuplexSession(block: suspend (DuplexSession) -> Unit): Unit =
        error("${this::class.simpleName} does not support a duplex server-initiated session")
}

/**
 * The triple a server-initiated wire test needs: the remote server stub to send
 * `didOpen`, the local server fixture to read [ConformanceLanguageServer.issuedClientCalls]
 * from, and the local client fixture to read its `*Requests` recording lists.
 */
class DuplexSession(
    val remote: KsrpcLanguageServer,
    val serverFixture: ConformanceLanguageServer,
    val clientFixture: ConformanceLanguageClient
)

/**
 * In-memory ksrpc channel: a pair of ktor [ByteChannel]s carrying the LSP
 * `Content-Length` jsonrpc wire, server and client wired in opposite channel
 * orders. Mirrors [InMemoryLspIntegrationTest].
 */
private object InMemoryTransport : Transport {
    override suspend fun withSessionAndFixture(
        block: suspend (KsrpcLanguageServer, ConformanceLanguageServer) -> Unit
    ) = withContext(Dispatchers.IO) {
        val clientToServer = ByteChannel(autoFlush = true)
        val serverToClient = ByteChannel(autoFlush = true)
        val fixture = ConformanceLanguageServer()
        val server = standaloneServerScope()
        server.launch {
            val conn = (clientToServer to serverToClient).asLspConnection()
            conn.connectAsLspServer(fixture)
        }
        try {
            val conn = (serverToClient to clientToServer).asLspConnection()
            val remote = conn.connectAsLspClient(ConformanceLanguageClient())
            block(remote, fixture)
        } finally {
            clientToServer.close()
            serverToClient.close()
            server.boundedCancel()
        }
    }

    override suspend fun withDuplexSession(block: suspend (DuplexSession) -> Unit): Unit =
        withContext(Dispatchers.IO) {
            val clientToServer = ByteChannel(autoFlush = true)
            val serverToClient = ByteChannel(autoFlush = true)
            val serverFixture = ConformanceLanguageServer()
            val clientFixture = ConformanceLanguageClient()
            val server = standaloneServerScope()
            server.launch {
                val conn = (clientToServer to serverToClient).asLspConnection()
                // The client stub returned here lets serverFixture call back over
                // the wire on its trigger.
                serverFixture.client = conn.connectAsLspServer(serverFixture)
            }
            try {
                val conn = (serverToClient to clientToServer).asLspConnection()
                val remote = conn.connectAsLspClient(clientFixture)
                block(DuplexSession(remote, serverFixture, clientFixture))
            } finally {
                clientToServer.close()
                serverToClient.close()
                server.boundedCancel()
            }
        }
}

/**
 * TCP loopback: a real ktor-network server socket on `127.0.0.1` and a client
 * socket. Both sides expose non-blocking ktor [ByteReadChannel]/[ByteWriteChannel]
 * (via [openReadChannel] / [openWriteChannel]) onto the LSP `Content-Length`
 * jsonrpc framing — the same channel type the in-memory transport uses, just
 * carried over real OS sockets.
 */
private object TcpTransport : Transport {
    override suspend fun withSessionAndFixture(
        block: suspend (KsrpcLanguageServer, ConformanceLanguageServer) -> Unit
    ) = runTcpSession { remote, fixture, _ -> block(remote, fixture) }

    override suspend fun withDuplexSession(block: suspend (DuplexSession) -> Unit): Unit =
        runTcpSession { remote, serverFixture, clientFixture ->
            block(DuplexSession(remote, serverFixture, clientFixture))
        }

    private suspend fun runTcpSession(
        block: suspend (
            KsrpcLanguageServer,
            ConformanceLanguageServer,
            ConformanceLanguageClient
        ) -> Unit
    ): Unit = withContext(Dispatchers.IO) {
        val selector = SelectorManager(Dispatchers.IO)
        val serverSocket = aSocket(selector).tcp()
            .bind(InetSocketAddress(InetAddress.getLoopbackAddress().hostAddress, 0))
        val acceptedSocket = AtomicReference<io.ktor.network.sockets.Socket?>(null)
        val serverFixture = ConformanceLanguageServer()
        val clientFixture = ConformanceLanguageClient()
        val server = standaloneServerScope()
        server.launch {
            val socket = serverSocket.accept()
            acceptedSocket.set(socket)
            val conn = (
                socket.openReadChannel() to socket.openWriteChannel(autoFlush = true)
                ).asLspConnection()
            serverFixture.client = conn.connectAsLspServer(serverFixture)
        }
        val clientSocket =
            aSocket(selector).tcp().connect(serverSocket.localAddress as InetSocketAddress)
        try {
            val conn = (
                clientSocket.openReadChannel() to
                    clientSocket.openWriteChannel(autoFlush = true)
                ).asLspConnection()
            val remote = conn.connectAsLspClient(clientFixture)
            block(remote, serverFixture, clientFixture)
        } finally {
            runCatching { clientSocket.close() }
            runCatching { acceptedSocket.get()?.close() }
            runCatching { serverSocket.close() }
            server.boundedCancel()
            runCatching { selector.close() }
        }
    }
}

/**
 * In-process stdio: two [PipedInputStream]/[PipedOutputStream] pairs simulate a
 * child process's stdin/stdout, driving the same JVM stream-based
 * [asLspConnection] a spawned LSP server would use. See the class KDoc note for
 * the in-process vs true-process distinction.
 */
private object StdioPipeTransport : Transport {
    override suspend fun withSessionAndFixture(
        block: suspend (KsrpcLanguageServer, ConformanceLanguageServer) -> Unit
    ) = runStdioSession { remote, fixture, _ -> block(remote, fixture) }

    override suspend fun withDuplexSession(block: suspend (DuplexSession) -> Unit): Unit =
        runStdioSession { remote, serverFixture, clientFixture ->
            block(DuplexSession(remote, serverFixture, clientFixture))
        }

    private suspend fun runStdioSession(
        block: suspend (
            KsrpcLanguageServer,
            ConformanceLanguageServer,
            ConformanceLanguageClient
        ) -> Unit
    ): Unit = withContext(Dispatchers.IO) {
        // client -> server pipe — 1 MiB buffer; the all-methods test sends
        // dozens of notifications/requests in quick succession and would
        // otherwise risk filling the smaller 64 KiB buffer (the stdio writer
        // blocks once the pipe buffer is full and the reader hasn't caught up,
        // which can deadlock the run loop).
        val serverIn = PipedInputStream(1 shl 20)
        val clientOut = PipedOutputStream(serverIn)
        // server -> client pipe
        val clientIn = PipedInputStream(1 shl 20)
        val serverOut = PipedOutputStream(clientIn)

        val serverFixture = ConformanceLanguageServer()
        val clientFixture = ConformanceLanguageClient()
        val server = standaloneServerScope()
        server.launch {
            val conn = (serverIn to serverOut).asLspConnection()
            serverFixture.client = conn.connectAsLspServer(serverFixture)
        }
        try {
            val conn = (clientIn to clientOut).asLspConnection()
            val remote = conn.connectAsLspClient(clientFixture)
            block(remote, serverFixture, clientFixture)
        } finally {
            runCatching { clientOut.close() }
            runCatching { serverOut.close() }
            runCatching { clientIn.close() }
            runCatching { serverIn.close() }
            server.boundedCancel()
        }
    }
}

/**
 * ksrpc relay (kodemirror's model): serve the [ConformanceLanguageServer] as a
 * [serialized] ksrpc service and consume it through the relay via [toStub] — no
 * byte framing, the call dispatch and (de)serialization happen across the ksrpc
 * relay boundary in-process.
 */
private object RelayTransport : Transport {
    /**
     * The ksrpc relay model serves the server [serialized] and consumes it via
     * [toStub] — there is no symmetric client-callback wire here, so the
     * server → client trigger test cannot be exercised over this transport.
     */
    override val supportsServerInitiated: Boolean = false

    override suspend fun withSessionAndFixture(
        block: suspend (KsrpcLanguageServer, ConformanceLanguageServer) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val env = lspKsrpcEnvironment()
            val fixture = ConformanceLanguageServer()
            val serialized = fixture.serialized<KsrpcLanguageServer, String>(env)
            val remote = serialized.toStub<KsrpcLanguageServer, String>()
            try {
                block(remote, fixture)
            } finally {
                runCatching {
                    withTimeout(TransportMatrixIntegrationTest.TEARDOWN_MS) { serialized.close() }
                }
            }
        }
    }
}

/**
 * A standalone scope for the server receive loop, deliberately *not* a child of
 * the calling [withContext] job. The server's transport read pump can block on an
 * uninterruptible socket/stream read that does not unwind promptly on cancel;
 * parenting it to the test scope would make [withContext] await it forever. As a
 * detached scope it is cancelled (best-effort) in teardown and never awaited
 * unbounded.
 */
private fun standaloneServerScope(): CoroutineScope =
    CoroutineScope(Dispatchers.IO + SupervisorJob())

/**
 * Cancel a detached server scope and give its children a bounded window to wind
 * down; a stuck blocking read that ignores cancellation is abandoned rather than
 * awaited (the JVM tears it down at process exit), so teardown never hangs CI.
 */
private suspend fun CoroutineScope.boundedCancel() {
    val job = this.coroutineContext[Job]
    job?.cancel()
    runCatching {
        withTimeout(TransportMatrixIntegrationTest.TEARDOWN_MS) { job?.join() }
    }
}

/**
 * Drives every server method (request + notification) exactly once via the
 * provided remote stub. Used by [TransportMatrixIntegrationTest] to verify the
 * wire path of all 73 server-side methods for issue #65. Each invocation is
 * tightly scoped: requests verify a non-null/expected canned response; the
 * fixture's notification log is the ground truth for notifications.
 */
private class AllMethodsDriver(
    private val remote: KsrpcLanguageServer,
    private val fixture: ConformanceLanguageServer
) {
    private val uri = ConformanceLanguageServer.Uri.MAIN
    private val doc = TextDocumentIdentifier(uri)
    private fun pos(line: Int = 0) = Position(line = line.toUInt(), character = 0u)
    private fun rng() = Range(start = pos(0), end = pos(0).copy(character = 4u))

    suspend fun runAll() {
        // --- lifecycle ---
        val init = remote.initialize(
            InitializeParams(
                capabilities = ClientCapabilities(),
                processId = 1,
                rootUri = "file:///conformance"
            )
        )
        assertNotNull(init.capabilities.hoverProvider)
        remote.initialized(com.monkopedia.lsp.InitializedParams())

        // --- text-document requests (already covered by smoke; still call for completeness) ---
        remote.textDocumentHover(HoverParams(textDocument = doc, position = pos(0)))
        remote.textDocumentDefinition(
            DefinitionParams(textDocument = doc, position = pos(0))
        )
        remote.textDocumentDeclaration(
            DeclarationParams(textDocument = doc, position = pos(0))
        )
        remote.textDocumentTypeDefinition(
            TypeDefinitionParams(textDocument = doc, position = pos(0))
        )
        remote.textDocumentImplementation(
            ImplementationParams(textDocument = doc, position = pos(0))
        )
        remote.textDocumentReferences(
            ReferenceParams(
                textDocument = doc,
                position = pos(0),
                context = ReferenceContext(includeDeclaration = true)
            )
        )
        remote.textDocumentDocumentSymbol(DocumentSymbolParams(textDocument = doc))
        remote.textDocumentCompletion(
            CompletionParams(textDocument = doc, position = pos(0))
        )

        // --- newly-covered text-document requests ---
        assertNotNull(
            remote.textDocumentSignatureHelp(
                SignatureHelpParams(textDocument = doc, position = pos(0))
            )
        )
        assertTrue(
            remote.textDocumentDocumentHighlight(
                DocumentHighlightParams(textDocument = doc, position = pos(0))
            ).isNotEmpty()
        )
        assertTrue(
            remote.textDocumentCodeAction(
                CodeActionParams(
                    textDocument = doc,
                    range = rng(),
                    context = CodeActionContext(diagnostics = emptyList())
                )
            ).isNotEmpty()
        )
        val resolvedAction = remote.codeActionResolve(CodeAction(title = "resolve me"))
        assertEquals(true, resolvedAction.isPreferred)

        val codeLens = remote.textDocumentCodeLens(CodeLensParams(textDocument = doc)).first()
        val resolvedLens = remote.codeLensResolve(codeLens.copy(command = null))
        assertNotNull(resolvedLens.command)

        assertTrue(
            remote.textDocumentDocumentLink(DocumentLinkParams(textDocument = doc)).isNotEmpty()
        )
        val resolvedLink = remote.documentLinkResolve(DocumentLink(range = rng()))
        assertEquals("resolved link tooltip", resolvedLink.tooltip)

        assertTrue(
            remote.textDocumentDocumentColor(DocumentColorParams(textDocument = doc)).isNotEmpty()
        )
        assertTrue(
            remote.textDocumentColorPresentation(
                ColorPresentationParams(
                    textDocument = doc,
                    color = Color(red = 1.0, green = 0.0, blue = 0.0, alpha = 1.0),
                    range = rng()
                )
            ).isNotEmpty()
        )

        assertTrue(
            remote.textDocumentSelectionRange(
                SelectionRangeParams(textDocument = doc, positions = listOf(pos(0)))
            ).isNotEmpty()
        )

        assertTrue(
            remote.textDocumentFoldingRange(FoldingRangeParams(textDocument = doc)).isNotEmpty()
        )

        // formatting family
        val fmtOptions = FormattingOptions(tabSize = 4u, insertSpaces = true)
        assertTrue(
            remote.textDocumentFormatting(
                DocumentFormattingParams(textDocument = doc, options = fmtOptions)
            ).isNotEmpty()
        )
        assertTrue(
            remote.textDocumentRangeFormatting(
                DocumentRangeFormattingParams(
                    textDocument = doc,
                    range = rng(),
                    options = fmtOptions
                )
            ).isNotEmpty()
        )
        assertTrue(
            remote.textDocumentRangesFormatting(
                DocumentRangesFormattingParams(
                    textDocument = doc,
                    ranges = listOf(rng()),
                    options = fmtOptions
                )
            ).isNotEmpty()
        )
        assertTrue(
            remote.textDocumentOnTypeFormatting(
                DocumentOnTypeFormattingParams(
                    textDocument = doc,
                    position = pos(0),
                    ch = "}",
                    options = fmtOptions
                )
            ).isNotEmpty()
        )

        // rename
        val renameEdit = remote.textDocumentRename(
            RenameParams(textDocument = doc, position = pos(0), newName = "renamed")
        )
        assertNotNull(renameEdit.changes)
        val prepRename = remote.textDocumentPrepareRename(
            PrepareRenameParams(textDocument = doc, position = pos(0))
        )
        assertNotNull(prepRename)

        // willSaveWaitUntil — request, not notification
        assertTrue(
            remote.textDocumentWillSaveWaitUntil(
                WillSaveTextDocumentParams(
                    textDocument = doc,
                    reason = TextDocumentSaveReason.MANUAL
                )
            ).isNotEmpty()
        )

        // completion item resolve
        val resolvedItem = remote.completionItemResolve(CompletionItem(label = "test"))
        assertEquals("resolved: test", resolvedItem.detail)

        // semantic tokens
        assertNotNull(
            remote.textDocumentSemanticTokensFull(SemanticTokensParams(textDocument = doc))
        )
        assertNotNull(
            remote.textDocumentSemanticTokensFullDelta(
                SemanticTokensDeltaParams(textDocument = doc, previousResultId = "prev-1")
            )
        )
        assertNotNull(
            remote.textDocumentSemanticTokensRange(
                SemanticTokensRangeParams(textDocument = doc, range = rng())
            )
        )

        // call hierarchy
        val callItem = remote.textDocumentPrepareCallHierarchy(
            CallHierarchyPrepareParams(textDocument = doc, position = pos(0))
        ).first()
        assertTrue(
            remote.callHierarchyIncomingCalls(
                CallHierarchyIncomingCallsParams(item = callItem)
            ).isNotEmpty()
        )
        assertTrue(
            remote.callHierarchyOutgoingCalls(
                CallHierarchyOutgoingCallsParams(item = callItem)
            ).isNotEmpty()
        )

        // type hierarchy
        val typeItem = remote.textDocumentPrepareTypeHierarchy(
            TypeHierarchyPrepareParams(textDocument = doc, position = pos(0))
        ).first()
        assertTrue(
            remote.typeHierarchySupertypes(
                TypeHierarchySupertypesParams(item = typeItem)
            ).isNotEmpty()
        )
        assertTrue(
            remote.typeHierarchySubtypes(
                TypeHierarchySubtypesParams(item = typeItem)
            ).isNotEmpty()
        )

        // linked editing, moniker, inline value, inline completion
        assertNotNull(
            remote.textDocumentLinkedEditingRange(
                LinkedEditingRangeParams(textDocument = doc, position = pos(0))
            )
        )
        assertTrue(
            remote.textDocumentMoniker(
                MonikerParams(textDocument = doc, position = pos(0))
            ).isNotEmpty()
        )
        assertTrue(
            remote.textDocumentInlineValue(
                InlineValueParams(
                    textDocument = doc,
                    range = rng(),
                    context = InlineValueContext(
                        frameId = 0,
                        stoppedLocation = rng()
                    )
                )
            ).isNotEmpty()
        )
        assertNotNull(
            remote.textDocumentInlineCompletion(
                InlineCompletionParams(
                    textDocument = doc,
                    position = pos(0),
                    context = InlineCompletionContext(
                        triggerKind = InlineCompletionTriggerKind.INVOKED
                    )
                )
            )
        )

        // inlayHint + resolve
        val hint = remote.textDocumentInlayHint(
            InlayHintParams(textDocument = doc, range = rng())
        ).first()
        val resolvedHint = remote.inlayHintResolve(hint)
        assertNotNull(resolvedHint.tooltip)

        // diagnostic family
        assertNotNull(
            remote.textDocumentDiagnostic(DocumentDiagnosticParams(textDocument = doc))
        )
        val wsDiag = remote.workspaceDiagnostic(
            WorkspaceDiagnosticParams(previousResultIds = emptyList())
        )
        assertTrue(wsDiag.items.isNotEmpty())

        // workspace symbol + resolve + executeCommand
        assertNotNull(remote.workspaceSymbol(WorkspaceSymbolParams(query = "q")))
        val wsSym = WorkspaceSymbol(
            name = "x",
            kind = SymbolKind.FUNCTION,
            location = com.monkopedia.lsp.Location(uri = uri, range = rng())
        )
        assertEquals("resolved", remote.workspaceSymbolResolve(wsSym).containerName)
        assertNotNull(
            remote.workspaceExecuteCommand(ExecuteCommandParams(command = "do.thing"))
        )

        // will/create/rename/delete file requests
        assertNotNull(
            remote.workspaceWillCreateFiles(CreateFilesParams(files = listOf(FileCreate(uri))))
        )
        assertNotNull(
            remote.workspaceWillRenameFiles(
                RenameFilesParams(files = listOf(FileRename(oldUri = uri, newUri = "$uri.new")))
            )
        )
        assertNotNull(
            remote.workspaceWillDeleteFiles(DeleteFilesParams(files = listOf(FileDelete(uri))))
        )

        // --- notifications (server records receipt) ---
        // textDocument lifecycle
        remote.textDocumentDidOpen(
            DidOpenTextDocumentParams(
                TextDocumentItem(uri = uri, languageId = "kotlin", version = 1, text = "x")
            )
        )
        remote.textDocumentDidChange(
            DidChangeTextDocumentParams(
                textDocument = VersionedTextDocumentIdentifier(uri = uri, version = 2),
                contentChanges = listOf(
                    com.monkopedia.lsp.TextDocumentContentChangeEventVariant(text = "y")
                )
            )
        )
        remote.textDocumentDidSave(DidSaveTextDocumentParams(textDocument = doc, text = "y"))
        remote.textDocumentWillSave(
            WillSaveTextDocumentParams(
                textDocument = doc,
                reason = TextDocumentSaveReason.MANUAL
            )
        )
        remote.textDocumentDidClose(DidCloseTextDocumentParams(textDocument = doc))

        // workspace
        remote.workspaceDidChangeConfiguration(
            DidChangeConfigurationParams(settings = JsonPrimitive("conf"))
        )
        remote.workspaceDidChangeWatchedFiles(
            DidChangeWatchedFilesParams(
                changes = listOf(FileEvent(uri = uri, type = FileChangeType.CHANGED))
            )
        )
        remote.workspaceDidChangeWorkspaceFolders(
            DidChangeWorkspaceFoldersParams(
                event = WorkspaceFoldersChangeEvent(
                    added = listOf(WorkspaceFolder(uri = uri, name = "root")),
                    removed = emptyList()
                )
            )
        )
        remote.workspaceDidCreateFiles(CreateFilesParams(files = listOf(FileCreate(uri))))
        remote.workspaceDidRenameFiles(
            RenameFilesParams(files = listOf(FileRename(oldUri = uri, newUri = "$uri.new")))
        )
        remote.workspaceDidDeleteFiles(DeleteFilesParams(files = listOf(FileDelete(uri))))

        // notebooks
        val notebook = NotebookDocument(
            uri = "$uri.ipynb",
            notebookType = "kotlin",
            version = 1,
            cells = listOf(
                NotebookCell(
                    kind = NotebookCellKind.CODE,
                    document = "$uri#cell0"
                )
            )
        )
        remote.notebookDocumentDidOpen(
            DidOpenNotebookDocumentParams(
                notebookDocument = notebook,
                cellTextDocuments = emptyList()
            )
        )
        remote.notebookDocumentDidChange(
            DidChangeNotebookDocumentParams(
                notebookDocument = VersionedNotebookDocumentIdentifier(
                    uri = notebook.uri,
                    version = 2
                )
            )
        )
        remote.notebookDocumentDidSave(
            DidSaveNotebookDocumentParams(
                notebookDocument = NotebookDocumentIdentifier(uri = notebook.uri)
            )
        )
        remote.notebookDocumentDidClose(
            DidCloseNotebookDocumentParams(
                notebookDocument = NotebookDocumentIdentifier(uri = notebook.uri),
                cellTextDocuments = emptyList()
            )
        )

        // $/setTrace, $/progress, window/workDoneProgress/cancel
        remote.setTrace(SetTraceParams(value = TraceValues.VERBOSE))
        remote.progress(
            ProgressParams(
                token = IntOrString.StringValue("p1"),
                value = JsonPrimitive("ping")
            )
        )
        remote.windowWorkDoneProgressCancel(
            WorkDoneProgressCancelParams(token = IntOrString.StringValue("p1"))
        )

        // The notifications were sent fire-and-forget; we need to drain a
        // *quiet period* on the wire before counting receipts, so that the
        // server has had time to process them all. We then shut down cleanly
        // (shutdown is a request, so the round-trip itself proves the server
        // is caught up on everything previously queued).
        val expected = setOf(
            "initialized",
            "textDocument/didOpen",
            "textDocument/didChange",
            "textDocument/didSave",
            "textDocument/willSave",
            "textDocument/didClose",
            "workspace/didChangeConfiguration",
            "workspace/didChangeWatchedFiles",
            "workspace/didChangeWorkspaceFolders",
            "workspace/didCreateFiles",
            "workspace/didRenameFiles",
            "workspace/didDeleteFiles",
            "notebookDocument/didOpen",
            "notebookDocument/didChange",
            "notebookDocument/didSave",
            "notebookDocument/didClose",
            "\$/setTrace",
            "\$/progress",
            "window/workDoneProgress/cancel"
        )

        // shutdown is a *request*: by the time its response arrives, every
        // previously-sent message has been dispatched on the wire. Some
        // transports may still be draining the notification-handler queue on
        // the server side though, so poll briefly for any stragglers.
        assertEquals(null, remote.shutdown())
        var seen = fixture.snapshotNotifications().map { it.method }.toSet()
        val deadline = System.currentTimeMillis() + 5_000
        while ((expected - seen).isNotEmpty() && System.currentTimeMillis() < deadline) {
            kotlinx.coroutines.delay(50)
            seen = fixture.snapshotNotifications().map { it.method }.toSet()
        }
        val missing = expected - seen
        assertTrue(
            missing.isEmpty(),
            "Expected the fixture to have recorded every server-side notification " +
                "after shutdown; missing=$missing, seen=$seen"
        )
        remote.exit()
    }
}
