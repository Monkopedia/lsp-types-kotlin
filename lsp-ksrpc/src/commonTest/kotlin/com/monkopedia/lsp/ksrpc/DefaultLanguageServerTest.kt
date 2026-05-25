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
import com.monkopedia.lsp.SetTraceParams
import com.monkopedia.lsp.TraceValues
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

/**
 * The `Default*` base classes must let a subclass ignore notifications it doesn't
 * care about: an un-overridden notification is a no-op (fire-and-forget has no
 * response channel, so throwing would crash the receive loop). Requests, which
 * carry a meaningful return, still throw [NotImplementedError] until overridden.
 */
class DefaultLanguageServerTest {

    @Test
    fun `un-overridden notifications are no-ops`() = runTest {
        val server = object : DefaultLanguageServer() {}
        // None of these should throw.
        server.exit()
        server.initialized(InitializedParams())
        server.setTrace(SetTraceParams(value = TraceValues.OFF))
    }

    @Test
    fun `un-overridden requests still throw NotImplementedError`() = runTest {
        val server = object : DefaultLanguageServer() {}
        assertFailsWith<NotImplementedError> { server.shutdown() }
    }
}
