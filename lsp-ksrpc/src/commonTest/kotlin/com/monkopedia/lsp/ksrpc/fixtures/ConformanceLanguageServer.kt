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
package com.monkopedia.lsp.ksrpc.fixtures

import com.monkopedia.lsp.BooleanOr
import com.monkopedia.lsp.CallHierarchyIncomingCall
import com.monkopedia.lsp.CallHierarchyIncomingCallsParams
import com.monkopedia.lsp.CallHierarchyItem
import com.monkopedia.lsp.CallHierarchyOutgoingCall
import com.monkopedia.lsp.CallHierarchyOutgoingCallsParams
import com.monkopedia.lsp.CallHierarchyPrepareParams
import com.monkopedia.lsp.CodeAction
import com.monkopedia.lsp.CodeActionParams
import com.monkopedia.lsp.CodeLens
import com.monkopedia.lsp.CodeLensParams
import com.monkopedia.lsp.Color
import com.monkopedia.lsp.ColorInformation
import com.monkopedia.lsp.ColorPresentation
import com.monkopedia.lsp.ColorPresentationParams
import com.monkopedia.lsp.Command
import com.monkopedia.lsp.CompletionItem
import com.monkopedia.lsp.CompletionItemKind
import com.monkopedia.lsp.CompletionList
import com.monkopedia.lsp.CompletionOptions
import com.monkopedia.lsp.CompletionParams
import com.monkopedia.lsp.CreateFilesParams
import com.monkopedia.lsp.Declaration
import com.monkopedia.lsp.DeclarationLink
import com.monkopedia.lsp.DeclarationParams
import com.monkopedia.lsp.DefaultLanguageServer
import com.monkopedia.lsp.Definition
import com.monkopedia.lsp.DefinitionLink
import com.monkopedia.lsp.DefinitionParams
import com.monkopedia.lsp.DeleteFilesParams
import com.monkopedia.lsp.Diagnostic
import com.monkopedia.lsp.DidChangeConfigurationParams
import com.monkopedia.lsp.DidChangeNotebookDocumentParams
import com.monkopedia.lsp.DidChangeTextDocumentParams
import com.monkopedia.lsp.DidChangeWatchedFilesParams
import com.monkopedia.lsp.DidChangeWorkspaceFoldersParams
import com.monkopedia.lsp.DidCloseNotebookDocumentParams
import com.monkopedia.lsp.DidCloseTextDocumentParams
import com.monkopedia.lsp.DidOpenNotebookDocumentParams
import com.monkopedia.lsp.DidOpenTextDocumentParams
import com.monkopedia.lsp.DidSaveNotebookDocumentParams
import com.monkopedia.lsp.DidSaveTextDocumentParams
import com.monkopedia.lsp.DocumentColorParams
import com.monkopedia.lsp.DocumentDiagnosticParams
import com.monkopedia.lsp.DocumentDiagnosticReport
import com.monkopedia.lsp.DocumentFormattingParams
import com.monkopedia.lsp.DocumentHighlight
import com.monkopedia.lsp.DocumentHighlightKind
import com.monkopedia.lsp.DocumentHighlightParams
import com.monkopedia.lsp.DocumentLink
import com.monkopedia.lsp.DocumentLinkParams
import com.monkopedia.lsp.DocumentOnTypeFormattingParams
import com.monkopedia.lsp.DocumentRangeFormattingParams
import com.monkopedia.lsp.DocumentRangesFormattingParams
import com.monkopedia.lsp.DocumentSymbol
import com.monkopedia.lsp.DocumentSymbolParams
import com.monkopedia.lsp.ExecuteCommandParams
import com.monkopedia.lsp.FoldingRange
import com.monkopedia.lsp.FoldingRangeKind
import com.monkopedia.lsp.FoldingRangeParams
import com.monkopedia.lsp.Hover
import com.monkopedia.lsp.HoverContents
import com.monkopedia.lsp.HoverParams
import com.monkopedia.lsp.ImplementationParams
import com.monkopedia.lsp.InitializeParams
import com.monkopedia.lsp.InitializeResult
import com.monkopedia.lsp.InitializeResultServerInfo
import com.monkopedia.lsp.InitializedParams
import com.monkopedia.lsp.InlayHint
import com.monkopedia.lsp.InlayHintParams
import com.monkopedia.lsp.InlineCompletionItem
import com.monkopedia.lsp.InlineCompletionList
import com.monkopedia.lsp.InlineCompletionParams
import com.monkopedia.lsp.InlineValue
import com.monkopedia.lsp.InlineValueParams
import com.monkopedia.lsp.InlineValueText
import com.monkopedia.lsp.LSPAny
import com.monkopedia.lsp.LinkedEditingRangeParams
import com.monkopedia.lsp.LinkedEditingRanges
import com.monkopedia.lsp.Location
import com.monkopedia.lsp.LocationLink
import com.monkopedia.lsp.Moniker
import com.monkopedia.lsp.MonikerKind
import com.monkopedia.lsp.MonikerParams
import com.monkopedia.lsp.Position
import com.monkopedia.lsp.PrepareRenameParams
import com.monkopedia.lsp.PrepareRenameResult
import com.monkopedia.lsp.PrepareRenameResultRange
import com.monkopedia.lsp.ProgressParams
import com.monkopedia.lsp.Range
import com.monkopedia.lsp.ReferenceParams
import com.monkopedia.lsp.RelatedFullDocumentDiagnosticReport
import com.monkopedia.lsp.RenameFilesParams
import com.monkopedia.lsp.RenameParams
import com.monkopedia.lsp.SelectionRange
import com.monkopedia.lsp.SelectionRangeParams
import com.monkopedia.lsp.SemanticTokens
import com.monkopedia.lsp.SemanticTokensDeltaParams
import com.monkopedia.lsp.SemanticTokensParams
import com.monkopedia.lsp.SemanticTokensRangeParams
import com.monkopedia.lsp.ServerCapabilities
import com.monkopedia.lsp.SetTraceParams
import com.monkopedia.lsp.SignatureHelp
import com.monkopedia.lsp.SignatureHelpParams
import com.monkopedia.lsp.SignatureInformation
import com.monkopedia.lsp.SingleOrArray
import com.monkopedia.lsp.StringOr
import com.monkopedia.lsp.StringValue
import com.monkopedia.lsp.SymbolInformation
import com.monkopedia.lsp.SymbolKind
import com.monkopedia.lsp.TextDocumentCodeActionResult
import com.monkopedia.lsp.TextDocumentCompletionResult
import com.monkopedia.lsp.TextDocumentDeclarationResult
import com.monkopedia.lsp.TextDocumentDefinitionResult
import com.monkopedia.lsp.TextDocumentDocumentSymbolResult
import com.monkopedia.lsp.TextDocumentImplementationResult
import com.monkopedia.lsp.TextDocumentInlineCompletionResult
import com.monkopedia.lsp.TextDocumentSemanticTokensFullDeltaResult
import com.monkopedia.lsp.TextDocumentTypeDefinitionResult
import com.monkopedia.lsp.TextEdit
import com.monkopedia.lsp.TypeDefinitionParams
import com.monkopedia.lsp.TypeHierarchyItem
import com.monkopedia.lsp.TypeHierarchyPrepareParams
import com.monkopedia.lsp.TypeHierarchySubtypesParams
import com.monkopedia.lsp.TypeHierarchySupertypesParams
import com.monkopedia.lsp.UniquenessLevel
import com.monkopedia.lsp.WillSaveTextDocumentParams
import com.monkopedia.lsp.WorkDoneProgressCancelParams
import com.monkopedia.lsp.WorkspaceDiagnosticParams
import com.monkopedia.lsp.WorkspaceDiagnosticReport
import com.monkopedia.lsp.WorkspaceEdit
import com.monkopedia.lsp.WorkspaceFullDocumentDiagnosticReport
import com.monkopedia.lsp.WorkspaceSymbol
import com.monkopedia.lsp.WorkspaceSymbolParams
import com.monkopedia.lsp.markdown
import com.monkopedia.lsp.string
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * Deterministic conformance fixture server: every typed-union result method
 * returns a canned value chosen by the request's *position line* (or, for hover,
 * the line). Downstream integration tests (#45/#46/#47) drive this server with
 * the well-known inputs below and assert the concrete branch that round-trips
 * back over the wire.
 *
 * The fixture keys branch selection off [HoverParams.position]`.line` (and the
 * analogous `position.line` of the other text-document requests). Line numbers
 * are stable contract; do not renumber them without updating the dependent
 * tests.
 *
 * ## Union-branch selection table
 *
 * `textDocument/hover` — result [Hover], `contents: HoverContents`:
 *
 * | position.line | branch                               |
 * |---------------|--------------------------------------|
 * | 0             | [HoverContents.MarkupContentValue]   |
 * | 1             | [HoverContents.MarkedStringValue]    |
 * | 2             | [HoverContents.MarkedStringArray]    |
 *
 * `textDocument/definition` — [TextDocumentDefinitionResult]:
 *
 * | position.line | branch                                                      |
 * |---------------|-------------------------------------------------------------|
 * | 0             | [TextDocumentDefinitionResult.DefinitionValue] (single [Location], i.e. [SingleOrArray.Single]) |
 * | 1             | [TextDocumentDefinitionResult.DefinitionValue] ([Location]`[]`, i.e. [SingleOrArray.Multiple])  |
 * | 2             | [TextDocumentDefinitionResult.DefinitionLinkArray] ([LocationLink]`[]`) |
 *
 * `textDocument/declaration` — [TextDocumentDeclarationResult]: same line → branch
 * mapping as definition (single / array / link-array).
 *
 * `textDocument/typeDefinition` — [TextDocumentTypeDefinitionResult]: same mapping.
 *
 * `textDocument/implementation` — [TextDocumentImplementationResult]: same mapping.
 *
 * `textDocument/completion` — [TextDocumentCompletionResult]:
 *
 * | position.line | branch                                            |
 * |---------------|---------------------------------------------------|
 * | 0             | [TextDocumentCompletionResult.CompletionListValue] |
 * | 1             | [TextDocumentCompletionResult.CompletionItemArray] |
 *
 * `textDocument/documentSymbol` — [TextDocumentDocumentSymbolResult]: keyed off
 * the document URI rather than a position (the request has no position):
 *
 * | textDocument.uri ends with | branch                                              |
 * |----------------------------|-----------------------------------------------------|
 * | `#hierarchical` (default)  | [TextDocumentDocumentSymbolResult.DocumentSymbolArray] |
 * | `#flat`                    | [TextDocumentDocumentSymbolResult.SymbolInformationArray] |
 *
 * `textDocument/references` — always a [Location]`[]`.
 *
 * The remaining server methods (signatureHelp, formatting, rename, codeAction,
 * codeLens, foldingRange, semanticTokens, inlayHint, documentHighlight) return
 * simple well-formed canned values; they are not branch-exhaustive but never
 * throw.
 *
 * Use the [Uri] / [Lines] constants below from tests so the contract stays in
 * one place.
 *
 * ## Notification recording (issue #65)
 *
 * For full wire-coverage every notification handler also records its receipt
 * into [notifications]. Tests can drain [drainNotifications] or observe via
 * [notificationFlow] to assert that a notification arrived. The key is the LSP
 * method name (e.g. `textDocument/didChange`).
 */
open class ConformanceLanguageServer : DefaultLanguageServer() {

    /**
     * Receipt of a notification on the server side. [method] is the LSP method
     * name (e.g. `textDocument/didChange`); [paramsSummary] is a short
     * deterministic stringification of the params used for asserting that the
     * payload survived the round-trip.
     */
    data class NotificationReceipt(val method: String, val paramsSummary: String)

    private val notificationMutex = Mutex()
    private val notificationsBuffer = mutableListOf<NotificationReceipt>()

    /** Hot flow of notification receipts; tests may collect this in parallel. */
    val notificationFlow: MutableSharedFlow<NotificationReceipt> =
        MutableSharedFlow(extraBufferCapacity = 256)

    /** Drain and return all notification receipts seen so far. */
    suspend fun drainNotifications(): List<NotificationReceipt> = notificationMutex.withLock {
        val snapshot = notificationsBuffer.toList()
        notificationsBuffer.clear()
        snapshot
    }

    /** Snapshot the current notification log without clearing. */
    suspend fun snapshotNotifications(): List<NotificationReceipt> = notificationMutex.withLock {
        notificationsBuffer.toList()
    }

    private suspend fun record(method: String, paramsSummary: String) {
        notificationMutex.withLock {
            notificationsBuffer += NotificationReceipt(method, paramsSummary)
        }
        notificationFlow.tryEmit(NotificationReceipt(method, paramsSummary))
    }

    /** Well-known document URIs the fixture recognises. */
    object Uri {
        const val MAIN = "file:///conformance/main.kt"
        const val HIERARCHICAL_SYMBOLS = "file:///conformance/symbols.kt#hierarchical"
        const val FLAT_SYMBOLS = "file:///conformance/symbols.kt#flat"
    }

    /** Well-known request lines that select union branches (see class KDoc). */
    object Lines {
        const val SINGLE = 0
        const val ARRAY = 1
        const val LINK = 2
    }

    private fun pos(line: Int, character: Int = 0): Position =
        Position(line = line.toUInt(), character = character.toUInt())

    private fun range(line: Int): Range = Range(start = pos(line, 0), end = pos(line, 4))

    private fun location(uri: String, line: Int): Location =
        Location(uri = uri, range = range(line))

    private fun locationLink(uri: String, line: Int): LocationLink = LocationLink(
        originSelectionRange = range(line),
        targetUri = uri,
        targetRange = range(line + 10),
        targetSelectionRange = range(line + 10)
    )

    override suspend fun initialize(params: InitializeParams): InitializeResult = InitializeResult(
        capabilities = ServerCapabilities(
            hoverProvider = BooleanOr.BooleanValue(true),
            definitionProvider = BooleanOr.BooleanValue(true),
            declarationProvider = BooleanOr.BooleanValue(true),
            typeDefinitionProvider = BooleanOr.BooleanValue(true),
            implementationProvider = BooleanOr.BooleanValue(true),
            referencesProvider = BooleanOr.BooleanValue(true),
            documentSymbolProvider = BooleanOr.BooleanValue(true),
            completionProvider = CompletionOptions(),
            signatureHelpProvider = com.monkopedia.lsp.SignatureHelpOptions(),
            documentHighlightProvider = BooleanOr.BooleanValue(true),
            documentFormattingProvider = BooleanOr.BooleanValue(true),
            renameProvider = BooleanOr.BooleanValue(true),
            codeActionProvider = BooleanOr.BooleanValue(true),
            codeLensProvider = com.monkopedia.lsp.CodeLensOptions(),
            foldingRangeProvider = BooleanOr.BooleanValue(true),
            inlayHintProvider = BooleanOr.BooleanValue(true)
        ),
        serverInfo = InitializeResultServerInfo(
            name = "ConformanceLanguageServer",
            version = "1.0.0"
        )
    )

    override suspend fun shutdown(): Nothing? = null

    // region union-branch-exhaustive methods

    override suspend fun textDocumentHover(params: HoverParams): Hover {
        val contents = when (params.position.line.toInt()) {
            Lines.SINGLE -> HoverContents.markdown("**markup** hover at line 0")

            Lines.ARRAY -> HoverContents.string("marked-string hover at line 1")

            else -> HoverContents.MarkedStringArray(
                listOf(
                    StringOr.StringValue("marked one"),
                    StringOr.StringValue("marked two")
                )
            )
        }
        return Hover(contents = contents, range = range(params.position.line.toInt()))
    }

    override suspend fun textDocumentDefinition(
        params: DefinitionParams
    ): TextDocumentDefinitionResult = when (params.position.line.toInt()) {
        Lines.SINGLE -> TextDocumentDefinitionResult.DefinitionValue(
            singleDefinition(Uri.MAIN, 0)
        )

        Lines.ARRAY -> TextDocumentDefinitionResult.DefinitionValue(
            arrayDefinition(Uri.MAIN)
        )

        else -> TextDocumentDefinitionResult.DefinitionLinkArray(linkDefinitions(Uri.MAIN))
    }

    override suspend fun textDocumentDeclaration(
        params: DeclarationParams
    ): TextDocumentDeclarationResult = when (params.position.line.toInt()) {
        Lines.SINGLE -> TextDocumentDeclarationResult.DeclarationValue(
            singleDeclaration(Uri.MAIN, 0)
        )

        Lines.ARRAY -> TextDocumentDeclarationResult.DeclarationValue(
            arrayDeclaration(Uri.MAIN)
        )

        else -> TextDocumentDeclarationResult.DeclarationLinkArray(linkDeclarations(Uri.MAIN))
    }

    override suspend fun textDocumentTypeDefinition(
        params: TypeDefinitionParams
    ): TextDocumentTypeDefinitionResult = when (params.position.line.toInt()) {
        Lines.SINGLE -> TextDocumentTypeDefinitionResult.DefinitionValue(
            singleDefinition(Uri.MAIN, 0)
        )

        Lines.ARRAY -> TextDocumentTypeDefinitionResult.DefinitionValue(
            arrayDefinition(Uri.MAIN)
        )

        else -> TextDocumentTypeDefinitionResult.DefinitionLinkArray(linkDefinitions(Uri.MAIN))
    }

    override suspend fun textDocumentImplementation(
        params: ImplementationParams
    ): TextDocumentImplementationResult = when (params.position.line.toInt()) {
        Lines.SINGLE -> TextDocumentImplementationResult.DefinitionValue(
            singleDefinition(Uri.MAIN, 0)
        )

        Lines.ARRAY -> TextDocumentImplementationResult.DefinitionValue(
            arrayDefinition(Uri.MAIN)
        )

        else -> TextDocumentImplementationResult.DefinitionLinkArray(linkDefinitions(Uri.MAIN))
    }

    override suspend fun textDocumentReferences(params: ReferenceParams): List<Location> =
        listOf(location(Uri.MAIN, 0), location(Uri.MAIN, 1), location(Uri.MAIN, 2))

    override suspend fun textDocumentCompletion(
        params: CompletionParams
    ): TextDocumentCompletionResult = when (params.position.line.toInt()) {
        Lines.SINGLE -> TextDocumentCompletionResult.CompletionListValue(
            CompletionList(
                isIncomplete = false,
                items = listOf(
                    CompletionItem(label = "fromList", kind = CompletionItemKind.FUNCTION)
                )
            )
        )

        else -> TextDocumentCompletionResult.CompletionItemArray(
            listOf(
                CompletionItem(label = "fromArrayA", kind = CompletionItemKind.VALUE),
                CompletionItem(label = "fromArrayB", kind = CompletionItemKind.VALUE)
            )
        )
    }

    override suspend fun textDocumentDocumentSymbol(
        params: DocumentSymbolParams
    ): TextDocumentDocumentSymbolResult = if (params.textDocument.uri.endsWith("#flat")) {
        TextDocumentDocumentSymbolResult.SymbolInformationArray(
            listOf(
                SymbolInformation(
                    name = "flatSymbol",
                    kind = SymbolKind.FUNCTION,
                    location = location(params.textDocument.uri, 0)
                )
            )
        )
    } else {
        TextDocumentDocumentSymbolResult.DocumentSymbolArray(
            listOf(
                DocumentSymbol(
                    name = "hierarchicalSymbol",
                    kind = SymbolKind.CLASS,
                    range = range(0),
                    selectionRange = range(0),
                    children = listOf(
                        DocumentSymbol(
                            name = "childMethod",
                            kind = SymbolKind.METHOD,
                            range = range(1),
                            selectionRange = range(1)
                        )
                    )
                )
            )
        )
    }

    // endregion

    // region simple well-formed canned methods

    override suspend fun textDocumentSignatureHelp(params: SignatureHelpParams): SignatureHelp =
        SignatureHelp(
            signatures = listOf(
                SignatureInformation(
                    label = "fun conformance(value: Int): Int",
                    documentation = StringOr.StringValue("canned signature")
                )
            ),
            activeSignature = 0u,
            activeParameter = 0u
        )

    override suspend fun textDocumentDocumentHighlight(
        params: DocumentHighlightParams
    ): List<DocumentHighlight> = listOf(
        DocumentHighlight(range = range(0), kind = DocumentHighlightKind.TEXT),
        DocumentHighlight(range = range(1), kind = DocumentHighlightKind.WRITE)
    )

    override suspend fun textDocumentFormatting(params: DocumentFormattingParams): List<TextEdit> =
        listOf(TextEdit(range = range(0), newText = "formatted\n"))

    override suspend fun textDocumentRename(params: RenameParams): WorkspaceEdit = WorkspaceEdit(
        changes = mapOf(
            Uri.MAIN to listOf(TextEdit(range = range(0), newText = params.newName))
        )
    )

    override suspend fun textDocumentCodeAction(
        params: CodeActionParams
    ): List<com.monkopedia.lsp.TextDocumentCodeActionResult> = listOf(
        CodeAction(title = "Canned quick fix"),
        Command(title = "Canned command", command = "conformance.command")
    )

    override suspend fun textDocumentCodeLens(params: CodeLensParams): List<CodeLens> = listOf(
        CodeLens(
            range = range(0),
            command = Command(title = "Canned lens", command = "conformance.lens")
        )
    )

    override suspend fun textDocumentFoldingRange(params: FoldingRangeParams): List<FoldingRange> =
        listOf(
            FoldingRange(startLine = 0u, endLine = 5u, kind = FoldingRangeKind.REGION)
        )

    override suspend fun textDocumentSemanticTokensFull(
        params: SemanticTokensParams
    ): SemanticTokens = SemanticTokens(
        resultId = "conformance-1",
        data = listOf(0u, 0u, 4u, 0u, 0u)
    )

    override suspend fun textDocumentInlayHint(params: InlayHintParams): List<InlayHint> = listOf(
        InlayHint(
            position = pos(0, 4),
            label = StringOr.StringValue(": Int")
        )
    )

    override suspend fun inlayHintResolve(params: InlayHint): InlayHint = params.copy(
        tooltip = StringOr.StringValue("resolved tooltip")
    )

    override suspend fun textDocumentDocumentColor(
        params: DocumentColorParams
    ): List<ColorInformation> = listOf(
        ColorInformation(
            range = range(0),
            color = Color(red = 1.0, green = 0.0, blue = 0.0, alpha = 1.0)
        )
    )

    override suspend fun textDocumentColorPresentation(
        params: ColorPresentationParams
    ): List<ColorPresentation> = listOf(
        ColorPresentation(label = "red")
    )

    override suspend fun textDocumentSelectionRange(
        params: SelectionRangeParams
    ): List<SelectionRange> = listOf(
        SelectionRange(range = range(0), parent = SelectionRange(range = range(0)))
    )

    override suspend fun textDocumentDocumentLink(params: DocumentLinkParams): List<DocumentLink> =
        listOf(
            DocumentLink(range = range(0), target = "https://example.com/conformance")
        )

    override suspend fun documentLinkResolve(params: DocumentLink): DocumentLink = params.copy(
        tooltip = "resolved link tooltip"
    )

    override suspend fun textDocumentRangeFormatting(
        params: DocumentRangeFormattingParams
    ): List<TextEdit> = listOf(TextEdit(range = range(0), newText = "range-formatted\n"))

    override suspend fun textDocumentRangesFormatting(
        params: DocumentRangesFormattingParams
    ): List<TextEdit> = listOf(TextEdit(range = range(0), newText = "ranges-formatted\n"))

    override suspend fun textDocumentOnTypeFormatting(
        params: DocumentOnTypeFormattingParams
    ): List<TextEdit> = listOf(TextEdit(range = range(0), newText = "on-type-formatted\n"))

    override suspend fun textDocumentPrepareRename(
        params: PrepareRenameParams
    ): PrepareRenameResult = PrepareRenameResultRange(
        range = range(params.position.line.toInt()),
        placeholder = "newName"
    )

    override suspend fun textDocumentWillSaveWaitUntil(
        params: WillSaveTextDocumentParams
    ): List<TextEdit> = listOf(TextEdit(range = range(0), newText = "// pre-save\n"))

    override suspend fun completionItemResolve(params: CompletionItem): CompletionItem =
        params.copy(detail = "resolved: ${params.label}")

    override suspend fun codeActionResolve(params: CodeAction): CodeAction =
        params.copy(isPreferred = true)

    override suspend fun codeLensResolve(params: CodeLens): CodeLens = params.copy(
        command = Command(title = "Resolved lens", command = "conformance.lens.resolved")
    )

    override suspend fun textDocumentSemanticTokensFullDelta(
        params: SemanticTokensDeltaParams
    ): TextDocumentSemanticTokensFullDeltaResult = SemanticTokens(
        resultId = "conformance-delta-${params.previousResultId}",
        data = listOf(0u, 0u, 4u, 0u, 0u)
    )

    override suspend fun textDocumentSemanticTokensRange(
        params: SemanticTokensRangeParams
    ): SemanticTokens = SemanticTokens(
        resultId = "conformance-range",
        data = listOf(0u, 0u, 4u, 0u, 0u)
    )

    override suspend fun textDocumentLinkedEditingRange(
        params: LinkedEditingRangeParams
    ): LinkedEditingRanges = LinkedEditingRanges(
        ranges = listOf(range(0), range(1)),
        wordPattern = "[a-zA-Z_][a-zA-Z0-9_]*"
    )

    override suspend fun textDocumentMoniker(params: MonikerParams): List<Moniker> = listOf(
        Moniker(
            scheme = "tsc",
            identifier = "conformance#symbol",
            unique = UniquenessLevel.SCHEME,
            kind = MonikerKind.LOCAL
        )
    )

    override suspend fun textDocumentInlineValue(params: InlineValueParams): List<InlineValue> =
        listOf(InlineValueText(range = range(0), text = "42"))

    override suspend fun textDocumentInlineCompletion(
        params: InlineCompletionParams
    ): TextDocumentInlineCompletionResult =
        TextDocumentInlineCompletionResult.InlineCompletionListValue(
            InlineCompletionList(
                items = listOf(
                    InlineCompletionItem(
                        insertText = StringOr.StringValue("inline-completion")
                    )
                )
            )
        )

    override suspend fun textDocumentDiagnostic(
        params: DocumentDiagnosticParams
    ): DocumentDiagnosticReport = RelatedFullDocumentDiagnosticReport(
        kind = "full",
        resultId = "conformance-doc-diag",
        items = listOf(
            Diagnostic(range = range(0), message = "canned doc diagnostic")
        )
    )

    override suspend fun workspaceDiagnostic(
        params: WorkspaceDiagnosticParams
    ): WorkspaceDiagnosticReport = WorkspaceDiagnosticReport(
        items = listOf(
            WorkspaceFullDocumentDiagnosticReport(
                kind = "full",
                resultId = "conformance-ws-diag",
                items = listOf(
                    Diagnostic(range = range(0), message = "canned workspace diagnostic")
                ),
                uri = Uri.MAIN,
                version = 1
            )
        )
    )

    // ---- Call hierarchy ----

    override suspend fun textDocumentPrepareCallHierarchy(
        params: CallHierarchyPrepareParams
    ): List<CallHierarchyItem> = listOf(callHierarchyItem("preparedCall"))

    override suspend fun callHierarchyIncomingCalls(
        params: CallHierarchyIncomingCallsParams
    ): List<CallHierarchyIncomingCall> = listOf(
        CallHierarchyIncomingCall(
            from = callHierarchyItem("incomingCaller"),
            fromRanges = listOf(range(0))
        )
    )

    override suspend fun callHierarchyOutgoingCalls(
        params: CallHierarchyOutgoingCallsParams
    ): List<CallHierarchyOutgoingCall> = listOf(
        CallHierarchyOutgoingCall(
            to = callHierarchyItem("outgoingCallee"),
            fromRanges = listOf(range(0))
        )
    )

    // ---- Type hierarchy ----

    override suspend fun textDocumentPrepareTypeHierarchy(
        params: TypeHierarchyPrepareParams
    ): List<TypeHierarchyItem> = listOf(typeHierarchyItem("preparedType"))

    override suspend fun typeHierarchySupertypes(
        params: TypeHierarchySupertypesParams
    ): List<TypeHierarchyItem> = listOf(typeHierarchyItem("Supertype"))

    override suspend fun typeHierarchySubtypes(
        params: TypeHierarchySubtypesParams
    ): List<TypeHierarchyItem> = listOf(typeHierarchyItem("Subtype"))

    // ---- Workspace operations ----

    override suspend fun workspaceSymbol(params: WorkspaceSymbolParams): LSPAny {
        // workspace/symbol's result is a JsonElement union (SymbolInformation[] |
        // WorkspaceSymbol[]). Emit a SymbolInformation[] for simplicity — lsp4j
        // accepts it.
        return kotlinx.serialization.json.buildJsonArray {
            add(
                buildJsonObject {
                    put("name", JsonPrimitive("ws:${params.query}"))
                    put("kind", JsonPrimitive(SymbolKind.FUNCTION.value.toInt()))
                    put(
                        "location",
                        buildJsonObject {
                            put("uri", JsonPrimitive(Uri.MAIN))
                            put(
                                "range",
                                buildJsonObject {
                                    put(
                                        "start",
                                        buildJsonObject {
                                            put("line", JsonPrimitive(0))
                                            put("character", JsonPrimitive(0))
                                        }
                                    )
                                    put(
                                        "end",
                                        buildJsonObject {
                                            put("line", JsonPrimitive(0))
                                            put("character", JsonPrimitive(4))
                                        }
                                    )
                                }
                            )
                        }
                    )
                }
            )
        }
    }

    override suspend fun workspaceSymbolResolve(params: WorkspaceSymbol): WorkspaceSymbol =
        params.copy(containerName = "resolved")

    override suspend fun workspaceExecuteCommand(params: ExecuteCommandParams): LSPAny =
        buildJsonObject {
            put("command", JsonPrimitive(params.command))
            put("status", JsonPrimitive("ok"))
        }

    override suspend fun workspaceWillCreateFiles(params: CreateFilesParams): WorkspaceEdit =
        WorkspaceEdit(
            changes = mapOf(
                Uri.MAIN to listOf(TextEdit(range = range(0), newText = "// will-create\n"))
            )
        )

    override suspend fun workspaceWillRenameFiles(params: RenameFilesParams): WorkspaceEdit =
        WorkspaceEdit(
            changes = mapOf(
                Uri.MAIN to listOf(TextEdit(range = range(0), newText = "// will-rename\n"))
            )
        )

    override suspend fun workspaceWillDeleteFiles(params: DeleteFilesParams): WorkspaceEdit =
        WorkspaceEdit(
            changes = mapOf(
                Uri.MAIN to listOf(TextEdit(range = range(0), newText = "// will-delete\n"))
            )
        )

    private fun callHierarchyItem(name: String): CallHierarchyItem = CallHierarchyItem(
        name = name,
        kind = SymbolKind.FUNCTION,
        uri = Uri.MAIN,
        range = range(0),
        selectionRange = range(0)
    )

    private fun typeHierarchyItem(name: String): TypeHierarchyItem = TypeHierarchyItem(
        name = name,
        kind = SymbolKind.CLASS,
        uri = Uri.MAIN,
        range = range(0),
        selectionRange = range(0)
    )

    // endregion

    // region notifications — record receipt for assertion

    override suspend fun initialized(params: InitializedParams) {
        record("initialized", "")
    }

    override suspend fun exit() {
        record("exit", "")
    }

    override suspend fun setTrace(params: SetTraceParams) {
        record("\$/setTrace", params.value.name)
    }

    override suspend fun progress(params: ProgressParams) {
        record("\$/progress", "token=${params.token}")
    }

    override suspend fun windowWorkDoneProgressCancel(params: WorkDoneProgressCancelParams) {
        record("window/workDoneProgress/cancel", "token=${params.token}")
    }

    override suspend fun textDocumentDidOpen(params: DidOpenTextDocumentParams) {
        record("textDocument/didOpen", "uri=${params.textDocument.uri}")
    }

    override suspend fun textDocumentDidChange(params: DidChangeTextDocumentParams) {
        record("textDocument/didChange", "uri=${params.textDocument.uri}")
    }

    override suspend fun textDocumentDidClose(params: DidCloseTextDocumentParams) {
        record("textDocument/didClose", "uri=${params.textDocument.uri}")
    }

    override suspend fun textDocumentDidSave(params: DidSaveTextDocumentParams) {
        record("textDocument/didSave", "uri=${params.textDocument.uri}")
    }

    override suspend fun textDocumentWillSave(params: WillSaveTextDocumentParams) {
        record("textDocument/willSave", "uri=${params.textDocument.uri}")
    }

    override suspend fun workspaceDidChangeConfiguration(params: DidChangeConfigurationParams) {
        record("workspace/didChangeConfiguration", params.settings.toString().take(64))
    }

    override suspend fun workspaceDidChangeWatchedFiles(params: DidChangeWatchedFilesParams) {
        record(
            "workspace/didChangeWatchedFiles",
            "changes=${params.changes.size}"
        )
    }

    override suspend fun workspaceDidChangeWorkspaceFolders(
        params: DidChangeWorkspaceFoldersParams
    ) {
        record(
            "workspace/didChangeWorkspaceFolders",
            "added=${params.event.added.size},removed=${params.event.removed.size}"
        )
    }

    override suspend fun workspaceDidCreateFiles(params: CreateFilesParams) {
        record("workspace/didCreateFiles", "files=${params.files.size}")
    }

    override suspend fun workspaceDidRenameFiles(params: RenameFilesParams) {
        record("workspace/didRenameFiles", "files=${params.files.size}")
    }

    override suspend fun workspaceDidDeleteFiles(params: DeleteFilesParams) {
        record("workspace/didDeleteFiles", "files=${params.files.size}")
    }

    override suspend fun notebookDocumentDidOpen(params: DidOpenNotebookDocumentParams) {
        record(
            "notebookDocument/didOpen",
            "uri=${params.notebookDocument.uri}"
        )
    }

    override suspend fun notebookDocumentDidChange(params: DidChangeNotebookDocumentParams) {
        record(
            "notebookDocument/didChange",
            "uri=${params.notebookDocument.uri}"
        )
    }

    override suspend fun notebookDocumentDidSave(params: DidSaveNotebookDocumentParams) {
        record(
            "notebookDocument/didSave",
            "uri=${params.notebookDocument.uri}"
        )
    }

    override suspend fun notebookDocumentDidClose(params: DidCloseNotebookDocumentParams) {
        record(
            "notebookDocument/didClose",
            "uri=${params.notebookDocument.uri}"
        )
    }

    // endregion

    // region branch-construction helpers

    private fun singleDefinition(uri: String, line: Int): Definition =
        SingleOrArray.single(location(uri, line))

    private fun arrayDefinition(uri: String): Definition =
        SingleOrArray.multiple(listOf(location(uri, 0), location(uri, 1)))

    private fun linkDefinitions(uri: String): List<DefinitionLink> =
        listOf(locationLink(uri, 0), locationLink(uri, 1))

    private fun singleDeclaration(uri: String, line: Int): Declaration =
        SingleOrArray.single(location(uri, line))

    private fun arrayDeclaration(uri: String): Declaration =
        SingleOrArray.multiple(listOf(location(uri, 0), location(uri, 1)))

    private fun linkDeclarations(uri: String): List<DeclarationLink> =
        listOf(locationLink(uri, 0), locationLink(uri, 1))

    // endregion
}
