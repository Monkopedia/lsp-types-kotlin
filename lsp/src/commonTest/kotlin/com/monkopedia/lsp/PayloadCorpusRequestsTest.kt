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
 * Portable per-method payload round-trip corpus — REQUEST params (#43).
 *
 * For each representative real-world LSP JSON payload we decode to the typed
 * value, assert the expected concrete type, re-encode, and assert that the
 * re-encoded JSON decodes back to a value equal to the first decode
 * (serialize -> deserialize -> serialize round-trip stability).
 *
 * Lives in commonTest so it runs on JVM, native, and wasmJs using only
 * commonTest-safe APIs.
 */
class PayloadCorpusRequestsTest {

    private val json = Json { ignoreUnknownKeys = true }

    /** Decode, assert concrete type, then assert decode == decode(encode(decode)). */
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
    private val docId = """{"uri":"file:///project/src/Main.kt"}"""
    private val pos = """{"line":3,"character":2}"""

    // ---- initialize (params) ----

    @Test
    fun `initialize params full`() {
        val p = roundTrip(
            InitializeParams.serializer(),
            """{
                "processId": 4242,
                "clientInfo": {"name": "vscode", "version": "1.89.0"},
                "locale": "en-us",
                "rootUri": "file:///project",
                "capabilities": {
                    "textDocument": {"hover": {"contentFormat": ["markdown", "plaintext"]}},
                    "workspace": {"workspaceFolders": true}
                },
                "trace": "verbose",
                "workspaceFolders": [
                    {"uri": "file:///project", "name": "project"}
                ]
            }"""
        )
        assertEquals(4242, p.processId)
        assertEquals("vscode", p.clientInfo?.name)
        assertEquals(1, p.workspaceFolders?.size)
    }

    @Test
    fun `initialize params null processId`() {
        val p = roundTrip(
            InitializeParams.serializer(),
            """{"processId": null, "rootUri": null, "capabilities": {}}"""
        )
        assertEquals(null, p.processId)
    }

    // ---- textDocument/hover (params) ----

    @Test
    fun `hover params`() {
        val p = roundTrip(
            HoverParams.serializer(),
            """{"textDocument": $docId, "position": $pos}"""
        )
        assertEquals(3u, p.position.line)
    }

    // ---- definition / declaration / typeDefinition / implementation (params) ----

    @Test
    fun `definition params`() {
        roundTrip(
            DefinitionParams.serializer(),
            """{"textDocument": $docId, "position": $pos, "workDoneToken": "wd-1"}"""
        )
    }

    @Test
    fun `declaration params`() {
        roundTrip(DeclarationParams.serializer(), """{"textDocument": $docId, "position": $pos}""")
    }

    @Test
    fun `typeDefinition params`() {
        roundTrip(
            TypeDefinitionParams.serializer(),
            """{"textDocument": $docId, "position": $pos}"""
        )
    }

    @Test
    fun `implementation params`() {
        roundTrip(
            ImplementationParams.serializer(),
            """{"textDocument": $docId, "position": $pos}"""
        )
    }

    // ---- references (params, with context) ----

    @Test
    fun `references params`() {
        val p = roundTrip(
            ReferenceParams.serializer(),
            """{
                "textDocument": $docId,
                "position": $pos,
                "context": {"includeDeclaration": true},
                "partialResultToken": 7
            }"""
        )
        assertEquals(true, p.context.includeDeclaration)
    }

    // ---- completion (params) + completionItem/resolve (params) ----

    @Test
    fun `completion params with context`() {
        val p = roundTrip(
            CompletionParams.serializer(),
            """{
                "textDocument": $docId,
                "position": $pos,
                "context": {"triggerKind": 2, "triggerCharacter": "."}
            }"""
        )
        assertEquals(".", p.context?.triggerCharacter)
    }

    @Test
    fun `completionItem resolve params`() {
        val item = roundTrip(
            CompletionItem.serializer(),
            """{
                "label": "println",
                "kind": 3,
                "detail": "fun println(message: Any?)",
                "documentation": {"kind": "markdown", "value": "Prints **message**."},
                "insertText": "println()",
                "data": {"id": 17}
            }"""
        )
        assertEquals("println", item.label)
        assertIs<StringOr.Value<MarkupContent>>(item.documentation)
    }

    // ---- documentSymbol (params) ----

    @Test
    fun `documentSymbol params`() {
        roundTrip(DocumentSymbolParams.serializer(), """{"textDocument": $docId}""")
    }

    // ---- signatureHelp (params, with context) ----

    @Test
    fun `signatureHelp params with context`() {
        val p = roundTrip(
            SignatureHelpParams.serializer(),
            """{
                "textDocument": $docId,
                "position": $pos,
                "context": {"triggerKind": 1, "isRetrigger": false}
            }"""
        )
        assertEquals(false, p.context?.isRetrigger)
    }

    // ---- rename (params) + prepareRename (params) ----

    @Test
    fun `rename params`() {
        val p = roundTrip(
            RenameParams.serializer(),
            """{"textDocument": $docId, "position": $pos, "newName": "renamed"}"""
        )
        assertEquals("renamed", p.newName)
    }

    @Test
    fun `prepareRename params`() {
        roundTrip(
            PrepareRenameParams.serializer(),
            """{"textDocument": $docId, "position": $pos}"""
        )
    }

    // ---- formatting / rangeFormatting (params) ----

    @Test
    fun `formatting params`() {
        val p = roundTrip(
            DocumentFormattingParams.serializer(),
            """{
                "textDocument": $docId,
                "options": {"tabSize": 4, "insertSpaces": true, "trimTrailingWhitespace": true}
            }"""
        )
        assertEquals(4u, p.options.tabSize)
    }

    @Test
    fun `rangeFormatting params`() {
        roundTrip(
            DocumentRangeFormattingParams.serializer(),
            """{
                "textDocument": $docId,
                "range": $range,
                "options": {"tabSize": 2, "insertSpaces": false}
            }"""
        )
    }

    // ---- codeAction (params, with context) ----

    @Test
    fun `codeAction params`() {
        val p = roundTrip(
            CodeActionParams.serializer(),
            """{
                "textDocument": $docId,
                "range": $range,
                "context": {
                    "diagnostics": [
                        {"range": $range, "message": "unused import", "severity": 2}
                    ],
                    "only": ["quickfix"],
                    "triggerKind": 1
                }
            }"""
        )
        assertEquals(1, p.context.diagnostics.size)
    }

    // ---- codeLens (params) ----

    @Test
    fun `codeLens params`() {
        roundTrip(CodeLensParams.serializer(), """{"textDocument": $docId}""")
    }

    // ---- foldingRange (params) ----

    @Test
    fun `foldingRange params`() {
        roundTrip(FoldingRangeParams.serializer(), """{"textDocument": $docId}""")
    }

    // ---- semanticTokens/full (params) ----

    @Test
    fun `semanticTokens full params`() {
        roundTrip(SemanticTokensParams.serializer(), """{"textDocument": $docId}""")
    }

    // ---- inlayHint (params) ----

    @Test
    fun `inlayHint params`() {
        roundTrip(InlayHintParams.serializer(), """{"textDocument": $docId, "range": $range}""")
    }

    // ---- documentHighlight (params) ----

    @Test
    fun `documentHighlight params`() {
        roundTrip(
            DocumentHighlightParams.serializer(),
            """{"textDocument": $docId, "position": $pos}"""
        )
    }

    // ---- workspace/symbol (params) ----

    @Test
    fun `workspaceSymbol params`() {
        val p = roundTrip(
            WorkspaceSymbolParams.serializer(),
            """{"query": "Foo", "partialResultToken": "wsp-1"}"""
        )
        assertEquals("Foo", p.query)
    }
}
