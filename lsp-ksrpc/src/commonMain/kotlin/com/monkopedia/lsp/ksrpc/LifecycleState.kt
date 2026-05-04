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

/**
 * Tracks the LSP connection lifecycle as defined by the spec:
 *
 * 1. **Initializing** — server is up but client hasn't sent `initialize` yet.
 *    Only `initialize` and `exit` may be processed.
 * 2. **Initialized** — `initialize` request returned and `initialized` notification was sent.
 *    All methods are allowed.
 * 3. **ShuttingDown** — `shutdown` request received. Only `exit` may follow.
 * 4. **Exited** — `exit` notification received. Connection should be closed.
 *
 * Use [allowsMethod] before dispatching incoming requests/notifications, and
 * [transitionTo] to advance the state when lifecycle events occur.
 */
class LifecycleState {

    @kotlin.concurrent.Volatile
    var phase: Phase = Phase.INITIALIZING
        private set

    /**
     * Returns `true` if the given method is allowed in the current state.
     *
     * - Always allows `initialize`, `initialized`, `shutdown`, `exit` (the lifecycle methods).
     * - Always allows `$/`-prefixed methods (transport-level, e.g. `$/cancelRequest`, `$/progress`).
     * - In [Phase.INITIALIZING] — only lifecycle and `$/`-prefixed methods.
     * - In [Phase.INITIALIZED] — everything.
     * - In [Phase.SHUTTING_DOWN] — only `exit` and `$/`-prefixed methods.
     * - In [Phase.EXITED] — nothing.
     */
    fun allowsMethod(method: String): Boolean = when (phase) {
        Phase.INITIALIZING -> method in INIT_METHODS || method.startsWith("\$/")
        Phase.INITIALIZED -> true
        Phase.SHUTTING_DOWN -> method == "exit" || method.startsWith("\$/")
        Phase.EXITED -> false
    }

    /**
     * Transition the state. Throws if the transition is not legal.
     */
    fun transitionTo(next: Phase) {
        val current = phase
        if (!isLegalTransition(current, next)) {
            error("Illegal LSP lifecycle transition: $current → $next")
        }
        phase = next
    }

    enum class Phase {
        INITIALIZING,
        INITIALIZED,
        SHUTTING_DOWN,
        EXITED
    }

    companion object {
        private val INIT_METHODS = setOf("initialize", "initialized", "exit")

        private fun isLegalTransition(from: Phase, to: Phase): Boolean = when (from) {
            Phase.INITIALIZING -> to == Phase.INITIALIZED || to == Phase.EXITED
            Phase.INITIALIZED -> to == Phase.SHUTTING_DOWN || to == Phase.EXITED
            Phase.SHUTTING_DOWN -> to == Phase.EXITED
            Phase.EXITED -> false
        }
    }
}
