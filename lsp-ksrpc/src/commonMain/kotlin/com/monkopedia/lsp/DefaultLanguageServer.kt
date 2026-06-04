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

import kotlinx.serialization.json.JsonElement

/**
 * Default [KsrpcLanguageServer]: unimplemented requests throw NotImplementedError; notifications are no-ops.
 * Subclass and override only what you need.
 */
open class DefaultLanguageServer : KsrpcLanguageServer {

    override suspend fun textDocumentImplementation(
        params: ImplementationParams
    ): TextDocumentImplementationResult? =
        throw NotImplementedError("textDocumentImplementation not implemented")

    override suspend fun textDocumentTypeDefinition(
        params: TypeDefinitionParams
    ): TextDocumentTypeDefinitionResult? =
        throw NotImplementedError("textDocumentTypeDefinition not implemented")

    override suspend fun textDocumentDocumentColor(
        params: DocumentColorParams
    ): List<ColorInformation> =
        throw NotImplementedError("textDocumentDocumentColor not implemented")

    override suspend fun textDocumentColorPresentation(
        params: ColorPresentationParams
    ): List<ColorPresentation> =
        throw NotImplementedError("textDocumentColorPresentation not implemented")

    override suspend fun textDocumentFoldingRange(params: FoldingRangeParams): List<FoldingRange>? =
        throw NotImplementedError("textDocumentFoldingRange not implemented")

    override suspend fun textDocumentDeclaration(
        params: DeclarationParams
    ): TextDocumentDeclarationResult? =
        throw NotImplementedError("textDocumentDeclaration not implemented")

    override suspend fun textDocumentSelectionRange(
        params: SelectionRangeParams
    ): List<SelectionRange>? =
        throw NotImplementedError("textDocumentSelectionRange not implemented")

    override suspend fun textDocumentPrepareCallHierarchy(
        params: CallHierarchyPrepareParams
    ): List<CallHierarchyItem>? =
        throw NotImplementedError("textDocumentPrepareCallHierarchy not implemented")

    override suspend fun callHierarchyIncomingCalls(
        params: CallHierarchyIncomingCallsParams
    ): List<CallHierarchyIncomingCall>? =
        throw NotImplementedError("callHierarchyIncomingCalls not implemented")

    override suspend fun callHierarchyOutgoingCalls(
        params: CallHierarchyOutgoingCallsParams
    ): List<CallHierarchyOutgoingCall>? =
        throw NotImplementedError("callHierarchyOutgoingCalls not implemented")

    override suspend fun textDocumentSemanticTokensFull(
        params: SemanticTokensParams
    ): SemanticTokens? = throw NotImplementedError("textDocumentSemanticTokensFull not implemented")

    override suspend fun textDocumentSemanticTokensFullDelta(
        params: SemanticTokensDeltaParams
    ): TextDocumentSemanticTokensFullDeltaResult? =
        throw NotImplementedError("textDocumentSemanticTokensFullDelta not implemented")

    override suspend fun textDocumentSemanticTokensRange(
        params: SemanticTokensRangeParams
    ): SemanticTokens? =
        throw NotImplementedError("textDocumentSemanticTokensRange not implemented")

    override suspend fun textDocumentLinkedEditingRange(
        params: LinkedEditingRangeParams
    ): LinkedEditingRanges? =
        throw NotImplementedError("textDocumentLinkedEditingRange not implemented")

    override suspend fun workspaceWillCreateFiles(params: CreateFilesParams): WorkspaceEdit? =
        throw NotImplementedError("workspaceWillCreateFiles not implemented")

    override suspend fun workspaceWillRenameFiles(params: RenameFilesParams): WorkspaceEdit? =
        throw NotImplementedError("workspaceWillRenameFiles not implemented")

    override suspend fun workspaceWillDeleteFiles(params: DeleteFilesParams): WorkspaceEdit? =
        throw NotImplementedError("workspaceWillDeleteFiles not implemented")

    override suspend fun textDocumentMoniker(params: MonikerParams): List<Moniker>? =
        throw NotImplementedError("textDocumentMoniker not implemented")

    override suspend fun textDocumentPrepareTypeHierarchy(
        params: TypeHierarchyPrepareParams
    ): List<TypeHierarchyItem>? =
        throw NotImplementedError("textDocumentPrepareTypeHierarchy not implemented")

