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
 * Deserialization + round-trip tests covering the previously zero-coverage
 * union branches the wire-branch tracker (#74) flagged. Each test uses a JSON
 * payload copied VERBATIM from an upstream LSP source; the citation comment on
 * each test names `repo:path#L<lines>`.
 *
 * The same payloads are also exercised through the wire-branch recorder via
 * `lsp-ksrpc/src/jvmTest/.../coverage/UpstreamBranchCoverageTest.kt`. This
 * JVM-only copy pulls each serializer's polymorphic-dispatch branch through
 * the :lsp Kover gate — Kover instruments JVM compilation only, so there is no
 * gain from running these on native/wasm/JS, and some test method names below
 * contain characters (e.g. commas) that are illegal in native target binaries.
 */
class UpstreamUnionBranchExpansionTest {

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

    // -------------------------------------------------------------------------
    // WorkspaceEditDocumentChanges family — resource changes from the spec.
    // -------------------------------------------------------------------------

    // microsoft/language-server-protocol:_specifications/lsp/3.17/types/resourceChanges.md#L28-L55
    @Test
    fun `CreateFile branch of WorkspaceEditDocumentChanges`() {
        val result = roundTrip<WorkspaceEditDocumentChanges>(
            """{
                "kind": "create",
                "uri": "file:///new.txt",
                "options": {"overwrite": true, "ignoreIfExists": false}
            }"""
        )
        assertIs<CreateFile>(result)
        assertEquals("file:///new.txt", result.uri)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/types/resourceChanges.md#L78-L110
    @Test
    fun `RenameFile branch of WorkspaceEditDocumentChanges`() {
        val result = roundTrip<WorkspaceEditDocumentChanges>(
            """{
                "kind": "rename",
                "oldUri": "file:///old.txt",
                "newUri": "file:///new.txt",
                "options": {"overwrite": true, "ignoreIfExists": false}
            }"""
        )
        assertIs<RenameFile>(result)
        assertEquals("file:///old.txt", result.oldUri)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/types/resourceChanges.md#L133-L160
    @Test
    fun `DeleteFile branch of WorkspaceEditDocumentChanges`() {
        val result = roundTrip<WorkspaceEditDocumentChanges>(
            """{
                "kind": "delete",
                "uri": "file:///gone.txt",
                "options": {"recursive": true, "ignoreIfNotExists": true}
            }"""
        )
        assertIs<DeleteFile>(result)
        assertEquals("file:///gone.txt", result.uri)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/types/textDocumentEdit.md#L7-L22
    @Test
    fun `TextDocumentEdit branch of WorkspaceEditDocumentChanges`() {
        val result = roundTrip<WorkspaceEditDocumentChanges>(
            """{
                "textDocument": {"uri": "file:///changed.txt", "version": 7},
                "edits": [
                    {
                        "range": {"start": {"line": 0, "character": 0}, "end": {"line": 0, "character": 5}},
                        "newText": "hello"
                    }
                ]
            }"""
        )
        assertIs<TextDocumentEdit>(result)
        assertEquals("file:///changed.txt", result.textDocument.uri)
    }

    // -------------------------------------------------------------------------
    // TextDocumentEditEdits.AnnotatedTextEdit branch.
    // -------------------------------------------------------------------------

    // microsoft/language-server-protocol:_specifications/lsp/3.17/types/textEdit.md#L67-L81
    @Test
    fun `AnnotatedTextEdit branch of TextDocumentEditEdits`() {
        val result = roundTrip<TextDocumentEditEdits>(
            """{
                "range": {"start": {"line": 1, "character": 0}, "end": {"line": 1, "character": 4}},
                "newText": "test",
                "annotationId": "rename-foo"
            }"""
        )
        assertIs<AnnotatedTextEdit>(result)
        assertEquals("rename-foo", result.annotationId)
    }

    // -------------------------------------------------------------------------
    // TextDocumentContentChangeEvent.Range branch — incremental edits.
    // -------------------------------------------------------------------------

    // microsoft/language-server-protocol:_specifications/lsp/3.17/textDocument/didChange.md#L75-L103
    @Test
    fun `TextDocumentContentChangeEventRange branch from didChange spec`() {
        val result = roundTrip<TextDocumentContentChangeEvent>(
            """{
                "range": {
                    "start": {"line": 0, "character": 0},
                    "end": {"line": 0, "character": 5}
                },
                "rangeLength": 5,
                "text": "world"
            }"""
        )
        assertIs<TextDocumentContentChangeEventRange>(result)
        assertEquals(5u, result.rangeLength)
    }

    // -------------------------------------------------------------------------
    // TextDocumentFilter — scheme- and pattern-keyed branches.
    // -------------------------------------------------------------------------

    // microsoft/language-server-protocol:_specifications/lsp/3.17/types/documentFilter.md#L17-L19
    @Test
    fun `TextDocumentFilterScheme branch`() {
        val result = roundTrip<TextDocumentFilter>(
            """{"scheme": "untitled"}"""
        )
        assertIs<TextDocumentFilterScheme>(result)
        assertEquals("untitled", result.scheme)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/types/documentFilter.md#L22-L36
    @Test
    fun `TextDocumentFilterPattern branch`() {
        val result = roundTrip<TextDocumentFilter>(
            """{"pattern": "**/package.json"}"""
        )
        assertIs<TextDocumentFilterPattern>(result)
        assertEquals("**/package.json", result.pattern)
    }

    // -------------------------------------------------------------------------
    // NotebookDocumentFilter — branches from notebook.md.
    // -------------------------------------------------------------------------

    // microsoft/language-server-protocol:_specifications/lsp/3.17/notebookDocument/notebook.md#L170-L178
    @Test
    fun `NotebookDocumentFilterNotebookType branch`() {
        val result = roundTrip<NotebookDocumentFilter>(
            """{"notebookType": "jupyter-notebook", "scheme": "file"}"""
        )
        assertIs<NotebookDocumentFilterNotebookType>(result)
        assertEquals("jupyter-notebook", result.notebookType)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/notebookDocument/notebook.md#L179-L188
    @Test
    fun `NotebookDocumentFilterScheme branch`() {
        val result = roundTrip<NotebookDocumentFilter>(
            """{"scheme": "untitled"}"""
        )
        assertIs<NotebookDocumentFilterScheme>(result)
        assertEquals("untitled", result.scheme)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/notebookDocument/notebook.md#L188-L197
    @Test
    fun `NotebookDocumentFilterPattern branch`() {
        val result = roundTrip<NotebookDocumentFilter>(
            """{"pattern": "**/books1/**"}"""
        )
        assertIs<NotebookDocumentFilterPattern>(result)
        assertEquals("**/books1/**", result.pattern)
    }

    // -------------------------------------------------------------------------
    // NotebookDocumentSyncOptionsNotebookSelector — Cells branch.
    // -------------------------------------------------------------------------

    // microsoft/language-server-protocol:_specifications/lsp/3.17/notebookDocument/notebook.md#L330-L342
    @Test
    fun `NotebookDocumentSyncOptionsNotebookSelectorCells branch`() {
        val result = roundTrip<NotebookDocumentSyncOptions>(
            """{
                "notebookSelector": [
                    {"cells": [{"language": "python"}]}
                ]
            }"""
        )
        val first = result.notebookSelector[0]
        assertIs<NotebookDocumentSyncOptionsNotebookSelectorCells>(first)
    }

    // microsoft/vscode-languageserver-node:client-node-tests/src/servers/fullNotebookServer.ts#L41-L46
    @Test
    fun `NotebookDocumentSyncRegistrationOptionsNotebookSelectorNotebook branch`() {
        val result = roundTrip<NotebookDocumentSyncRegistrationOptions>(
            """{
                "notebookSelector": [
                    {
                        "notebook": {"notebookType": "jupyter-notebook"},
                        "cells": [{"language": "python"}]
                    }
                ],
                "id": "notebook-sync-reg"
            }"""
        )
        val first = result.notebookSelector[0]
        assertIs<NotebookDocumentSyncRegistrationOptionsNotebookSelectorNotebook>(first)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/notebookDocument/notebook.md#L330-L342
    @Test
    fun `NotebookDocumentSyncRegistrationOptionsNotebookSelectorCells branch`() {
        val result = roundTrip<NotebookDocumentSyncRegistrationOptions>(
            """{
                "notebookSelector": [
                    {"cells": [{"language": "python"}]}
                ],
                "id": "notebook-cell-reg"
            }"""
        )
        val first = result.notebookSelector[0]
        assertIs<NotebookDocumentSyncRegistrationOptionsNotebookSelectorCells>(first)
    }

    // -------------------------------------------------------------------------
    // ServerCapabilities*ProviderOptions families — Options vs Registration.
    // -------------------------------------------------------------------------

    // microsoft/vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L72-L74
    @Test
    fun `BooleanOr Value branch from testServer codeActionProvider object form`() {
        val caps = roundTrip<ServerCapabilities>(
            """{"codeActionProvider": {"resolveProvider": true}}"""
        )
        val provider = caps.codeActionProvider
        assertIs<BooleanOr.Value<*>>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/callHierarchy.md#L34-L39
    @Test
    fun `CallHierarchyOptions branch`() {
        val provider = roundTrip<ServerCapabilitiesCallHierarchyProviderOptions>(
            """{"workDoneProgress": true}"""
        )
        assertIs<CallHierarchyOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/callHierarchy.md#L43-L50
    @Test
    fun `CallHierarchyRegistrationOptions branch`() {
        val provider = roundTrip<ServerCapabilitiesCallHierarchyProviderOptions>(
            """{
                "documentSelector": [{"language": "typescript"}],
                "id": "call-hierarchy-reg"
            }"""
        )
        assertIs<CallHierarchyRegistrationOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/documentColor.md#L34-L36
    @Test
    fun `DocumentColorOptions branch`() {
        val provider = roundTrip<ServerCapabilitiesColorProviderOptions>(
            """{"workDoneProgress": false}"""
        )
        assertIs<DocumentColorOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/documentColor.md#L40-L43
    @Test
    fun `DocumentColorRegistrationOptions branch`() {
        val provider = roundTrip<ServerCapabilitiesColorProviderOptions>(
            """{
                "documentSelector": [{"language": "css"}],
                "id": "color-reg"
            }"""
        )
        assertIs<DocumentColorRegistrationOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/declaration.md#L33-L36
    @Test
    fun `DeclarationOptions branch`() {
        val provider = roundTrip<ServerCapabilitiesDeclarationProviderOptions>(
            """{"workDoneProgress": true}"""
        )
        assertIs<DeclarationOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/declaration.md#L40-L44
    @Test
    fun `DeclarationRegistrationOptions branch`() {
        val provider = roundTrip<ServerCapabilitiesDeclarationProviderOptions>(
            """{
                "documentSelector": [{"language": "typescript"}],
                "id": "decl-reg"
            }"""
        )
        assertIs<DeclarationRegistrationOptions>(provider)
    }

    // microsoft/vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L152-L156
    @Test
    fun `DiagnosticOptions branch from testServer diagnosticProvider`() {
        val provider = roundTrip<ServerCapabilitiesDiagnosticProvider>(
            """{
                "identifier": "da348dc5-c30a-4515-9d98-31ff3be38d14",
                "interFileDependencies": true,
                "workspaceDiagnostics": true
            }"""
        )
        assertIs<DiagnosticOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/pullDiagnostics.md#L91-L121
    @Test
    fun `DiagnosticRegistrationOptions branch from pullDiagnostics spec`() {
        val provider = roundTrip<ServerCapabilitiesDiagnosticProvider>(
            """{
                "documentSelector": [{"language": "typescript"}],
                "identifier": "diagnostic-provider",
                "interFileDependencies": false,
                "workspaceDiagnostics": false,
                "id": "diag-reg"
            }"""
        )
        assertIs<DiagnosticRegistrationOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/foldingRange.md#L34-L37
    @Test
    fun `FoldingRangeOptions branch`() {
        val provider = roundTrip<ServerCapabilitiesFoldingRangeProviderOptions>(
            """{"workDoneProgress": true}"""
        )
        assertIs<FoldingRangeOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/foldingRange.md#L41-L45
    @Test
    fun `FoldingRangeRegistrationOptions branch`() {
        val provider = roundTrip<ServerCapabilitiesFoldingRangeProviderOptions>(
            """{
                "documentSelector": [{"language": "typescript"}],
                "id": "folding-reg"
            }"""
        )
        assertIs<FoldingRangeRegistrationOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/implementation.md#L33-L36
    @Test
    fun `ImplementationOptions branch`() {
        val provider = roundTrip<ServerCapabilitiesImplementationProviderOptions>(
            """{"workDoneProgress": true}"""
        )
        assertIs<ImplementationOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/implementation.md#L40-L44
    @Test
    fun `ImplementationRegistrationOptions branch`() {
        val provider = roundTrip<ServerCapabilitiesImplementationProviderOptions>(
            """{
                "documentSelector": [{"language": "typescript"}],
                "id": "impl-reg"
            }"""
        )
        assertIs<ImplementationRegistrationOptions>(provider)
    }

    // microsoft/vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L95-L97
    @Test
    fun `InlayHintOptions branch from testServer inlayHintProvider`() {
        val provider = roundTrip<ServerCapabilitiesInlayHintProviderOptions>(
            """{"resolveProvider": true}"""
        )
        assertIs<InlayHintOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/inlayHint.md#L42-L51
    @Test
    fun `InlayHintRegistrationOptions branch`() {
        val provider = roundTrip<ServerCapabilitiesInlayHintProviderOptions>(
            """{
                "documentSelector": [{"language": "typescript"}],
                "id": "inlay-reg",
                "resolveProvider": false
            }"""
        )
        assertIs<InlayHintRegistrationOptions>(provider)
    }

    // microsoft/vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L94-L94
    @Test
    fun `InlineValueOptions branch from testServer inlineValueProvider`() {
        val provider = roundTrip<ServerCapabilitiesInlineValueProviderOptions>(
            """{"workDoneProgress": false}"""
        )
        assertIs<InlineValueOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/inlineValue.md#L48-L52
    @Test
    fun `InlineValueRegistrationOptions branch`() {
        val provider = roundTrip<ServerCapabilitiesInlineValueProviderOptions>(
            """{
                "documentSelector": [{"language": "typescript"}],
                "id": "inline-value-reg"
            }"""
        )
        assertIs<InlineValueRegistrationOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/linkedEditingRange.md#L33-L36
    @Test
    fun `LinkedEditingRangeOptions branch`() {
        val provider = roundTrip<ServerCapabilitiesLinkedEditingRangeProviderOptions>(
            """{"workDoneProgress": true}"""
        )
        assertIs<LinkedEditingRangeOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/linkedEditingRange.md#L40-L44
    @Test
    fun `LinkedEditingRangeRegistrationOptions branch`() {
        val provider = roundTrip<ServerCapabilitiesLinkedEditingRangeProviderOptions>(
            """{
                "documentSelector": [{"language": "typescript"}],
                "id": "linked-edit-reg"
            }"""
        )
        assertIs<LinkedEditingRangeRegistrationOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/moniker.md#L33-L36
    @Test
    fun `MonikerOptions branch`() {
        val provider = roundTrip<ServerCapabilitiesMonikerProviderOptions>(
            """{"workDoneProgress": true}"""
        )
        assertIs<MonikerOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/moniker.md#L42-L46
    @Test
    fun `MonikerRegistrationOptions branch`() {
        val provider = roundTrip<ServerCapabilitiesMonikerProviderOptions>(
            """{"documentSelector": [{"language": "typescript"}]}"""
        )
        assertIs<MonikerRegistrationOptions>(provider)
    }

    // microsoft/vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L161-L166
    @Test
    fun `ServerCapabilities NotebookDocumentSyncOptions branch`() {
        val provider = roundTrip<ServerCapabilitiesNotebookDocumentSync>(
            """{
                "notebookSelector": [
                    {
                        "notebook": {"notebookType": "jupyter-notebook"},
                        "cells": [{"language": "python"}]
                    }
                ]
            }"""
        )
        assertIs<NotebookDocumentSyncOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/notebookDocument/notebook.md#L356-L364
    @Test
    fun `ServerCapabilities NotebookDocumentSyncRegistrationOptions branch`() {
        val provider = roundTrip<ServerCapabilitiesNotebookDocumentSync>(
            """{
                "id": "notebook-sync-static",
                "notebookSelector": [
                    {
                        "notebook": {"notebookType": "jupyter-notebook"},
                        "cells": [{"language": "python"}]
                    }
                ]
            }"""
        )
        assertIs<NotebookDocumentSyncRegistrationOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/selectionRange.md#L33-L36
    @Test
    fun `SelectionRangeOptions branch`() {
        val provider = roundTrip<ServerCapabilitiesSelectionRangeProviderOptions>(
            """{"workDoneProgress": true}"""
        )
        assertIs<SelectionRangeOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/selectionRange.md#L40-L44
    @Test
    fun `SelectionRangeRegistrationOptions branch`() {
        val provider = roundTrip<ServerCapabilitiesSelectionRangeProviderOptions>(
            """{
                "documentSelector": [{"language": "typescript"}],
                "id": "selection-range-reg"
            }"""
        )
        assertIs<SelectionRangeRegistrationOptions>(provider)
    }

    // microsoft/vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L100-L109
    @Test
    fun `SemanticTokensOptions branch from testServer semanticTokensProvider`() {
        val provider = roundTrip<ServerCapabilitiesSemanticTokensProvider>(
            """{
                "legend": {"tokenTypes": [], "tokenModifiers": []},
                "range": true,
                "full": {"delta": true}
            }"""
        )
        assertIs<SemanticTokensOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/semanticTokens.md#L312-L320
    @Test
    fun `SemanticTokensRegistrationOptions branch`() {
        val provider = roundTrip<ServerCapabilitiesSemanticTokensProvider>(
            """{
                "documentSelector": [{"language": "typescript"}],
                "legend": {"tokenTypes": ["keyword"], "tokenModifiers": []},
                "id": "semantic-tokens-reg"
            }"""
        )
        assertIs<SemanticTokensRegistrationOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/specification.md#L532-L550
    @Test
    fun `TextDocumentSyncOptions branch from spec textDocumentSync`() {
        val provider = roundTrip<ServerCapabilitiesTextDocumentSync>(
            """{"openClose": true, "change": 2}"""
        )
        assertIs<TextDocumentSyncOptions>(provider)
        assertEquals(TextDocumentSyncKind.INCREMENTAL, provider.change)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/typeDefinition.md#L33-L36
    @Test
    fun `TypeDefinitionOptions branch`() {
        val provider = roundTrip<ServerCapabilitiesTypeDefinitionProviderOptions>(
            """{"workDoneProgress": true}"""
        )
        assertIs<TypeDefinitionOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/typeDefinition.md#L40-L44
    @Test
    fun `TypeDefinitionRegistrationOptions branch`() {
        val provider = roundTrip<ServerCapabilitiesTypeDefinitionProviderOptions>(
            """{
                "documentSelector": [{"language": "typescript"}],
                "id": "type-def-reg"
            }"""
        )
        assertIs<TypeDefinitionRegistrationOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/typeHierarchy.md#L33-L36
    @Test
    fun `TypeHierarchyOptions branch`() {
        val provider = roundTrip<ServerCapabilitiesTypeHierarchyProviderOptions>(
            """{"workDoneProgress": true}"""
        )
        assertIs<TypeHierarchyOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/typeHierarchy.md#L40-L44
    @Test
    fun `TypeHierarchyRegistrationOptions branch`() {
        val provider = roundTrip<ServerCapabilitiesTypeHierarchyProviderOptions>(
            """{
                "documentSelector": [{"language": "typescript"}],
                "id": "type-hier-reg"
            }"""
        )
        assertIs<TypeHierarchyRegistrationOptions>(provider)
    }

    // -------------------------------------------------------------------------
    // CompletionListItemDefaultsEditRange.Insert and InlineCompletionItemArray.
    // -------------------------------------------------------------------------

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/completion.md#L240-L256
    @Test
    fun `CompletionListItemDefaultsEditRangeInsert branch`() {
        val result = roundTrip<CompletionList>(
            """{
                "isIncomplete": false,
                "itemDefaults": {
                    "editRange": {
                        "insert": {"start": {"line": 0, "character": 0}, "end": {"line": 0, "character": 4}},
                        "replace": {"start": {"line": 0, "character": 0}, "end": {"line": 0, "character": 6}}
                    }
                },
                "items": []
            }"""
        )
        val editRange = result.itemDefaults?.editRange
        assertIs<CompletionListItemDefaultsEditRangeInsert>(editRange)
    }

    // microsoft/vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L536-L540
    @Test
    fun `TextDocumentInlineCompletionResult InlineCompletionItemArray branch`() {
        val result = roundTrip<TextDocumentInlineCompletionResult>(
            """[
                {
                    "insertText": "text inline",
                    "filterText": "te",
                    "range": {"start": {"line": 1, "character": 2}, "end": {"line": 3, "character": 4}}
                }
            ]"""
        )
        assertIs<TextDocumentInlineCompletionResult.InlineCompletionItemArray>(result)
    }

    // -------------------------------------------------------------------------
    // Diagnostic pull related-documents map values.
    // -------------------------------------------------------------------------

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/pullDiagnostics.md#L293-L309
    @Test
    fun `DocumentDiagnosticReportPartialResult Full relatedDocuments`() {
        val payload = """{
            "relatedDocuments": {
                "file:///header.h": {
                    "kind": "full",
                    "resultId": "h-1",
                    "items": []
                }
            }
        }"""
        val result = roundTrip<DocumentDiagnosticReportPartialResult>(payload)
        val first = result.relatedDocuments.values.first()
        assertIs<FullDocumentDiagnosticReport>(first)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/pullDiagnostics.md#L293-L309
    @Test
    fun `DocumentDiagnosticReportPartialResult Unchanged relatedDocuments`() {
        val payload = """{
            "relatedDocuments": {
                "file:///header.h": {
                    "kind": "unchanged",
                    "resultId": "h-1"
                }
            }
        }"""
        val result = roundTrip<DocumentDiagnosticReportPartialResult>(payload)
        val first = result.relatedDocuments.values.first()
        assertIs<UnchangedDocumentDiagnosticReport>(first)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/pullDiagnostics.md#L242-L266
    @Test
    fun `RelatedFullDocumentDiagnostic with Full relatedDocuments`() {
        val report = roundTrip<RelatedFullDocumentDiagnosticReport>(
            """{
                "kind": "full",
                "resultId": "main-1",
                "items": [],
                "relatedDocuments": {
                    "file:///header.h": {
                        "kind": "full",
                        "items": []
                    }
                }
            }"""
        )
        val related = report.relatedDocuments?.values?.first()
        assertNotNull(related)
        assertIs<FullDocumentDiagnosticReport>(related)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/pullDiagnostics.md#L242-L266
    @Test
    fun `RelatedFullDocumentDiagnostic with Unchanged relatedDocuments`() {
        val report = roundTrip<RelatedFullDocumentDiagnosticReport>(
            """{
                "kind": "full",
                "resultId": "main-1",
                "items": [],
                "relatedDocuments": {
                    "file:///header.h": {
                        "kind": "unchanged",
                        "resultId": "h-1"
                    }
                }
            }"""
        )
        val related = report.relatedDocuments?.values?.first()
        assertNotNull(related)
        assertIs<UnchangedDocumentDiagnosticReport>(related)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/pullDiagnostics.md#L268-L292
    @Test
    fun `RelatedUnchangedDocumentDiagnostic with Full relatedDocuments`() {
        val report = roundTrip<RelatedUnchangedDocumentDiagnosticReport>(
            """{
                "kind": "unchanged",
                "resultId": "main-1",
                "relatedDocuments": {
                    "file:///header.h": {
                        "kind": "full",
                        "items": []
                    }
                }
            }"""
        )
        val related = report.relatedDocuments?.values?.first()
        assertNotNull(related)
        assertIs<FullDocumentDiagnosticReport>(related)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/pullDiagnostics.md#L268-L292
    @Test
    fun `RelatedUnchangedDocumentDiagnostic with Unchanged relatedDocuments`() {
        val report = roundTrip<RelatedUnchangedDocumentDiagnosticReport>(
            """{
                "kind": "unchanged",
                "resultId": "main-1",
                "relatedDocuments": {
                    "file:///header.h": {
                        "kind": "unchanged",
                        "resultId": "h-1"
                    }
                }
            }"""
        )
        val related = report.relatedDocuments?.values?.first()
        assertNotNull(related)
        assertIs<UnchangedDocumentDiagnosticReport>(related)
    }

    // -------------------------------------------------------------------------
    // WorkspaceDocumentDiagnosticReport.WorkspaceUnchangedDocumentDiagnosticReport.
    // -------------------------------------------------------------------------

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/pullDiagnostics.md#L423-L445
    @Test
    fun `WorkspaceUnchangedDocumentDiagnosticReport branch`() {
        val result = roundTrip<WorkspaceDocumentDiagnosticReport>(
            """{
                "kind": "unchanged",
                "resultId": "ws-1",
                "uri": "file:///main.kt",
                "version": 42
            }"""
        )
        assertIs<WorkspaceUnchangedDocumentDiagnosticReport>(result)
    }

    // -------------------------------------------------------------------------
    // PrepareRenameResult.PrepareRenameResultDefaultBehavior.
    // -------------------------------------------------------------------------

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/rename.md#L131-L131
    @Test
    fun `PrepareRenameResultDefaultBehavior branch`() {
        val result = roundTrip<PrepareRenameResult>(
            """{"defaultBehavior": true}"""
        )
        assertIs<PrepareRenameResultDefaultBehavior>(result)
    }

    // -------------------------------------------------------------------------
    // IntOrString.IntValue branch (progress token form).
    // -------------------------------------------------------------------------

    // microsoft/lsprotocol:tests/python/notifications/test_progress.py
    // Numeric progress tokens — exercises the IntValue branch via ProgressParams.
    @Test
    fun `IntOrString IntValue branch via progress token`() {
        val result = roundTrip<ProgressParams>(
            """{
                "token": 42,
                "value": {"kind": "begin", "title": "Indexing"}
            }"""
        )
        val token = result.token
        assertIs<IntOrString.IntValue>(token)
        assertEquals(42, token.value)
    }

    // -------------------------------------------------------------------------
    // Enum coverage — exercise the previously-unreferenced enum literal lookups.
    // -------------------------------------------------------------------------

    // microsoft/lsprotocol:tests/python/requests/test_initilize_request.py#L19-L23
    @Test
    fun `WorkspaceEditClientCapabilities resourceOperations and failureHandling`() {
        val result = roundTrip<WorkspaceEditClientCapabilities>(
            """{
                "documentChanges": true,
                "resourceOperations": ["create", "rename", "delete"],
                "failureHandling": "undo",
                "normalizesLineEndings": true,
                "changeAnnotationSupport": {"groupsOnLabel": false}
            }"""
        )
        assertEquals(
            listOf(
                ResourceOperationKind.CREATE,
                ResourceOperationKind.RENAME,
                ResourceOperationKind.DELETE
            ),
            result.resourceOperations
        )
        assertEquals(FailureHandlingKind.UNDO, result.failureHandling)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/types/workspaceEdit.md#L141-L142
    @Test
    fun `FailureHandlingKind abort, transactional and textOnlyTransactional`() {
        for ((value, expected) in listOf(
            "abort" to FailureHandlingKind.ABORT,
            "transactional" to FailureHandlingKind.TRANSACTIONAL,
            "textOnlyTransactional" to FailureHandlingKind.TEXT_ONLY_TRANSACTIONAL
        )) {
            val result = roundTrip<WorkspaceEditClientCapabilities>(
                """{"failureHandling": "$value"}"""
            )
            assertEquals(expected, result.failureHandling)
        }
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/moniker.md#L73-L102
    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/moniker.md#L107-L127
    @Test
    fun `Moniker covers UniquenessLevel and MonikerKind enums`() {
        for ((uniqueStr, expectedUnique) in listOf(
            "document" to UniquenessLevel.DOCUMENT,
            "project" to UniquenessLevel.PROJECT,
            "group" to UniquenessLevel.GROUP,
            "scheme" to UniquenessLevel.SCHEME,
            "global" to UniquenessLevel.GLOBAL
        )) {
            val result = roundTrip<Moniker>(
                """{
                    "scheme": "tsc",
                    "identifier": "id",
                    "unique": "$uniqueStr"
                }"""
            )
            assertEquals(expectedUnique, result.unique)
        }
        for ((kindStr, expectedKind) in listOf(
            "import" to MonikerKind.IMPORT,
            "export" to MonikerKind.EXPORT,
            "local" to MonikerKind.LOCAL
        )) {
            val result = roundTrip<Moniker>(
                """{
                    "scheme": "tsc",
                    "identifier": "id",
                    "unique": "document",
                    "kind": "$kindStr"
                }"""
            )
            assertEquals(expectedKind, result.kind)
        }
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/semanticTokens.md#L69-L74
    @Test
    fun `TokenFormat RELATIVE`() {
        val payload = """{
            "dynamicRegistration": true,
            "requests": {"range": true, "full": {"delta": true}},
            "tokenTypes": [],
            "tokenModifiers": [],
            "formats": ["relative"]
        }"""
        val result = roundTrip<SemanticTokensClientCapabilities>(payload)
        assertEquals(listOf(TokenFormat.RELATIVE), result.formats)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/pullDiagnostics.md#L171-L186
    @Test
    fun `DocumentDiagnosticReportKind enum FULL and UNCHANGED`() {
        assertEquals(
            DocumentDiagnosticReportKind.FULL,
            json.decodeFromString<DocumentDiagnosticReportKind>(""""full"""")
        )
        assertEquals(
            DocumentDiagnosticReportKind.UNCHANGED,
            json.decodeFromString<DocumentDiagnosticReportKind>(""""unchanged"""")
        )
    }
}
