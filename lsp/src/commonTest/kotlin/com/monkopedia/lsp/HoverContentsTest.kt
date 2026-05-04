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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class HoverContentsTest {

    private val json = Json { encodeDefaults = false }

    @Test
    fun `markdown produces MarkupContent with markdown kind`() {
        val element = HoverContents.markdown("**hello**")
        val obj = (element as JsonObject).jsonObject
        assertEquals("markdown", obj["kind"]?.jsonPrimitive?.content)
        assertEquals("**hello**", obj["value"]?.jsonPrimitive?.content)
    }

    @Test
    fun `plaintext produces MarkupContent with plaintext kind`() {
        val element = HoverContents.plaintext("hello")
        val obj = (element as JsonObject).jsonObject
        assertEquals("plaintext", obj["kind"]?.jsonPrimitive?.content)
        assertEquals("hello", obj["value"]?.jsonPrimitive?.content)
    }

    @Test
    fun `string produces a JSON string primitive - the deprecated MarkedString shape`() {
        val element = HoverContents.string("a hint")
        val prim = element as JsonPrimitive
        assertEquals(true, prim.isString)
        assertEquals("a hint", prim.content)
    }

    @Test
    fun `markup wraps a MarkupContent value`() {
        val element = HoverContents.markup(MarkupContent(MarkupKind.MARKDOWN, "hi"))
        val obj = (element as JsonObject).jsonObject
        assertEquals("markdown", obj["kind"]?.jsonPrimitive?.content)
        assertEquals("hi", obj["value"]?.jsonPrimitive?.content)
    }

    @Test
    fun `Hover with markdown contents round-trips through JSON`() {
        val original = Hover(contents = HoverContents.markdown("**bold**"))
        val encoded = json.encodeToString(Hover.serializer(), original)
        val decoded = json.decodeFromString(Hover.serializer(), encoded)
        // Hover.contents is JsonElement, so equality check works on JsonElement ==.
        assertEquals(original.contents, decoded.contents)
    }
}
