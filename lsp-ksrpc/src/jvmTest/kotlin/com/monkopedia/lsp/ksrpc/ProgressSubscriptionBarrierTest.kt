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

import com.monkopedia.lsp.ProgressParams
import kotlin.test.assertEquals
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test

/**
 * Guards the subscription barrier on [ProgressTokenRegistry.events].
 *
 * A `$/progress` notification dispatched the instant the barrier opens must be
 * observed. [ProgressTokenRegistry] is a hot `SharedFlow` with `replay = 0`, so an
 * event emitted before the subscription is registered is dropped and the observer
 * waits forever.
 *
 * The barrier must be `onSubscription` on [ProgressTokenRegistry.events]. `onStart`
 * on the filtered `observe(token)` flow is NOT a barrier — it runs *before* the
 * operator chain subscribes upstream, so it opens the gate a beat early and the
 * emit can land in the gap. That is precisely the bug this guards: it reddened main
 * on 2026-08-01 (nightly run 30693670833) after passing five consecutive nights.
 *
 * On the correct implementation this is deterministic: `SharedFlowImpl.collect`
 * allocates the subscriber slot before invoking `onSubscription`, so the count is
 * always exactly zero. The rounds exist to catch the *incorrect* one — the old
 * pattern drops on the order of 0.2%-0.8% of rounds, so [ROUNDS] rounds turn a
 * coin-flip nightly failure into a reliable detector.
 */
class ProgressSubscriptionBarrierTest {

    @Test
    fun `a dispatch issued the instant the barrier opens is always observed`() = runBlocking {
        var dropped = 0
        repeat(ROUNDS) {
            val registry = ProgressTokenRegistry()
            val token = registry.allocateToken()
            val subscribed = CompletableDeferred<Unit>()
            val received = CompletableDeferred<ProgressParams>()

            val collector = launch(Dispatchers.Default) {
                registry.events
                    .onSubscription { subscribed.complete(Unit) }
                    .filter { it.token == token }
                    .collect { received.complete(it) }
            }

            subscribed.await()
            registry.dispatch(ProgressParams(token = token, value = JsonPrimitive("x")))

            if (withTimeoutOrNull(DROP_TIMEOUT_MS) { received.await() } == null) dropped++
            collector.cancel()
        }
        assertEquals(
            0,
            dropped,
            "$dropped/$ROUNDS progress notifications were dropped after the subscription " +
                "barrier opened. The barrier is not holding — check that the collector " +
                "uses onSubscription on ProgressTokenRegistry.events rather than onStart " +
                "on the filtered observe(token) flow."
        )
    }

    private companion object {
        /**
         * High enough that the broken pattern fails essentially every run: at the
         * measured 0.2%-0.8% per-round drop rate, the chance of 2000 clean rounds is
         * about 2% at the low end and vanishing at the high end. Costs ~2s when the
         * barrier holds, because no round ever waits out [DROP_TIMEOUT_MS].
         */
        const val ROUNDS = 2000

        /**
         * Only ever reached on a dropped event, so it trades directly against how long
         * a *failing* run takes. The real notification arrives in microseconds.
         */
        const val DROP_TIMEOUT_MS = 250L
    }
}
