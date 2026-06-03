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

import com.monkopedia.lsp.KsrpcLanguageServer
import com.monkopedia.lsp.LifecycleTrackingLanguageServer

/**
 * Wrap this [KsrpcLanguageServer] so [lifecycle] advances automatically as the
 * client drives the connection: to [LifecycleState.Phase.INITIALIZED] on the
 * `initialized` notification, [LifecycleState.Phase.SHUTTING_DOWN] on `shutdown`,
 * and [LifecycleState.Phase.EXITED] on `exit`. Every other method delegates to the
 * receiver unchanged.
 *
 * `connectAsLspServer(server, lifecycle)` applies the same tracking when you own a
 * dedicated LSP connection. Use this factory directly when you instead host the
 * server as a ksrpc **sub-service** — e.g. returning it from a `@KsMethod` over an
 * existing ksrpc connection (a nested-service model) — so you still get the phase
 * machine without the dedicated-connection helper. Observe phases via
 * [LifecycleState.phases]; gate server→client emissions on
 * [LifecycleState.awaitInitialized] to respect LSP ordering.
 *
 * ```
 * val lifecycle = LifecycleState()
 *
 * @KsMethod("/lsp")
 * suspend fun lsp(client: KsrpcLanguageClient): KsrpcLanguageServer =
 *     myServerImpl.tracked(lifecycle)
 * ```
 */
public fun KsrpcLanguageServer.tracked(lifecycle: LifecycleState): KsrpcLanguageServer =
    LifecycleTrackingLanguageServer(this, lifecycle)
