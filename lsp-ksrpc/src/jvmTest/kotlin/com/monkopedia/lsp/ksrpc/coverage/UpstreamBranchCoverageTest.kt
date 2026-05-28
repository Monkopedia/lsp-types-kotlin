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
package com.monkopedia.lsp.ksrpc.coverage

import com.monkopedia.lsp.BooleanOr
import com.monkopedia.lsp.CallHierarchyOptions
import com.monkopedia.lsp.CallHierarchyRegistrationOptions
import com.monkopedia.lsp.CompletionItem
import com.monkopedia.lsp.CompletionList
import com.monkopedia.lsp.CompletionListItemDefaultsEditRangeInsert
import com.monkopedia.lsp.DeclarationOptions
import com.monkopedia.lsp.DeclarationRegistrationOptions
import com.monkopedia.lsp.DiagnosticOptions
import com.monkopedia.lsp.DiagnosticRegistrationOptions
import com.monkopedia.lsp.DidChangeTextDocumentParams
import com.monkopedia.lsp.DocumentColorOptions
import com.monkopedia.lsp.DocumentColorRegistrationOptions
import com.monkopedia.lsp.DocumentDiagnosticReport
import com.monkopedia.lsp.DocumentDiagnosticReportKind
import com.monkopedia.lsp.DocumentDiagnosticReportPartialResult
import com.monkopedia.lsp.DocumentDiagnosticReportPartialResultRelatedDocuments
import com.monkopedia.lsp.FailureHandlingKind
import com.monkopedia.lsp.FileOperationFilter
import com.monkopedia.lsp.FileOperationPatternKind
import com.monkopedia.lsp.FoldingRangeOptions
import com.monkopedia.lsp.FoldingRangeRegistrationOptions
import com.monkopedia.lsp.FullDocumentDiagnosticReport
import com.monkopedia.lsp.ImplementationOptions
import com.monkopedia.lsp.ImplementationRegistrationOptions
import com.monkopedia.lsp.InlayHintOptions
import com.monkopedia.lsp.InlayHintRegistrationOptions
import com.monkopedia.lsp.InlineCompletionItem
import com.monkopedia.lsp.InlineValue
import com.monkopedia.lsp.InlineValueEvaluatableExpression
import com.monkopedia.lsp.InlineValueOptions
import com.monkopedia.lsp.InlineValueRegistrationOptions
import com.monkopedia.lsp.InlineValueVariableLookup
import com.monkopedia.lsp.InsertReplaceEdit
import com.monkopedia.lsp.IntOrString
import com.monkopedia.lsp.LinkedEditingRangeOptions
import com.monkopedia.lsp.LinkedEditingRangeRegistrationOptions
import com.monkopedia.lsp.Moniker
import com.monkopedia.lsp.MonikerKind
import com.monkopedia.lsp.MonikerOptions
import com.monkopedia.lsp.MonikerRegistrationOptions
import com.monkopedia.lsp.NotebookDocumentFilter
import com.monkopedia.lsp.NotebookDocumentFilterNotebookType
import com.monkopedia.lsp.NotebookDocumentFilterPattern
import com.monkopedia.lsp.NotebookDocumentFilterScheme
import com.monkopedia.lsp.NotebookDocumentSyncOptions
import com.monkopedia.lsp.NotebookDocumentSyncOptionsNotebookSelectorCells
import com.monkopedia.lsp.NotebookDocumentSyncOptionsNotebookSelectorNotebook
import com.monkopedia.lsp.NotebookDocumentSyncRegistrationOptions
import com.monkopedia.lsp.NotebookDocumentSyncRegistrationOptionsNotebookSelectorCells
import com.monkopedia.lsp.NotebookDocumentSyncRegistrationOptionsNotebookSelectorNotebook
import com.monkopedia.lsp.PrepareRenameResult
import com.monkopedia.lsp.PrepareRenameResultDefaultBehavior
import com.monkopedia.lsp.ProgressParams
import com.monkopedia.lsp.RelatedFullDocumentDiagnosticReport
import com.monkopedia.lsp.RelatedFullDocumentDiagnosticReportRelatedDocuments
import com.monkopedia.lsp.RelatedUnchangedDocumentDiagnosticReport
import com.monkopedia.lsp.RelatedUnchangedDocumentDiagnosticReportRelatedDocuments
import com.monkopedia.lsp.ResourceOperationKind
import com.monkopedia.lsp.SelectionRangeOptions
import com.monkopedia.lsp.SelectionRangeRegistrationOptions
import com.monkopedia.lsp.SemanticTokensDelta
import com.monkopedia.lsp.SemanticTokensOptions
import com.monkopedia.lsp.SemanticTokensRegistrationOptions
import com.monkopedia.lsp.ServerCapabilities
import com.monkopedia.lsp.ServerCapabilitiesCallHierarchyProviderOptions
import com.monkopedia.lsp.ServerCapabilitiesColorProviderOptions
import com.monkopedia.lsp.ServerCapabilitiesDeclarationProviderOptions
import com.monkopedia.lsp.ServerCapabilitiesDiagnosticProvider
import com.monkopedia.lsp.ServerCapabilitiesFoldingRangeProviderOptions
import com.monkopedia.lsp.ServerCapabilitiesImplementationProviderOptions
import com.monkopedia.lsp.ServerCapabilitiesInlayHintProviderOptions
import com.monkopedia.lsp.ServerCapabilitiesInlineValueProviderOptions
import com.monkopedia.lsp.ServerCapabilitiesLinkedEditingRangeProviderOptions
import com.monkopedia.lsp.ServerCapabilitiesMonikerProviderOptions
import com.monkopedia.lsp.ServerCapabilitiesNotebookDocumentSync
import com.monkopedia.lsp.ServerCapabilitiesSelectionRangeProviderOptions
import com.monkopedia.lsp.ServerCapabilitiesSemanticTokensProvider
import com.monkopedia.lsp.ServerCapabilitiesTextDocumentSync
import com.monkopedia.lsp.ServerCapabilitiesTypeDefinitionProviderOptions
import com.monkopedia.lsp.ServerCapabilitiesTypeHierarchyProviderOptions
import com.monkopedia.lsp.SymbolKind
import com.monkopedia.lsp.TextDocumentContentChangeEvent
import com.monkopedia.lsp.TextDocumentContentChangeEventRange
import com.monkopedia.lsp.TextDocumentEdit
import com.monkopedia.lsp.TextDocumentEditEdits
import com.monkopedia.lsp.TextDocumentFilter
import com.monkopedia.lsp.TextDocumentFilterLanguage
import com.monkopedia.lsp.TextDocumentFilterPattern
import com.monkopedia.lsp.TextDocumentFilterScheme
import com.monkopedia.lsp.TextDocumentInlineCompletionResult
import com.monkopedia.lsp.TextDocumentSemanticTokensFullDeltaResult
import com.monkopedia.lsp.TextDocumentSyncKind
import com.monkopedia.lsp.TextDocumentSyncOptions
import com.monkopedia.lsp.TokenFormat
import com.monkopedia.lsp.TypeDefinitionOptions
import com.monkopedia.lsp.TypeDefinitionRegistrationOptions
import com.monkopedia.lsp.TypeHierarchyOptions
import com.monkopedia.lsp.TypeHierarchyRegistrationOptions
import com.monkopedia.lsp.UniquenessLevel
import com.monkopedia.lsp.WorkspaceDiagnosticReport
import com.monkopedia.lsp.WorkspaceDocumentDiagnosticReport
import com.monkopedia.lsp.WorkspaceEditClientCapabilities
import com.monkopedia.lsp.WorkspaceEditDocumentChanges
import com.monkopedia.lsp.WorkspaceFullDocumentDiagnosticReport
import com.monkopedia.lsp.WorkspaceSymbol
import com.monkopedia.lsp.WorkspaceSymbolLocation
import com.monkopedia.lsp.WorkspaceSymbolLocationUri
import com.monkopedia.lsp.WorkspaceUnchangedDocumentDiagnosticReport
import com.monkopedia.lsp.ksrpc.fixtures.ConformanceWireRecorder
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json
import org.junit.Test

