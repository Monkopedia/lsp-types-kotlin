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
 * // Subscribe before issuing the request that will emit progress.
 * launch {
 *     registry.observe(token).collect { p ->
 *         // p.value is JsonElement — decode as WorkDoneProgressBegin/Report/End
 *         println("progress: $p")
 *     }
 * }
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
     * Observe `$/progress` notifications for [token]. The flow emits each matching
     * `ProgressParams` until the consumer cancels. The registry is hot — events
     * emitted before subscription are not replayed.
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
