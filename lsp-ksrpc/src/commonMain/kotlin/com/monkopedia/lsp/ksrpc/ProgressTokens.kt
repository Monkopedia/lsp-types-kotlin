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

import com.monkopedia.lsp.IntOrString
import com.monkopedia.lsp.ProgressParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter

/**
 * Registry for LSP progress reporting via `$/progress` notifications.
 *
 * The LSP spec defines two flavors of progress:
 *
 * - **Work-done progress** — clients pass a `workDoneToken` in request params; the server
 *   emits `$/progress` notifications with the same token. The client uses [observe]
 *   to receive updates.
 *
 * - **Server-initiated progress** — the server sends `window/workDoneProgress/create`
 *   first to register a token, then emits `$/progress` notifications for it.
 *
 * This registry is bidirectional: either side can register a token and observe progress
 * for it. The receiver of `$/progress` notifications calls [dispatch] to route them to
 * registered observers.
 *
 * Typical client-side usage:
 *
 * ```
 * val registry = ProgressTokenRegistry()
 * val token = registry.allocateToken()
 *
 * // Subscribe before issuing the request that will emit progress. The registry is
 * // hot, so wait until the subscription is actually registered — `onSubscription`
 * // on [events], not `onStart` on [observe], is what guarantees that.
 * val subscribed = CompletableDeferred<Unit>()
 * launch {
 *     registry.events
 *         .onSubscription { subscribed.complete(Unit) }
 *         .filter { it.token == token }
 *         .collect { p ->
 *             // p.value is JsonElement — decode as WorkDoneProgressBegin/Report/End
 *             println("progress: $p")
 *         }
 * }
 * subscribed.await()
 *
 * server.textDocumentReferences(
 *     ReferenceParams(workDoneToken = token, ...)
 * )
 *
 * // In the LanguageClient impl that handles $/progress notifications:
 * override suspend fun progress(params: ProgressParams) {
 *     registry.dispatch(params)
 * }
 * ```
 */
class ProgressTokenRegistry {

    private val incoming = MutableSharedFlow<ProgressParams>(extraBufferCapacity = 64)
    private val tokenCounter = AtomicTokenCounter()

    /** Allocate a fresh integer progress token, unique per registry instance. */
    fun allocateToken(): IntOrString = IntOrString.IntValue(tokenCounter.next().toInt())

    /** Allocate a fresh string progress token using the given prefix. */
    fun allocateToken(prefix: String): IntOrString =
        IntOrString.StringValue("$prefix-${tokenCounter.next()}")

    /**
     * Every `$/progress` notification passed to [dispatch], unfiltered.
     *
     * This is the same hot stream [observe] filters, exposed so a subscriber can
     * apply [kotlinx.coroutines.flow.onSubscription] — the only way to know the
     * subscription is actually registered. [observe] returns a *filtered* flow, and
     * `onStart` on it runs **before** the upstream subscription exists, so gating a
     * request on `onStart` still races the response:
     *
     * ```
     * val subscribed = CompletableDeferred<Unit>()
     * launch {
     *     registry.events
     *         .onSubscription { subscribed.complete(Unit) }
     *         .filter { it.token == token }
     *         .collect { ... }
     * }
     * subscribed.await()   // now the stream is live — safe to trigger the work
     * server.textDocumentReferences(ReferenceParams(workDoneToken = token, ...))
     * ```
     */
    val events: SharedFlow<ProgressParams> = incoming.asSharedFlow()

    /**
     * Observe `$/progress` notifications for [token]. The flow emits each matching
     * `ProgressParams` until the consumer cancels. The registry is hot — events
     * emitted before subscription are not replayed, so if you must not miss the
     * first event, subscribe via [events] and wait for `onSubscription` before
     * triggering the work that reports progress.
     */
    fun observe(token: IntOrString): Flow<ProgressParams> = incoming.filter { it.token == token }

    /** Dispatch an incoming `$/progress` notification to all registered observers. */
    suspend fun dispatch(params: ProgressParams) {
        incoming.emit(params)
    }
}

/** Monotonic counter for allocating progress tokens. */
@OptIn(kotlin.concurrent.atomics.ExperimentalAtomicApi::class)
internal class AtomicTokenCounter {
    private val counter = kotlin.concurrent.atomics.AtomicLong(0L)

    fun next(): Long = counter.fetchAndAdd(1L) + 1L
}
