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
package com.monkopedia.lsp.ksrpc.fixtures

/**
 * Coverage observation hook used by the conformance fixtures (#66).
 *
 * The [ConformanceLanguageServer] and [ConformanceLanguageClient] fixtures call
 * [serverCallback] and [clientCallback] respectively from every method override
 * they implement, passing the LSP wire method name (e.g.
 * `"textDocument/hover"`). The callbacks default to no-ops so the fixtures'
 * public contract is unchanged for callers that do not opt in.
 *
 * The JVM wire-coverage tracker (`WireMethodRecorder`) assigns real callbacks
 * during its `@BeforeClass` / `@AfterClass` lifecycle to push observed methods
 * into the shared recorder. Other targets — common, native, JS — see the
 * defaults and pay no cost.
 *
 * The hook is intentionally a `var` of `(String) -> Unit` rather than a typed
 * recorder so the fixture file stays platform-agnostic.
 */
object ConformanceWireRecorder {

    private val NOOP: (String) -> Unit = { _ -> }

    @kotlin.concurrent.Volatile
    var serverCallback: ((String) -> Unit) = NOOP

    @kotlin.concurrent.Volatile
    var clientCallback: ((String) -> Unit) = NOOP

    /**
     * Fires from a [ConformanceLanguageServer] override after it has accepted a
     * server-side call. Tests should not call this directly.
     */
    fun observeServer(methodName: String) {
        serverCallback(methodName)
    }

    /**
     * Fires from a [ConformanceLanguageClient] override after it has accepted a
     * server-initiated client call. Tests should not call this directly.
     */
    fun observeClient(methodName: String) {
        clientCallback(methodName)
    }

    /** Reset both callbacks to no-ops. JVM tracker calls this on teardown. */
    fun reset() {
        serverCallback = NOOP
        clientCallback = NOOP
    }
}
