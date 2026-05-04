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
@file:OptIn(DelicateCoroutinesApi::class)

package com.monkopedia.lsp.ksrpc

import com.monkopedia.lsp.BooleanOr
import com.monkopedia.lsp.ClientCapabilities
import com.monkopedia.lsp.DefaultLanguageClient
import com.monkopedia.lsp.DefaultLanguageServer
import com.monkopedia.lsp.DidOpenTextDocumentParams
import com.monkopedia.lsp.InitializeParams
import com.monkopedia.lsp.InitializeResult
import com.monkopedia.lsp.LanguageClient
import com.monkopedia.lsp.LanguageServer
import com.monkopedia.lsp.MessageType
import com.monkopedia.lsp.ServerCapabilities
import com.monkopedia.lsp.ShowMessageParams
import com.monkopedia.lsp.TextDocumentItem
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.close
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

/**
 * End-to-end test: a [LanguageServer] impl on one side of a pair of in-memory
 * byte channels, a [LanguageClient] impl on the other. Exercises the JSON-RPC
 * wire (Content-Length framing, generated `@KsService` interfaces,
 * `@KsNotification` dispatch).
 *
 * Server side runs on `GlobalScope.launch(Dispatchers.Default)` to mirror the
 * pattern used by ksrpc's own JSON-RPC tests — the server's receiver coroutine
 * runs independently and is shut down by closing the channels.
 */
class InMemoryLspIntegrationTest {

    @Test
    fun `client initialize round-trips through the connection`() = runBlocking(Dispatchers.IO) {
        val received = CompletableDeferred<InitializeParams>()
        val server = object : DefaultLanguageServer() {
            override suspend fun initialize(params: InitializeParams): InitializeResult {
                received.complete(params)
                return InitializeResult(
                    capabilities = ServerCapabilities(
                        hoverProvider = BooleanOr.BooleanValue(true)
                    )
                )
            }
        }

        runWithLspConnection(server, DefaultLanguageClient()) { remoteServer ->
            withTimeout(5_000) {
                val result = remoteServer.initialize(
                    InitializeParams(
                        capabilities = ClientCapabilities(),
                        processId = 1234,
                        rootUri = "file:///workspace"
                    )
                )
                assertNotNull(result)
                assertNotNull(result.capabilities.hoverProvider)
                assertEquals(1234, received.await().processId)
            }
        }
    }

    @Test
    fun `cancellation propagates from client to server handler`() =
        runBlocking(Dispatchers.IO) {
            val handlerStarted = CompletableDeferred<Unit>()
            val handlerCancelled = CompletableDeferred<Unit>()

            val server = object : DefaultLanguageServer() {
                override suspend fun initialize(params: InitializeParams): InitializeResult {
                    handlerStarted.complete(Unit)
                    try {
                        awaitCancellation()
                    } catch (e: kotlinx.coroutines.CancellationException) {
                        handlerCancelled.complete(Unit)
                        throw e
                    }
                }
            }

            runWithLspConnection(server, DefaultLanguageClient()) { remoteServer ->
                coroutineScope {
                    val initJob = async {
                        remoteServer.initialize(
                            InitializeParams(
                                capabilities = ClientCapabilities(),
                                processId = null,
                                rootUri = null
                            )
                        )
                    }
                    withTimeout(5_000) { handlerStarted.await() }
                    initJob.cancel()
                    withTimeout(5_000) { handlerCancelled.await() }
                    // Confirms the LSP `$/cancelRequest` convention worked end-to-end:
                    // client coroutine cancellation → wire notification → server handler cancellation.
                }
            }
        }

