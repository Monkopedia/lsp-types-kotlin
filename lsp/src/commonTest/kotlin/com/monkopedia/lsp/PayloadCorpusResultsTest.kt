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
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * Portable per-method payload round-trip corpus — RESULT payloads (#43).
 *
 * Exercises every branch of the typed method-result unions: each
 * location-shaped result (single Location, Location[], LocationLink[]),
 * both completion result shapes, both documentSymbol shapes, both
 * semanticTokens/full delta shapes, hover contents (markup / marked-string /
 * array), ServerCapabilities provider shapes (boolean vs options), and the
 * remaining typed results. Asserts serialize -> deserialize -> serialize
 * round-trip stability.
 */
class PayloadCorpusResultsTest {

    private val json = Json { ignoreUnknownKeys = true }

    private inline fun <reified T> roundTrip(serializer: KSerializer<T>, payload: String): T {
        val decoded = json.decodeFromString(serializer, payload)
        assertIs<T>(decoded)
        val reEncoded = json.encodeToString(serializer, decoded)
        val reDecoded = json.decodeFromString(serializer, reEncoded)
        assertEquals(decoded, reDecoded, "round-trip not stable for $payload")
        return decoded
    }

    /** Round-trip for sealed unions backed by a content-polymorphic serializer. */
    private fun <T> roundTripPoly(
        serializer: DeserializationStrategy<T>,
        encoder: KSerializer<T>,
        payload: String
    ): T {
        val decoded = json.decodeFromString(serializer, payload)
        val reEncoded = json.encodeToString(encoder, decoded)
        val reDecoded = json.decodeFromString(serializer, reEncoded)
        assertEquals(decoded, reDecoded, "round-trip not stable for $payload")
        return decoded
    }

    private val range =
        """{"start":{"line":3,"character":2},"end":{"line":3,"character":9}}"""
    private val uri = "file:///project/src/Main.kt"
    private val location = """{"uri":"$uri","range":$range}"""
    private val locationLink =
        """{"targetUri":"$uri","targetRange":$range,"targetSelectionRange":$range,""" +
            """"originSelectionRange":$range}"""

    // ---- initialize (result) — ServerCapabilities provider branches ----

    @Test
    fun `initialize result with boolean providers`() {
        val r = roundTrip(
            InitializeResult.serializer(),
            """{
                "capabilities": {
                    "hoverProvider": true,
                    "definitionProvider": true,
                    "declarationProvider": false,
                    "renameProvider": true,
                    "textDocumentSync": 2
                },
                "serverInfo": {"name": "kotlin-lsp", "version": "0.1.0"}
            }"""
        )
        assertIs<BooleanOr.BooleanValue>(r.capabilities.hoverProvider)
        assertEquals("kotlin-lsp", r.serverInfo?.name)
    }

    @Test
    fun `initialize result with options providers`() {
        val r = roundTrip(
            InitializeResult.serializer(),
            """{
                "capabilities": {
                    "hoverProvider": {"workDoneProgress": true},
                    "completionProvider": {"triggerCharacters": ["."], "resolveProvider": true},
                    "signatureHelpProvider": {"triggerCharacters": ["(", ","]},
                    "renameProvider": {"prepareProvider": true},
                    "semanticTokensProvider": {
                        "legend": {"tokenTypes": ["keyword"], "tokenModifiers": ["static"]},
                        "full": true
                    },
                    "textDocumentSync": {"openClose": true, "change": 2}
                }
            }"""
        )
        assertIs<BooleanOr.Value<HoverOptions>>(r.capabilities.hoverProvider)
        assertIs<TextDocumentSyncOptions>(r.capabilities.textDocumentSync)
    }

    // ---- textDocument/hover (result) — every HoverContents branch ----

    @Test
    fun `hover result markup content`() {
        val r = roundTrip(
            Hover.serializer(),
            """{"contents": {"kind": "markdown", "value": "**doc**"}, "range": $range}"""
        )
        assertIs<HoverContents.MarkupContentValue>(r.contents)
    }

    @Test
    fun `hover result marked string`() {
        val r = roundTrip(
            Hover.serializer(),
            """{"contents": "a plain hint"}"""
        )
        val c = assertIs<HoverContents.MarkedStringValue>(r.contents)
        assertIs<StringOr.StringValue>(c.value)
    }

    @Test
    fun `hover result marked string code object`() {
        val r = roundTrip(
            Hover.serializer(),
            """{"contents": {"language": "kotlin", "value": "val x = 1"}}"""
        )
        val c = assertIs<HoverContents.MarkedStringValue>(r.contents)
        assertIs<StringOr.Value<MarkedStringObject>>(c.value)
    }

    @Test
    fun `hover result marked string array`() {
        val r = roundTrip(
            Hover.serializer(),
            """{"contents": ["one", {"language": "kotlin", "value": "two"}]}"""
        )
        val c = assertIs<HoverContents.MarkedStringArray>(r.contents)
        assertEquals(2, c.value.size)
    }

    // ---- definition (result) — single / array / link-array ----

    @Test
    fun `definition result single location`() {
        val r = roundTripPoly(
            TextDocumentDefinitionResultSerializer,
            TextDocumentDefinitionResultSerializer,
            location
        )
        val b = assertIs<TextDocumentDefinitionResult.DefinitionValue>(r)
        assertIs<SingleOrArray.Single<Location>>(b.value)
    }

    @Test
    fun `definition result location array`() {
        val r = roundTripPoly(
            TextDocumentDefinitionResultSerializer,
            TextDocumentDefinitionResultSerializer,
            "[$location,$location]"
        )
        val b = assertIs<TextDocumentDefinitionResult.DefinitionValue>(r)
        assertIs<SingleOrArray.Multiple<Location>>(b.value)
    }

    @Test
    fun `definition result location-link array`() {
        val r = roundTripPoly(
            TextDocumentDefinitionResultSerializer,
            TextDocumentDefinitionResultSerializer,
            "[$locationLink]"
        )
        assertIs<TextDocumentDefinitionResult.DefinitionLinkArray>(r)
    }

    // ---- declaration (result) — single / array / link-array ----

    @Test
    fun `declaration result single location`() {
        val r = roundTripPoly(
            TextDocumentDeclarationResultSerializer,
            TextDocumentDeclarationResultSerializer,
            location
        )
        assertIs<TextDocumentDeclarationResult.DeclarationValue>(r)
    }

    @Test
    fun `declaration result location array`() {
        val r = roundTripPoly(
            TextDocumentDeclarationResultSerializer,
            TextDocumentDeclarationResultSerializer,
            "[$location]"
        )
        assertIs<TextDocumentDeclarationResult.DeclarationValue>(r)
    }

    @Test
    fun `declaration result location-link array`() {
        val r = roundTripPoly(
            TextDocumentDeclarationResultSerializer,
            TextDocumentDeclarationResultSerializer,
            "[$locationLink]"
        )
        assertIs<TextDocumentDeclarationResult.DeclarationLinkArray>(r)
    }

    // ---- typeDefinition (result) — single / array / link-array ----

    @Test
    fun `typeDefinition result single location`() {
        val r = roundTripPoly(
            TextDocumentTypeDefinitionResultSerializer,
            TextDocumentTypeDefinitionResultSerializer,
            location
        )
        assertIs<TextDocumentTypeDefinitionResult.DefinitionValue>(r)
    }

    @Test
    fun `typeDefinition result location array`() {
        val r = roundTripPoly(
            TextDocumentTypeDefinitionResultSerializer,
            TextDocumentTypeDefinitionResultSerializer,
            "[$location]"
        )
        assertIs<TextDocumentTypeDefinitionResult.DefinitionValue>(r)
    }

    @Test
    fun `typeDefinition result location-link array`() {
        val r = roundTripPoly(
            TextDocumentTypeDefinitionResultSerializer,
            TextDocumentTypeDefinitionResultSerializer,
            "[$locationLink]"
        )
        assertIs<TextDocumentTypeDefinitionResult.DefinitionLinkArray>(r)
    }

    // ---- implementation (result) — single / array / link-array ----

    @Test
    fun `implementation result single location`() {
        val r = roundTripPoly(
            TextDocumentImplementationResultSerializer,
            TextDocumentImplementationResultSerializer,
            location
        )
        assertIs<TextDocumentImplementationResult.DefinitionValue>(r)
    }

    @Test
    fun `implementation result location array`() {
        val r = roundTripPoly(
            TextDocumentImplementationResultSerializer,
            TextDocumentImplementationResultSerializer,
            "[$location]"
        )
        assertIs<TextDocumentImplementationResult.DefinitionValue>(r)
    }

    @Test
    fun `implementation result location-link array`() {
        val r = roundTripPoly(
            TextDocumentImplementationResultSerializer,
            TextDocumentImplementationResultSerializer,
            "[$locationLink]"
        )
        assertIs<TextDocumentImplementationResult.DefinitionLinkArray>(r)
    }

    // ---- references (result) — Location[] ----

    @Test
    fun `references result location array`() {
        val r = roundTrip(
            kotlinx.serialization.builtins.ListSerializer(Location.serializer()),
            "[$location,$location]"
        )
        assertEquals(2, r.size)
    }

    // ---- completion (result) — CompletionList and CompletionItem[] ----

    @Test
    fun `completion result completion list`() {
        val r = roundTripPoly(
            TextDocumentCompletionResultSerializer,
            TextDocumentCompletionResultSerializer,
            """{
                "isIncomplete": false,
                "itemDefaults": {"insertTextFormat": 2},
                "items": [
                    {"label": "foo", "kind": 3, "insertText": "foo()"},
                    {"label": "bar", "kind": 6}
                ]
            }"""
        )
        val b = assertIs<TextDocumentCompletionResult.CompletionListValue>(r)
        assertEquals(2, b.value.items.size)
    }

    @Test
    fun `completion result completion item array`() {
        val r = roundTripPoly(
            TextDocumentCompletionResultSerializer,
            TextDocumentCompletionResultSerializer,
            """[{"label": "foo"}, {"label": "bar", "documentation": "text"}]"""
        )
        val b = assertIs<TextDocumentCompletionResult.CompletionItemArray>(r)
        assertEquals(2, b.value.size)
    }

    // ---- documentSymbol (result) — DocumentSymbol[] and SymbolInformation[] ----

    @Test
    fun `documentSymbol result documentSymbol array`() {
        val r = roundTripPoly(
            TextDocumentDocumentSymbolResultSerializer,
            TextDocumentDocumentSymbolResultSerializer,
            """[{
                "name": "MyClass",
                "kind": 5,
                "range": $range,
                "selectionRange": $range,
                "children": [
                    {"name": "method", "kind": 6, "range": $range, "selectionRange": $range}
                ]
            }]"""
        )
        assertIs<TextDocumentDocumentSymbolResult.DocumentSymbolArray>(r)
    }

    @Test
    fun `documentSymbol result symbolInformation array`() {
        val r = roundTripPoly(
            TextDocumentDocumentSymbolResultSerializer,
            TextDocumentDocumentSymbolResultSerializer,
            """[{"name": "MyClass", "kind": 5, "location": $location, "containerName": "pkg"}]"""
        )
        assertIs<TextDocumentDocumentSymbolResult.SymbolInformationArray>(r)
    }

    // ---- signatureHelp (result) ----

    @Test
    fun `signatureHelp result`() {
        val r = roundTrip(
            SignatureHelp.serializer(),
            """{
                "signatures": [
                    {
                        "label": "fun foo(a: Int, b: String)",
                        "documentation": {"kind": "markdown", "value": "docs"},
                        "parameters": [
                            {"label": "a: Int"},
                            {"label": [13, 22]}
                        ],
                        "activeParameter": 0
                    }
                ],
                "activeSignature": 0,
                "activeParameter": 0
            }"""
        )
        assertEquals(1, r.signatures.size)
        assertIs<StringOr.StringValue>(r.signatures.single().parameters!![0].label)
        assertIs<StringOr.Value<List<UInt>>>(r.signatures.single().parameters!![1].label)
    }

    // ---- rename (result: WorkspaceEdit) + prepareRename (result branches) ----

    @Test
    fun `rename result workspace edit with changes`() {
        val r = roundTrip(
            WorkspaceEdit.serializer(),
            """{
                "changes": {
                    "$uri": [{"range": $range, "newText": "renamed"}]
                }
            }"""
        )
        assertEquals(1, r.changes?.size)
    }

    @Test
    fun `rename result workspace edit with documentChanges`() {
        val r = roundTrip(
            WorkspaceEdit.serializer(),
            """{
                "documentChanges": [
                    {
                        "textDocument": {"uri": "$uri", "version": 1},
                        "edits": [{"range": $range, "newText": "x"}]
                    },
                    {"kind": "create", "uri": "file:///project/src/New.kt"},
                    {"kind": "rename", "oldUri": "file:///a.kt", "newUri": "file:///b.kt"},
                    {"kind": "delete", "uri": "file:///gone.kt"}
                ]
            }"""
        )
        val changes = r.documentChanges!!
        assertEquals(4, changes.size)
        assertIs<TextDocumentEdit>(changes[0])
        assertIs<CreateFile>(changes[1])
        assertIs<RenameFile>(changes[2])
        assertIs<DeleteFile>(changes[3])
    }

    @Test
    fun `prepareRename result range branch`() {
        val r = roundTripPoly(
            PrepareRenameResultSerializer,
            PrepareRenameResultSerializer,
            range
        )
        assertIs<Range>(r)
    }

    @Test
    fun `prepareRename result range with placeholder branch`() {
        val r = roundTripPoly(
            PrepareRenameResultSerializer,
            PrepareRenameResultSerializer,
            """{"range": $range, "placeholder": "name"}"""
        )
        assertIs<PrepareRenameResultRange>(r)
    }

    @Test
    fun `prepareRename result default behavior branch`() {
        val r = roundTripPoly(
            PrepareRenameResultSerializer,
            PrepareRenameResultSerializer,
            """{"defaultBehavior": true}"""
        )
        assertIs<PrepareRenameResultDefaultBehavior>(r)
    }

    // ---- formatting / rangeFormatting (result: TextEdit[]) ----

    @Test
    fun `formatting result text edit array`() {
        val r = roundTrip(
            kotlinx.serialization.builtins.ListSerializer(TextEdit.serializer()),
            """[{"range": $range, "newText": "    formatted"}]"""
        )
        assertEquals(1, r.size)
    }

    // ---- codeAction (result: (Command | CodeAction)[]) — both branches ----

    @Test
    fun `codeAction result command branch`() {
        val r = roundTripPoly(
            TextDocumentCodeActionResultSerializer,
            TextDocumentCodeActionResultSerializer,
            """{"title": "Run", "command": "editor.run", "arguments": [1, "two"]}"""
        )
        assertIs<Command>(r)
    }

    @Test
    fun `codeAction result codeAction branch`() {
        val r = roundTripPoly(
            TextDocumentCodeActionResultSerializer,
            TextDocumentCodeActionResultSerializer,
            """{
                "title": "Add import",
                "kind": "quickfix",
                "diagnostics": [{"range": $range, "message": "unresolved"}],
                "isPreferred": true,
                "edit": {"changes": {"$uri": [{"range": $range, "newText": "import x"}]}}
            }"""
        )
        assertIs<CodeAction>(r)
    }

    // ---- codeLens (result: CodeLens[]) ----

    @Test
    fun `codeLens result array`() {
        val r = roundTrip(
            kotlinx.serialization.builtins.ListSerializer(CodeLens.serializer()),
            """[{"range": $range, "command": {"title": "2 refs", "command": "showRefs"}}]"""
        )
        assertEquals(1, r.size)
    }

    // ---- foldingRange (result: FoldingRange[]) ----

    @Test
    fun `foldingRange result array`() {
        val r = roundTrip(
            kotlinx.serialization.builtins.ListSerializer(FoldingRange.serializer()),
            """[{"startLine": 1, "endLine": 9, "kind": "region"}]"""
        )
        assertEquals(1, r.size)
    }

    // ---- semanticTokens/full (result: SemanticTokens) + full/delta (both branches) ----

    @Test
    fun `semanticTokens full result`() {
        val r = roundTrip(
            SemanticTokens.serializer(),
            """{"resultId": "1", "data": [0, 0, 5, 0, 0, 1, 4, 3, 1, 0]}"""
        )
        assertEquals(10, r.data.size)
    }

    @Test
    fun `semanticTokens full delta result tokens branch`() {
        val r = roundTripPoly(
            TextDocumentSemanticTokensFullDeltaResultSerializer,
            TextDocumentSemanticTokensFullDeltaResultSerializer,
            """{"resultId": "2", "data": [0, 0, 5, 0, 0]}"""
        )
        assertIs<SemanticTokens>(r)
    }

    @Test
    fun `semanticTokens full delta result delta branch`() {
        val r = roundTripPoly(
            TextDocumentSemanticTokensFullDeltaResultSerializer,
            TextDocumentSemanticTokensFullDeltaResultSerializer,
            """{"resultId": "3", "edits": [{"start": 0, "deleteCount": 5, "data": [1, 2, 3, 4, 5]}]}"""
        )
        assertIs<SemanticTokensDelta>(r)
    }

    // ---- inlayHint (result: InlayHint[]) — both label branches ----

    @Test
    fun `inlayHint result string label`() {
        val r = roundTrip(
            kotlinx.serialization.builtins.ListSerializer(InlayHint.serializer()),
            """[{"position": {"line": 3, "character": 2}, "label": ": Int", "kind": 1}]"""
        )
        assertIs<StringOr.StringValue>(r.single().label)
    }

    @Test
    fun `inlayHint result label-parts label`() {
        val r = roundTrip(
            kotlinx.serialization.builtins.ListSerializer(InlayHint.serializer()),
            """[{
                "position": {"line": 3, "character": 2},
                "label": [{"value": ": "}, {"value": "Int", "tooltip": "kotlin.Int"}],
                "paddingLeft": true
            }]"""
        )
        assertIs<StringOr.Value<List<InlayHintLabelPart>>>(r.single().label)
    }

    // ---- documentHighlight (result: DocumentHighlight[]) ----

    @Test
    fun `documentHighlight result array`() {
        val r = roundTrip(
            kotlinx.serialization.builtins.ListSerializer(DocumentHighlight.serializer()),
            """[{"range": $range, "kind": 2}]"""
        )
        assertEquals(1, r.size)
    }

    // ---- workspace/symbol (result stays JsonElement) ----

    @Test
    fun `workspaceSymbol result as JsonElement`() {
        val r = roundTrip(
            JsonElement.serializer(),
            """[{"name": "Foo", "kind": 5, "location": {"uri": "$uri"}}]"""
        )
        assertIs<kotlinx.serialization.json.JsonArray>(r)
        assertEquals(1, r.size)
    }
}
