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
import kotlin.test.assertTrue
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class UnionTypesTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    data class HoverOptions(val workDoneProgress: Boolean? = null)

    // ---- BooleanOr<T> ----

    @Test
    fun `BooleanOr deserializes boolean true`() {
        val result = json.decodeFromString(
            BooleanOrSerializer(HoverOptions.serializer()),
            "true"
        )
        assertTrue(result is BooleanOr.BooleanValue)
        assertEquals(true, result.value)
    }

    @Test
    fun `BooleanOr deserializes boolean false`() {
        val result = json.decodeFromString(
            BooleanOrSerializer(HoverOptions.serializer()),
            "false"
        )
        assertTrue(result is BooleanOr.BooleanValue)
        assertEquals(false, result.value)
    }

    @Test
    fun `BooleanOr deserializes object`() {
        val result = json.decodeFromString(
            BooleanOrSerializer(HoverOptions.serializer()),
            """{"workDoneProgress": true}"""
        )
        assertTrue(result is BooleanOr.Value)
        assertEquals(true, result.value.workDoneProgress)
    }

    @Test
    fun `BooleanOr round-trips boolean`() {
        val serializer = BooleanOrSerializer(HoverOptions.serializer())
        val original: BooleanOr<HoverOptions> = BooleanOr.BooleanValue(true)
        val encoded = json.encodeToString(serializer, original)
        assertEquals("true", encoded)
    }

    @Test
    fun `BooleanOr round-trips object`() {
        val serializer = BooleanOrSerializer(HoverOptions.serializer())
        val original: BooleanOr<HoverOptions> = BooleanOr.Value(
            HoverOptions(workDoneProgress = true)
        )
        val encoded = json.encodeToString(serializer, original)
        val decoded = json.decodeFromString(serializer, encoded)
        assertTrue(decoded is BooleanOr.Value)
        assertEquals(true, decoded.value.workDoneProgress)
    }

    // ---- SingleOrArray<T> ----

    @Test
    fun `SingleOrArray deserializes single object`() {
        val result = json.decodeFromString(
            SingleOrArraySerializer(Position.serializer()),
            """{"line": 1, "character": 2}"""
        )
        assertTrue(result is SingleOrArray.Single)
        assertEquals(1u, result.value.line)
    }

    @Test
    fun `SingleOrArray deserializes array`() {
        val result = json.decodeFromString(
            SingleOrArraySerializer(Position.serializer()),
            """[{"line": 1, "character": 2}, {"line": 3, "character": 4}]"""
        )
        assertTrue(result is SingleOrArray.Multiple)
        assertEquals(2, result.value.size)
        assertEquals(3u, result.value[1].line)
    }

    @Test
    fun `SingleOrArray round-trips`() {
        val serializer = SingleOrArraySerializer(Position.serializer())
        val original: SingleOrArray<Position> =
            SingleOrArray.Multiple(listOf(Position(1u, 2u), Position(3u, 4u)))
        val encoded = json.encodeToString(serializer, original)
        val decoded = json.decodeFromString(serializer, encoded)
        assertTrue(decoded is SingleOrArray.Multiple)
        assertEquals(2, decoded.value.size)
    }

    // ---- StringOr<T> ----

    @Test
    fun `StringOr deserializes string`() {
        val result = json.decodeFromString(
            StringOrSerializer(MarkupContent.serializer()),
            "\"plain text\""
        )
        assertTrue(result is StringOr.StringValue)
        assertEquals("plain text", result.value)
    }

    @Test
    fun `StringOr deserializes object`() {
        val result = json.decodeFromString(
            StringOrSerializer(MarkupContent.serializer()),
            """{"kind": "markdown", "value": "# Hi"}"""
        )
        assertTrue(result is StringOr.Value)
        assertEquals("# Hi", result.value.value)
    }

    // ---- IntOrString ----

    @Test
    fun `IntOrString deserializes int`() {
        val result = json.decodeFromString(IntOrStringSerializer, "42")
        assertTrue(result is IntOrString.IntValue)
        assertEquals(42, result.value)
    }

    @Test
    fun `IntOrString deserializes string`() {
        val result = json.decodeFromString(IntOrStringSerializer, "\"token-1\"")
        assertTrue(result is IntOrString.StringValue)
        assertEquals("token-1", result.value)
    }

    @Test
    fun `IntOrString round-trips`() {
        val intVal: IntOrString = IntOrString.IntValue(99)
        assertEquals("99", json.encodeToString(IntOrStringSerializer, intVal))
        val strVal: IntOrString = IntOrString.StringValue("abc")
        assertEquals("\"abc\"", json.encodeToString(IntOrStringSerializer, strVal))
    }
}
