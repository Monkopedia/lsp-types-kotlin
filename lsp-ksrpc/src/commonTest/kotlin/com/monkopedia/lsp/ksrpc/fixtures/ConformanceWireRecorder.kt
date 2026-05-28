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
 * Coverage observation hook used by the conformance fixtures (#66, #74).
 *
 * The [ConformanceLanguageServer] and [ConformanceLanguageClient] fixtures call
 * [serverCallback] and [clientCallback] respectively from every method override
 * they implement, passing the LSP wire method name (e.g.
 * `"textDocument/hover"`). The callbacks default to no-ops so the fixtures'
 * public contract is unchanged for callers that do not opt in.
 *
 * For per-union-branch coverage (#74) the same fixtures pipe every typed param
 * the fixture receives and every typed result the fixture returns through
 * [valueCallback] so a JVM-side walker can record which sealed-interface
 * subclasses were actually exercised over the wire. The default callback is a
 * no-op so non-JVM targets pay nothing.
 *
 * The JVM wire-coverage trackers (`WireMethodRecorder`, `WireBranchRecorder`)
 * assign real callbacks during their `install()` calls to push observed methods
 * and values into the shared recorders. Other targets — common, native, JS —
 * see the defaults and pay no cost.
 *
 * The hooks are intentionally `var`s of plain function types rather than typed
 * recorders so the fixture file stays platform-agnostic.
 */
object ConformanceWireRecorder {

    private val NOOP_NAME: (String) -> Unit = { _ -> }
    private val NOOP_VALUE: (Any?) -> Unit = { _ -> }

    @kotlin.concurrent.Volatile
    var serverCallback: ((String) -> Unit) = NOOP_NAME

    @kotlin.concurrent.Volatile
    var clientCallback: ((String) -> Unit) = NOOP_NAME

    @kotlin.concurrent.Volatile
    var valueCallback: ((Any?) -> Unit) = NOOP_VALUE

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

    /**
     * Fires from a [ConformanceLanguageServer] / [ConformanceLanguageClient]
     * override with each typed value the fixture observed over the wire — both
     * params received from the peer and results the fixture returns. The
     * JVM-side branch recorder walks the value, notes every sealed-interface
     * subclass it encounters and records it for the end-of-suite report. Tests
     * should not call this directly.
     */
    fun observeValue(value: Any?) {
        valueCallback(value)
    }

    /** Reset all callbacks to no-ops. JVM tracker calls this on teardown. */
    fun reset() {
        serverCallback = NOOP_NAME
        clientCallback = NOOP_NAME
        valueCallback = NOOP_VALUE
    }
}
