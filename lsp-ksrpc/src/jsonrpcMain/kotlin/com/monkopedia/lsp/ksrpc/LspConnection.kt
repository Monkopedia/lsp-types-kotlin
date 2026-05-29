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
import com.monkopedia.lsp.LifecycleTrackingLanguageServer
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * Permissive Json configuration that tolerates unknown fields. Real LSP servers
 * routinely return capability objects with vendor extensions (e.g. clangd's
 * `astProvider`) that aren't in the metaModel — strict parsing rejects them.
 */
public val LSP_JSON: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = false
}

/**
 * Default [KsrpcEnvironment] for LSP. Uses [LSP_JSON] (permissive parsing).
 * Override by passing your own to [asLspConnection] if you need a different
 * configuration (custom logger, error listener, scope).
 */
public fun lspKsrpcEnvironment(): KsrpcEnvironment<String> = ksrpcEnvironment(LSP_JSON) { }

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
    env: KsrpcEnvironment<String> = lspKsrpcEnvironment()
): SingleChannelConnection<String> {
    // The JSON-RPC connection launches a long-lived read pump (it reads framed
    // messages off the channel for the connection's whole life). ksrpc's
    // `asJsonRpcConnection` hosts that pump in `CoroutineScope(coroutineContext)`,
    // i.e. parented to whatever job is active when this `suspend fun` runs. If the
    // caller drives this from a `runBlocking { ... }`, the pump becomes a CHILD of
    // that `runBlocking` job — and under structured concurrency `runBlocking` will
    // not return until all its children complete. A pump parked on a dead/half-open
    // stream that never reaches clean EOF then wedges the caller's `runBlocking`
    // forever (issue #87, the production-facing form of the #79 test hang).
    //
    // Break that parent/child edge: host the pump in a fresh, connection-owned
    // `SupervisorJob` that is NOT a child of the caller's job. We keep the caller's
    // *dispatcher* (so the pump runs on the same threading model — important for the
    // single-threaded JS/wasm event loop, which has no `Dispatchers.IO`), but the
    // new Job means the pump's lifecycle is the connection's, not the caller's. The
    // caller's `runBlocking` returns as soon as the caller's own work finishes; a
    // wedged pump can no longer hold it open.
    //
    // ksrpc launches the pump as a CHILD of the coroutine that runs
    // `asJsonRpcConnection` (it does `CoroutineScope(coroutineContext).launch { }`).
    // So we must run `asJsonRpcConnection` from a coroutine we `launch`-and-forget on
    // the detached `pumpScope` and hand the built connection back via a
    // `CompletableDeferred`. We never `join` that launched coroutine — it stays alive
    // holding the pump — so the pump is owned by `pumpScope` alone, never by the
    // caller's structured-concurrency tree. The caller only awaits the deferred, which
    // is completed the instant the connection is built (the pump is launched but not
    // awaited), so this returns promptly and can never wedge.
    val callerDispatcher = coroutineContext[ContinuationInterceptor] ?: EmptyCoroutineContext
    val pumpScope = CoroutineScope(SupervisorJob() + callerDispatcher)
    val built = CompletableDeferred<SingleChannelConnection<String>>()
    pumpScope.launch {
        try {
            built.complete(
                asJsonRpcConnection(
                    env = env,
                    includeContentHeaders = true,
                    cancellationConvention = JsonRpcCancellationConvention.Lsp
                )
            )
        } catch (t: Throwable) {
            built.completeExceptionally(t)
            throw t
        }
    }
    return built.await()
}

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

/**
 * Like [connectAsLspServer], but advances [lifecycle] automatically as the client
 * drives the connection: to [LifecycleState.Phase.INITIALIZED] on the `initialized`
 * notification, [LifecycleState.Phase.SHUTTING_DOWN] on `shutdown`, and
 * [LifecycleState.Phase.EXITED] on `exit`. The server impl no longer has to drive
 * the phase machine by hand; observe it via [LifecycleState.phases] /
 * [LifecycleState.awaitInitialized].
 */
suspend fun SingleChannelConnection<String>.connectAsLspServer(
    server: KsrpcLanguageServer,
    lifecycle: LifecycleState
): KsrpcLanguageClient = connectAsLspServer(LifecycleTrackingLanguageServer(server, lifecycle))
