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
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CoroutineScope

/**
 * Open an LSP-compatible JSON-RPC connection over a pair of byte streams.
 *
 * Configures `Content-Length` framing and the LSP `$/cancelRequest` cancellation
 * convention. Use [connectAsLspClient] or [connectAsLspServer] to wire up service stubs.
 */
suspend fun Pair<InputStream, OutputStream>.asLspConnection(
    env: KsrpcEnvironment<String> = lspKsrpcEnvironment()
): SingleChannelConnection<String> {
    val (input, output) = this
    // Parent the stdout-pump coroutine to the caller's job (via coroutineContext)
    // rather than GlobalScope, so it is cancelled when the caller's scope ends.
    val writeChannel = CoroutineScope(coroutineContext).reader(coroutineContext) {
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

/**
 * JVM implementation of [LspServerProcess]. Wraps a [java.lang.Process] and the
 * LSP connection built over its stdio. Unlike the lower-level
 * [ProcessBuilder.asLspConnection] helper, this carries the process handle, so the
 * caller can [kill] / [close] the child deterministically.
 */
private class JvmLspServerProcess(
    private val process: Process,
    override val connection: SingleChannelConnection<String>
) : LspServerProcess {

    override val pid: Long
        get() = process.pid()

    /**
     * Terminate the child. Requests a graceful [Process.destroy], then — after a short
     * bounded grace window — force-kills with [Process.destroyForcibly]. Bounded and
     * non-suspending: safe to call from a `finally`. Idempotent (destroying an
     * already-dead process is a no-op).
     */
    override fun kill() {
        if (!process.isAlive) return
        process.destroy()
        // Bounded grace, then force. waitFor with a timeout never blocks indefinitely.
        if (!process.waitFor(GRACE_MILLIS, TimeUnit.MILLISECONDS)) {
            process.destroyForcibly()
        }
    }

    /**
     * Tear down: close the child's stdio streams (so the ksrpc pump observes EOF and
     * winds down) and [kill] the child. Bounded — never an unbounded `waitFor`. Call
     * this exactly once during teardown.
     */
    override fun close() {
        runCatching { process.outputStream.close() }
        runCatching { process.inputStream.close() }
        kill()
    }

    private companion object {
        const val GRACE_MILLIS = 1_000L
    }
}

/**
 * JVM `actual` for [spawnLspServer]. Launches [command] via [ProcessBuilder] with its
 * stdin/stdout piped, builds the LSP connection over those streams with the existing
 * [Pair.asLspConnection] helper, and returns a [LspServerProcess] carrying the real
 * [java.lang.Process] handle.
 */
public actual suspend fun spawnLspServer(
    command: List<String>,
    env: KsrpcEnvironment<String>
): LspServerProcess {
    require(command.isNotEmpty()) { "command must not be empty" }
    val process = ProcessBuilder(command)
        .redirectInput(ProcessBuilder.Redirect.PIPE)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .start()
    val connection = (process.inputStream to process.outputStream).asLspConnection(env)
    return JvmLspServerProcess(process, connection)
}