    override suspend fun typeHierarchySupertypes(
        params: TypeHierarchySupertypesParams
    ): List<TypeHierarchyItem>? =
        throw NotImplementedError("typeHierarchySupertypes not implemented")

    override suspend fun typeHierarchySubtypes(
        params: TypeHierarchySubtypesParams
    ): List<TypeHierarchyItem>? = throw NotImplementedError("typeHierarchySubtypes not implemented")

    override suspend fun textDocumentInlineValue(params: InlineValueParams): List<InlineValue>? =
        throw NotImplementedError("textDocumentInlineValue not implemented")

    override suspend fun textDocumentInlayHint(params: InlayHintParams): List<InlayHint>? =
        throw NotImplementedError("textDocumentInlayHint not implemented")

    override suspend fun inlayHintResolve(params: InlayHint): InlayHint =
        throw NotImplementedError("inlayHintResolve not implemented")

    override suspend fun textDocumentDiagnostic(
        params: DocumentDiagnosticParams
    ): DocumentDiagnosticReport =
        throw NotImplementedError("textDocumentDiagnostic not implemented")

    override suspend fun workspaceDiagnostic(
        params: WorkspaceDiagnosticParams
    ): WorkspaceDiagnosticReport = throw NotImplementedError("workspaceDiagnostic not implemented")

    override suspend fun textDocumentInlineCompletion(
        params: InlineCompletionParams
    ): TextDocumentInlineCompletionResult? =
        throw NotImplementedError("textDocumentInlineCompletion not implemented")

    override suspend fun initialize(params: InitializeParams): InitializeResult =
        throw NotImplementedError("initialize not implemented")

    override suspend fun shutdown(): Nothing? =
        throw NotImplementedError("shutdown not implemented")

    override suspend fun textDocumentWillSaveWaitUntil(
        params: WillSaveTextDocumentParams
    ): List<TextEdit>? = throw NotImplementedError("textDocumentWillSaveWaitUntil not implemented")

    override suspend fun textDocumentCompletion(
        params: CompletionParams
    ): TextDocumentCompletionResult? =
        throw NotImplementedError("textDocumentCompletion not implemented")

    override suspend fun completionItemResolve(params: CompletionItem): CompletionItem =
        throw NotImplementedError("completionItemResolve not implemented")

    override suspend fun textDocumentHover(params: HoverParams): Hover? =
        throw NotImplementedError("textDocumentHover not implemented")

    override suspend fun textDocumentSignatureHelp(params: SignatureHelpParams): SignatureHelp? =
        throw NotImplementedError("textDocumentSignatureHelp not implemented")

    override suspend fun textDocumentDefinition(
        params: DefinitionParams
    ): TextDocumentDefinitionResult? =
        throw NotImplementedError("textDocumentDefinition not implemented")

    override suspend fun textDocumentReferences(params: ReferenceParams): List<Location>? =
        throw NotImplementedError("textDocumentReferences not implemented")

    override suspend fun textDocumentDocumentHighlight(
        params: DocumentHighlightParams
    ): List<DocumentHighlight>? =
        throw NotImplementedError("textDocumentDocumentHighlight not implemented")

    override suspend fun textDocumentDocumentSymbol(
        params: DocumentSymbolParams
    ): TextDocumentDocumentSymbolResult? =
        throw NotImplementedError("textDocumentDocumentSymbol not implemented")

    override suspend fun textDocumentCodeAction(
        params: CodeActionParams
    ): List<TextDocumentCodeActionResult>? =
        throw NotImplementedError("textDocumentCodeAction not implemented")

    override suspend fun codeActionResolve(params: CodeAction): CodeAction =
        throw NotImplementedError("codeActionResolve not implemented")

    override suspend fun workspaceSymbol(params: WorkspaceSymbolParams): JsonElement? =
        throw NotImplementedError("workspaceSymbol not implemented")

    override suspend fun workspaceSymbolResolve(params: WorkspaceSymbol): WorkspaceSymbol =
        throw NotImplementedError("workspaceSymbolResolve not implemented")

    override suspend fun textDocumentCodeLens(params: CodeLensParams): List<CodeLens>? =
        throw NotImplementedError("textDocumentCodeLens not implemented")

    override suspend fun codeLensResolve(params: CodeLens): CodeLens =
        throw NotImplementedError("codeLensResolve not implemented")

