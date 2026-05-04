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
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json

/**
 * Deserialization tests using JSON samples from vscode-languageserver-node:
 * - testServer.ts (integration test server)
 * - converter.test.ts (protocol converter tests)
 *
 * These verify that our generated types can parse real-world VSCode LSP messages.
 */
class VscodeServerSampleTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ---- Hover ----

    @Test
    fun `Hover with MarkupContent`() {
        val result = json.decodeFromString<Hover>(
            """{"contents": {"kind": "plaintext", "value": "foo"}}"""
        )
        assertNotNull(result.contents)
    }

    // ---- DocumentHighlight ----

    @Test
    fun `DocumentHighlight from testServer`() {
        val result = json.decodeFromString<DocumentHighlight>(
            """{
                "range": {"start": {"line": 2, "character": 2}, "end": {"line": 2, "character": 2}},
                "kind": 1
            }"""
        )
        assertEquals(2u, result.range.start.line)
        assertEquals(2u, result.range.start.character)
        assertEquals(DocumentHighlightKind.TEXT, result.kind)
    }

    // ---- DocumentLink ----

    @Test
    fun `DocumentLink from testServer`() {
        val result = json.decodeFromString<DocumentLink>(
            """{
                "range": {"start": {"line": 1, "character": 2}, "end": {"line": 8, "character": 9}},
                "target": "file:///foo/bar",
                "tooltip": "tooltip"
            }"""
        )
        assertEquals(1u, result.range.start.line)
        assertEquals(2u, result.range.start.character)
        assertEquals(8u, result.range.end.line)
        assertEquals(9u, result.range.end.character)
        assertEquals("file:///foo/bar", result.target)
        assertEquals("tooltip", result.tooltip)
    }

    // ---- DocumentSymbol ----

    @Test
    fun `DocumentSymbol from testServer`() {
        val result = json.decodeFromString<DocumentSymbol>(
            """{
                "name": "name",
                "kind": 18,
                "tags": [1],
                "range": {"start": {"line": 1, "character": 2}, "end": {"line": 8, "character": 9}},
                "selectionRange": {"start": {"line": 1, "character": 2}, "end": {"line": 8, "character": 9}}
            }"""
        )
        assertEquals("name", result.name)
        assertEquals(SymbolKind.ARRAY, result.kind)
        assertEquals(1, result.tags?.size)
        assertEquals(SymbolTag.DEPRECATED, result.tags?.get(0))
        assertEquals(1u, result.range.start.line)
        assertEquals(2u, result.range.start.character)
        assertEquals(8u, result.selectionRange.end.line)
        assertEquals(9u, result.selectionRange.end.character)
    }

    // ---- ColorInformation ----

    @Test
    fun `ColorInformation from testServer`() {
        val result = json.decodeFromString<ColorInformation>(
            """{
                "range": {"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 2}},
                "color": {"red": 1, "green": 2, "blue": 3, "alpha": 4}
            }"""
        )
        assertEquals(1u, result.range.start.line)
        assertEquals(1u, result.range.start.character)
        assertEquals(1.0, result.color.red)
        assertEquals(2.0, result.color.green)
        assertEquals(3.0, result.color.blue)
        assertEquals(4.0, result.color.alpha)
    }

    // ---- ColorPresentation ----

    @Test
    fun `ColorPresentation from testServer`() {
        val result = json.decodeFromString<ColorPresentation>(
            """{"label": "label"}"""
        )
        assertEquals("label", result.label)
    }

    // ---- FoldingRange ----

    @Test
    fun `FoldingRange from testServer`() {
        val result = json.decodeFromString<FoldingRange>(
            """{"startLine": 1, "endLine": 2}"""
        )
        assertEquals(1u, result.startLine)
        assertEquals(2u, result.endLine)
    }

    // ---- SelectionRange ----

    @Test
    fun `SelectionRange from testServer`() {
        val result = json.decodeFromString<SelectionRange>(
            """{
                "range": {"start": {"line": 1, "character": 2}, "end": {"line": 3, "character": 4}}
            }"""
        )
        assertEquals(1u, result.range.start.line)
        assertEquals(2u, result.range.start.character)
        assertEquals(3u, result.range.end.line)
        assertEquals(4u, result.range.end.character)
    }

    // ---- CallHierarchyItem ----

    @Test
    fun `CallHierarchyItem from testServer`() {
        val result = json.decodeFromString<CallHierarchyItem>(
            """{
                "name": "name",
                "kind": 12,
                "uri": "file:///example.ts",
                "range": {"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 1}},
                "selectionRange": {"start": {"line": 2, "character": 2}, "end": {"line": 2, "character": 2}}
            }"""
        )
        assertEquals("name", result.name)
        assertEquals(SymbolKind.FUNCTION, result.kind)
        assertEquals("file:///example.ts", result.uri)
        assertEquals(1u, result.range.start.line)
        assertEquals(2u, result.selectionRange.start.line)
    }

    // ---- CallHierarchyOutgoingCall ----

    @Test
    fun `CallHierarchyOutgoingCall from testServer`() {
        val result = json.decodeFromString<CallHierarchyOutgoingCall>(
            """{
                "to": {
                    "name": "name",
                    "kind": 12,
                    "uri": "file:///example.ts",
                    "range": {"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 1}},
                    "selectionRange": {"start": {"line": 2, "character": 2}, "end": {"line": 2, "character": 2}}
                },
                "fromRanges": [{"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 1}}]
            }"""
        )
        assertEquals("name", result.to.name)
        assertEquals(SymbolKind.FUNCTION, result.to.kind)
        assertEquals("file:///example.ts", result.to.uri)
        assertEquals(1, result.fromRanges.size)
        assertEquals(1u, result.fromRanges[0].start.line)
    }

    // ---- SemanticTokens ----

    @Test
    fun `SemanticTokens from testServer`() {
        val result = json.decodeFromString<SemanticTokens>(
            """{"resultId": "1", "data": []}"""
        )
        assertEquals("1", result.resultId)
        assertEquals(0, result.data.size)
    }

    // ---- SemanticTokensDelta ----

    @Test
    fun `SemanticTokensDelta from testServer`() {
        val result = json.decodeFromString<SemanticTokensDelta>(
            """{"resultId": "3", "edits": []}"""
        )
        assertEquals("3", result.resultId)
        assertEquals(0, result.edits.size)
    }

    // ---- LinkedEditingRanges ----

    @Test
    fun `LinkedEditingRanges from testServer`() {
        val result = json.decodeFromString<LinkedEditingRanges>(
            """{
                "ranges": [{"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 1}}],
                "wordPattern": "\\w"
            }"""
        )
        assertEquals(1, result.ranges.size)
        assertEquals(1u, result.ranges[0].start.line)
        assertEquals("\\w", result.wordPattern)
    }

    // ---- TypeHierarchyItem ----

    @Test
    fun `TypeHierarchyItem from testServer`() {
        val result = json.decodeFromString<TypeHierarchyItem>(
            """{
                "name": "ClazzB",
                "kind": 5,
                "uri": "file:///example.ts",
                "range": {"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 1}},
                "selectionRange": {"start": {"line": 2, "character": 2}, "end": {"line": 2, "character": 2}}
            }"""
        )
        assertEquals("ClazzB", result.name)
        assertEquals(SymbolKind.CLASS, result.kind)
        assertEquals("file:///example.ts", result.uri)
        assertEquals(1u, result.range.start.line)
        assertEquals(2u, result.selectionRange.start.line)
    }

    // ---- InlineCompletionItem ----

    @Test
    fun `InlineCompletionItem from testServer`() {
        val result = json.decodeFromString<InlineCompletionItem>(
            """{
                "insertText": "text inline",
                "filterText": "te",
                "range": {"start": {"line": 1, "character": 2}, "end": {"line": 3, "character": 4}}
            }"""
        )
        assertNotNull(result.insertText)
        assertEquals("te", result.filterText)
        assertEquals(1u, result.range?.start?.line)
        assertEquals(2u, result.range?.start?.character)
        assertEquals(3u, result.range?.end?.line)
        assertEquals(4u, result.range?.end?.character)
    }

    // ---- LocationLink ----

    @Test
    fun `LocationLink from converter test`() {
        val result = json.decodeFromString<LocationLink>(
            """{
                "targetUri": "file:///home/dirkb/test.ts",
                "targetRange": {"start": {"line": 0, "character": 0}, "end": {"line": 0, "character": 10}},
                "targetSelectionRange": {"start": {"line": 0, "character": 0}, "end": {"line": 0, "character": 10}}
            }"""
        )
        assertEquals("file:///home/dirkb/test.ts", result.targetUri)
        assertEquals(0u, result.targetRange.start.line)
        assertEquals(10u, result.targetRange.end.character)
        assertEquals(0u, result.targetSelectionRange.start.line)
        assertEquals(10u, result.targetSelectionRange.end.character)
    }

    // ---- Full InitializeResult from testServer ----

    @Test
    fun `full InitializeResult from testServer`() {
        val result = json.decodeFromString<InitializeResult>(
            """{
                "capabilities": {
                    "textDocumentSync": 1,
                    "definitionProvider": true,
                    "hoverProvider": true,
                    "completionProvider": {
                        "resolveProvider": true,
                        "triggerCharacters": ["\"", ":"]
                    },
                    "signatureHelpProvider": {
                        "triggerCharacters": [":"],
                        "retriggerCharacters": [":"]
                    },
                    "referencesProvider": true,
                    "documentHighlightProvider": true,
                    "codeActionProvider": {"resolveProvider": true},
                    "documentFormattingProvider": true,
                    "renameProvider": {"prepareProvider": true},
                    "documentLinkProvider": {"resolveProvider": true},
                    "documentSymbolProvider": true,
                    "colorProvider": true,
                    "declarationProvider": true,
                    "foldingRangeProvider": true,
                    "implementationProvider": true,
                    "selectionRangeProvider": true,
                    "inlayHintProvider": {"resolveProvider": true},
                    "typeDefinitionProvider": true,
                    "callHierarchyProvider": true,
                    "semanticTokensProvider": {
                        "legend": {"tokenTypes": [], "tokenModifiers": []},
                        "range": true,
                        "full": {"delta": true}
                    },
                    "linkedEditingRangeProvider": true,
                    "typeHierarchyProvider": true,
                    "workspaceSymbolProvider": {"resolveProvider": true}
                },
                "customResults": {"hello": "world"}
            }"""
        )
        assertNotNull(result.capabilities)
        assertNotNull(result.capabilities.completionProvider)
        assertEquals(true, result.capabilities.completionProvider?.resolveProvider)
        assertEquals(listOf("\"", ":"), result.capabilities.completionProvider?.triggerCharacters)
        assertNotNull(result.capabilities.signatureHelpProvider)
        assertNotNull(result.capabilities.textDocumentSync)
        assertNotNull(result.capabilities.hoverProvider)
        assertNotNull(result.capabilities.definitionProvider)
        assertNotNull(result.capabilities.referencesProvider)
        assertNotNull(result.capabilities.documentHighlightProvider)
        assertNotNull(result.capabilities.codeActionProvider)
        assertNotNull(result.capabilities.documentFormattingProvider)
        assertNotNull(result.capabilities.renameProvider)
        assertNotNull(result.capabilities.documentLinkProvider)
        assertEquals(true, result.capabilities.documentLinkProvider?.resolveProvider)
        assertNotNull(result.capabilities.documentSymbolProvider)
        assertNotNull(result.capabilities.colorProvider)
        assertNotNull(result.capabilities.declarationProvider)
        assertNotNull(result.capabilities.foldingRangeProvider)
        assertNotNull(result.capabilities.implementationProvider)
        assertNotNull(result.capabilities.selectionRangeProvider)
        assertNotNull(result.capabilities.inlayHintProvider)
        assertNotNull(result.capabilities.typeDefinitionProvider)
        assertNotNull(result.capabilities.callHierarchyProvider)
        assertNotNull(result.capabilities.semanticTokensProvider)
        assertNotNull(result.capabilities.linkedEditingRangeProvider)
        assertNotNull(result.capabilities.typeHierarchyProvider)
        assertNotNull(result.capabilities.workspaceSymbolProvider)
    }

    // ---- WorkspaceSymbol ----

    @Test
    fun `WorkspaceSymbol from testServer`() {
        val result = json.decodeFromString<WorkspaceSymbol>(
            """{
                "name": "name",
                "kind": 5,
                "location": {
                    "uri": "file:///abc.txt",
                    "range": {"start": {"line": 1, "character": 2}, "end": {"line": 3, "character": 4}}
                }
            }"""
        )
        assertEquals("name", result.name)
        assertEquals(SymbolKind.CLASS, result.kind)
        assertNotNull(result.location)
    }

    // ---- CompletionItem full ----

    @Test
    fun `CompletionItem full from testServer`() {
        val result = json.decodeFromString<CompletionItem>(
            """{
                "label": "item",
                "detail": "detail",
                "filterText": "filter",
                "insertText": "insert",
                "insertTextFormat": 1,
                "kind": 5,
                "sortText": "sort",
                "additionalTextEdits": [
                    {
                        "range": {"start": {"line": 1, "character": 2}, "end": {"line": 1, "character": 2}},
                        "newText": "insert"
                    }
                ],
                "command": {"title": "title", "command": "commandId"},
                "commitCharacters": ["."],
                "tags": [1]
            }"""
        )
        assertEquals("item", result.label)
        assertEquals("detail", result.detail)
        assertEquals("filter", result.filterText)
        assertEquals("insert", result.insertText)
        assertEquals(InsertTextFormat.PLAIN_TEXT, result.insertTextFormat)
        assertEquals(CompletionItemKind.FIELD, result.kind)
        assertEquals("sort", result.sortText)
        assertEquals(1, result.additionalTextEdits?.size)
        assertEquals("insert", result.additionalTextEdits?.get(0)?.newText)
        assertEquals("title", result.command?.title)
        assertEquals("commandId", result.command?.command)
        assertEquals(listOf("."), result.commitCharacters)
        assertEquals(1, result.tags?.size)
        assertEquals(CompletionItemTag.DEPRECATED, result.tags?.get(0))
    }

    // ---- InlayHint with label parts ----

    @Test
    fun `InlayHint with label parts from testServer`() {
        val result = json.decodeFromString<InlayHint>(
            """{
                "position": {"line": 1, "character": 1},
                "label": [{"value": "type", "tooltip": "tooltip"}],
                "kind": 1,
                "textEdits": [
                    {
                        "range": {"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 1}},
                        "newText": "number"
                    }
                ]
            }"""
        )
        assertEquals(1u, result.position.line)
        assertEquals(1u, result.position.character)
        assertNotNull(result.label)
        assertEquals(InlayHintKind.TYPE, result.kind)
        assertEquals(1, result.textEdits?.size)
        assertEquals("number", result.textEdits?.get(0)?.newText)
    }

    // ---- WorkspaceDiagnosticReport ----

    @Test
    fun `WorkspaceDiagnosticReport from testServer`() {
        val result = json.decodeFromString<WorkspaceDiagnosticReport>(
            """{
                "items": [
                    {
                        "kind": "full",
                        "uri": "uri",
                        "version": 1,
                        "items": [
                            {
                                "range": {"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 1}},
                                "message": "diagnostic",
                                "severity": 1
                            }
                        ]
                    }
                ]
            }"""
        )
        assertEquals(1, result.items.size)
        assertNotNull(result.items[0])
    }

    // ---- Diagnostic with relatedInformation ----

    @Test
    fun `Diagnostic with relatedInformation from testServer`() {
        val result = json.decodeFromString<Diagnostic>(
            """{
                "range": {"start": {"line": 1, "character": 2}, "end": {"line": 8, "character": 9}},
                "message": "error",
                "severity": 1,
                "code": 99,
                "source": "source",
                "tags": [1],
                "relatedInformation": [
                    {
                        "message": "related",
                        "location": {
                            "uri": "file://localhost/folder/file",
                            "range": {"start": {"line": 0, "character": 1}, "end": {"line": 2, "character": 3}}
                        }
                    }
                ]
            }"""
        )
        assertEquals(1u, result.range.start.line)
        assertEquals(2u, result.range.start.character)
        assertEquals(8u, result.range.end.line)
        assertEquals(9u, result.range.end.character)
        assertEquals("error", result.message)
        assertEquals(DiagnosticSeverity.ERROR, result.severity)
        assertNotNull(result.code)
        assertEquals("source", result.source)
        assertEquals(1, result.tags?.size)
        assertEquals(DiagnosticTag.UNNECESSARY, result.tags?.get(0))
        assertEquals(1, result.relatedInformation?.size)
        assertEquals("related", result.relatedInformation?.get(0)?.message)
        assertEquals(
            "file://localhost/folder/file",
            result.relatedInformation?.get(0)?.location?.uri
        )
        assertEquals(0u, result.relatedInformation?.get(0)?.location?.range?.start?.line)
        assertEquals(1u, result.relatedInformation?.get(0)?.location?.range?.start?.character)
    }
}
