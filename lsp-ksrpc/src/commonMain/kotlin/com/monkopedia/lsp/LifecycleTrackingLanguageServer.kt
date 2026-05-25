// Auto-generated from LSP metaModel.json — do not edit manually.
// Generator: lsp-codegen

@file:Suppress(
    "unused",
    "PropertyName",
    "ktlint:standard:class-naming",
    "ktlint:standard:filename",
    "ktlint:standard:max-line-length",
    "ktlint:standard:parameter-wrapping"
)

package com.monkopedia.lsp

import com.monkopedia.lsp.ksrpc.LifecycleState
import kotlinx.serialization.json.JsonElement

/**
 * Wraps a [KsrpcLanguageServer], delegating every method while advancing
 * [com.monkopedia.lsp.ksrpc.LifecycleState] on `initialized` / `shutdown` /
 * `exit`. Used by `connectAsLspServer(server, lifecycle)`.
 */
internal class LifecycleTrackingLanguageServer(
    private val delegate: KsrpcLanguageServer,
    private val lifecycle: LifecycleState
) : KsrpcLanguageServer {

    override suspend fun textDocumentImplementation(
        params: ImplementationParams
    ): TextDocumentImplementationResult = delegate.textDocumentImplementation(params)

    override suspend fun textDocumentTypeDefinition(
        params: TypeDefinitionParams
    ): TextDocumentTypeDefinitionResult = delegate.textDocumentTypeDefinition(params)

    override suspend fun textDocumentDocumentColor(
        params: DocumentColorParams
    ): List<ColorInformation> = delegate.textDocumentDocumentColor(params)

    override suspend fun textDocumentColorPresentation(
        params: ColorPresentationParams
    ): List<ColorPresentation> = delegate.textDocumentColorPresentation(params)

    override suspend fun textDocumentFoldingRange(params: FoldingRangeParams): List<FoldingRange> =
        delegate.textDocumentFoldingRange(params)

    override suspend fun textDocumentDeclaration(
        params: DeclarationParams
    ): TextDocumentDeclarationResult = delegate.textDocumentDeclaration(params)

    override suspend fun textDocumentSelectionRange(
        params: SelectionRangeParams
    ): List<SelectionRange> = delegate.textDocumentSelectionRange(params)

    override suspend fun textDocumentPrepareCallHierarchy(
        params: CallHierarchyPrepareParams
    ): List<CallHierarchyItem> = delegate.textDocumentPrepareCallHierarchy(params)

    override suspend fun callHierarchyIncomingCalls(
        params: CallHierarchyIncomingCallsParams
    ): List<CallHierarchyIncomingCall> = delegate.callHierarchyIncomingCalls(params)

    override suspend fun callHierarchyOutgoingCalls(
        params: CallHierarchyOutgoingCallsParams
    ): List<CallHierarchyOutgoingCall> = delegate.callHierarchyOutgoingCalls(params)

    override suspend fun textDocumentSemanticTokensFull(
        params: SemanticTokensParams
    ): SemanticTokens = delegate.textDocumentSemanticTokensFull(params)

    override suspend fun textDocumentSemanticTokensFullDelta(
        params: SemanticTokensDeltaParams
    ): TextDocumentSemanticTokensFullDeltaResult =
        delegate.textDocumentSemanticTokensFullDelta(params)

    override suspend fun textDocumentSemanticTokensRange(
        params: SemanticTokensRangeParams
    ): SemanticTokens = delegate.textDocumentSemanticTokensRange(params)

    override suspend fun textDocumentLinkedEditingRange(
        params: LinkedEditingRangeParams
    ): LinkedEditingRanges = delegate.textDocumentLinkedEditingRange(params)

    override suspend fun workspaceWillCreateFiles(params: CreateFilesParams): WorkspaceEdit =
        delegate.workspaceWillCreateFiles(params)

    override suspend fun workspaceWillRenameFiles(params: RenameFilesParams): WorkspaceEdit =
        delegate.workspaceWillRenameFiles(params)

    override suspend fun workspaceWillDeleteFiles(params: DeleteFilesParams): WorkspaceEdit =
        delegate.workspaceWillDeleteFiles(params)

    override suspend fun textDocumentMoniker(params: MonikerParams): List<Moniker> =
        delegate.textDocumentMoniker(params)

    override suspend fun textDocumentPrepareTypeHierarchy(
        params: TypeHierarchyPrepareParams
    ): List<TypeHierarchyItem> = delegate.textDocumentPrepareTypeHierarchy(params)

    override suspend fun typeHierarchySupertypes(
        params: TypeHierarchySupertypesParams
    ): List<TypeHierarchyItem> = delegate.typeHierarchySupertypes(params)

    override suspend fun typeHierarchySubtypes(
        params: TypeHierarchySubtypesParams
    ): List<TypeHierarchyItem> = delegate.typeHierarchySubtypes(params)

    override suspend fun textDocumentInlineValue(params: InlineValueParams): List<InlineValue> =
        delegate.textDocumentInlineValue(params)

    override suspend fun textDocumentInlayHint(params: InlayHintParams): List<InlayHint> =
        delegate.textDocumentInlayHint(params)

    override suspend fun inlayHintResolve(params: InlayHint): InlayHint =
        delegate.inlayHintResolve(params)

    override suspend fun textDocumentDiagnostic(
        params: DocumentDiagnosticParams
    ): DocumentDiagnosticReport = delegate.textDocumentDiagnostic(params)

    override suspend fun workspaceDiagnostic(
        params: WorkspaceDiagnosticParams
    ): WorkspaceDiagnosticReport = delegate.workspaceDiagnostic(params)

    override suspend fun textDocumentInlineCompletion(
        params: InlineCompletionParams
    ): TextDocumentInlineCompletionResult = delegate.textDocumentInlineCompletion(params)

    override suspend fun initialize(params: InitializeParams): InitializeResult =
        delegate.initialize(params)

    override suspend fun shutdown(): Nothing? {
        val result = delegate.shutdown()
        lifecycle.advanceTo(LifecycleState.Phase.SHUTTING_DOWN)
        return result
    }

    override suspend fun textDocumentWillSaveWaitUntil(
        params: WillSaveTextDocumentParams
    ): List<TextEdit> = delegate.textDocumentWillSaveWaitUntil(params)

    override suspend fun textDocumentCompletion(
        params: CompletionParams
    ): TextDocumentCompletionResult = delegate.textDocumentCompletion(params)

    override suspend fun completionItemResolve(params: CompletionItem): CompletionItem =
        delegate.completionItemResolve(params)

    override suspend fun textDocumentHover(params: HoverParams): Hover =
        delegate.textDocumentHover(params)

    override suspend fun textDocumentSignatureHelp(params: SignatureHelpParams): SignatureHelp =
        delegate.textDocumentSignatureHelp(params)

    override suspend fun textDocumentDefinition(
        params: DefinitionParams
    ): TextDocumentDefinitionResult = delegate.textDocumentDefinition(params)

    override suspend fun textDocumentReferences(params: ReferenceParams): List<Location> =
        delegate.textDocumentReferences(params)

    override suspend fun textDocumentDocumentHighlight(
        params: DocumentHighlightParams
    ): List<DocumentHighlight> = delegate.textDocumentDocumentHighlight(params)

    override suspend fun textDocumentDocumentSymbol(
        params: DocumentSymbolParams
    ): TextDocumentDocumentSymbolResult = delegate.textDocumentDocumentSymbol(params)

    override suspend fun textDocumentCodeAction(
        params: CodeActionParams
    ): List<TextDocumentCodeActionResult> = delegate.textDocumentCodeAction(params)

    override suspend fun codeActionResolve(params: CodeAction): CodeAction =
        delegate.codeActionResolve(params)

    override suspend fun workspaceSymbol(params: WorkspaceSymbolParams): JsonElement =
        delegate.workspaceSymbol(params)

    override suspend fun workspaceSymbolResolve(params: WorkspaceSymbol): WorkspaceSymbol =
        delegate.workspaceSymbolResolve(params)

    override suspend fun textDocumentCodeLens(params: CodeLensParams): List<CodeLens> =
        delegate.textDocumentCodeLens(params)

    override suspend fun codeLensResolve(params: CodeLens): CodeLens =
        delegate.codeLensResolve(params)

    override suspend fun textDocumentDocumentLink(params: DocumentLinkParams): List<DocumentLink> =
        delegate.textDocumentDocumentLink(params)

    override suspend fun documentLinkResolve(params: DocumentLink): DocumentLink =
        delegate.documentLinkResolve(params)

    override suspend fun textDocumentFormatting(params: DocumentFormattingParams): List<TextEdit> =
        delegate.textDocumentFormatting(params)

    override suspend fun textDocumentRangeFormatting(
        params: DocumentRangeFormattingParams
    ): List<TextEdit> = delegate.textDocumentRangeFormatting(params)

    override suspend fun textDocumentRangesFormatting(
        params: DocumentRangesFormattingParams
    ): List<TextEdit> = delegate.textDocumentRangesFormatting(params)

    override suspend fun textDocumentOnTypeFormatting(
        params: DocumentOnTypeFormattingParams
    ): List<TextEdit> = delegate.textDocumentOnTypeFormatting(params)

    override suspend fun textDocumentRename(params: RenameParams): WorkspaceEdit =
        delegate.textDocumentRename(params)

    override suspend fun textDocumentPrepareRename(
        params: PrepareRenameParams
    ): PrepareRenameResult = delegate.textDocumentPrepareRename(params)

    override suspend fun workspaceExecuteCommand(params: ExecuteCommandParams): LSPAny =
        delegate.workspaceExecuteCommand(params)

    override suspend fun workspaceDidChangeWorkspaceFolders(
        params: DidChangeWorkspaceFoldersParams
    ) {
        delegate.workspaceDidChangeWorkspaceFolders(params)
    }

    override suspend fun windowWorkDoneProgressCancel(params: WorkDoneProgressCancelParams) {
        delegate.windowWorkDoneProgressCancel(params)
    }

    override suspend fun workspaceDidCreateFiles(params: CreateFilesParams) {
        delegate.workspaceDidCreateFiles(params)
    }

    override suspend fun workspaceDidRenameFiles(params: RenameFilesParams) {
        delegate.workspaceDidRenameFiles(params)
    }

    override suspend fun workspaceDidDeleteFiles(params: DeleteFilesParams) {
        delegate.workspaceDidDeleteFiles(params)
    }

    override suspend fun notebookDocumentDidOpen(params: DidOpenNotebookDocumentParams) {
        delegate.notebookDocumentDidOpen(params)
    }

    override suspend fun notebookDocumentDidChange(params: DidChangeNotebookDocumentParams) {
        delegate.notebookDocumentDidChange(params)
    }

    override suspend fun notebookDocumentDidSave(params: DidSaveNotebookDocumentParams) {
        delegate.notebookDocumentDidSave(params)
    }

    override suspend fun notebookDocumentDidClose(params: DidCloseNotebookDocumentParams) {
        delegate.notebookDocumentDidClose(params)
    }

    override suspend fun initialized(params: InitializedParams) {
        delegate.initialized(params)
        lifecycle.advanceTo(LifecycleState.Phase.INITIALIZED)
    }

    override suspend fun exit() {
        delegate.exit()
        lifecycle.advanceTo(LifecycleState.Phase.EXITED)
    }

    override suspend fun workspaceDidChangeConfiguration(params: DidChangeConfigurationParams) {
        delegate.workspaceDidChangeConfiguration(params)
    }

    override suspend fun textDocumentDidOpen(params: DidOpenTextDocumentParams) {
        delegate.textDocumentDidOpen(params)
    }

    override suspend fun textDocumentDidChange(params: DidChangeTextDocumentParams) {
        delegate.textDocumentDidChange(params)
    }

    override suspend fun textDocumentDidClose(params: DidCloseTextDocumentParams) {
        delegate.textDocumentDidClose(params)
    }

    override suspend fun textDocumentDidSave(params: DidSaveTextDocumentParams) {
        delegate.textDocumentDidSave(params)
    }

    override suspend fun textDocumentWillSave(params: WillSaveTextDocumentParams) {
        delegate.textDocumentWillSave(params)
    }

    override suspend fun workspaceDidChangeWatchedFiles(params: DidChangeWatchedFilesParams) {
        delegate.workspaceDidChangeWatchedFiles(params)
    }

    override suspend fun setTrace(params: SetTraceParams) {
        delegate.setTrace(params)
    }

    override suspend fun progress(params: ProgressParams) {
        delegate.progress(params)
    }
}
