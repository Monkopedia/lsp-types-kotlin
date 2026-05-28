/*
 * Copyright 2026 Jason Monk
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
import kotlinx.serialization.json.Json

/**
 * Deserialization + round-trip tests using JSON payloads copied VERBATIM from the
 * microsoft/language-server-protocol specification markdown.
 *
 * Each test cites its upstream source via `repo:path#Lstart-Lend` in a comment.
 *
 * Repo: https://github.com/microsoft/language-server-protocol
 */
class LspSpecExpansionTest {

    private val json = Json { ignoreUnknownKeys = true }

    private inline fun <reified T> roundTrip(payload: String): T {
        val decoded = json.decodeFromString<T>(payload)
        val reencoded = json.encodeToString(
            kotlinx.serialization.serializer<T>(),
            decoded
        )
        val redecoded = json.decodeFromString<T>(reencoded)
        assertEquals(decoded, redecoded, "round-trip equality failed")
        return decoded
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/types/partialResults.md#L7-L24
    // `textDocument/references` request params with both workDoneToken and partialResultToken.
    @Test
    fun `references params with both progress tokens from spec partialResults`() {
        val result = roundTrip<ReferenceParams>(
            """{
                "textDocument": {
                    "uri": "file:///folder/file.ts"
                },
                "position": {
                    "line": 9,
                    "character": 5
                },
                "context": {
                    "includeDeclaration": true
                },
                "workDoneToken": "1d546990-40a3-4b77-b134-46622995f6ae",
                "partialResultToken": "5f6f349e-4f81-4a3b-afff-ee04bff96804"
            }"""
        )
        assertEquals("file:///folder/file.ts", result.textDocument.uri)
        assertEquals(9u, result.position.line)
        assertEquals(5u, result.position.character)
        assertEquals(true, result.context.includeDeclaration)
        // workDoneToken/partialResultToken are ProgressToken (IntOrString).
        val workDoneToken = result.workDoneToken
        assertIs<IntOrString.StringValue>(workDoneToken)
        assertEquals("1d546990-40a3-4b77-b134-46622995f6ae", workDoneToken.value)
        val partialResultToken = result.partialResultToken
        assertIs<IntOrString.StringValue>(partialResultToken)
        assertEquals("5f6f349e-4f81-4a3b-afff-ee04bff96804", partialResultToken.value)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/types/workDoneProgress.md#L119-L134
    // Client-initiated work done progress: references request with workDoneToken only.
    @Test
    fun `references params with client-initiated workDoneToken from spec workDoneProgress`() {
        val result = roundTrip<ReferenceParams>(
            """{
                "textDocument": {
                    "uri": "file:///folder/file.ts"
                },
                "position": {
                    "line": 9,
                    "character": 5
                },
                "context": {
                    "includeDeclaration": true
                },
                "workDoneToken": "1d546990-40a3-4b77-b134-46622995f6ae"
            }"""
        )
        assertEquals(true, result.context.includeDeclaration)
        assertNotNull(result.workDoneToken)
        assertEquals(null, result.partialResultToken)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/types/workDoneProgress.md#L152-L163
    // `$/progress` notification params with a `begin` payload.
    // Targets methods: $/progress (notification), WorkDoneProgressBegin (begin branch).
    @Test
    fun `progress notification params with WorkDoneProgressBegin from spec`() {
        val result = roundTrip<ProgressParams>(
            """{
                "token": "1d546990-40a3-4b77-b134-46622995f6ae",
                "value": {
                    "kind": "begin",
                    "title": "Finding references for A#foo",
                    "cancellable": false,
                    "message": "Processing file X.ts",
                    "percentage": 0
                }
            }"""
        )
        val token = result.token
        assertIs<IntOrString.StringValue>(token)
        assertEquals("1d546990-40a3-4b77-b134-46622995f6ae", token.value)
        // The value is a raw JsonElement; sanity-check via re-decode as WorkDoneProgressBegin.
        val begin = json.decodeFromJsonElement(
            WorkDoneProgressBegin.serializer(),
            result.value
        )
        assertEquals("begin", begin.kind)
        assertEquals("Finding references for A#foo", begin.title)
        assertEquals(false, begin.cancellable)
        assertEquals("Processing file X.ts", begin.message)
        assertEquals(0u, begin.percentage)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/types/workDoneProgress.md#L11-L38
    // WorkDoneProgressBegin standalone (the `value` of a `$/progress` notification).
    @Test
    fun `WorkDoneProgressBegin standalone from spec`() {
        val result = roundTrip<WorkDoneProgressBegin>(
            """{
                "kind": "begin",
                "title": "Finding references for A#foo",
                "cancellable": false,
                "message": "Processing file X.ts",
                "percentage": 0
            }"""
        )
        assertEquals("begin", result.kind)
        assertEquals("Finding references for A#foo", result.title)
        assertEquals(false, result.cancellable)
        assertEquals(0u, result.percentage)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/types/workDoneProgress.md#L56-L92
    // WorkDoneProgressReport — second branch of the WorkDoneProgress union.
    @Test
    fun `WorkDoneProgressReport from spec`() {
        val result = roundTrip<WorkDoneProgressReport>(
            """{
                "kind": "report",
                "cancellable": true,
                "message": "Halfway",
                "percentage": 50
            }"""
        )
        assertEquals("report", result.kind)
        assertEquals(true, result.cancellable)
        assertEquals("Halfway", result.message)
        assertEquals(50u, result.percentage)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/types/workDoneProgress.md#L95-L106
    // WorkDoneProgressEnd — third branch of the WorkDoneProgress union.
    @Test
    fun `WorkDoneProgressEnd from spec`() {
        val result = roundTrip<WorkDoneProgressEnd>(
            """{
                "kind": "end",
                "message": "Found 5 references"
            }"""
        )
        assertEquals("end", result.kind)
        assertEquals("Found 5 references", result.message)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/types/workDoneProgress.md#L172-L178
    // Server capability fragment showing referencesProvider with workDoneProgress=true.
    @Test
    fun `ReferenceOptions with workDoneProgress from spec`() {
        val result = roundTrip<ReferenceOptions>(
            """{"workDoneProgress": true}"""
        )
        assertEquals(true, result.workDoneProgress)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/types/documentFilter.md#L1-L8
    // The DocumentFilter example: `{ language: 'json', pattern: '**/package.json' }`.
    // (Spec example uses JS object literal; converted to JSON only by quoting keys.)
    @Test
    fun `TextDocumentFilter with language+pattern from spec documentFilter`() {
        // The branch resolution prefers `language` when present (see TextDocumentFilterSerializer).
        val result = roundTrip<TextDocumentFilter>(
            """{"language": "json", "pattern": "**/package.json"}"""
        )
        assertIs<TextDocumentFilterLanguage>(result)
        assertEquals("json", result.language)
        assertEquals("**/package.json", result.pattern)
    }
}