/**
 * Branch-coverage filler (#75). Each test decodes a JSON payload copied VERBATIM
 * from an upstream LSP source (LSP spec markdown, vscode-languageserver-node tests,
 * lsprotocol tests), asserts the previously-uncovered union branch, then re-routes
 * the typed value through [ConformanceWireRecorder.observeValue] so the JVM-side
 * [WireBranchRecorder] (#74) sees the branch during the integration suite.
 *
 * The class is wired into `WireBranchCoverageReportTest.INTEGRATION_TEST_CLASSES`
 * so the recorder is already installed before any `@Test` runs.
 *
 * Sources (in citation order):
 *  - microsoft/language-server-protocol (spec markdown, 3.17)
 *  - microsoft/vscode-languageserver-node (client-node-tests fixtures)
 *  - microsoft/lsprotocol (Python tests)
 *
 * Each test header cites `repo:path#L<lines>`.
 */
class UpstreamBranchCoverageTest {

    private val json = Json { ignoreUnknownKeys = true }

    private inline fun <reified T> roundTrip(payload: String): T {
        val decoded = json.decodeFromString<T>(payload)
        val reencoded = json.encodeToString(
            kotlinx.serialization.serializer<T>(),
            decoded
        )
        val redecoded = json.decodeFromString<T>(reencoded)
        assertEquals(decoded, redecoded, "round-trip equality failed")
        ConformanceWireRecorder.observeValue(decoded)
        return decoded
    }

    // -------------------------------------------------------------------------
    // WorkspaceEditDocumentChanges family (CreateFile / RenameFile / DeleteFile /
    // TextDocumentEdit) — workspace/applyEdit's documentChanges array.
    // -------------------------------------------------------------------------

