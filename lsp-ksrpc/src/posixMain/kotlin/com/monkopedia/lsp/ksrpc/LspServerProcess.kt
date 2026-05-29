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
@file:OptIn(ExperimentalForeignApi::class)

package com.monkopedia.lsp.ksrpc

import com.monkopedia.ksrpc.KsrpcEnvironment
import com.monkopedia.ksrpc.channels.SingleChannelConnection
import com.monkopedia.ksrpc.sockets.posixFileReadChannel
import com.monkopedia.ksrpc.sockets.posixFileWriteChannel
import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.cstr
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toCValues
import platform.posix.SIGKILL
import platform.posix.SIGTERM
import platform.posix.STDIN_FILENO
import platform.posix.STDOUT_FILENO
import platform.posix.WNOHANG
import platform.posix._exit
import platform.posix.close
import platform.posix.dup2
import platform.posix.execvp
import platform.posix.fork
import platform.posix.kill
import platform.posix.pipe
import platform.posix.usleep
import platform.posix.waitpid

/**
 * A spawned external LSP server child process and the JSON-RPC connection to it.
 *
 * This is the Kotlin/Native analog of the JVM
 * [ProcessBuilder.asLspConnection][com.monkopedia.lsp.ksrpc.asLspConnection] helper.
 * Kotlin/Native has no `ProcessBuilder`, so [spawnLspServer] forks/execs the server
 * directly and wires the parent ends of two `pipe(2)`s — the child's stdin and
 * stdout — as ksrpc posix-fd byte channels feeding the shared
 * [asLspConnection][com.monkopedia.lsp.ksrpc.asLspConnection] path.
 *
 * Obtain the language-server stub with
 * [connection]`.`[connectAsLspClient][com.monkopedia.lsp.ksrpc.connectAsLspClient].
 *
 * ## Teardown
 *
 * LSP servers are expected to be torn down by the client. Prefer a clean
 * `shutdown`/`exit` round-trip, but that depends on the server cooperating; for a
 * guaranteed-bounded teardown call [kill] (SIGTERM, then SIGKILL after a grace
 * window) and then [close]. Closing the channels lets the ksrpc posix reader thread
 * observe EOF and terminate (ksrpc >= 1.1.0, the #201 fix), so teardown never wedges.
 *
 * Not Windows (mingw): there is no posix process model there. Use the JVM helper for
 * a portable client, or a relay channel for mingw.
 */
public class LspServerProcess internal constructor(
    /** PID of the spawned server child. */
    public val pid: Int,
    /**
     * JSON-RPC connection to the child over its stdio. Wire it with
     * [connectAsLspClient][com.monkopedia.lsp.ksrpc.connectAsLspClient].
     */
    public val connection: SingleChannelConnection<String>,
    private val stdinWriteFd: Int,
    private val stdoutReadFd: Int
) {
    /**
     * Signal the child to terminate. Sends SIGTERM, and if the process is still
     * alive after a short grace window, SIGKILL. Bounded and non-suspending: safe
     * to call from a `finally` during teardown. Idempotent.
     */
    public fun kill() {
        if (pid <= 0) return
        kill(pid, SIGTERM)
        // Short bounded grace, then force-kill. We poll waitpid(WNOHANG) rather
        // than block, so this can never hang even if the child ignores SIGTERM.
        memScoped {
            val status = allocArray<IntVar>(1)
            if (!waitBounded(status)) {
                kill(pid, SIGKILL)
                // Reap so we don't leak a zombie. Final bounded poll window.
                waitBounded(status)
            }
        }
    }

    /**
     * Poll `waitpid(WNOHANG)` up to [GRACE_POLLS] times with a short sleep between.
     * Returns true once the child is reaped (or already gone), false if the bounded
     * window elapsed with the child still alive. Never blocks indefinitely.
     */
    private fun waitBounded(status: CArrayPointer<IntVar>): Boolean {
        repeat(GRACE_POLLS) {
            // Non-zero means reaped (pid) or no such child (-1) — either way, done.
            if (waitpid(pid, status, WNOHANG) != 0) return true
            spinWait()
        }
        return false
    }

    /**
     * Tear down: [kill] the child (if still running) and close the parent pipe fds.
     * Killing the child EOFs both pipes, which ends ksrpc's posix reader/writer
     * threads (each closes its own fd on exit, ksrpc >= 1.1.0 #201), so teardown
     * terminates deterministically rather than wedging on a blocked `read(2)`.
     *
     * The explicit [close] calls here are a belt-and-suspenders prompt-close in case
     * a channel thread hasn't yet observed EOF; they may race that thread's own close
     * of the same fd. Call this exactly once during teardown (it is not safe to call
     * repeatedly: a second call could close an unrelated fd that reused the number).
     */
    public fun close() {
        kill()
        close(stdinWriteFd)
        close(stdoutReadFd)
    }

    private companion object {
        const val GRACE_POLLS = 200
    }
}

