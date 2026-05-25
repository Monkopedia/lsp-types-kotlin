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
package com.monkopedia.lsp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * "Required but nullable" spec fields (e.g. `InitializeParams.processId`,
 * `rootUri`) now default to `null`, so callers can omit them instead of being
 * forced to pass `field = null`.
 */
class NullableDefaultsTest {

    @Test
    fun `InitializeParams omits required-nullable fields`() {
        // Compiles only because processId and rootUri default to null.
        val params = InitializeParams()
        assertNull(params.processId)
        assertNull(params.rootUri)
    }

    @Test
    fun `InitializeParams with only the field you care about`() {
        val params = InitializeParams(processId = 4321)
        assertEquals(4321, params.processId)
        assertNull(params.rootUri)
    }
}
