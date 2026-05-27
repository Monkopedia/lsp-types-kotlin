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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * Portable per-method payload round-trip corpus — NOTIFICATION params (#43).
 *
 * Covers didOpen/didChange/didClose/didSave, publishDiagnostics, $/progress
 * (WorkDoneProgress begin/report/end) and $/cancelRequest, asserting
 * serialize -> deserialize -> serialize round-trip stability.
 */
class PayloadCorpusNotificationsTest {

    private val json = Json { ignoreUnknownKeys = true }

    private inline fun <reified T> roundTrip(serializer: KSerializer<T>, payload: String): T {
        val decoded = json.decodeFromString(serializer, payload)
        assertIs<T>(decoded)
        val reEncoded = json.encodeToString(serializer, decoded)
        val reDecoded = json.decodeFromString(serializer, reEncoded)
        assertEquals(decoded, reDecoded, "round-trip not stable for $payload")
        return decoded
    }

    private val range =
        """{"start":{"line":3,"character":2},"end":{"line":3,"character":9}}"""
    private val uri = "file:///project/src/Main.kt"

    // ---- textDocument/didOpen ----

    @Test
    fun `didOpen params`() {
        val p = roundTrip(
            DidOpenTextDocumentParams.serializer(),
            """{
                "textDocument": {
                    "uri": "$uri",
                    "languageId": "kotlin",
                    "version": 1,
                    "text": "fun main() {}\n"
                }
            }"""
        )
        assertEquals("kotlin", p.textDocument.languageId)
        assertEquals(1, p.textDocument.version)
    }

    // ---- textDocument/didChange (incremental + full content changes) ----

    @Test
    fun `didChange incremental content change`() {
        val p = roundTrip(
            DidChangeTextDocumentParams.serializer(),
            """{
                "textDocument": {"uri": "$uri", "version": 2},
                "contentChanges": [
                    {"range": $range, "rangeLength": 7, "text": "newText"}
                ]
            }"""
        )
        assertEquals(2, p.textDocument.version)
        assertIs<TextDocumentContentChangeEventRange>(p.contentChanges.single())
    }

    @Test
    fun `didChange full content change`() {
        val p = roundTrip(
            DidChangeTextDocumentParams.serializer(),
            """{
                "textDocument": {"uri": "$uri", "version": 3},
                "contentChanges": [
                    {"text": "fun main() { println(1) }\n"}
                ]
            }"""
        )
        assertIs<TextDocumentContentChangeEventVariant>(p.contentChanges.single())
    }

    // ---- textDocument/didClose ----

    @Test
    fun `didClose params`() {
        roundTrip(
            DidCloseTextDocumentParams.serializer(),
            """{"textDocument": {"uri": "$uri"}}"""
        )
    }

    // ---- textDocument/didSave (with and without text) ----

    @Test
    fun `didSave params with text`() {
        val p = roundTrip(
            DidSaveTextDocumentParams.serializer(),
            """{"textDocument": {"uri": "$uri"}, "text": "saved content"}"""
        )
        assertEquals("saved content", p.text)
    }

    @Test
    fun `didSave params without text`() {
        val p = roundTrip(
            DidSaveTextDocumentParams.serializer(),
            """{"textDocument": {"uri": "$uri"}}"""
        )
        assertEquals(null, p.text)
    }

    // ---- textDocument/publishDiagnostics ----

    @Test
    fun `publishDiagnostics params`() {
        val p = roundTrip(
            PublishDiagnosticsParams.serializer(),
            """{
                "uri": "$uri",
                "version": 4,
                "diagnostics": [
                    {
                        "range": $range,
                        "severity": 1,
                        "code": "E001",
                        "source": "kotlinc",
                        "message": "unresolved reference",
                        "tags": [1],
                        "relatedInformation": [
                            {
                                "location": {"uri": "$uri", "range": $range},
                                "message": "first seen here"
                            }
                        ]
                    }
                ]
            }"""
        )
        assertEquals(1, p.diagnostics.size)
        assertIs<IntOrString.StringValue>(p.diagnostics.single().code)
    }

    @Test
    fun `publishDiagnostics empty list clears diagnostics`() {
        val p = roundTrip(
            PublishDiagnosticsParams.serializer(),
            """{"uri": "$uri", "diagnostics": []}"""
        )
        assertEquals(0, p.diagnostics.size)
    }

    // ---- $/progress: token + arbitrary value payload ----

    @Test
    fun `progress params with string token`() {
        val p = roundTrip(
            ProgressParams.serializer(),
            """{"token": "progress-1", "value": {"kind": "begin", "title": "Indexing"}}"""
        )
        assertIs<IntOrString.StringValue>(p.token)
    }

    @Test
    fun `progress params with integer token`() {
        val p = roundTrip(
            ProgressParams.serializer(),
            """{"token": 99, "value": {"kind": "end"}}"""
        )
        assertIs<IntOrString.IntValue>(p.token)
    }

    // ---- WorkDoneProgress begin / report / end (the $/progress value shapes) ----

    @Test
    fun `workDoneProgress begin`() {
        val v = roundTrip(
            WorkDoneProgressBegin.serializer(),
            """{
                "kind": "begin",
                "title": "Indexing",
                "cancellable": true,
                "message": "0/25 files",
                "percentage": 0
            }"""
        )
        assertEquals("begin", v.kind)
        assertEquals(0u, v.percentage)
    }

    @Test
    fun `workDoneProgress report`() {
        val v = roundTrip(
            WorkDoneProgressReport.serializer(),
            """{"kind": "report", "message": "12/25 files", "percentage": 48}"""
        )
        assertEquals("report", v.kind)
        assertEquals(48u, v.percentage)
    }

    @Test
    fun `workDoneProgress end`() {
        val v = roundTrip(
            WorkDoneProgressEnd.serializer(),
            """{"kind": "end", "message": "Done"}"""
        )
        assertEquals("end", v.kind)
    }

    // ---- $/cancelRequest ----

    @Test
    fun `cancelRequest with integer id`() {
        val p = roundTrip(CancelParams.serializer(), """{"id": 12}""")
        assertIs<IntOrString.IntValue>(p.id)
    }

    @Test
    fun `cancelRequest with string id`() {
        val p = roundTrip(CancelParams.serializer(), """{"id": "req-abc"}""")
        assertIs<IntOrString.StringValue>(p.id)
    }
}
