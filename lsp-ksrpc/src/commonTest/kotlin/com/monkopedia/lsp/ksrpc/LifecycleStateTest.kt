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

import com.monkopedia.lsp.DefaultLanguageServer
import com.monkopedia.lsp.InitializedParams
import com.monkopedia.lsp.LifecycleTrackingLanguageServer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

class LifecycleStateTest {

    @Test
    fun `initializing allows initialize and exit only`() {
        val state = LifecycleState()
        assertTrue(state.allowsMethod("initialize"))
        assertTrue(state.allowsMethod("initialized"))
        assertTrue(state.allowsMethod("exit"))
        assertFalse(state.allowsMethod("textDocument/hover"))
        assertFalse(state.allowsMethod("shutdown"))
    }

    @Test
    fun `initializing allows dollar-prefixed methods`() {
        val state = LifecycleState()
        assertTrue(state.allowsMethod("\$/cancelRequest"))
        assertTrue(state.allowsMethod("\$/progress"))
    }

    @Test
    fun `initialized allows everything`() {
        val state = LifecycleState()
        state.transitionTo(LifecycleState.Phase.INITIALIZED)
        assertTrue(state.allowsMethod("textDocument/hover"))
        assertTrue(state.allowsMethod("shutdown"))
        assertTrue(state.allowsMethod("exit"))
    }

    @Test
    fun `shutting down allows exit only`() {
        val state = LifecycleState()
        state.transitionTo(LifecycleState.Phase.INITIALIZED)
        state.transitionTo(LifecycleState.Phase.SHUTTING_DOWN)
        assertFalse(state.allowsMethod("textDocument/hover"))
        assertFalse(state.allowsMethod("initialize"))
        assertTrue(state.allowsMethod("exit"))
        assertTrue(state.allowsMethod("\$/cancelRequest"))
    }

    @Test
    fun `exited allows nothing`() {
        val state = LifecycleState()
        state.transitionTo(LifecycleState.Phase.INITIALIZED)
        state.transitionTo(LifecycleState.Phase.EXITED)
        assertFalse(state.allowsMethod("textDocument/hover"))
        assertFalse(state.allowsMethod("exit"))
        assertFalse(state.allowsMethod("\$/cancelRequest"))
    }

    @Test
    fun `illegal transition throws`() {
        val state = LifecycleState()
        // Cannot go directly to SHUTTING_DOWN from INITIALIZING
        assertFailsWith<IllegalStateException> {
            state.transitionTo(LifecycleState.Phase.SHUTTING_DOWN)
        }
    }

    @Test
    fun `cannot transition out of EXITED`() {
        val state = LifecycleState()
        state.transitionTo(LifecycleState.Phase.EXITED)
        assertEquals(LifecycleState.Phase.EXITED, state.phase)
        assertFailsWith<IllegalStateException> {
            state.transitionTo(LifecycleState.Phase.INITIALIZED)
        }
    }

    @Test
    fun `advanceTo ignores illegal transitions without throwing`() {
        val state = LifecycleState()
        assertFalse(state.advanceTo(LifecycleState.Phase.SHUTTING_DOWN))
        assertEquals(LifecycleState.Phase.INITIALIZING, state.phase)
        assertTrue(state.advanceTo(LifecycleState.Phase.INITIALIZED))
        assertEquals(LifecycleState.Phase.INITIALIZED, state.phase)
        // Re-applying the same phase is not a legal forward transition: a no-op.
        assertFalse(state.advanceTo(LifecycleState.Phase.INITIALIZED))
    }

    @Test
    fun `awaitInitialized suspends until INITIALIZED`() = runTest {
        val state = LifecycleState()
        var resumed = false
        val waiter = launch {
            state.awaitInitialized()
            resumed = true
        }
        assertFalse(resumed)
        state.transitionTo(LifecycleState.Phase.INITIALIZED)
        waiter.join()
        assertTrue(resumed)
    }

    @Test
    fun `tracking wrapper advances lifecycle on lifecycle calls`() = runTest {
        val lifecycle = LifecycleState()
        val delegate = object : DefaultLanguageServer() {
            override suspend fun shutdown(): Nothing? = null
        }
        val server = LifecycleTrackingLanguageServer(delegate, lifecycle)

        assertEquals(LifecycleState.Phase.INITIALIZING, lifecycle.phase)
        server.initialized(InitializedParams())
        assertEquals(LifecycleState.Phase.INITIALIZED, lifecycle.phase)
        server.shutdown()
        assertEquals(LifecycleState.Phase.SHUTTING_DOWN, lifecycle.phase)
        server.exit()
        assertEquals(LifecycleState.Phase.EXITED, lifecycle.phase)
    }
}
