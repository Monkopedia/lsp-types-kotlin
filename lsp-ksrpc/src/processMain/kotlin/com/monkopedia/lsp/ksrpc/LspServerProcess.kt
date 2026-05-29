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

/**
 * A spawned external LSP server child process and the JSON-RPC connection to it.
 *
 * This is the single, cross-platform handle returned by [spawnLspServer]. The JVM and
 * Kotlin/Native (posix-fd) implementations expose the identical surface — the contract
 * is this shared interface, so the compiler enforces that neither platform drifts. On
 * the JVM it wraps a [java.lang.Process]; on native it owns the forked child PID and the
 * parent ends of its stdin/stdout pipes.
 *
 * Obtain the language-server stub with
 * [connection]`.`[connectAsLspClient][com.monkopedia.lsp.ksrpc.connectAsLspClient].
 *
 * ## Teardown
 *
 * LSP servers are expected to be torn down by the client. Prefer a clean
 * `shutdown`/`exit` round-trip, but that depends on the server cooperating; for a
 * guaranteed-bounded teardown call [kill] (SIGTERM, then SIGKILL after a grace window on
 * posix; `destroy()` then a bounded `destroyForcibly()` on the JVM) and then [close].
 * Both are bounded and must never hang, so they are safe to call from a `finally`.
 *
 * Not available on Windows (mingw), JS, or wasm: those targets have no process-spawn
 * model and do not see [spawnLspServer] or this interface at all.
 */
public interface LspServerProcess {
    /** PID of the spawned server child. */
    public val pid: Long

    /**
     * JSON-RPC connection to the child over its stdio. Wire it with
     * [connectAsLspClient][com.monkopedia.lsp.ksrpc.connectAsLspClient].
     */
    public val connection: SingleChannelConnection<String>

    /**
     * Signal the child to terminate. Bounded and non-suspending: on posix sends
     * SIGTERM, then SIGKILL after a short grace window; on the JVM calls `destroy()`
     * then a bounded `destroyForcibly()`. Safe to call from a `finally` during
     * teardown. Idempotent.
     */
    public fun kill()

    /**
     * Tear down: [kill] the child (if still running) and release the connection /
     * process resources. Bounded — it must never block indefinitely. Call this exactly
     * once during teardown.
     */
    public fun close()
}

/**
 * Spawn an external LSP server child process and open an LSP-compatible JSON-RPC
 * connection over its stdin/stdout. Unified across the JVM and Kotlin/Native posix-fd
 * targets via `expect`/`actual`, so both expose the identical [LspServerProcess] handle.
 *
 * [command] is the argv to launch (e.g. `listOf("clangd", "--log=error")`); the program
 * is resolved against `PATH`. The returned [LspServerProcess] owns the child — call
 * [LspServerProcess.connection]`.`[connectAsLspClient][com.monkopedia.lsp.ksrpc.connectAsLspClient]
 * to drive the server, and [LspServerProcess.kill] / [LspServerProcess.close] to tear it
 * down (both bounded; never hang).
 *
 * ```
 * val proc = spawnLspServer(listOf("clangd", "--log=error"))
 * try {
 *     val server = proc.connection.connectAsLspClient(MyClientImpl)
 *     val initResult = server.initialize(InitializeParams(...))
 *     // ... drive the server ...
 * } finally {
 *     proc.close() // bounded kill + release; never hangs
 * }
 * ```
 */
public expect suspend fun spawnLspServer(
    command: List<String>,
    env: KsrpcEnvironment<String> = lspKsrpcEnvironment()
): LspServerProcess
