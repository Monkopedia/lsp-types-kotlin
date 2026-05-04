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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
}
