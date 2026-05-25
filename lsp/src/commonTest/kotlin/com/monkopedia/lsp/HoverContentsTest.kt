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
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

class HoverContentsTest {

    private val json = Json { encodeDefaults = false }

    @Test
    fun `markdown builds a MarkupContent markdown branch`() {
        val contents = HoverContents.markdown("**hello**")
        assertIs<HoverContents.MarkupContentValue>(contents)
        assertEquals(MarkupKind.MARKDOWN, contents.value.kind)
        assertEquals("**hello**", contents.value.value)
    }

    @Test
    fun `plaintext builds a MarkupContent plaintext branch`() {
        val contents = HoverContents.plaintext("hello")
        assertIs<HoverContents.MarkupContentValue>(contents)
        assertEquals(MarkupKind.PLAIN_TEXT, contents.value.kind)
    }

    @Test
    fun `string builds a MarkedString string branch`() {
        val contents = HoverContents.string("a hint")
        assertIs<HoverContents.MarkedStringValue>(contents)
        val marked = contents.value
        assertTrue(marked is StringOr.StringValue)
        assertEquals("a hint", marked.value)
    }

    @Test
    fun `markup wraps a MarkupContent value`() {
        val contents = HoverContents.markup(MarkupContent(MarkupKind.MARKDOWN, "hi"))
        assertIs<HoverContents.MarkupContentValue>(contents)
        assertEquals("hi", contents.value.value)
    }

    @Test
    fun `Hover with markdown contents round-trips as a MarkupContent`() {
        val original = Hover(contents = HoverContents.markdown("**bold**"))
        val decoded = json.decodeFromString(
            Hover.serializer(),
            json.encodeToString(Hover.serializer(), original)
        )
        val contents = decoded.contents
        assertIs<HoverContents.MarkupContentValue>(contents)
        assertEquals("**bold**", contents.value.value)
        assertEquals(MarkupKind.MARKDOWN, contents.value.kind)
    }

    @Test
    fun `Hover with a MarkedString array round-trips`() {
        val original = Hover(
            contents = HoverContents.MarkedStringArray(
                listOf<MarkedString>(StringOr.StringValue("one"), StringOr.StringValue("two"))
            )
        )
        val decoded = json.decodeFromString(
            Hover.serializer(),
            json.encodeToString(Hover.serializer(), original)
        )
        val contents = decoded.contents
        assertIs<HoverContents.MarkedStringArray>(contents)
        assertEquals(2, contents.value.size)
    }
}
