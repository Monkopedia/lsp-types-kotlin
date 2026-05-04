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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

/**
 * Tests that the generated sealed-interface union types correctly discriminate
 * between branches when deserializing real LSP JSON payloads.
 */
class UnionDiscriminatorTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ---- DocumentDiagnosticReport — discriminates on `kind` ----

    @Test
    fun `DocumentDiagnosticReport full variant`() {
        val result = json.decodeFromString(
            DocumentDiagnosticReportSerializer,
            """{
                "kind": "full",
                "items": [
                    {
                        "range": {"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 1}},
                        "message": "diag",
                        "severity": 1
                    }
                ]
            }"""
        )
        assertIs<RelatedFullDocumentDiagnosticReport>(result)
        assertEquals(1, result.items.size)
    }

    @Test
    fun `DocumentDiagnosticReport unchanged variant`() {
        val result = json.decodeFromString(
            DocumentDiagnosticReportSerializer,
            """{"kind": "unchanged", "resultId": "abc-123"}"""
        )
        assertIs<RelatedUnchangedDocumentDiagnosticReport>(result)
        assertEquals("abc-123", result.resultId)
    }

    // ---- InlineValue — discriminates on unique required fields ----

    @Test
    fun `InlineValue text variant`() {
        val result = json.decodeFromString(
            InlineValueSerializer,
            """{
                "range": {"start": {"line": 1, "character": 0}, "end": {"line": 1, "character": 5}},
                "text": "x = 42"
            }"""
        )
        assertIs<InlineValueText>(result)
        assertEquals("x = 42", result.text)
    }

    @Test
    fun `InlineValue variableLookup variant`() {
        val result = json.decodeFromString(
            InlineValueSerializer,
            """{
                "range": {"start": {"line": 1, "character": 0}, "end": {"line": 1, "character": 5}},
                "variableName": "x",
                "caseSensitiveLookup": true
            }"""
        )
        assertIs<InlineValueVariableLookup>(result)
        assertEquals("x", result.variableName)
    }

    @Test
    fun `InlineValue evaluatableExpression variant`() {
        val result = json.decodeFromString(
            InlineValueSerializer,
            """{
                "range": {"start": {"line": 1, "character": 0}, "end": {"line": 1, "character": 5}},
                "expression": "compute()"
            }"""
        )
        assertIs<InlineValueEvaluatableExpression>(result)
        assertEquals("compute()", result.expression)
    }

    // ---- BooleanOr<T> in real ServerCapabilities ----

    @Test
    fun `ServerCapabilities hoverProvider as boolean`() {
        val result = json.decodeFromString<ServerCapabilities>(
            """{"hoverProvider": true}"""
        )
        assertNotNull(result.hoverProvider)
        assertTrue(result.hoverProvider is BooleanOr.BooleanValue)
        assertEquals(true, (result.hoverProvider as BooleanOr.BooleanValue).value)
    }

    @Test
    fun `ServerCapabilities hoverProvider as object`() {
        val result = json.decodeFromString<ServerCapabilities>(
            """{"hoverProvider": {"workDoneProgress": true}}"""
        )
        assertNotNull(result.hoverProvider)
        assertTrue(result.hoverProvider is BooleanOr.Value)
        assertEquals(true, (result.hoverProvider as BooleanOr.Value).value.workDoneProgress)
    }

    // ---- BooleanOr<sub-sealed> for multi-options like declarationProvider ----

    @Test
    fun `ServerCapabilities declarationProvider as boolean`() {
        val result = json.decodeFromString<ServerCapabilities>(
            """{"declarationProvider": false}"""
        )
        assertNotNull(result.declarationProvider)
        assertTrue(result.declarationProvider is BooleanOr.BooleanValue)
    }

    @Test
    fun `ServerCapabilities declarationProvider as DeclarationOptions`() {
        val result = json.decodeFromString<ServerCapabilities>(
            """{"declarationProvider": {"workDoneProgress": true}}"""
        )
        assertNotNull(result.declarationProvider)
        assertTrue(result.declarationProvider is BooleanOr.Value)
    }

    // ---- StringOr<T> for documentation fields ----

    @Test
    fun `CompletionItem documentation as plain string`() {
        val result = json.decodeFromString<CompletionItem>(
            """{"label": "x", "documentation": "Just text"}"""
        )
        assertNotNull(result.documentation)
        assertTrue(result.documentation is StringOr.StringValue)
        assertEquals("Just text", (result.documentation as StringOr.StringValue).value)
    }

    @Test
    fun `CompletionItem documentation as MarkupContent`() {
        val result = json.decodeFromString<CompletionItem>(
            """{"label": "x", "documentation": {"kind": "markdown", "value": "**bold**"}}"""
        )
        assertNotNull(result.documentation)
        assertTrue(result.documentation is StringOr.Value)
        assertEquals(
            "**bold**",
            (result.documentation as StringOr.Value).value.value
        )
    }

    // ---- IntOrString for ProgressToken ----

    @Test
    fun `ProgressToken as integer`() {
        val result = json.decodeFromString(IntOrStringSerializer, "42")
        assertIs<IntOrString.IntValue>(result)
        assertEquals(42, result.value)
    }

    @Test
    fun `ProgressToken as string`() {
        val result = json.decodeFromString(IntOrStringSerializer, "\"my-token\"")
        assertIs<IntOrString.StringValue>(result)
        assertEquals("my-token", result.value)
    }

    // ---- Round-trip tests for round-trip stability ----

    @Test
    fun `BooleanOr round-trip preserves boolean`() {
        val original: BooleanOr<HoverOptions> = BooleanOr.BooleanValue(true)
        val encoded = json.encodeToString(BooleanOrSerializer(HoverOptions.serializer()), original)
        val decoded = json.decodeFromString(BooleanOrSerializer(HoverOptions.serializer()), encoded)
        assertTrue(decoded is BooleanOr.BooleanValue)
        assertEquals(true, decoded.value)
    }

    @Test
    fun `InlineValue round-trip preserves variant`() {
        val original: InlineValue = InlineValueText(
            range = Range(Position(1u, 0u), Position(1u, 5u)),
            text = "hello"
        )
        val encoded = json.encodeToString(InlineValueSerializer, original)
        val decoded = json.decodeFromString(InlineValueSerializer, encoded)
        assertIs<InlineValueText>(decoded)
        assertEquals("hello", decoded.text)
    }

    @Test
    fun `DocumentDiagnosticReport round-trip preserves kind`() {
        val original: DocumentDiagnosticReport = RelatedUnchangedDocumentDiagnosticReport(
            kind = "unchanged",
            resultId = "v42"
        )
        val encoded = json.encodeToString(DocumentDiagnosticReportSerializer, original)
        val decoded = json.decodeFromString(DocumentDiagnosticReportSerializer, encoded)
        assertIs<RelatedUnchangedDocumentDiagnosticReport>(decoded)
        assertEquals("v42", decoded.resultId)
    }
}
