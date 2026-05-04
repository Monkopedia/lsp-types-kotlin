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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlinx.serialization.json.JsonPrimitive

class ProgressTokensTest {

    @Test
    fun `allocateToken returns unique values`() {
        val registry = ProgressTokenRegistry()
        val tokens = (1..5).map { registry.allocateToken() }
        assertEquals(5, tokens.toSet().size)
    }

    @Test
    fun `allocateToken with prefix returns string tokens`() {
        val registry = ProgressTokenRegistry()
        val token = registry.allocateToken("rename")
        assertTrue(token is IntOrString.StringValue)
        assertTrue(token.value.startsWith("rename-"))
    }

    @Test
    fun `int and string tokens are distinct`() {
        val registry = ProgressTokenRegistry()
        val intToken = registry.allocateToken()
        val strToken = registry.allocateToken("foo")
        assertNotEquals(intToken, strToken)
    }

    @Test
    fun `dispatch routes events to matching token observers`() = runTest {
        val registry = ProgressTokenRegistry()
        val token = registry.allocateToken()
        val otherToken = IntOrString.IntValue(99999)

        val received = mutableListOf<ProgressParams>()
        val job = launch {
            registry.observe(token).collect { received += it }
        }
        yield()

        registry.dispatch(ProgressParams(token = token, value = JsonPrimitive("first")))
        registry.dispatch(ProgressParams(token = otherToken, value = JsonPrimitive("ignore")))
        registry.dispatch(ProgressParams(token = token, value = JsonPrimitive("second")))
        yield()

        job.cancel()
        assertEquals(2, received.size)
    }
}
