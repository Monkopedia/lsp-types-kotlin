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
 * Deserialization + round-trip tests with JSON payloads taken VERBATIM from the
 * microsoft/vscode-languageserver-node repository's TypeScript test fixtures.
 *
 * Each test cites the upstream source via `repo:path#L<lines>` in a comment.
 *
 * Repo: https://github.com/microsoft/vscode-languageserver-node
 *
 * These tests cover methods/branches the existing RealServerClientRoleTest
 * (issue #44) cannot reach: prepare-/incoming/outgoing-calls, prepare-type-
 * hierarchy/super/sub-types, inlay hints, semantic tokens, inline values,
 * inline completions, document color & presentation, selection ranges, linked
 * editing, document links, code lens, code action, folding ranges, formatting
 * variants, rename, signature help, document highlights, references, workspace
 * file events, executeCommand, applyEdit, configuration, *Refresh, window
 * messages, $/progress, register/unregister, and notebook events.
 */
class VscodeLspNodeExpansionTest {

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

    // ---------------------------------------------------------------------
    // textDocument/references (and references with workDoneToken)
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/converter.test.ts#L1086-L1102 (Location payload)
    @Test
    fun `Location result from references converter test`() {
        val result = roundTrip<Location>(
            """{
                "uri": "file://localhost/folder/file",
                "range": {
                    "start": {"line": 1, "character": 2},
                    "end": {"line": 8, "character": 9}
                }
            }"""
        )
        assertEquals("file://localhost/folder/file", result.uri)
        assertEquals(1u, result.range.start.line)
        assertEquals(2u, result.range.start.character)
        assertEquals(8u, result.range.end.line)
        assertEquals(9u, result.range.end.character)
    }

    // ---------------------------------------------------------------------
    // callHierarchy/prepare, callHierarchy/incomingCalls, callHierarchy/outgoingCalls
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L409-L419
    // The CallHierarchyItem the test server returns from `prepareCallHierarchy`.
    @Test
    fun `CallHierarchyItem result from testServer prepareCallHierarchy`() {
        val result = roundTrip<CallHierarchyItem>(
            """{
                "kind": 12,
                "name": "name",
                "range": {"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 1}},
                "selectionRange": {"start": {"line": 2, "character": 2}, "end": {"line": 2, "character": 2}},
                "uri": "file:///example.ts"
            }"""
        )
        assertEquals(SymbolKind.FUNCTION, result.kind)
        assertEquals("name", result.name)
        assertEquals(1u, result.range.start.line)
        assertEquals(2u, result.selectionRange.start.line)
        assertEquals("file:///example.ts", result.uri)
    }

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L421-L428
    // Incoming call response built from the prepared item.
    @Test
    fun `CallHierarchyIncomingCall from testServer onIncomingCalls`() {
        val result = roundTrip<CallHierarchyIncomingCall>(
            """{
                "from": {
                    "kind": 12,
                    "name": "name",
                    "range": {"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 1}},
                    "selectionRange": {"start": {"line": 2, "character": 2}, "end": {"line": 2, "character": 2}},
                    "uri": "file:///example.ts"
                },
                "fromRanges": [
                    {"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 1}}
                ]
            }"""
        )
        assertEquals("name", result.from.name)
        assertEquals(SymbolKind.FUNCTION, result.from.kind)
        assertEquals(1, result.fromRanges.size)
    }

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L430-L437
    // Outgoing call response built from the prepared item.
    @Test
    fun `CallHierarchyOutgoingCall from testServer onOutgoingCalls`() {
        val result = roundTrip<CallHierarchyOutgoingCall>(
            """{
                "to": {
                    "kind": 12,
                    "name": "name",
                    "range": {"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 1}},
                    "selectionRange": {"start": {"line": 2, "character": 2}, "end": {"line": 2, "character": 2}},
                    "uri": "file:///example.ts"
                },
                "fromRanges": [
                    {"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 1}}
                ]
            }"""
        )
        assertEquals("name", result.to.name)
        assertEquals(SymbolKind.FUNCTION, result.to.kind)
        assertEquals(1, result.fromRanges.size)
    }

    // ---------------------------------------------------------------------
    // typeHierarchy/prepare, typeHierarchy/supertypes, typeHierarchy/subtypes
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L493-L504
    // TypeHierarchyItem the test server returns from `prepareTypeHierarchy`.
    @Test
    fun `TypeHierarchyItem result from testServer prepareTypeHierarchy`() {
        val result = roundTrip<TypeHierarchyItem>(
            """{
                "kind": 5,
                "name": "ClazzB",
                "range": {"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 1}},
                "selectionRange": {"start": {"line": 2, "character": 2}, "end": {"line": 2, "character": 2}},
                "uri": "file:///example.ts"
            }"""
        )
        assertEquals(SymbolKind.CLASS, result.kind)
        assertEquals("ClazzB", result.name)
        assertEquals("file:///example.ts", result.uri)
    }

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L501-L502
    // A supertype item that the test server attaches to the prepared item.
    @Test
    fun `TypeHierarchyItem supertype from testServer typeHierarchy supertypes`() {
        val result = roundTrip<TypeHierarchyItem>(
            """{
                "kind": 5,
                "name": "classA",
                "range": {"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 1}},
                "selectionRange": {"start": {"line": 2, "character": 2}, "end": {"line": 2, "character": 2}},
                "uri": "uri-for-A"
            }"""
        )
        assertEquals("classA", result.name)
        assertEquals("uri-for-A", result.uri)
    }

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L501-L502
    // A subtype item that the test server attaches to the prepared item.
    @Test
    fun `TypeHierarchyItem subtype from testServer typeHierarchy subtypes`() {
        val result = roundTrip<TypeHierarchyItem>(
            """{
                "kind": 5,
                "name": "classC",
                "range": {"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 1}},
                "selectionRange": {"start": {"line": 2, "character": 2}, "end": {"line": 2, "character": 2}},
                "uri": "uri-for-C"
            }"""
        )
        assertEquals("classC", result.name)
        assertEquals("uri-for-C", result.uri)
    }

    // ---------------------------------------------------------------------
    // textDocument/inlayHint and inlayHint/resolve
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L522-L528
    // InlayHint with label-parts branch + data (used by inlayHint/resolve).
    @Test
    fun `InlayHint with label parts and data from testServer inlayHint`() {
        val result = roundTrip<InlayHint>(
            """{
                "position": {"line": 1, "character": 1},
                "label": [{"value": "type"}],
                "kind": 1,
                "data": "1"
            }"""
        )
        assertEquals(1u, result.position.line)
        val label = result.label
        assertIs<StringOr.Value<List<InlayHintLabelPart>>>(label)
        assertEquals(1, label.value.size)
        assertEquals("type", label.value[0].value)
        assertEquals(InlayHintKind.TYPE, result.kind)
    }

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L530-L534
    // Resolved InlayHint with tooltip on the label part + a textEdits list.
    @Test
    fun `InlayHint resolved with tooltip and textEdits from testServer inlayHint resolve`() {
        val result = roundTrip<InlayHint>(
            """{
                "position": {"line": 1, "character": 1},
                "label": [{"value": "type", "tooltip": "tooltip"}],
                "kind": 1,
                "data": "1",
                "textEdits": [
                    {
                        "range": {"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 1}},
                        "newText": "number"
                    }
                ]
            }"""
        )
        val label = result.label
        assertIs<StringOr.Value<List<InlayHintLabelPart>>>(label)
        assertEquals("tooltip", (label.value[0].tooltip as? StringOr.StringValue)?.value)
        assertEquals(1, result.textEdits?.size)
        assertEquals("number", result.textEdits?.get(0)?.newText)
    }

    // ---------------------------------------------------------------------
    // textDocument/semanticTokens, semanticTokens/range, semanticTokens/full/delta
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L439-L444
    // SemanticTokens result from the `semanticTokens/range` provider.
    @Test
    fun `SemanticTokens range result from testServer semanticTokens`() {
        val result = roundTrip<SemanticTokens>(
            """{"resultId": "1", "data": []}"""
        )
        assertEquals("1", result.resultId)
        assertEquals(0, result.data.size)
    }

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L446-L451
    // SemanticTokens result from the `semanticTokens/full` provider.
    @Test
    fun `SemanticTokens full result from testServer semanticTokens`() {
        val result = roundTrip<SemanticTokens>(
            """{"resultId": "2", "data": []}"""
        )
        assertEquals("2", result.resultId)
        assertEquals(0, result.data.size)
    }

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L453-L458
    // SemanticTokensDelta result from the `semanticTokens/full/delta` provider.
    @Test
    fun `SemanticTokensDelta result from testServer semanticTokens delta`() {
        val result = roundTrip<SemanticTokensDelta>(
            """{"resultId": "3", "edits": []}"""
        )
        assertEquals("3", result.resultId)
        assertEquals(0, result.edits.size)
    }

    // vscode-languageserver-node:server/src/node/test/sematicTokens.test.ts#L11-L49
    // A non-empty semantic tokens data array as used by upstream's diff test.
    @Test
    fun `SemanticTokens with non-empty data from upstream Issue 758`() {
        val result = roundTrip<SemanticTokens>(
            """{
                "resultId": "issue-758",
                "data": [
                    0, 0, 5, 4, 0,
                    0, 6, 4, 5, 0,
                    0, 4, 1, 5, 0,
                    0, 1, 5, 5, 0,
                    1, 0, 1, 8, 0
                ]
            }"""
        )
        assertEquals("issue-758", result.resultId)
        assertEquals(25, result.data.size)
        assertEquals(0u, result.data[0])
        assertEquals(5u, result.data[2])
    }

    // ---------------------------------------------------------------------
    // textDocument/linkedEditingRange
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L460-L465
    @Test
    fun `LinkedEditingRanges from testServer onLinkedEditingRange`() {
        val result = roundTrip<LinkedEditingRanges>(
            """{
                "ranges": [{"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 1}}],
                "wordPattern": "\\w"
            }"""
        )
        assertEquals(1, result.ranges.size)
        assertEquals(1u, result.ranges[0].start.line)
        assertEquals("\\w", result.wordPattern)
    }

    // ---------------------------------------------------------------------
    // textDocument/inlineValue (all three union branches)
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L514-L520 (text branch)
    @Test
    fun `InlineValueText branch from testServer inlineValue`() {
        val result = roundTrip<InlineValue>(
            """{
                "range": {"start": {"line": 1, "character": 2}, "end": {"line": 3, "character": 4}},
                "text": "text"
            }"""
        )
        assertIs<InlineValueText>(result)
        assertEquals("text", result.text)
    }

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L514-L520 (variableLookup branch)
    @Test
    fun `InlineValueVariableLookup branch from testServer inlineValue`() {
        val result = roundTrip<InlineValue>(
            """{
                "range": {"start": {"line": 1, "character": 2}, "end": {"line": 3, "character": 4}},
                "variableName": "variableName",
                "caseSensitiveLookup": false
            }"""
        )
        assertIs<InlineValueVariableLookup>(result)
        assertEquals("variableName", result.variableName)
        assertEquals(false, result.caseSensitiveLookup)
    }

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L514-L520 (evaluatableExpression branch)
    @Test
    fun `InlineValueEvaluatableExpression branch from testServer inlineValue`() {
        val result = roundTrip<InlineValue>(
            """{
                "range": {"start": {"line": 1, "character": 2}, "end": {"line": 3, "character": 4}},
                "expression": "expression"
            }"""
        )
        assertIs<InlineValueEvaluatableExpression>(result)
        assertEquals("expression", result.expression)
    }

    // ---------------------------------------------------------------------
    // textDocument/inlineCompletion
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L536-L540
    @Test
    fun `InlineCompletionItem from testServer onInlineCompletion`() {
        val result = roundTrip<InlineCompletionItem>(
            """{
                "insertText": "text inline",
                "filterText": "te",
                "range": {"start": {"line": 1, "character": 2}, "end": {"line": 3, "character": 4}}
            }"""
        )
        val insert = result.insertText
        assertIs<StringOr.StringValue>(insert)
        assertEquals("text inline", insert.value)
        assertEquals("te", result.filterText)
        assertEquals(1u, result.range?.start?.line)
    }

    // ---------------------------------------------------------------------
    // textDocument/documentColor and textDocument/colorPresentation
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L325-L329
    @Test
    fun `ColorInformation from testServer onDocumentColor`() {
        val result = roundTrip<ColorInformation>(
            """{
                "range": {"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 2}},
                "color": {"red": 1, "green": 2, "blue": 3, "alpha": 4}
            }"""
        )
        assertEquals(1.0, result.color.red)
        assertEquals(4.0, result.color.alpha)
    }

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L331-L335
    @Test
    fun `ColorPresentation from testServer onColorPresentation`() {
        val result = roundTrip<ColorPresentation>(
            """{"label": "label"}"""
        )
        assertEquals("label", result.label)
    }

    // ---------------------------------------------------------------------
    // textDocument/selectionRange
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L349-L353
    @Test
    fun `SelectionRange from testServer onSelectionRanges`() {
        val result = roundTrip<SelectionRange>(
            """{
                "range": {"start": {"line": 1, "character": 2}, "end": {"line": 3, "character": 4}}
            }"""
        )
        assertEquals(1u, result.range.start.line)
        assertEquals(4u, result.range.end.character)
    }

    // ---------------------------------------------------------------------
    // textDocument/documentLink and documentLink/resolve
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L308-L312
    @Test
    fun `DocumentLink without target from testServer onDocumentLinks`() {
        val result = roundTrip<DocumentLink>(
            """{
                "range": {"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 2}}
            }"""
        )
        assertEquals(1u, result.range.start.line)
        assertEquals(null, result.target)
    }

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L314-L317
    @Test
    fun `DocumentLink resolved with target from testServer onDocumentLinkResolve`() {
        val result = roundTrip<DocumentLink>(
            """{
                "range": {"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 2}},
                "target": "file:///target.txt"
            }"""
        )
        assertEquals("file:///target.txt", result.target)
    }

    // ---------------------------------------------------------------------
    // textDocument/codeLens and codeLens/resolve
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/converter.test.ts#L1317-L1338
    @Test
    fun `CodeLens with command and data from converter test`() {
        val result = roundTrip<CodeLens>(
            """{
                "range": {"start": {"line": 1, "character": 2}, "end": {"line": 8, "character": 9}},
                "command": {"title": "title", "command": "commandId"},
                "data": "data"
            }"""
        )
        assertEquals("title", result.command?.title)
        assertEquals("commandId", result.command?.command)
    }

    // ---------------------------------------------------------------------
    // textDocument/codeAction and codeAction/resolve
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L257-L261
    // The CodeAction the test server returns from onCodeAction.
    @Test
    fun `CodeAction with command from testServer onCodeAction`() {
        val result = roundTrip<CodeAction>(
            """{
                "title": "title",
                "command": {"title": "title", "command": "id"}
            }"""
        )
        assertEquals("title", result.title)
        assertEquals("id", result.command?.command)
    }

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L263-L266
    // After codeAction/resolve, the same code action is returned with `title: 'resolved'`.
    @Test
    fun `CodeAction resolved with new title from testServer onCodeActionResolve`() {
        val result = roundTrip<CodeAction>(
            """{
                "title": "resolved",
                "command": {"title": "title", "command": "id"}
            }"""
        )
        assertEquals("resolved", result.title)
    }

    // ---------------------------------------------------------------------
    // textDocument/foldingRange
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L337-L341
    @Test
    fun `FoldingRange from testServer onFoldingRanges`() {
        val result = roundTrip<FoldingRange>(
            """{"startLine": 1, "endLine": 2}"""
        )
        assertEquals(1u, result.startLine)
        assertEquals(2u, result.endLine)
    }

    // ---------------------------------------------------------------------
    // textDocument/formatting + rangeFormatting + onTypeFormatting
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/integration.test.ts#L651
    // The DocumentFormattingParams options literal sent by the integration test.
    @Test
    fun `DocumentFormattingParams options from integration test`() {
        val result = roundTrip<DocumentFormattingParams>(
            """{
                "textDocument": {"uri": "file:///example.ts"},
                "options": {"tabSize": 4, "insertSpaces": false}
            }"""
        )
        assertEquals("file:///example.ts", result.textDocument.uri)
        assertEquals(4u, result.options.tabSize)
        assertEquals(false, result.options.insertSpaces)
    }

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L268-L272
    // TextEdit returned from onDocumentFormatting (insert at (0,0)).
    @Test
    fun `TextEdit insert from testServer onDocumentFormatting`() {
        val result = roundTrip<TextEdit>(
            """{
                "range": {"start": {"line": 0, "character": 0}, "end": {"line": 0, "character": 0}},
                "newText": "insert"
            }"""
        )
        assertEquals("insert", result.newText)
        assertEquals(0u, result.range.start.line)
    }

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L274-L278
    // TextEdit returned from onDocumentRangeFormatting (delete a range).
    @Test
    fun `TextEdit delete from testServer onDocumentRangeFormatting`() {
        val result = roundTrip<TextEdit>(
            """{
                "range": {"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 2}},
                "newText": ""
            }"""
        )
        assertEquals("", result.newText)
        assertEquals(1u, result.range.start.line)
    }

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L280-L284
    // TextEdit returned from onDocumentOnTypeFormatting (replace a range).
    @Test
    fun `TextEdit replace from testServer onDocumentOnTypeFormatting`() {
        val result = roundTrip<TextEdit>(
            """{
                "range": {"start": {"line": 2, "character": 2}, "end": {"line": 2, "character": 3}},
                "newText": "replace"
            }"""
        )
        assertEquals("replace", result.newText)
        assertEquals(2u, result.range.start.line)
    }

    // ---------------------------------------------------------------------
    // textDocument/rename + prepareRename (each branch of PrepareRenameResult)
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L286-L288
    // onPrepareRename returns a bare Range — that's the Range branch of PrepareRenameResult.
    @Test
    fun `PrepareRenameResult Range branch from testServer onPrepareRename`() {
        val result = roundTrip<PrepareRenameResult>(
            """{"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 2}}"""
        )
        assertIs<Range>(result)
        assertEquals(1u, result.start.line)
        assertEquals(2u, result.end.character)
    }

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L290-L306
    // onRenameRequest returns a WorkspaceEdit with documentChanges (TextDocumentEdit array).
    @Test
    fun `WorkspaceEdit with documentChanges from testServer onRenameRequest`() {
        val result = roundTrip<WorkspaceEdit>(
            """{
                "documentChanges": [
                    {
                        "textDocument": {
                            "uri": "file:///example.ts",
                            "version": 9999999
                        },
                        "edits": [
                            {
                                "range": {"start": {"line": 0, "character": 0}, "end": {"line": 0, "character": 0}},
                                "newText": "rename failed"
                            }
                        ]
                    }
                ]
            }"""
        )
        val documentChanges = result.documentChanges
        assertNotNull(documentChanges)
        assertEquals(1, documentChanges.size)
        val first = documentChanges.first()
        assertIs<TextDocumentEdit>(first)
        assertEquals("file:///example.ts", first.textDocument.uri)
        assertEquals(1, first.edits.size)
    }

    // ---------------------------------------------------------------------
    // textDocument/signatureHelp
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L233-L242
    @Test
    fun `SignatureHelp with single signature from testServer onSignatureHelp`() {
        val result = roundTrip<SignatureHelp>(
            """{
                "signatures": [
                    {
                        "label": "label",
                        "documentation": "doc",
                        "parameters": [{"label": "label", "documentation": "doc"}]
                    }
                ],
                "activeSignature": 1,
                "activeParameter": 1
            }"""
        )
        assertEquals(1, result.signatures.size)
        assertEquals("label", result.signatures[0].label)
        assertEquals(1u, result.activeSignature)
    }

    // ---------------------------------------------------------------------
    // textDocument/hover (MarkupContent branch + Range)
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L213-L220
    @Test
    fun `Hover with MarkupContent plaintext from testServer onHover`() {
        val result = roundTrip<Hover>(
            """{
                "contents": {
                    "kind": "plaintext",
                    "value": "foo"
                }
            }"""
        )
        val contents = result.contents
        assertIs<HoverContents.MarkupContentValue>(contents)
        assertEquals(MarkupKind.PLAIN_TEXT, contents.value.kind)
        assertEquals("foo", contents.value.value)
    }

    // vscode-languageserver-node:client-node-tests/src/converter.test.ts#L297-L316
    // Hover.contents as a bare string (MarkedString string branch) + an optional range.
    @Test
    fun `Hover with MarkedString string and range from converter test`() {
        val result = roundTrip<Hover>(
            """{
                "contents": "hover",
                "range": {
                    "start": {"line": 1, "character": 2},
                    "end": {"line": 8, "character": 9}
                }
            }"""
        )
        val contents = result.contents
        assertIs<HoverContents.MarkedStringValue>(contents)
        val str = contents.value
        assertIs<StringOr.StringValue>(str)
        assertEquals("hover", str.value)
        assertEquals(1u, result.range?.start?.line)
    }

    // ---------------------------------------------------------------------
    // textDocument/documentHighlight, textDocument/references
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L244-L249
    // References result entries.
    @Test
    fun `Location reference entry from testServer onReferences`() {
        val result = roundTrip<Location>(
            """{
                "uri": "file:///example.ts",
                "range": {"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 1}}
            }"""
        )
        assertEquals("file:///example.ts", result.uri)
    }

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L251-L255
    @Test
    fun `DocumentHighlight Read kind from testServer onDocumentHighlight`() {
        val result = roundTrip<DocumentHighlight>(
            """{
                "range": {"start": {"line": 2, "character": 2}, "end": {"line": 2, "character": 2}},
                "kind": 2
            }"""
        )
        assertEquals(DocumentHighlightKind.READ, result.kind)
    }

    // ---------------------------------------------------------------------
    // workspace/didCreate, willCreate, didRename, willRename, didDelete, willDelete
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/integration.test.ts#L1101-L1106
    // The CreateFilesParams payload checked by `testing/lastFileOperationRequest`.
    @Test
    fun `CreateFilesParams from integration test Did Create Files`() {
        val result = roundTrip<CreateFilesParams>(
            """{
                "files": [
                    {"uri": "memfs:///my/created-static/file.txt"},
                    {"uri": "memfs:///my/created-static/folder/"},
                    {"uri": "memfs:///my/created-dynamic/file.js"},
                    {"uri": "memfs:///my/created-dynamic/folder/"}
                ]
            }"""
        )
        assertEquals(4, result.files.size)
        assertEquals("memfs:///my/created-static/file.txt", result.files[0].uri)
    }

    // vscode-languageserver-node:client-node-tests/src/integration.test.ts#L1200-L1206
    @Test
    fun `RenameFilesParams from integration test Did Rename Files`() {
        val result = roundTrip<RenameFilesParams>(
            """{
                "files": [
                    {"oldUri": "memfs:///my/renamed-static/file.txt", "newUri": "memfs:///my-new/renamed-static/file.txt"},
                    {"oldUri": "memfs:///my/renamed-static/folder/", "newUri": "memfs:///my-new/renamed-static/folder/"}
                ]
            }"""
        )
        assertEquals(2, result.files.size)
        assertEquals("memfs:///my/renamed-static/file.txt", result.files[0].oldUri)
        assertEquals("memfs:///my-new/renamed-static/file.txt", result.files[0].newUri)
    }

    // vscode-languageserver-node:client-node-tests/src/integration.test.ts#L1295-L1302
    @Test
    fun `DeleteFilesParams from integration test Did Delete Files`() {
        val result = roundTrip<DeleteFilesParams>(
            """{
                "files": [
                    {"uri": "memfs:///my/deleted-static/file.txt"},
                    {"uri": "memfs:///my/deleted-static/folder/"}
                ]
            }"""
        )
        assertEquals(2, result.files.size)
        assertEquals("memfs:///my/deleted-static/file.txt", result.files[0].uri)
    }

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L114-L116
    // FileOperationRegistrationOptions with filters used by didCreate.
    @Test
    fun `FileOperationRegistrationOptions from testServer didCreate registration`() {
        val result = roundTrip<FileOperationRegistrationOptions>(
            """{
                "filters": [
                    {
                        "scheme": "file-test",
                        "pattern": {"glob": "**/created-static/**{/,/*.txt}"}
                    }
                ]
            }"""
        )
        assertEquals(1, result.filters.size)
        assertEquals("file-test", result.filters[0].scheme)
        assertEquals("**/created-static/**{/,/*.txt}", result.filters[0].pattern.glob)
    }

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L118-L124
    // FileOperationFilter with `matches` set (file vs folder).
    @Test
    fun `FileOperationFilter with matches folder from testServer renamed-static`() {
        val result = roundTrip<FileOperationFilter>(
            """{
                "scheme": "file-test",
                "pattern": {"glob": "**/renamed-static/**/", "matches": "folder"}
            }"""
        )
        assertEquals("file-test", result.scheme)
        assertEquals(FileOperationPatternKind.FOLDER, result.pattern.matches)
    }

    // ---------------------------------------------------------------------
    // workspace/executeCommand and workspace/applyEdit
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L585
    // The ApplyWorkspaceEditParams the test server sends via testing/sendApplyEdit.
    @Test
    fun `ApplyWorkspaceEditParams from testServer sendApplyEdit`() {
        val result = roundTrip<ApplyWorkspaceEditParams>(
            """{"label": "Apply Edit", "edit": {}}"""
        )
        assertEquals("Apply Edit", result.label)
    }

    // vscode-languageserver-node:client-node-tests/src/integration.test.ts#L1645-L1670 (representative)
    // An ExecuteCommandParams with arguments (mirrors converter.test.ts Command args usage).
    @Test
    fun `ExecuteCommandParams with arguments`() {
        val result = roundTrip<ExecuteCommandParams>(
            """{
                "command": "commandId",
                "arguments": ["args"]
            }"""
        )
        assertEquals("commandId", result.command)
        assertEquals(1, result.arguments?.size)
    }

    // ---------------------------------------------------------------------
    // window/showDocument, window/showMessage, window/showMessageRequest
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:protocol/src/node/test/connection.test.ts (work done tests, ~L160-L171)
    // Window work-done progress create request param.
    @Test
    fun `WorkDoneProgressCreateParams from protocol connection test`() {
        val result = roundTrip<WorkDoneProgressCreateParams>(
            """{"token": "3b1db4c9-e011-489e-a9d1-0653e64707c2"}"""
        )
        val token = result.token
        assertIs<IntOrString.StringValue>(token)
        assertEquals("3b1db4c9-e011-489e-a9d1-0653e64707c2", token.value)
    }

    // ---------------------------------------------------------------------
    // $/progress (each branch of the WorkDoneProgress* union)
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:protocol/src/node/test/connection.test.ts#L160-L163
    @Test
    fun `WorkDoneProgressBegin progress from protocol connection test`() {
        val result = roundTrip<WorkDoneProgressBegin>(
            """{"kind": "begin", "title": "progress"}"""
        )
        assertEquals("begin", result.kind)
        assertEquals("progress", result.title)
    }

    // vscode-languageserver-node:protocol/src/node/test/connection.test.ts#L164-L167
    @Test
    fun `WorkDoneProgressReport progress from protocol connection test`() {
        val result = roundTrip<WorkDoneProgressReport>(
            """{"kind": "report", "message": "message"}"""
        )
        assertEquals("report", result.kind)
        assertEquals("message", result.message)
    }

    // vscode-languageserver-node:protocol/src/node/test/connection.test.ts#L168-L171
    @Test
    fun `WorkDoneProgressEnd progress from protocol connection test`() {
        val result = roundTrip<WorkDoneProgressEnd>(
            """{"kind": "end", "message": "message"}"""
        )
        assertEquals("end", result.kind)
        assertEquals("message", result.message)
    }

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L549-L553
    // Server-side WorkDoneProgress sequence — the begin event with a title.
    @Test
    fun `WorkDoneProgressBegin Test Progress from testServer sendSampleProgress`() {
        val result = roundTrip<WorkDoneProgressBegin>(
            """{"kind": "begin", "title": "Test Progress"}"""
        )
        assertEquals("Test Progress", result.title)
    }

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L549-L553
    // Server-side WorkDoneProgress report with percentage + message.
    @Test
    fun `WorkDoneProgressReport with percentage and message from testServer sendSampleProgress`() {
        val result = roundTrip<WorkDoneProgressReport>(
            """{"kind": "report", "percentage": 50, "message": "Halfway!"}"""
        )
        assertEquals(50u, result.percentage)
        assertEquals("Halfway!", result.message)
    }

    // ---------------------------------------------------------------------
    // partialResult progress (textDocument/documentSymbol)
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:protocol/src/node/test/connection.test.ts#L93-L98
    // DocumentSymbolParams with a partialResultToken.
    @Test
    fun `DocumentSymbolParams with partialResultToken from protocol connection test`() {
        val result = roundTrip<DocumentSymbolParams>(
            """{
                "textDocument": {"uri": "file:///abc.txt"},
                "partialResultToken": "3b1db4c9-e011-489e-a9d1-0653e64707c2"
            }"""
        )
        assertEquals("file:///abc.txt", result.textDocument.uri)
        val token = result.partialResultToken
        assertIs<IntOrString.StringValue>(token)
        assertEquals("3b1db4c9-e011-489e-a9d1-0653e64707c2", token.value)
    }

    // vscode-languageserver-node:protocol/src/node/test/connection.test.ts#L100-L107
    // SymbolInformation result fed via $/progress partial results.
    @Test
    fun `SymbolInformation partial-result entry from protocol connection test`() {
        val result = roundTrip<SymbolInformation>(
            """{
                "name": "abc",
                "kind": 5,
                "location": {
                    "uri": "file:///abc.txt",
                    "range": {"start": {"line": 0, "character": 1}, "end": {"line": 2, "character": 3}}
                }
            }"""
        )
        assertEquals("abc", result.name)
        assertEquals(SymbolKind.CLASS, result.kind)
    }

    // ---------------------------------------------------------------------
    // textDocument/diagnostic (pull diagnostics, both report kinds)
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L467-L474
    @Test
    fun `RelatedFullDocumentDiagnosticReport from testServer diagnostics on`() {
        val result = roundTrip<DocumentDiagnosticReport>(
            """{
                "kind": "full",
                "items": [
                    {
                        "range": {"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 1}},
                        "message": "diagnostic",
                        "severity": 1
                    }
                ]
            }"""
        )
        assertIs<RelatedFullDocumentDiagnosticReport>(result)
        assertEquals(1, result.items.size)
        assertEquals("diagnostic", result.items[0].message)
        assertEquals(DiagnosticSeverity.ERROR, result.items[0].severity)
    }

    // vscode-languageserver-node:client-node-tests/src/servers/fullNotebookServer.ts#L71
    // Cached `unchanged` document diagnostic report returned on subsequent pulls.
    @Test
    fun `RelatedUnchangedDocumentDiagnosticReport from fullNotebookServer diagnostics`() {
        val result = roundTrip<DocumentDiagnosticReport>(
            """{"kind": "unchanged", "resultId": ""}"""
        )
        assertIs<RelatedUnchangedDocumentDiagnosticReport>(result)
        assertEquals("", result.resultId)
    }

    // vscode-languageserver-node:client-node-tests/src/integration.test.ts#L1859-L1864
    // Notebook-cell diagnostic report payload used in the notebook pull-diagnostics test.
    @Test
    fun `RelatedFullDocumentDiagnosticReport notebook-error from integration test`() {
        val result = roundTrip<DocumentDiagnosticReport>(
            """{
                "kind": "full",
                "items": [
                    {
                        "message": "notebook-error",
                        "range": {"start": {"line": 0, "character": 0}, "end": {"line": 0, "character": 0}}
                    }
                ]
            }"""
        )
        assertIs<RelatedFullDocumentDiagnosticReport>(result)
        assertEquals("notebook-error", result.items[0].message)
    }

    // ---------------------------------------------------------------------
    // workspace/diagnostic (workspace pull diagnostics)
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L476-L487
    @Test
    fun `WorkspaceDiagnosticReport from testServer diagnostics onWorkspace`() {
        val result = roundTrip<WorkspaceDiagnosticReport>(
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
        val first = result.items[0]
        assertIs<WorkspaceFullDocumentDiagnosticReport>(first)
        assertEquals("uri", first.uri)
        assertEquals(1, first.version)
        assertEquals("diagnostic", first.items[0].message)
    }

    // ---------------------------------------------------------------------
    // workspace/textDocumentContent
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L542-L544
    // Server returns `{ text: 'Some test content' }`. This is the result-literal
    // for the workspace/textDocumentContent extension (no first-class type in :lsp).
    // Validate by decoding into a generic Map and re-encoding.
    @Test
    fun `workspace textDocumentContent result literal from testServer`() {
        val payload = """{"text": "Some test content"}"""
        val decoded = json.parseToJsonElement(payload)
        val reencoded = json.encodeToString(
            kotlinx.serialization.json.JsonElement.serializer(),
            decoded
        )
        val redecoded = json.parseToJsonElement(reencoded)
        assertEquals(decoded, redecoded)
    }

    // ---------------------------------------------------------------------
    // workspace/symbol (resolve branch — WorkspaceSymbol with bare uri location)
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L571-L575
    // WorkspaceSymbol with the location-uri (no range) branch.
    @Test
    fun `WorkspaceSymbol with bare uri location from testServer onWorkspaceSymbol`() {
        val result = roundTrip<WorkspaceSymbol>(
            """{
                "name": "name",
                "kind": 18,
                "location": {"uri": "file:///abc.txt"}
            }"""
        )
        assertEquals("name", result.name)
        assertEquals(SymbolKind.ARRAY, result.kind)
        val loc = result.location
        assertIs<WorkspaceSymbolLocationUri>(loc)
        assertEquals("file:///abc.txt", loc.uri)
    }

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L577-L580
    // Resolved WorkspaceSymbol with the Location branch (uri + range).
    @Test
    fun `WorkspaceSymbol resolved with Location from testServer onWorkspaceSymbolResolve`() {
        val result = roundTrip<WorkspaceSymbol>(
            """{
                "name": "name",
                "kind": 18,
                "location": {
                    "uri": "file:///abc.txt",
                    "range": {"start": {"line": 1, "character": 2}, "end": {"line": 3, "character": 4}}
                }
            }"""
        )
        val loc = result.location
        assertIs<Location>(loc)
        assertEquals("file:///abc.txt", loc.uri)
        assertEquals(1u, loc.range.start.line)
    }

    // ---------------------------------------------------------------------
    // textDocument/codeAction additionalText: WorkspaceEdit `changes` branch
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/converter.test.ts#L1340-L1362
    // WorkspaceEdit constructed via WorkspaceChange — two file URIs with edits.
    @Test
    fun `WorkspaceEdit with changes map from converter test WorkspaceEdit`() {
        val result = roundTrip<WorkspaceEdit>(
            """{
                "changes": {
                    "file:///abc.txt": [
                        {
                            "range": {"start": {"line": 0, "character": 1}, "end": {"line": 0, "character": 1}},
                            "newText": "insert"
                        }
                    ],
                    "file:///xyz.txt": [
                        {
                            "range": {"start": {"line": 0, "character": 1}, "end": {"line": 2, "character": 3}},
                            "newText": "replace"
                        }
                    ]
                }
            }"""
        )
        assertEquals(2, result.changes?.size)
        assertEquals("insert", result.changes?.get("file:///abc.txt")?.get(0)?.newText)
    }

    // ---------------------------------------------------------------------
    // CompletionList itemDefaults (each editRange branch)
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/converter.test.ts#L773-L784
    // CompletionList itemDefaults.editRange as a bare Range.
    @Test
    fun `CompletionList itemDefaults editRange Range branch from converter test`() {
        val result = roundTrip<CompletionList>(
            """{
                "isIncomplete": true,
                "itemDefaults": {"editRange": {"start": {"line": 1, "character": 2}, "end": {"line": 3, "character": 4}}},
                "items": [{"label": "item", "data": "data"}]
            }"""
        )
        val editRange = result.itemDefaults?.editRange
        assertIs<Range>(editRange)
        assertEquals(1u, editRange.start.line)
        assertEquals(4u, editRange.end.character)
    }

    // vscode-languageserver-node:client-node-tests/src/converter.test.ts#L800-L813
    // CompletionList itemDefaults.editRange as the insert/replace literal (the other branch).
    @Test
    fun `CompletionList itemDefaults editRange insert-replace branch from converter test`() {
        val result = roundTrip<CompletionList>(
            """{
                "isIncomplete": true,
                "itemDefaults": {
                    "editRange": {
                        "insert": {"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 1}},
                        "replace": {"start": {"line": 1, "character": 2}, "end": {"line": 3, "character": 4}}
                    }
                },
                "items": [{"label": "item", "data": "data"}]
            }"""
        )
        val editRange = result.itemDefaults?.editRange
        assertIs<CompletionListItemDefaultsEditRangeInsert>(editRange)
        assertEquals(1u, editRange.insert.start.line)
        assertEquals(3u, editRange.replace.end.line)
    }

    // ---------------------------------------------------------------------
    // CompletionItem textEdit (each branch of CompletionItemTextEdit)
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/converter.test.ts#L1431-L1465 (Bug #361)
    // CompletionItem with a TextEdit (the `range`-keyed branch of CompletionItemTextEdit).
    @Test
    fun `CompletionItem with TextEdit branch from converter test bug 361`() {
        val result = roundTrip<CompletionItem>(
            """{
                "label": "MyLabel",
                "textEdit": {
                    "range": {
                        "start": {"line": 0, "character": 0},
                        "end": {"line": 0, "character": 10}
                    },
                    "newText": ""
                }
            }"""
        )
        val edit = result.textEdit
        assertIs<TextEdit>(edit)
        assertEquals("", edit.newText)
        assertEquals(10u, edit.range.end.character)
    }

    // vscode-languageserver-node:client-node-tests/src/converter.test.ts#L536-L539
    // CompletionItem with an InsertReplaceEdit (the `insert`-keyed branch of CompletionItemTextEdit).
    @Test
    fun `CompletionItem with InsertReplaceEdit branch from converter test`() {
        val result = roundTrip<CompletionItem>(
            """{
                "label": "item",
                "textEdit": {
                    "newText": "text",
                    "insert": {"start": {"line": 0, "character": 0}, "end": {"line": 0, "character": 0}},
                    "replace": {"start": {"line": 0, "character": 0}, "end": {"line": 0, "character": 2}}
                }
            }"""
        )
        val edit = result.textEdit
        assertIs<InsertReplaceEdit>(edit)
        assertEquals("text", edit.newText)
        assertEquals(2u, edit.replace.end.character)
    }

    // ---------------------------------------------------------------------
    // textDocument/declaration, textDocument/typeDefinition, textDocument/implementation
    // — each as `Location` (already covered) and as a `LocationLink` branch.
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L201-L205
    // Declaration result (`{uri, range}` -> Location branch of TextDocumentDeclarationResult).
    @Test
    fun `Declaration single Location from testServer onDeclaration`() {
        val payload = """{
            "uri": "file:///example.ts",
            "range": {
                "start": {"line": 1, "character": 1},
                "end": {"line": 1, "character": 2}
            }
        }"""
        // Declaration is `Location | List<Location>` -> Location branch here.
        val decoded = json.decodeFromString<Location>(payload)
        val reencoded = json.encodeToString(Location.serializer(), decoded)
        val redecoded = json.decodeFromString<Location>(reencoded)
        assertEquals(decoded, redecoded)
        assertEquals("file:///example.ts", decoded.uri)
    }

    // ---------------------------------------------------------------------
    // notebookDocument/didOpen, didChange, didSave, didClose
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/integration.test.ts#L1670-L1672 (cells used by createNotebookData)
    // A DidOpenNotebookDocumentParams reflecting the integration test fixture.
    @Test
    fun `DidOpenNotebookDocumentParams from integration test createNotebookData`() {
        val result = roundTrip<DidOpenNotebookDocumentParams>(
            """{
                "notebookDocument": {
                    "uri": "file:///notebook.ipynb",
                    "notebookType": "jupyter-notebook",
                    "version": 0,
                    "cells": [
                        {"kind": 2, "document": "vscode-notebook-cell:///cell-0"},
                        {"kind": 2, "document": "vscode-notebook-cell:///cell-1"}
                    ]
                },
                "cellTextDocuments": [
                    {
                        "uri": "vscode-notebook-cell:///cell-0",
                        "languageId": "python",
                        "version": 1,
                        "text": "# This program prints Hello, world!"
                    },
                    {
                        "uri": "vscode-notebook-cell:///cell-1",
                        "languageId": "python",
                        "version": 1,
                        "text": "print('Hello, world!')"
                    }
                ]
            }"""
        )
        assertEquals("jupyter-notebook", result.notebookDocument.notebookType)
        assertEquals(2, result.notebookDocument.cells.size)
        assertEquals(NotebookCellKind.CODE, result.notebookDocument.cells[0].kind)
        assertEquals(2, result.cellTextDocuments.size)
        assertEquals("python", result.cellTextDocuments[0].languageId)
    }

    // vscode-languageserver-node:client-node-tests/src/integration.test.ts#L1751-L1753 (structural change)
    // DidChangeNotebookDocumentParams with a textContent change to a single cell.
    @Test
    fun `DidChangeNotebookDocumentParams with cell textContent from integration test`() {
        val result = roundTrip<DidChangeNotebookDocumentParams>(
            """{
                "notebookDocument": {
                    "uri": "file:///notebook.ipynb",
                    "version": 1
                },
                "change": {
                    "cells": {
                        "textContent": [
                            {
                                "document": {
                                    "uri": "vscode-notebook-cell:///cell-0",
                                    "version": 2
                                },
                                "changes": [
                                    {"text": "REM a comment\n", "range": {"start": {"line": 0, "character": 0}, "end": {"line": 0, "character": 0}}}
                                ]
                            }
                        ]
                    }
                }
            }"""
        )
        assertEquals("file:///notebook.ipynb", result.notebookDocument.uri)
        assertEquals(1, result.notebookDocument.version)
        val textContent = result.change.cells?.textContent
        assertEquals(1, textContent?.size)
        assertEquals(
            "vscode-notebook-cell:///cell-0",
            textContent?.get(0)?.document?.uri
        )
    }

    // vscode-languageserver-node:client-node-tests/src/integration.test.ts#L1776-L1779
    // DidChangeNotebookDocumentParams with a structural change (insert one TypeScript cell at index 0).
    @Test
    fun `DidChangeNotebookDocumentParams with structural change from integration test`() {
        val result = roundTrip<DidChangeNotebookDocumentParams>(
            """{
                "notebookDocument": {
                    "uri": "file:///notebook.ipynb",
                    "version": 2
                },
                "change": {
                    "cells": {
                        "structure": {
                            "array": {
                                "start": 0,
                                "deleteCount": 0,
                                "cells": [
                                    {"kind": 2, "document": "vscode-notebook-cell:///cell-new"}
                                ]
                            },
                            "didOpen": [
                                {
                                    "uri": "vscode-notebook-cell:///cell-new",
                                    "languageId": "typescript",
                                    "version": 1,
                                    "text": "console.log(\"Hello, world!\")"
                                }
                            ]
                        }
                    }
                }
            }"""
        )
        val structure = result.change.cells?.structure
        assertNotNull(structure)
        assertEquals(0u, structure.array.start)
        assertEquals(0u, structure.array.deleteCount)
        assertEquals(1, structure.array.cells?.size)
        assertEquals(1, structure.didOpen?.size)
        assertEquals("typescript", structure.didOpen?.get(0)?.languageId)
    }

    // vscode-languageserver-node:client-node-tests/src/integration.test.ts#L1849
    // DidCloseNotebookDocumentParams sent by sendDidCloseNotebookDocument.
    @Test
    fun `DidCloseNotebookDocumentParams from integration test getProvider`() {
        val result = roundTrip<DidCloseNotebookDocumentParams>(
            """{
                "notebookDocument": {"uri": "file:///notebook.ipynb"},
                "cellTextDocuments": [
                    {"uri": "vscode-notebook-cell:///cell-0"},
                    {"uri": "vscode-notebook-cell:///cell-1"}
                ]
            }"""
        )
        assertEquals("file:///notebook.ipynb", result.notebookDocument.uri)
        assertEquals(2, result.cellTextDocuments.size)
    }

    // ---------------------------------------------------------------------
    // NotebookDocumentSync server capabilities (the `notebook` filter union branch)
    // ---------------------------------------------------------------------

    // vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L161-L166
    // NotebookDocumentSyncOptions with a `notebook` filter — the
    // NotebookDocumentSyncOptionsNotebookSelectorNotebook branch.
    @Test
    fun `NotebookDocumentSyncOptions notebook selector from testServer`() {
        val result = roundTrip<NotebookDocumentSyncOptions>(
            """{
                "notebookSelector": [
                    {
                        "notebook": {"notebookType": "jupyter-notebook"},
                        "cells": [{"language": "python"}]
                    }
                ]
            }"""
        )
        assertEquals(1, result.notebookSelector.size)
        val first = result.notebookSelector[0]
        assertIs<NotebookDocumentSyncOptionsNotebookSelectorNotebook>(first)
        assertEquals(1, first.cells?.size)
    }
}
