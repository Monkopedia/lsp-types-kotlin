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

import com.monkopedia.ksrpc.KsrpcEnvironment
import com.monkopedia.ksrpc.channels.SingleChannelConnection
import com.monkopedia.ksrpc.channels.connect
import com.monkopedia.ksrpc.jsonrpc.JsonRpcCancellationConvention
import com.monkopedia.ksrpc.jsonrpc.asJsonRpcConnection
import com.monkopedia.ksrpc.ksrpcEnvironment
import com.monkopedia.lsp.KsrpcLanguageClient
import com.monkopedia.lsp.KsrpcLanguageServer
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel

/**
 * Open a JSON-RPC connection over a pair of byte channels using the LSP wire format:
 * `Content-Length` framed messages and `$/cancelRequest` cancellation.
 *
 * The receiver is `(read, write)` — for two endpoints in the same process to talk
 * to each other you need two paired connections with the channels in opposite
 * orders. For real LSP scenarios use the JVM-only [stdInLspConnection] (server
 * reading its own stdin) or [ProcessBuilder.asLspConnection] (client spawning
 * a server child process).
 *
 * Use [connectAsLspClient] / [connectAsLspServer] on the returned connection to
 * host a [KsrpcLanguageClient] / [KsrpcLanguageServer] and obtain a stub for the
 * remote side.
 */
suspend fun Pair<ByteReadChannel, ByteWriteChannel>.asLspConnection(
    env: KsrpcEnvironment<String> = ksrpcEnvironment { }
): SingleChannelConnection<String> = asJsonRpcConnection(
    env = env,
    includeContentHeaders = true,
    cancellationConvention = JsonRpcCancellationConvention.Lsp
)

/**
 * Wire this connection from the client side: register a [KsrpcLanguageClient]
 * implementation so the server can call back, and get a [KsrpcLanguageServer]
 * stub for sending requests/notifications.
 *
 * Typical usage:
 *
 * ```
 * val server = connection.connectAsLspClient(MyClientImpl)
 * val initResult = server.initialize(InitializeParams(...))
 * server.initialized(InitializedParams())
 * val hover = server.textDocumentHover(...)
 * ```
 */
suspend fun SingleChannelConnection<String>.connectAsLspClient(
    client: KsrpcLanguageClient
): KsrpcLanguageServer {
    lateinit var server: KsrpcLanguageServer
    connect<KsrpcLanguageClient, KsrpcLanguageServer, String> { remoteServer ->
        server = remoteServer
        client
    }
    return server
}

/**
 * Wire this connection from the server side: register a [KsrpcLanguageServer]
 * implementation so the client can call into the server, and get a
 * [KsrpcLanguageClient] stub for sending notifications and requests back.
 *
 * Typical usage from inside a server `main`:
 *
 * ```
 * val connection = stdInLspConnection()
 * val client = connection.connectAsLspServer(MyServerImpl)
 * // Now MyServerImpl can call client.windowShowMessage(...) etc.
 * ```
 */
suspend fun SingleChannelConnection<String>.connectAsLspServer(
    server: KsrpcLanguageServer
): KsrpcLanguageClient {
    lateinit var client: KsrpcLanguageClient
    connect<KsrpcLanguageServer, KsrpcLanguageClient, String> { remoteClient ->
        client = remoteClient
        server
    }
    return client
}
