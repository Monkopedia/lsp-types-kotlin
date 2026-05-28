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
package com.monkopedia.lsp.ksrpc.coverage

import com.monkopedia.lsp.LanguageClient
import com.monkopedia.lsp.LanguageServer
import com.monkopedia.lsp.ksrpc.fixtures.ConformanceWireRecorder
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap

/**
 * Process-wide JVM recorder for LSP wire-method coverage (#66).
 *
 * The conformance fixtures (`ConformanceLanguageServer` /
 * `ConformanceLanguageClient`) call into [ConformanceWireRecorder] from every
 * override they implement. [install] wires those hooks to push the observed
 * method names into [serverCalled] / [clientCalled]. The end-of-suite
 * `WireCoverageReportTest` reads the totals back, writes a Markdown report and
 * fails on regression against `wire-coverage-baseline.txt`.
 *
 * The two sets are `ConcurrentHashMap.newKeySet()` instances so parallel
 * coroutines / transports can record concurrently without lock contention.
 */
object WireMethodRecorder {

    private val _serverCalled: MutableSet<String> = ConcurrentHashMap.newKeySet()
    private val _clientCalled: MutableSet<String> = ConcurrentHashMap.newKeySet()

    /** Read-only snapshot of every server-method name observed so far. */
    val serverCalled: Set<String> get() = _serverCalled.toSet()

    /** Read-only snapshot of every client-method name observed so far. */
    val clientCalled: Set<String> get() = _clientCalled.toSet()

    /**
     * Install the JVM recorder callbacks into [ConformanceWireRecorder].
     * Idempotent — calling twice is fine. The integration tests do not need to
     * call this explicitly; [WireCoverageReportTest] installs it for every JVM
     * test run via a JUnit `@BeforeClass` hook.
     */
    fun install() {
        ConformanceWireRecorder.serverCallback = { name -> _serverCalled.add(name) }
        ConformanceWireRecorder.clientCallback = { name -> _clientCalled.add(name) }
    }

    /** Clear the recorded sets. Test-only. */
    fun reset() {
        _serverCalled.clear()
        _clientCalled.clear()
    }

    /**
     * Reflectively enumerate every `const val ...: String` declared on
     * [LanguageServer.Companion] — the canonical LSP wire-method surface for
     * the server side.
     */
    fun introspectServerSurface(): Set<String> =
        methodNamesFromCompanion(LanguageServer::class.java)

    /**
     * Reflectively enumerate every `const val ...: String` declared on
     * [LanguageClient.Companion] — the canonical LSP wire-method surface for
     * the client side.
     */
    fun introspectClientSurface(): Set<String> =
        methodNamesFromCompanion(LanguageClient::class.java)

    /**
     * Read every `public static final String` field from the host class — Kotlin
     * `companion object`'s `const val` declarations compile to static finals on
     * the host class (with `@JvmField`-like semantics for primitive/String
     * constants). The introspection therefore lives on the outer interface, not
     * on the nested `$Companion` class.
     */
    private fun methodNamesFromCompanion(hostClass: Class<*>): Set<String> =
        hostClass.declaredFields
            .asSequence()
            .filter { f ->
                Modifier.isStatic(f.modifiers) &&
                    Modifier.isFinal(f.modifiers) &&
                    Modifier.isPublic(f.modifiers) &&
                    f.type == String::class.java
            }
            .mapNotNull { f ->
                runCatching {
                    f.isAccessible = true
                    f.get(null) as? String
                }.getOrNull()
            }
            .toSet()
}
