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
import kotlinx.serialization.json.Json

/**
 * Method-result unions (e.g. `textDocument/definition`, `/completion`,
 * `/documentSymbol`) are now strict sealed types discriminated by shape and by
 * array-element fields.
 */
class MethodResultUnionTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val range =
        """{"start":{"line":1,"character":0},"end":{"line":1,"character":5}}"""

    // ---- textDocument/definition: Definition | DefinitionLink[] ----

    @Test
    fun `definition single Location object`() {
        val r = json.decodeFromString(
            TextDocumentDefinitionResultSerializer,
            """{"uri": "file:///a.kt", "range": $range}"""
        )
        val branch = assertIs<TextDocumentDefinitionResult.DefinitionValue>(r)
        assertIs<SingleOrArray.Single<Location>>(branch.value)
    }

    @Test
    fun `definition Location array`() {
        val r = json.decodeFromString(
            TextDocumentDefinitionResultSerializer,
            """[{"uri": "file:///a.kt", "range": $range}]"""
        )
        val branch = assertIs<TextDocumentDefinitionResult.DefinitionValue>(r)
        assertIs<SingleOrArray.Multiple<Location>>(branch.value)
    }

    @Test
    fun `definition LocationLink array discriminated by targetUri`() {
        val r = json.decodeFromString(
            TextDocumentDefinitionResultSerializer,
            """[{"targetUri": "file:///a.kt", "targetRange": $range, "targetSelectionRange": $range}]"""
        )
        val branch = assertIs<TextDocumentDefinitionResult.DefinitionLinkArray>(r)
        assertEquals(1, branch.value.size)
    }

    // ---- textDocument/completion: CompletionItem[] | CompletionList ----

    @Test
    fun `completion list object`() {
        val r = json.decodeFromString(
            TextDocumentCompletionResultSerializer,
            """{"isIncomplete": false, "items": [{"label": "foo"}]}"""
        )
        val branch = assertIs<TextDocumentCompletionResult.CompletionListValue>(r)
        assertEquals(false, branch.value.isIncomplete)
    }

    @Test
    fun `completion item array`() {
        val r = json.decodeFromString(
            TextDocumentCompletionResultSerializer,
            """[{"label": "foo"}, {"label": "bar"}]"""
        )
        val branch = assertIs<TextDocumentCompletionResult.CompletionItemArray>(r)
        assertEquals(2, branch.value.size)
    }

    // ---- textDocument/documentSymbol: SymbolInformation[] | DocumentSymbol[] ----

    @Test
    fun `documentSymbol DocumentSymbol array discriminated by range`() {
        val r = json.decodeFromString(
            TextDocumentDocumentSymbolResultSerializer,
            """[{"name": "f", "kind": 12, "range": $range, "selectionRange": $range}]"""
        )
        assertIs<TextDocumentDocumentSymbolResult.DocumentSymbolArray>(r)
    }

    @Test
    fun `documentSymbol SymbolInformation array`() {
        val r = json.decodeFromString(
            TextDocumentDocumentSymbolResultSerializer,
            """[{"name": "f", "kind": 12, "location": {"uri": "file:///a.kt", "range": $range}}]"""
        )
        assertIs<TextDocumentDocumentSymbolResult.SymbolInformationArray>(r)
    }
}
