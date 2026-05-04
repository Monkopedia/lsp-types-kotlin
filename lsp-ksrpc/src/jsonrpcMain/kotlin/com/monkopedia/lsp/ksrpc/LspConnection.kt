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
import com.monkopedia.lsp.LanguageClient
import com.monkopedia.lsp.LanguageServer
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel

/**
 * Open a JSON-RPC connection over a pair of byte channels using the LSP wire format
 * (`Content-Length` framing + `$/cancelRequest` cancellation convention).
 *
 * Use [connectAsLspClient] / [connectAsLspServer] on the returned connection to host
 * a [LanguageClient] / [LanguageServer] implementation and obtain a stub for the remote side.
 */
suspend fun Pair<ByteReadChannel, ByteWriteChannel>.asLspConnection(
    env: KsrpcEnvironment<String> = ksrpcEnvironment { }
): SingleChannelConnection<String> = asJsonRpcConnection(
    env = env,
    includeContentHeaders = true,
    cancellationConvention = JsonRpcCancellationConvention.Lsp
)

/**
 * Connect this LSP connection from the client side: hosts a [LanguageClient] implementation
 * and returns a [LanguageServer] stub for sending requests/notifications to the server.
 *
 * The server side ([LanguageServer]) is exposed by the remote process; the client side
 * ([LanguageClient]) is hosted locally.
 */
suspend fun SingleChannelConnection<String>.connectAsLspClient(
    client: LanguageClient
): LanguageServer {
    lateinit var server: LanguageServer
    connect<LanguageClient, LanguageServer, String> { remoteServer ->
        server = remoteServer
        client
    }
    return server
}

/**
 * Connect this LSP connection from the server side: hosts a [LanguageServer] implementation
 * and returns a [LanguageClient] stub for sending requests/notifications back to the client.
 */
suspend fun SingleChannelConnection<String>.connectAsLspServer(
    server: LanguageServer
): LanguageClient {
    lateinit var client: LanguageClient
    connect<LanguageServer, LanguageClient, String> { remoteClient ->
        client = remoteClient
        server
    }
    return client
}