    @Test
    fun `bidirectional - server publishes diagnostics back to client`() =
        runBlocking(Dispatchers.IO) {
            val publishedDiagnostics =
                CompletableDeferred<com.monkopedia.lsp.PublishDiagnosticsParams>()

            // Capture the client stub from the server side so the server can call back.
            val serverSideClient = CompletableDeferred<com.monkopedia.lsp.LanguageClient>()

            val server = object : DefaultLanguageServer() {
                override suspend fun initialize(params: InitializeParams): InitializeResult {
                    // Simulate the server pushing a diagnostic immediately after init.
                    serverSideClient.await().textDocumentPublishDiagnostics(
                        com.monkopedia.lsp.PublishDiagnosticsParams(
                            uri = "file:///foo.kt",
                            diagnostics = emptyList()
                        )
                    )
                    return InitializeResult(capabilities = ServerCapabilities())
                }
            }
            val client = object : DefaultLanguageClient() {
                override suspend fun textDocumentPublishDiagnostics(
                    params: com.monkopedia.lsp.PublishDiagnosticsParams
                ) {
                    publishedDiagnostics.complete(params)
                }
            }

            runWithLspConnectionCapturingClient(server, client, serverSideClient) { remoteServer ->
                withTimeout(5_000) {
                    remoteServer.initialize(
                        InitializeParams(
                            capabilities = ClientCapabilities(),
                            processId = null,
                            rootUri = null
                        )
                    )
                    val diag = publishedDiagnostics.await()
                    assertEquals("file:///foo.kt", diag.uri)
                }
            }
        }

    @Test
    fun `notification reaches server without expecting a response`() = runBlocking(Dispatchers.IO) {
        val opened = CompletableDeferred<DidOpenTextDocumentParams>()
        val server = object : DefaultLanguageServer() {
            override suspend fun initialize(params: InitializeParams): InitializeResult =
                InitializeResult(capabilities = ServerCapabilities())

            override suspend fun textDocumentDidOpen(params: DidOpenTextDocumentParams) {
                opened.complete(params)
            }
        }

        runWithLspConnection(server, DefaultLanguageClient()) { remoteServer ->
            withTimeout(5_000) {
                remoteServer.initialize(
                    InitializeParams(
                        capabilities = ClientCapabilities(),
                        processId = null,
                        rootUri = null
                    )
                )
                remoteServer.textDocumentDidOpen(
                    DidOpenTextDocumentParams(
                        textDocument = TextDocumentItem(
                            uri = "file:///foo.kt",
                            languageId = "kotlin",
                            version = 1,
                            text = "fun main() {}"
                        )
                    )
                )
                val params = opened.await()
                assertEquals("file:///foo.kt", params.textDocument.uri)
            }
        }
    }
}

/**
 * Like [runWithLspConnection] but completes [serverSideClient] with the server's
 * stub of the client, so the server can call back to the client during the test.
 */
private suspend fun runWithLspConnectionCapturingClient(
    server: LanguageServer,
    client: LanguageClient,
    serverSideClient: CompletableDeferred<LanguageClient>,
    block: suspend (LanguageServer) -> Unit
) {
    val clientToServer = ByteChannel(autoFlush = true)
    val serverToClient = ByteChannel(autoFlush = true)

    GlobalScope.launch(Dispatchers.Default) {
        val conn = (clientToServer to serverToClient).asLspConnection()
        serverSideClient.complete(conn.connectAsLspServer(server))
    }

    try {
        val conn = (serverToClient to clientToServer).asLspConnection()
        conn.connectAsLspClient(client).let { remoteServer ->
            block(remoteServer)
        }
    } finally {
        clientToServer.close()
        serverToClient.close()
    }
}

/**
 * Wire a [LanguageServer] and [LanguageClient] together over a pair of in-memory
 * byte channels. The server runs on `GlobalScope` so its receiver coroutine
 * doesn't block scope completion. [block] runs against the server stub from
 * the client side. Channels are closed when [block] returns.
 */
private suspend fun runWithLspConnection(
    server: LanguageServer,
    client: LanguageClient,
    block: suspend (LanguageServer) -> Unit
) {
    val clientToServer = ByteChannel(autoFlush = true)
    val serverToClient = ByteChannel(autoFlush = true)

    GlobalScope.launch(Dispatchers.Default) {
        val conn = (clientToServer to serverToClient).asLspConnection()
        conn.connectAsLspServer(server)
    }

    try {
        val conn = (serverToClient to clientToServer).asLspConnection()
        conn.connectAsLspClient(client).let { remoteServer ->
            block(remoteServer)
        }
    } finally {
        clientToServer.close()
        serverToClient.close()
    }
}