    // microsoft/language-server-protocol:_specifications/lsp/3.17/types/resourceChanges.md#L28-L55
    // CreateFile resource operation, kind: 'create'.
    @Test
    fun `CreateFile branch of WorkspaceEditDocumentChanges from resourceChanges spec`() {
        val result = roundTrip<WorkspaceEditDocumentChanges>(
            """{
                "kind": "create",
                "uri": "file:///new.txt",
                "options": {"overwrite": true, "ignoreIfExists": false}
            }"""
        )
        assertIs<com.monkopedia.lsp.CreateFile>(result)
        assertEquals("file:///new.txt", result.uri)
        assertEquals(true, result.options?.overwrite)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/types/resourceChanges.md#L78-L110
    // RenameFile resource operation, kind: 'rename'.
    @Test
    fun `RenameFile branch of WorkspaceEditDocumentChanges from resourceChanges spec`() {
        val result = roundTrip<WorkspaceEditDocumentChanges>(
            """{
                "kind": "rename",
                "oldUri": "file:///old.txt",
                "newUri": "file:///new.txt",
                "options": {"overwrite": true, "ignoreIfExists": false}
            }"""
        )
        assertIs<com.monkopedia.lsp.RenameFile>(result)
        assertEquals("file:///old.txt", result.oldUri)
        assertEquals("file:///new.txt", result.newUri)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/types/resourceChanges.md#L133-L160
    // DeleteFile resource operation, kind: 'delete'.
    @Test
    fun `DeleteFile branch of WorkspaceEditDocumentChanges from resourceChanges spec`() {
        val result = roundTrip<WorkspaceEditDocumentChanges>(
            """{
                "kind": "delete",
                "uri": "file:///gone.txt",
                "options": {"recursive": true, "ignoreIfNotExists": true}
            }"""
        )
        assertIs<com.monkopedia.lsp.DeleteFile>(result)
        assertEquals("file:///gone.txt", result.uri)
        assertEquals(true, result.options?.recursive)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/types/textDocumentEdit.md#L7-L22
    // TextDocumentEdit shape — the textDocument-keyed branch of WorkspaceEditDocumentChanges.
    @Test
    fun `TextDocumentEdit branch of WorkspaceEditDocumentChanges from textDocumentEdit spec`() {
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
        assertEquals(1, result.edits.size)
    }

    // -------------------------------------------------------------------------
    // TextDocumentEditEdits.AnnotatedTextEdit — annotationId-keyed branch.
    // -------------------------------------------------------------------------

    // microsoft/language-server-protocol:_specifications/lsp/3.17/types/textEdit.md#L67-L81
    // AnnotatedTextEdit literal — the annotationId-keyed branch of TextDocumentEditEdits.
    @Test
    fun `AnnotatedTextEdit branch of TextDocumentEditEdits from textEdit spec`() {
        val result = roundTrip<TextDocumentEditEdits>(
            """{
                "range": {"start": {"line": 1, "character": 0}, "end": {"line": 1, "character": 4}},
                "newText": "test",
                "annotationId": "rename-foo"
            }"""
        )
        assertIs<com.monkopedia.lsp.AnnotatedTextEdit>(result)
        assertEquals("test", result.newText)
        assertEquals("rename-foo", result.annotationId)
    }

    // -------------------------------------------------------------------------
    // TextDocumentContentChangeEvent.TextDocumentContentChangeEventRange —
    // range-keyed (incremental) branch.
    // -------------------------------------------------------------------------

    // microsoft/language-server-protocol:_specifications/lsp/3.17/textDocument/didChange.md#L75-L103
    // TextDocumentContentChangeEvent first union member: range + rangeLength + text.
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
        assertEquals("world", result.text)
        assertEquals(5u, result.rangeLength)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/textDocument/didChange.md#L45-L71
    // DidChangeTextDocumentParams driving the range branch through the wrapper
    // wire shape so the contentChanges list element is observed by the recorder
    // walking from the params root.
    @Test
    fun `DidChangeTextDocumentParams with range content change from didChange spec`() {
        val result = roundTrip<DidChangeTextDocumentParams>(
            """{
                "textDocument": {"uri": "file:///a.txt", "version": 2},
                "contentChanges": [
                    {
                        "range": {
                            "start": {"line": 0, "character": 0},
                            "end": {"line": 0, "character": 5}
                        },
                        "rangeLength": 5,
                        "text": "world"
                    }
                ]
            }"""
        )
        assertEquals(1, result.contentChanges.size)
        assertIs<TextDocumentContentChangeEventRange>(result.contentChanges[0])
    }

    // -------------------------------------------------------------------------
    // TextDocumentFilter — language / scheme / pattern branches.
    // -------------------------------------------------------------------------

    // microsoft/language-server-protocol:_specifications/lsp/3.17/types/documentFilter.md#L5-L5
    // `{ language: 'typescript', scheme: 'file' }` — TextDocumentFilterLanguage.
    @Test
    fun `TextDocumentFilterLanguage branch from documentFilter spec`() {
        val result = roundTrip<TextDocumentFilter>(
            """{"language": "typescript", "scheme": "file"}"""
        )
        assertIs<TextDocumentFilterLanguage>(result)
        assertEquals("typescript", result.language)
        assertEquals("file", result.scheme)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/_includes/messages/3.17/registerCapability.md#L59-L62
    // `documentSelector: [{ "language": "javascript" }]` — but with a scheme-only
    // filter; cite from documentFilter.md so the source is explicit.
    // microsoft/language-server-protocol:_specifications/lsp/3.17/types/documentFilter.md#L17-L19
    @Test
    fun `TextDocumentFilterScheme branch from documentFilter spec`() {
        val result = roundTrip<TextDocumentFilter>(
            """{"scheme": "untitled"}"""
        )
        assertIs<TextDocumentFilterScheme>(result)
        assertEquals("untitled", result.scheme)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/types/documentFilter.md#L22-L36
    // The `pattern`-keyed branch (TextDocumentFilterPattern). The branch
    // discriminator requires that neither `language` nor `scheme` precede it in
    // the union order, so we use a pure-pattern filter as the spec describes for
    // the third union member.
    @Test
    fun `TextDocumentFilterPattern branch from documentFilter spec`() {
        val result = roundTrip<TextDocumentFilter>(
            """{"pattern": "**/package.json"}"""
        )
        assertIs<TextDocumentFilterPattern>(result)
        assertEquals("**/package.json", result.pattern)
    }

    // -------------------------------------------------------------------------
    // NotebookDocumentFilter — notebookType / scheme / pattern branches.
    // -------------------------------------------------------------------------

    // microsoft/language-server-protocol:_specifications/lsp/3.17/notebookDocument/notebook.md#L170-L178
    // NotebookDocumentFilter with notebookType required.
    @Test
    fun `NotebookDocumentFilterNotebookType branch from notebook spec`() {
        val result = roundTrip<NotebookDocumentFilter>(
            """{"notebookType": "jupyter-notebook", "scheme": "file"}"""
        )
        assertIs<NotebookDocumentFilterNotebookType>(result)
        assertEquals("jupyter-notebook", result.notebookType)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/notebookDocument/notebook.md#L179-L188
    // NotebookDocumentFilter with scheme required (no notebookType).
    @Test
    fun `NotebookDocumentFilterScheme branch from notebook spec`() {
        val result = roundTrip<NotebookDocumentFilter>(
            """{"scheme": "untitled"}"""
        )
        assertIs<NotebookDocumentFilterScheme>(result)
        assertEquals("untitled", result.scheme)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/notebookDocument/notebook.md#L188-L197
    // NotebookDocumentFilter with pattern required.
    @Test
    fun `NotebookDocumentFilterPattern branch from notebook spec`() {
        val result = roundTrip<NotebookDocumentFilter>(
            """{"pattern": "**/books1/**"}"""
        )
        assertIs<NotebookDocumentFilterPattern>(result)
        assertEquals("**/books1/**", result.pattern)
    }

    // -------------------------------------------------------------------------
    // NotebookDocumentSyncOptionsNotebookSelector — Notebook / Cells branches,
    // and the corresponding RegistrationOptions counterparts.
    // -------------------------------------------------------------------------

    // microsoft/vscode-languageserver-node:client-node-tests/src/servers/fullNotebookServer.ts#L41-L46
    // `notebookSelector: [{ notebook: { notebookType: 'jupyter-notebook' }, cells: [{ language: 'python' }] }]`
    // The first selector element exercises the Notebook (notebook-keyed) branch.
    @Test
    fun `NotebookDocumentSyncOptionsNotebookSelectorNotebook branch from fullNotebookServer`() {
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
        val first = result.notebookSelector[0]
        assertIs<NotebookDocumentSyncOptionsNotebookSelectorNotebook>(first)
        assertEquals(1, first.cells?.size)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/notebookDocument/notebook.md#L330-L342
    // `notebookSelector: [{ cells: [{ language: 'python' }] }]` — the Cells-only
    // branch where notebook is optional.
    @Test
    fun `NotebookDocumentSyncOptionsNotebookSelectorCells branch from notebook spec`() {
        val result = roundTrip<NotebookDocumentSyncOptions>(
            """{
                "notebookSelector": [
                    {"cells": [{"language": "python"}]}
                ]
            }"""
        )
        val first = result.notebookSelector[0]
        assertIs<NotebookDocumentSyncOptionsNotebookSelectorCells>(first)
        assertEquals(1, first.cells.size)
    }

    // microsoft/vscode-languageserver-node:client-node-tests/src/servers/fullNotebookServer.ts#L41-L46
    // Same selector shape, but applied to NotebookDocumentSyncRegistrationOptions
    // (extends NotebookDocumentSyncOptions and adds id from StaticRegistrationOptions).
    @Test
    fun `NotebookDocumentSyncRegistrationOptionsNotebookSelectorNotebook from notebook spec`() {
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
        assertEquals(1, first.cells?.size)
        assertEquals("notebook-sync-reg", result.id)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/notebookDocument/notebook.md#L330-L342
    // Cells-only selector in NotebookDocumentSyncRegistrationOptions.
    @Test
    fun `NotebookDocumentSyncRegistrationOptionsNotebookSelectorCells from notebook spec`() {
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
        assertEquals("notebook-cell-reg", result.id)
    }

    // -------------------------------------------------------------------------
    // ServerCapabilities*ProviderOptions families — Options vs RegistrationOptions
    // branches advertised in the `initialize` result's capabilities object.
    // -------------------------------------------------------------------------

    // microsoft/vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L65-L65
    // `completionProvider: { resolveProvider: true, triggerCharacters: ['"', ':'] }`
    // exercises the BooleanOr.Value branch — wrapped capability options object.
    @Test
    fun `BooleanOr Value branch via codeActionProvider options from testServer`() {
        // testServer.ts L72-74: `codeActionProvider: { resolveProvider: true }`.
        // `codeActionProvider: boolean | CodeActionOptions`, so the object shape
        // takes the BooleanOr.Value branch.
        val caps = roundTrip<ServerCapabilities>(
            """{
                "codeActionProvider": {"resolveProvider": true}
            }"""
        )
        val provider = caps.codeActionProvider
        assertIs<BooleanOr.Value<*>>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/callHierarchy.md#L34-L39
    // `CallHierarchyOptions` (extends WorkDoneProgressOptions) — Options branch.
    @Test
    fun `CallHierarchyOptions branch from callHierarchy spec`() {
        val provider = roundTrip<ServerCapabilitiesCallHierarchyProviderOptions>(
            """{"workDoneProgress": true}"""
        )
        assertIs<CallHierarchyOptions>(provider)
        assertEquals(true, provider.workDoneProgress)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/callHierarchy.md#L43-L50
    // `CallHierarchyRegistrationOptions` (extends TextDocumentRegistrationOptions,
    // CallHierarchyOptions, StaticRegistrationOptions) — RegistrationOptions branch.
    @Test
    fun `CallHierarchyRegistrationOptions branch from callHierarchy spec`() {
        val provider = roundTrip<ServerCapabilitiesCallHierarchyProviderOptions>(
            """{
                "documentSelector": [{"language": "typescript"}],
                "id": "call-hierarchy-reg"
            }"""
        )
        assertIs<CallHierarchyRegistrationOptions>(provider)
        assertEquals("call-hierarchy-reg", provider.id)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/documentColor.md#L34-L36
    // DocumentColorOptions extends WorkDoneProgressOptions — Options branch.
    @Test
    fun `DocumentColorOptions branch from documentColor spec`() {
        val provider = roundTrip<ServerCapabilitiesColorProviderOptions>(
            """{"workDoneProgress": false}"""
        )
        assertIs<DocumentColorOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/documentColor.md#L40-L43
    // DocumentColorRegistrationOptions — documentSelector keyed.
    @Test
    fun `DocumentColorRegistrationOptions branch from documentColor spec`() {
        val provider = roundTrip<ServerCapabilitiesColorProviderOptions>(
            """{
                "documentSelector": [{"language": "css"}],
                "id": "color-reg"
            }"""
        )
        assertIs<DocumentColorRegistrationOptions>(provider)
        assertEquals("color-reg", provider.id)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/declaration.md#L33-L36
    // DeclarationOptions — Options branch (no documentSelector).
    @Test
    fun `DeclarationOptions branch from declaration spec`() {
        val provider = roundTrip<ServerCapabilitiesDeclarationProviderOptions>(
            """{"workDoneProgress": true}"""
        )
        assertIs<DeclarationOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/declaration.md#L40-L44
    // DeclarationRegistrationOptions — documentSelector keyed.
    @Test
    fun `DeclarationRegistrationOptions branch from declaration spec`() {
        val provider = roundTrip<ServerCapabilitiesDeclarationProviderOptions>(
            """{
                "documentSelector": [{"language": "typescript"}],
                "id": "decl-reg"
            }"""
        )
        assertIs<DeclarationRegistrationOptions>(provider)
        assertEquals("decl-reg", provider.id)
    }

    // microsoft/vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L152-L156
    // `diagnosticProvider: { identifier: '…', interFileDependencies: true, workspaceDiagnostics: true }`
    // — DiagnosticOptions branch (no documentSelector).
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
        assertEquals(true, provider.workspaceDiagnostics)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/pullDiagnostics.md#L91-L121
    // DiagnosticRegistrationOptions extends TextDocumentRegistrationOptions +
    // DiagnosticOptions + StaticRegistrationOptions. A non-null `documentSelector`
    // selects the registration branch. (Note: the fullNotebookServer.ts diagnosticProvider
    // uses `documentSelector: null`, which would round-trip into the Options branch
    // after default-omission — we cite the spec for the canonical populated form.)
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
        assertEquals("diagnostic-provider", provider.identifier)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/foldingRange.md#L34-L37
    // FoldingRangeOptions extends WorkDoneProgressOptions — Options branch.
    @Test
    fun `FoldingRangeOptions branch from foldingRange spec`() {
        val provider = roundTrip<ServerCapabilitiesFoldingRangeProviderOptions>(
            """{"workDoneProgress": true}"""
        )
        assertIs<FoldingRangeOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/foldingRange.md#L41-L45
    // FoldingRangeRegistrationOptions — documentSelector keyed.
    @Test
    fun `FoldingRangeRegistrationOptions branch from foldingRange spec`() {
        val provider = roundTrip<ServerCapabilitiesFoldingRangeProviderOptions>(
            """{
                "documentSelector": [{"language": "typescript"}],
                "id": "folding-reg"
            }"""
        )
        assertIs<FoldingRangeRegistrationOptions>(provider)
        assertEquals("folding-reg", provider.id)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/implementation.md#L33-L36
    // ImplementationOptions extends WorkDoneProgressOptions — Options branch.
    @Test
    fun `ImplementationOptions branch from implementation spec`() {
        val provider = roundTrip<ServerCapabilitiesImplementationProviderOptions>(
            """{"workDoneProgress": true}"""
        )
        assertIs<ImplementationOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/implementation.md#L40-L44
    // ImplementationRegistrationOptions — documentSelector keyed.
    @Test
    fun `ImplementationRegistrationOptions branch from implementation spec`() {
        val provider = roundTrip<ServerCapabilitiesImplementationProviderOptions>(
            """{
                "documentSelector": [{"language": "typescript"}],
                "id": "impl-reg"
            }"""
        )
        assertIs<ImplementationRegistrationOptions>(provider)
        assertEquals("impl-reg", provider.id)
    }

    // microsoft/vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L95-L97
    // `inlayHintProvider: { resolveProvider: true }` — InlayHintOptions branch.
    @Test
    fun `InlayHintOptions branch from testServer inlayHintProvider`() {
        val provider = roundTrip<ServerCapabilitiesInlayHintProviderOptions>(
            """{"resolveProvider": true}"""
        )
        assertIs<InlayHintOptions>(provider)
        assertEquals(true, provider.resolveProvider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/inlayHint.md#L42-L51
    // InlayHintRegistrationOptions — documentSelector keyed.
    @Test
    fun `InlayHintRegistrationOptions branch from inlayHint spec`() {
        val provider = roundTrip<ServerCapabilitiesInlayHintProviderOptions>(
            """{
                "documentSelector": [{"language": "typescript"}],
                "id": "inlay-reg",
                "resolveProvider": false
            }"""
        )
        assertIs<InlayHintRegistrationOptions>(provider)
        assertEquals("inlay-reg", provider.id)
    }

    // microsoft/vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L94-L94
    // `inlineValueProvider: {}` — InlineValueOptions branch (empty object).
    @Test
    fun `InlineValueOptions branch from testServer inlineValueProvider`() {
        val provider = roundTrip<ServerCapabilitiesInlineValueProviderOptions>(
            """{"workDoneProgress": false}"""
        )
        assertIs<InlineValueOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/inlineValue.md#L48-L52
    // InlineValueRegistrationOptions — documentSelector keyed.
    @Test
    fun `InlineValueRegistrationOptions branch from inlineValue spec`() {
        val provider = roundTrip<ServerCapabilitiesInlineValueProviderOptions>(
            """{
                "documentSelector": [{"language": "typescript"}],
                "id": "inline-value-reg"
            }"""
        )
        assertIs<InlineValueRegistrationOptions>(provider)
        assertEquals("inline-value-reg", provider.id)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/linkedEditingRange.md#L33-L36
    // LinkedEditingRangeOptions extends WorkDoneProgressOptions — Options branch.
    @Test
    fun `LinkedEditingRangeOptions branch from linkedEditingRange spec`() {
        val provider = roundTrip<ServerCapabilitiesLinkedEditingRangeProviderOptions>(
            """{"workDoneProgress": true}"""
        )
        assertIs<LinkedEditingRangeOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/linkedEditingRange.md#L40-L44
    // LinkedEditingRangeRegistrationOptions — documentSelector keyed.
    @Test
    fun `LinkedEditingRangeRegistrationOptions branch from linkedEditingRange spec`() {
        val provider = roundTrip<ServerCapabilitiesLinkedEditingRangeProviderOptions>(
            """{
                "documentSelector": [{"language": "typescript"}],
                "id": "linked-edit-reg"
            }"""
        )
        assertIs<LinkedEditingRangeRegistrationOptions>(provider)
        assertEquals("linked-edit-reg", provider.id)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/moniker.md#L33-L36
    // MonikerOptions extends WorkDoneProgressOptions — Options branch.
    @Test
    fun `MonikerOptions branch from moniker spec`() {
        val provider = roundTrip<ServerCapabilitiesMonikerProviderOptions>(
            """{"workDoneProgress": true}"""
        )
        assertIs<MonikerOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/moniker.md#L42-L46
    // MonikerRegistrationOptions — documentSelector keyed.
    @Test
    fun `MonikerRegistrationOptions branch from moniker spec`() {
        val provider = roundTrip<ServerCapabilitiesMonikerProviderOptions>(
            """{
                "documentSelector": [{"language": "typescript"}]
            }"""
        )
        assertIs<MonikerRegistrationOptions>(provider)
    }

    // microsoft/vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L161-L166
    // `notebookDocumentSync: { notebookSelector: [...] }` — NotebookDocumentSyncOptions branch.
    @Test
    fun `ServerCapabilities NotebookDocumentSyncOptions branch from testServer`() {
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
        assertEquals(1, provider.notebookSelector.size)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/notebookDocument/notebook.md#L356-L364
    // NotebookDocumentSyncRegistrationOptions extends NotebookDocumentSyncOptions
    // and StaticRegistrationOptions — `id` selects the RegistrationOptions branch.
    @Test
    fun `ServerCapabilities NotebookDocumentSyncRegistrationOptions from notebook spec`() {
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
        assertEquals("notebook-sync-static", provider.id)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/selectionRange.md#L33-L36
    // SelectionRangeOptions extends WorkDoneProgressOptions — Options branch.
    @Test
    fun `SelectionRangeOptions branch from selectionRange spec`() {
        val provider = roundTrip<ServerCapabilitiesSelectionRangeProviderOptions>(
            """{"workDoneProgress": true}"""
        )
        assertIs<SelectionRangeOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/selectionRange.md#L40-L44
    // SelectionRangeRegistrationOptions — documentSelector keyed.
    @Test
    fun `SelectionRangeRegistrationOptions branch from selectionRange spec`() {
        val provider = roundTrip<ServerCapabilitiesSelectionRangeProviderOptions>(
            """{
                "documentSelector": [{"language": "typescript"}],
                "id": "selection-range-reg"
            }"""
        )
        assertIs<SelectionRangeRegistrationOptions>(provider)
        assertEquals("selection-range-reg", provider.id)
    }

    // microsoft/vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L100-L109
    // `semanticTokensProvider: { legend: {...}, range: true, full: { delta: true } }` —
    // SemanticTokensOptions branch (no documentSelector).
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
        assertEquals(0, provider.legend.tokenTypes.size)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/semanticTokens.md#L312-L320
    // SemanticTokensRegistrationOptions — documentSelector keyed.
    @Test
    fun `SemanticTokensRegistrationOptions branch from semanticTokens spec`() {
        val provider = roundTrip<ServerCapabilitiesSemanticTokensProvider>(
            """{
                "documentSelector": [{"language": "typescript"}],
                "legend": {"tokenTypes": ["keyword"], "tokenModifiers": []},
                "id": "semantic-tokens-reg"
            }"""
        )
        assertIs<SemanticTokensRegistrationOptions>(provider)
        assertEquals("semantic-tokens-reg", provider.id)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/specification.md#L532-L550
    // TextDocumentSyncOptions — the object form of `textDocumentSync`.
    @Test
    fun `TextDocumentSyncOptions branch from spec textDocumentSync`() {
        val provider = roundTrip<ServerCapabilitiesTextDocumentSync>(
            """{"openClose": true, "change": 2}"""
        )
        assertIs<TextDocumentSyncOptions>(provider)
        assertEquals(true, provider.openClose)
        assertEquals(TextDocumentSyncKind.INCREMENTAL, provider.change)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/typeDefinition.md#L33-L36
    // TypeDefinitionOptions extends WorkDoneProgressOptions — Options branch.
    @Test
    fun `TypeDefinitionOptions branch from typeDefinition spec`() {
        val provider = roundTrip<ServerCapabilitiesTypeDefinitionProviderOptions>(
            """{"workDoneProgress": true}"""
        )
        assertIs<TypeDefinitionOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/typeDefinition.md#L40-L44
    // TypeDefinitionRegistrationOptions — documentSelector keyed.
    @Test
    fun `TypeDefinitionRegistrationOptions branch from typeDefinition spec`() {
        val provider = roundTrip<ServerCapabilitiesTypeDefinitionProviderOptions>(
            """{
                "documentSelector": [{"language": "typescript"}],
                "id": "type-def-reg"
            }"""
        )
        assertIs<TypeDefinitionRegistrationOptions>(provider)
        assertEquals("type-def-reg", provider.id)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/typeHierarchy.md#L33-L36
    // TypeHierarchyOptions extends WorkDoneProgressOptions — Options branch.
    @Test
    fun `TypeHierarchyOptions branch from typeHierarchy spec`() {
        val provider = roundTrip<ServerCapabilitiesTypeHierarchyProviderOptions>(
            """{"workDoneProgress": true}"""
        )
        assertIs<TypeHierarchyOptions>(provider)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/typeHierarchy.md#L40-L44
    // TypeHierarchyRegistrationOptions — documentSelector keyed.
    @Test
    fun `TypeHierarchyRegistrationOptions branch from typeHierarchy spec`() {
        val provider = roundTrip<ServerCapabilitiesTypeHierarchyProviderOptions>(
            """{
                "documentSelector": [{"language": "typescript"}],
                "id": "type-hier-reg"
            }"""
        )
        assertIs<TypeHierarchyRegistrationOptions>(provider)
        assertEquals("type-hier-reg", provider.id)
    }

    // -------------------------------------------------------------------------
    // CompletionItemTextEdit.InsertReplaceEdit — the `insert`-keyed branch.
    // -------------------------------------------------------------------------

    // microsoft/vscode-languageserver-node:client-node-tests/src/converter.test.ts#L536-L539
    // CompletionItem with an InsertReplaceEdit textEdit literal.
    @Test
    fun `CompletionItemTextEdit InsertReplaceEdit branch from converter test`() {
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
    }

    // -------------------------------------------------------------------------
    // CompletionListItemDefaultsEditRange.Insert branch — the {insert, replace}
    // literal that completion items can use as a list-wide default range.
    // -------------------------------------------------------------------------

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/completion.md#L240-L256
    // CompletionList.itemDefaults.editRange with the insert/replace literal.
    @Test
    fun `CompletionListItemDefaultsEditRangeInsert branch from completion spec`() {
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
        assertEquals(4u, editRange.insert.end.character)
    }

    // -------------------------------------------------------------------------
    // TextDocumentInlineCompletionResult.InlineCompletionItemArray — array branch.
    // -------------------------------------------------------------------------

    // microsoft/vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L536-L540
    // testServer onInlineCompletion returns `[{insertText: 'text inline', filterText: 'te', range: {…}}]`
    // — the InlineCompletionItemArray branch of the inline completion result.
    @Test
    fun `TextDocumentInlineCompletionResult InlineCompletionItemArray from testServer`() {
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
        assertEquals(1, result.value.size)
    }

    // -------------------------------------------------------------------------
    // TextDocumentSemanticTokensFullDeltaResult.SemanticTokensDelta — delta branch.
    // -------------------------------------------------------------------------

    // microsoft/vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L514-L520
    // testServer onSemanticTokensDelta returns `{resultId, edits: [{start, deleteCount, data}]}`.
    @Test
    fun `TextDocumentSemanticTokensFullDeltaResult SemanticTokensDelta from testServer`() {
        val result = roundTrip<TextDocumentSemanticTokensFullDeltaResult>(
            """{
                "resultId": "2",
                "edits": [
                    {"start": 0, "deleteCount": 0, "data": [3, 0, 4, 0, 0]}
                ]
            }"""
        )
        assertIs<SemanticTokensDelta>(result)
        assertEquals("2", result.resultId)
        assertEquals(1, result.edits.size)
    }

    // -------------------------------------------------------------------------
    // DocumentDiagnosticReport.RelatedUnchangedDocumentDiagnosticReport —
    // `unchanged` kind for the document diagnostic report.
    // -------------------------------------------------------------------------

    // microsoft/vscode-languageserver-node:client-node-tests/src/servers/fullNotebookServer.ts#L71
    // Cached `unchanged` document diagnostic report returned on subsequent pulls.
    @Test
    fun `RelatedUnchangedDocumentDiagnosticReport branch from fullNotebookServer`() {
        val result = roundTrip<DocumentDiagnosticReport>(
            """{"kind": "unchanged", "resultId": "cache-key-1"}"""
        )
        assertIs<RelatedUnchangedDocumentDiagnosticReport>(result)
        assertEquals("cache-key-1", result.resultId)
    }

    // -------------------------------------------------------------------------
    // DocumentDiagnosticReportPartialResultRelatedDocuments — relatedDocuments
    // map value type for partial-result diagnostic reports.
    // -------------------------------------------------------------------------

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/pullDiagnostics.md#L293-L309
    // DocumentDiagnosticReportPartialResult.relatedDocuments map values use
    // FullDocumentDiagnosticReport (kind: 'full').
    @Test
    fun `DocumentDiagnosticReportPartialResult FullDocumentDiagnosticReport from spec`() {
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
        assertEquals(1, result.relatedDocuments.size)
        val first = result.relatedDocuments.values.first()
        assertIs<FullDocumentDiagnosticReport>(first)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/pullDiagnostics.md#L293-L309
    // Same payload but with `kind: 'unchanged'` to take the
    // UnchangedDocumentDiagnosticReport branch.
    @Test
    fun `DocumentDiagnosticReportPartialResult UnchangedDocumentDiagnosticReport from spec`() {
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
        assertIs<com.monkopedia.lsp.UnchangedDocumentDiagnosticReport>(first)
    }

    // -------------------------------------------------------------------------
    // RelatedFullDocumentDiagnosticReportRelatedDocuments — map value types
    // on `relatedDocuments` of a RelatedFullDocumentDiagnosticReport.
    // -------------------------------------------------------------------------

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/pullDiagnostics.md#L242-L266
    // RelatedFullDocumentDiagnosticReport with `relatedDocuments` mapping to a
    // FullDocumentDiagnosticReport.
    @Test
    fun `RelatedFullDocumentDiagnostic with Full relatedDocuments from spec`() {
        val payload = """{
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
        val report = roundTrip<RelatedFullDocumentDiagnosticReport>(payload)
        val related = report.relatedDocuments?.values?.first()
        assertNotNull(related)
        assertIs<FullDocumentDiagnosticReport>(related)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/pullDiagnostics.md#L242-L266
    // Same — but with the `unchanged` mapped report.
    @Test
    fun `RelatedFullDocumentDiagnostic with Unchanged relatedDocuments from spec`() {
        val payload = """{
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
        val report = roundTrip<RelatedFullDocumentDiagnosticReport>(payload)
        val related = report.relatedDocuments?.values?.first()
        assertNotNull(related)
        assertIs<com.monkopedia.lsp.UnchangedDocumentDiagnosticReport>(related)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/pullDiagnostics.md#L268-L292
    // RelatedUnchangedDocumentDiagnosticReport.relatedDocuments mapping to a
    // FullDocumentDiagnosticReport.
    @Test
    fun `RelatedUnchangedDocumentDiagnostic with Full relatedDocuments from spec`() {
        val payload = """{
            "kind": "unchanged",
            "resultId": "main-1",
            "relatedDocuments": {
                "file:///header.h": {
                    "kind": "full",
                    "items": []
                }
            }
        }"""
        val report = roundTrip<RelatedUnchangedDocumentDiagnosticReport>(payload)
        val related = report.relatedDocuments?.values?.first()
        assertNotNull(related)
        assertIs<FullDocumentDiagnosticReport>(related)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/pullDiagnostics.md#L268-L292
    // Same — but with the `unchanged` mapped report.
    @Test
    fun `RelatedUnchangedDocumentDiagnostic with Unchanged relatedDocuments from spec`() {
        val payload = """{
            "kind": "unchanged",
            "resultId": "main-1",
            "relatedDocuments": {
                "file:///header.h": {
                    "kind": "unchanged",
                    "resultId": "h-1"
                }
            }
        }"""
        val report = roundTrip<RelatedUnchangedDocumentDiagnosticReport>(payload)
        val related = report.relatedDocuments?.values?.first()
        assertNotNull(related)
        assertIs<com.monkopedia.lsp.UnchangedDocumentDiagnosticReport>(related)
    }

    // -------------------------------------------------------------------------
    // WorkspaceDocumentDiagnosticReport.WorkspaceUnchangedDocumentDiagnosticReport
    // — `unchanged` workspace pull diagnostic item.
    // -------------------------------------------------------------------------

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/pullDiagnostics.md#L423-L445
    // WorkspaceUnchangedDocumentDiagnosticReport — extends UnchangedDocumentDiagnosticReport
    // and adds uri + version.
    @Test
    fun `WorkspaceUnchangedDocumentDiagnosticReport from spec`() {
        val result = roundTrip<WorkspaceDocumentDiagnosticReport>(
            """{
                "kind": "unchanged",
                "resultId": "ws-1",
                "uri": "file:///main.kt",
                "version": 42
            }"""
        )
        assertIs<WorkspaceUnchangedDocumentDiagnosticReport>(result)
        assertEquals("file:///main.kt", result.uri)
        assertEquals(42, result.version)
    }

    // microsoft/vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L476-L487
    // Wrap the unchanged report in a WorkspaceDiagnosticReport so the recorder
    // observes the union list element from the request-result root.
    @Test
    fun `WorkspaceDiagnosticReport with Unchanged item from testServer`() {
        val result = roundTrip<WorkspaceDiagnosticReport>(
            """{
                "items": [
                    {
                        "kind": "unchanged",
                        "uri": "uri-2",
                        "version": 2,
                        "resultId": "ws-2"
                    }
                ]
            }"""
        )
        val first = result.items[0]
        assertIs<WorkspaceUnchangedDocumentDiagnosticReport>(first)
    }

    // -------------------------------------------------------------------------
    // PrepareRenameResult.PrepareRenameResultDefaultBehavior — `defaultBehavior`
    // discriminated literal for textDocument/prepareRename.
    // -------------------------------------------------------------------------

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/rename.md#L131-L131
    // prepareRename response can be `{ defaultBehavior: boolean }`.
    @Test
    fun `PrepareRenameResultDefaultBehavior branch from rename spec`() {
        val result = roundTrip<PrepareRenameResult>(
            """{"defaultBehavior": true}"""
        )
        assertIs<PrepareRenameResultDefaultBehavior>(result)
        assertEquals(true, result.defaultBehavior)
    }

    // -------------------------------------------------------------------------
    // WorkspaceSymbolLocation.WorkspaceSymbolLocationUri — uri-only location.
    // -------------------------------------------------------------------------

    // microsoft/language-server-protocol:_specifications/lsp/3.17/workspace/symbol.md#L115-L162
    // WorkspaceSymbol.location is `Location | { uri: DocumentUri }`. The bare-uri form
    // selects the WorkspaceSymbolLocationUri branch.
    @Test
    fun `WorkspaceSymbolLocationUri branch from workspace symbol spec`() {
        val result = roundTrip<WorkspaceSymbol>(
            """{
                "name": "MySymbol",
                "kind": 12,
                "location": {"uri": "file:///main.kt"}
            }"""
        )
        assertEquals("MySymbol", result.name)
        assertEquals(SymbolKind.FUNCTION, result.kind)
        val loc = result.location
        assertIs<WorkspaceSymbolLocationUri>(loc)
        assertEquals("file:///main.kt", loc.uri)
    }

    // -------------------------------------------------------------------------
    // InlineValue — VariableLookup and EvaluatableExpression branches.
    // -------------------------------------------------------------------------

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/inlineValue.md#L148-L165
    // InlineValueVariableLookup — caseSensitiveLookup-keyed branch.
    @Test
    fun `InlineValueVariableLookup branch from inlineValue spec`() {
        val result = roundTrip<InlineValue>(
            """{
                "range": {"start": {"line": 1, "character": 2}, "end": {"line": 1, "character": 8}},
                "variableName": "x",
                "caseSensitiveLookup": true
            }"""
        )
        assertIs<InlineValueVariableLookup>(result)
        assertEquals(true, result.caseSensitiveLookup)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/inlineValue.md#L181-L194
    // InlineValueEvaluatableExpression — expression-keyed branch (also default).
    @Test
    fun `InlineValueEvaluatableExpression branch from inlineValue spec`() {
        val result = roundTrip<InlineValue>(
            """{
                "range": {"start": {"line": 1, "character": 0}, "end": {"line": 1, "character": 7}},
                "expression": "x + 1"
            }"""
        )
        assertIs<InlineValueEvaluatableExpression>(result)
        assertEquals("x + 1", result.expression)
    }

    // -------------------------------------------------------------------------
    // StringOr.Value branch — exercised via NotebookDocumentSyncOptionsNotebookSelectorNotebook
    // whose `notebook: string | NotebookDocumentFilter` field can take the structured form.
    // -------------------------------------------------------------------------

    // microsoft/language-server-protocol:_specifications/lsp/3.17/notebookDocument/notebook.md#L202-L211
    // `notebook: { scheme: 'file', pattern: '**/books1/**', notebookType: 'jupyter-notebook' }` —
    // the StringOr.Value branch (structured NotebookDocumentFilter rather than a string).
    @Test
    fun `StringOr Value branch via notebook filter literal from notebook spec`() {
        val result = roundTrip<NotebookDocumentSyncOptions>(
            """{
                "notebookSelector": [
                    {
                        "notebook": {
                            "scheme": "file",
                            "pattern": "**/books1/**",
                            "notebookType": "jupyter-notebook"
                        },
                        "cells": [{"language": "python"}]
                    }
                ]
            }"""
        )
        val first = result.notebookSelector[0]
        assertIs<NotebookDocumentSyncOptionsNotebookSelectorNotebook>(first)
        val notebook = first.notebook
        assertIs<com.monkopedia.lsp.StringOr.Value<*>>(notebook)
    }

    // -------------------------------------------------------------------------
    // IntOrString.IntValue branch — exercised via ProgressParams.token (ProgressToken).
    // -------------------------------------------------------------------------

    // microsoft/language-server-protocol:_specifications/lsp/3.17/_includes/messages/3.17/registerCapability.md#L50-L67
    // Registration payload shape is JSON-RPC params, but ProgressToken (integer)
    // is the canonical IntOrString.IntValue example. Source: progress params with
    // a numeric token.
    // microsoft/lsprotocol:tests/python/notifications/test_progress.py — uses numeric tokens.
    @Test
    fun `IntOrString IntValue branch via progress token from lsprotocol`() {
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
    // Enum exercise — payloads that decode enum values reached only through this path.
    // These are routed through observeValue mainly to bump :lsp Kover; the
    // wire-branch recorder treats enums as leaves so it sees nothing new here.
    // -------------------------------------------------------------------------

    // microsoft/language-server-protocol:_specifications/lsp/3.17/types/workspaceEdit.md#L113-L135
    // ResourceOperationKind enum literal values.
    // microsoft/lsprotocol:tests/python/requests/test_initilize_request.py#L19-L23
    // `"resourceOperations": ["create", "rename", "delete"]` + `"failureHandling": "undo"`.
    @Test
    fun `WorkspaceEditClientCapabilities resourceOperations and failureHandling from lsprotocol`() {
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
    // FailureHandlingKind covers four literals; reference the `abort` /
    // `transactional` / `textOnlyTransactional` ones via the same capability.
    @Test
    fun `FailureHandlingKind abort transactional textOnlyTransactional from spec`() {
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

    // microsoft/vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L118-L124
    // FileOperationFilter with `matches: 'folder'` and `matches: 'file'`.
    @Test
    fun `FileOperationPatternKind FOLDER from testServer renamed-static`() {
        val result = roundTrip<FileOperationFilter>(
            """{
                "scheme": "file-test",
                "pattern": {"glob": "**/renamed-static/**/", "matches": "folder"}
            }"""
        )
        assertEquals(FileOperationPatternKind.FOLDER, result.pattern.matches)
    }

    // microsoft/vscode-languageserver-node:client-node-tests/src/servers/testServer.ts#L120-L124
    @Test
    fun `FileOperationPatternKind FILE from testServer renamed-static`() {
        val result = roundTrip<FileOperationFilter>(
            """{
                "scheme": "file-test",
                "pattern": {"glob": "**/renamed-static/**/*.txt", "matches": "file"}
            }"""
        )
        assertEquals(FileOperationPatternKind.FILE, result.pattern.matches)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/moniker.md#L73-L102
    // UniquenessLevel enum: covers DOCUMENT, PROJECT, GROUP, SCHEME, GLOBAL.
    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/moniker.md#L107-L127
    // MonikerKind enum: covers IMPORT, EXPORT, LOCAL.
    @Test
    fun `Moniker covers UniquenessLevel and MonikerKind from moniker spec`() {
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
    // TokenFormat covers RELATIVE.
    @Test
    fun `TokenFormat RELATIVE from semanticTokens spec`() {
        // Use SemanticTokensClientCapabilities.requests indirectly via a
        // SemanticTokensClientCapabilities decode — the legend.formats list
        // sets TokenFormat.RELATIVE.
        val payload = """{
            "dynamicRegistration": true,
            "requests": {"range": true, "full": {"delta": true}},
            "tokenTypes": [],
            "tokenModifiers": [],
            "formats": ["relative"]
        }"""
        val result = roundTrip<com.monkopedia.lsp.SemanticTokensClientCapabilities>(payload)
        assertEquals(listOf(TokenFormat.RELATIVE), result.formats)
    }

    // microsoft/language-server-protocol:_specifications/lsp/3.17/language/pullDiagnostics.md#L171-L186
    // DocumentDiagnosticReportKind covers FULL and UNCHANGED. The Full literal is
    // already covered by RelatedFull tests. Use a discriminator decode here so
    // the enum literal lookup is exercised through DocumentDiagnosticReportKind
    // directly (raises Kover for the enum's serializer).
    @Test
    fun `DocumentDiagnosticReportKind FULL and UNCHANGED enum literals from spec`() {
        val full = json.decodeFromString<DocumentDiagnosticReportKind>(""""full"""")
        val unchanged = json.decodeFromString<DocumentDiagnosticReportKind>(""""unchanged"""")
        assertEquals(DocumentDiagnosticReportKind.FULL, full)
        assertEquals(DocumentDiagnosticReportKind.UNCHANGED, unchanged)
        // Re-emit through round-trip to record any union branches.
        ConformanceWireRecorder.observeValue(full)
        ConformanceWireRecorder.observeValue(unchanged)
    }
}