/**
 * Spawn an external LSP server child process and open an LSP-compatible JSON-RPC
 * connection over its stdin/stdout. The Kotlin/Native analog of the JVM
 * [ProcessBuilder.asLspConnection][com.monkopedia.lsp.ksrpc.asLspConnection].
 *
 * [command] is the argv to exec (e.g. `listOf("clangd", "--log=error")`); it is
 * resolved against `PATH` via `execvp(3)`. The returned [LspServerProcess] owns the
 * child PID and pipe fds — call [LspServerProcess.connectAsLspClient] (via its
 * [connection][LspServerProcess.connection]) to drive the server, and
 * [LspServerProcess.kill] / [LspServerProcess.close] to tear it down.
 *
 * ```
 * val proc = spawnLspServer(listOf("clangd", "--log=error"))
 * try {
 *     val server = proc.connection.connectAsLspClient(MyClientImpl)
 *     val initResult = server.initialize(InitializeParams(...))
 *     // ... drive the server ...
 * } finally {
 *     proc.close() // bounded kill + fd close; never hangs
 * }
 * ```
 *
 * @throws IllegalStateException if `pipe(2)` or `fork(2)` fails.
 */
public suspend fun spawnLspServer(
    command: List<String>,
    env: KsrpcEnvironment<String> = lspKsrpcEnvironment()
): LspServerProcess {
    require(command.isNotEmpty()) { "command must not be empty" }

    // childStdin: parent writes [1], child reads [0]. childStdout: child writes [1],
    // parent reads [0].
    val (childStdinRead, childStdinWrite) = posixPipe()
    val (childStdoutRead, childStdoutWrite) = posixPipe()

    // Build the NULL-terminated argv and the program-name C string in the PARENT,
    // BEFORE fork. After fork, a Kotlin/Native child must touch only async-signal-safe
    // syscalls (dup2/close/execvp) — any K/N heap allocation (memScoped, cstr, List.map)
    // can deadlock on the allocator lock if another runtime thread held it at fork time.
    // The child inherits a copy of this scope's memory, so execvp sees a valid argv;
    // the parent frees the scope on return (it never reads argv). We exec inside the
    // same memScoped so the placement outlives the child's execvp.
    val program = command.first()
    memScoped {
        val argv = command.map { it.cstr.getPointer(this) } + listOf(null)
        val argvPtr = argv.toCValues().getPointer(this)

        val pid = fork()
        when {
            pid < 0 -> {
                close(childStdinRead)
                close(childStdinWrite)
                close(childStdoutRead)
                close(childStdoutWrite)
                error("fork() failed for ${command.first()}")
            }

            pid == 0 -> {
                // CHILD. Only async-signal-safe syscalls (dup2/close/execvp). The argv
                // array is already built (above, pre-fork); execvp converts the arg0
                // String, but the bulk of the allocation is hoisted out of the child.
                dup2(childStdinRead, STDIN_FILENO)
                dup2(childStdoutWrite, STDOUT_FILENO)
                close(childStdinRead)
                close(childStdinWrite)
                close(childStdoutRead)
                close(childStdoutWrite)
                execvp(program, argvPtr)
                // execvp only returns on failure.
                _exit(127)
                error("unreachable")
            }

            else -> {
                // PARENT. Close the child's ends; keep the parent ends.
                close(childStdinRead)
                close(childStdoutWrite)
                val output = posixFileWriteChannel(childStdinWrite)
                val input = posixFileReadChannel(childStdoutRead)
                val connection = (input to output).asLspConnection(env)
                return LspServerProcess(
                    pid = pid,
                    connection = connection,
                    stdinWriteFd = childStdinWrite,
                    stdoutReadFd = childStdoutRead
                )
            }
        }
    }
}

/** Returns `(readFd, writeFd)` for a fresh pipe. */
private fun posixPipe(): Pair<Int, Int> = memScoped {
    val fds = allocArray<IntVar>(2)
    check(pipe(fds) == 0) { "pipe() failed" }
    fds[0] to fds[1]
}

/** Bounded sleep between waitpid polls (5ms). [LspServerProcess.GRACE_POLLS] * 5ms = 1s total. */
private fun spinWait() {
    usleep(GRACE_POLL_INTERVAL_US)
}

private const val GRACE_POLL_INTERVAL_US: UInt = 5_000u
