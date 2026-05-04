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
import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import io.ktor.utils.io.read
import io.ktor.utils.io.reader
import java.io.InputStream
import java.io.OutputStream
import java.nio.channels.Channels
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.GlobalScope

/**
 * Open an LSP-compatible JSON-RPC connection over a pair of byte streams.
 *
 * Configures `Content-Length` framing and the LSP `$/cancelRequest` cancellation
 * convention. Use [connectAsLspClient] or [connectAsLspServer] to wire up service stubs.
 */
@Suppress("OPT_IN_USAGE")
suspend fun Pair<InputStream, OutputStream>.asLspConnection(
    env: KsrpcEnvironment<String> = lspKsrpcEnvironment()
): SingleChannelConnection<String> {
    val (input, output) = this
    val writeChannel = GlobalScope.reader(coroutineContext) {
        val outputChannel = Channels.newChannel(output)
        while (!channel.isClosedForRead) {
            channel.read { buffer ->
                outputChannel.write(buffer)
                output.flush()
            }
        }
    }.channel
    return (input.toByteReadChannel(coroutineContext) to writeChannel).asLspConnection(env)
}

/**
 * Open an LSP-compatible JSON-RPC connection over this process's standard input/output.
 *
 * The conventional way for an LSP server to talk to its parent process. From the
 * server's perspective:
 *
 * ```
 * suspend fun main() {
 *     val connection = stdInLspConnection()
 *     connection.connectAsLspServer(MyServerImpl)
 * }
 * ```
 */
suspend fun stdInLspConnection(
    env: KsrpcEnvironment<String> = lspKsrpcEnvironment()
): SingleChannelConnection<String> = (System.`in` to System.out).asLspConnection(env)

/**
 * Spawn a child process and open an LSP-compatible JSON-RPC connection over its
 * stdin/stdout streams. Useful for client code that talks to a real LSP server like
 * `ruff server` or `typescript-language-server`.
 *
 * ```
 * val connection = ProcessBuilder("ruff", "server").asLspConnection()
 * val server = connection.connectAsLspClient(MyClientImpl)
 * val initResult = server.initialize(InitializeParams(...))
 * ```
 */
suspend fun ProcessBuilder.asLspConnection(
    env: KsrpcEnvironment<String> = lspKsrpcEnvironment()
): SingleChannelConnection<String> {
    val process = redirectInput(ProcessBuilder.Redirect.PIPE)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .start()
    return (process.inputStream to process.outputStream).asLspConnection(env)
}
