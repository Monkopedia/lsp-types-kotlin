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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

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
    // These two pumps wrap BLOCKING `java.io` stream operations: the writer drains a
    // ktor channel into `output`, and `toByteReadChannel` pulls bytes off `input`'s
    // blocking `read()`. A blocking `read()` on a dead/half-open stream (e.g. after a
    // child process is force-killed and the pipe never reaches clean EOF) parks on a
    // non-interruptible syscall that may never return.
    //
    // Historically both were parented to the caller's `coroutineContext`. Under a
    // consumer's `runBlocking { ... }`, that made the parked read a CHILD of the
    // caller's job; structured concurrency then refused to let `runBlocking` return
    // until the (never-returning) read completed — wedging teardown (issue #87).
    //
    // Instead, host both pumps in a connection-owned `Dispatchers.IO` + `SupervisorJob`
    // scope that is NOT a child of the caller. `Dispatchers.IO` threads are daemon
    // threads, so even a pump parked on a dead fd can never keep the JVM (or the
    // caller's `runBlocking`) alive. The pumps still wind down normally on clean EOF /
    // connection close; this only changes WHO owns them, removing the wedge.
    val pumpScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val writeChannel = pumpScope.reader(pumpScope.coroutineContext) {
        val outputChannel = Channels.newChannel(output)
        while (!channel.isClosedForRead) {
            channel.read { buffer ->
                outputChannel.write(buffer)
                output.flush()
            }
        }
    }.channel
    return (input.toByteReadChannel(pumpScope.coroutineContext) to writeChannel)
        .asLspConnection(env)
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
 * Start this builder with the child's stdin/stdout piped back to us — the stdio wiring
 * every LSP child process needs, in one place.
 *
 * Sets stdin and stdout only, and deliberately does NOT touch stderr — because its two
 * call sites own their builder differently. [ProcessBuilder.asLspConnection] runs on a
 * builder the CALLER configured, so forcing a redirect here would silently discard an
 * explicit `redirectError(...)` choice; `ProcessBuilder.redirectError()` cannot tell
 * "the caller chose `PIPE`" from "the caller left the default", so there is no honest
 * way to override only the latter. [spawnLspServer] constructs its own builder and
 * therefore sets its own stderr policy there, where the choice is genuinely ours.
 *
 * Stdin/stdout are different: both call sites need them piped for the connection to
 * exist at all, so they are not a caller policy to preserve.
 *
 * A builder arriving here with stderr at `ProcessBuilder`'s default — which is `PIPE`,
 * not `INHERIT` — gives the child a pipe that nothing in this library drains
 * (`process.errorStream` is never read). Once the OS buffer fills (~64 KB on Linux) the
 * child blocks on its next stderr write and silently stops servicing LSP requests, so a
 * caller-supplied builder should set `redirectError` explicitly (issue #165).
 *
 * Command, environment and working directory stay the caller's business, carried on the
 * receiver.
 */
private fun ProcessBuilder.startPiped(): Process = redirectInput(ProcessBuilder.Redirect.PIPE)
    .redirectOutput(ProcessBuilder.Redirect.PIPE)
    .start()

/**
 * Spawn a child process and open an LSP-compatible JSON-RPC connection over its
 * stdin/stdout streams. Useful for client code that talks to a real LSP server like
 * `ruff server` or `typescript-language-server`.
 *
 * ```
 * val connection = ProcessBuilder("ruff", "server")
 *     .redirectError(ProcessBuilder.Redirect.INHERIT)
 *     .asLspConnection()
 * val server = connection.connectAsLspClient(MyClientImpl)
 * val initResult = server.initialize(InitializeParams(...))
 * ```
 *
 * Stdin and stdout are piped (that is what the connection is built from). **Stderr is
 * left exactly as configured on the receiver** — this function never overrides a choice
 * the caller made. That means the caller owns it, including the consequences of
 * `ProcessBuilder`'s default: the default is `PIPE`, not `INHERIT`, and nothing here
 * drains `process.errorStream`, so a chatty server (clangd, rust-analyzer, pyright all
 * log to stderr) blocks on its next stderr write once the OS pipe buffer fills — ~64 KB
 * on Linux — and silently stops answering LSP requests (issue #165).
 *
 * So set `redirectError` before calling this: `INHERIT` to send the server's log to your
 * own stderr (what [spawnLspServer] does), `DISCARD` to drop it, or `Redirect.to(file)`
 * to capture it.
 */
suspend fun ProcessBuilder.asLspConnection(
    env: KsrpcEnvironment<String> = lspKsrpcEnvironment()
): SingleChannelConnection<String> {
    val process = startPiped()
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
 *
 * The child's **stderr is inherited**: its log output goes straight to this process's
 * stderr. Two reasons, and they point the same way (issue #165):
 *
 * 1. This function builds the `ProcessBuilder` itself and exposes no parameter that
 *    reaches `redirectError`, so a caller has NO way to fix a bad default here. Leaving
 *    `ProcessBuilder`'s default of `PIPE` would hand the child a pipe nobody drains, and
 *    the child would block on its next stderr write past the ~64 KB buffer and silently
 *    stop answering — a hang with no diagnostic and no remedy.
 * 2. It matches the posix `actual`, whose forked child `dup2`s only `STDIN_FILENO` and
 *    `STDOUT_FILENO`, leaving fd 2 inherited from the parent. This makes the two
 *    platforms mean the same thing rather than two things.
 *
 * `redirectErrorStream(true)` is NOT used and must not be: merging stderr into stdout
 * would corrupt the `Content-Length` framing the connection is parsing.
 */
public actual suspend fun spawnLspServer(
    command: List<String>,
    env: KsrpcEnvironment<String>
): LspServerProcess {
    require(command.isNotEmpty()) { "command must not be empty" }
    val process = ProcessBuilder(command)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .startPiped()
    val connection = (process.inputStream to process.outputStream).asLspConnection(env)
    return JvmLspServerProcess(process, connection)
}
