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
import com.monkopedia.lsp.ClientCapabilities
import com.monkopedia.lsp.CompletionParams
import com.monkopedia.lsp.DefinitionParams
import com.monkopedia.lsp.DidOpenTextDocumentParams
import com.monkopedia.lsp.DocumentSymbolParams
import com.monkopedia.lsp.HoverContents
import com.monkopedia.lsp.HoverParams
import com.monkopedia.lsp.InitializeParams
import com.monkopedia.lsp.InitializedParams
import com.monkopedia.lsp.KsrpcLanguageServer
import com.monkopedia.lsp.Position
import com.monkopedia.lsp.SingleOrArray
import com.monkopedia.lsp.TextDocumentCompletionResult
import com.monkopedia.lsp.TextDocumentDefinitionResult
import com.monkopedia.lsp.TextDocumentDocumentSymbolResult
import com.monkopedia.lsp.TextDocumentIdentifier
import com.monkopedia.lsp.TextDocumentItem
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
     * Server → client surface: open the [ConformanceLanguageServer.Triggers.ALL]
     * URI to drive the fixture's `emitAllClientTriggers`. Assert that every method
     * the fixture issues appears on the local [ConformanceLanguageClient]'s
     * recording lists (i.e. the wire actually carried it). Skipped for transports
     * that do not carry server-initiated traffic (the in-process ksrpc relay).
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
    suspend fun withSession(block: suspend (KsrpcLanguageServer) -> Unit)

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
    override suspend fun withSession(block: suspend (KsrpcLanguageServer) -> Unit) =
        withContext(Dispatchers.IO) {
            val clientToServer = ByteChannel(autoFlush = true)
            val serverToClient = ByteChannel(autoFlush = true)
            val server = standaloneServerScope()
            server.launch {
                val conn = (clientToServer to serverToClient).asLspConnection()
                conn.connectAsLspServer(ConformanceLanguageServer())
            }
            try {
                val conn = (serverToClient to clientToServer).asLspConnection()
                val remote = conn.connectAsLspClient(ConformanceLanguageClient())
                block(remote)
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
    override suspend fun withSession(block: suspend (KsrpcLanguageServer) -> Unit) =
        runTcpSession { remote, _, _ -> block(remote) }

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
    override suspend fun withSession(block: suspend (KsrpcLanguageServer) -> Unit) =
        runStdioSession { remote, _, _ -> block(remote) }

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
        // client -> server pipe
        val serverIn = PipedInputStream(1 shl 16)
        val clientOut = PipedOutputStream(serverIn)
        // server -> client pipe
        val clientIn = PipedInputStream(1 shl 16)
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

    override suspend fun withSession(block: suspend (KsrpcLanguageServer) -> Unit) {
        withContext(Dispatchers.IO) {
            val env = lspKsrpcEnvironment()
            val serialized =
                ConformanceLanguageServer().serialized<KsrpcLanguageServer, String>(env)
            val remote = serialized.toStub<KsrpcLanguageServer, String>()
            try {
                block(remote)
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