    override suspend fun textDocumentDocumentLink(params: DocumentLinkParams): List<DocumentLink>? =
        throw NotImplementedError("textDocumentDocumentLink not implemented")

    override suspend fun documentLinkResolve(params: DocumentLink): DocumentLink =
        throw NotImplementedError("documentLinkResolve not implemented")

    override suspend fun textDocumentFormatting(params: DocumentFormattingParams): List<TextEdit>? =
        throw NotImplementedError("textDocumentFormatting not implemented")

    override suspend fun textDocumentRangeFormatting(
        params: DocumentRangeFormattingParams
    ): List<TextEdit>? = throw NotImplementedError("textDocumentRangeFormatting not implemented")

    override suspend fun textDocumentRangesFormatting(
        params: DocumentRangesFormattingParams
    ): List<TextEdit>? = throw NotImplementedError("textDocumentRangesFormatting not implemented")

    override suspend fun textDocumentOnTypeFormatting(
        params: DocumentOnTypeFormattingParams
    ): List<TextEdit>? = throw NotImplementedError("textDocumentOnTypeFormatting not implemented")

    override suspend fun textDocumentRename(params: RenameParams): WorkspaceEdit? =
        throw NotImplementedError("textDocumentRename not implemented")

    override suspend fun textDocumentPrepareRename(
        params: PrepareRenameParams
    ): PrepareRenameResult? = throw NotImplementedError("textDocumentPrepareRename not implemented")

    override suspend fun workspaceExecuteCommand(params: ExecuteCommandParams): LSPAny? =
        throw NotImplementedError("workspaceExecuteCommand not implemented")

    override suspend fun workspaceDidChangeWorkspaceFolders(
        params: DidChangeWorkspaceFoldersParams
    ) {
        // No-op by default; override to handle this notification.
    }

    override suspend fun windowWorkDoneProgressCancel(params: WorkDoneProgressCancelParams) {
        // No-op by default; override to handle this notification.
    }

    override suspend fun workspaceDidCreateFiles(params: CreateFilesParams) {
        // No-op by default; override to handle this notification.
    }

    override suspend fun workspaceDidRenameFiles(params: RenameFilesParams) {
        // No-op by default; override to handle this notification.
    }

    override suspend fun workspaceDidDeleteFiles(params: DeleteFilesParams) {
        // No-op by default; override to handle this notification.
    }

    override suspend fun notebookDocumentDidOpen(params: DidOpenNotebookDocumentParams) {
        // No-op by default; override to handle this notification.
    }

    override suspend fun notebookDocumentDidChange(params: DidChangeNotebookDocumentParams) {
        // No-op by default; override to handle this notification.
    }

    override suspend fun notebookDocumentDidSave(params: DidSaveNotebookDocumentParams) {
        // No-op by default; override to handle this notification.
    }

    override suspend fun notebookDocumentDidClose(params: DidCloseNotebookDocumentParams) {
        // No-op by default; override to handle this notification.
    }

    override suspend fun initialized(params: InitializedParams) {
        // No-op by default; override to handle this notification.
    }

    override suspend fun exit() {
        // No-op by default; override to handle this notification.
    }

    override suspend fun workspaceDidChangeConfiguration(params: DidChangeConfigurationParams) {
        // No-op by default; override to handle this notification.
    }

    override suspend fun textDocumentDidOpen(params: DidOpenTextDocumentParams) {
        // No-op by default; override to handle this notification.
    }

    override suspend fun textDocumentDidChange(params: DidChangeTextDocumentParams) {
        // No-op by default; override to handle this notification.
    }

    override suspend fun textDocumentDidClose(params: DidCloseTextDocumentParams) {
        // No-op by default; override to handle this notification.
    }

    override suspend fun textDocumentDidSave(params: DidSaveTextDocumentParams) {
        // No-op by default; override to handle this notification.
    }

    override suspend fun textDocumentWillSave(params: WillSaveTextDocumentParams) {
        // No-op by default; override to handle this notification.
    }

    override suspend fun workspaceDidChangeWatchedFiles(params: DidChangeWatchedFilesParams) {
        // No-op by default; override to handle this notification.
    }

    override suspend fun setTrace(params: SetTraceParams) {
        // No-op by default; override to handle this notification.
    }

    override suspend fun progress(params: ProgressParams) {
        // No-op by default; override to handle this notification.
    }
}
