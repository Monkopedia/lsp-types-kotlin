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

/**
 * Default LanguageServer where every method throws NotImplementedError.
 * Subclass and override only what you need.
 */
open class DefaultLanguageServer : LanguageServer {

    override suspend fun textDocumentImplementation(
        params: ImplementationParams
    ): kotlinx.serialization.json.JsonElement =
        throw NotImplementedError("textDocumentImplementation not implemented")

    override suspend fun textDocumentTypeDefinition(
        params: TypeDefinitionParams
    ): kotlinx.serialization.json.JsonElement =
        throw NotImplementedError("textDocumentTypeDefinition not implemented")

    override suspend fun textDocumentDocumentColor(
        params: DocumentColorParams
    ): List<ColorInformation> =
        throw NotImplementedError("textDocumentDocumentColor not implemented")

    override suspend fun textDocumentColorPresentation(
        params: ColorPresentationParams
    ): List<ColorPresentation> =
        throw NotImplementedError("textDocumentColorPresentation not implemented")

    override suspend fun textDocumentFoldingRange(params: FoldingRangeParams): List<FoldingRange> =
        throw NotImplementedError("textDocumentFoldingRange not implemented")

    override suspend fun textDocumentDeclaration(
        params: DeclarationParams
    ): kotlinx.serialization.json.JsonElement =
        throw NotImplementedError("textDocumentDeclaration not implemented")

    override suspend fun textDocumentSelectionRange(
        params: SelectionRangeParams
    ): List<SelectionRange> =
        throw NotImplementedError("textDocumentSelectionRange not implemented")

    override suspend fun textDocumentPrepareCallHierarchy(
        params: CallHierarchyPrepareParams
    ): List<CallHierarchyItem> =
        throw NotImplementedError("textDocumentPrepareCallHierarchy not implemented")

    override suspend fun callHierarchyIncomingCalls(
        params: CallHierarchyIncomingCallsParams
    ): List<CallHierarchyIncomingCall> =
        throw NotImplementedError("callHierarchyIncomingCalls not implemented")

    override suspend fun callHierarchyOutgoingCalls(
        params: CallHierarchyOutgoingCallsParams
    ): List<CallHierarchyOutgoingCall> =
        throw NotImplementedError("callHierarchyOutgoingCalls not implemented")

    override suspend fun textDocumentSemanticTokensFull(
        params: SemanticTokensParams
    ): SemanticTokens = throw NotImplementedError("textDocumentSemanticTokensFull not implemented")

    override suspend fun textDocumentSemanticTokensFullDelta(
        params: SemanticTokensDeltaParams
    ): TextDocumentSemanticTokensFullDeltaResult =
        throw NotImplementedError("textDocumentSemanticTokensFullDelta not implemented")

    override suspend fun textDocumentSemanticTokensRange(
        params: SemanticTokensRangeParams
    ): SemanticTokens = throw NotImplementedError("textDocumentSemanticTokensRange not implemented")

    override suspend fun textDocumentLinkedEditingRange(
        params: LinkedEditingRangeParams
    ): LinkedEditingRanges =
        throw NotImplementedError("textDocumentLinkedEditingRange not implemented")

    override suspend fun workspaceWillCreateFiles(params: CreateFilesParams): WorkspaceEdit =
        throw NotImplementedError("workspaceWillCreateFiles not implemented")

    override suspend fun workspaceWillRenameFiles(params: RenameFilesParams): WorkspaceEdit =
        throw NotImplementedError("workspaceWillRenameFiles not implemented")

    override suspend fun workspaceWillDeleteFiles(params: DeleteFilesParams): WorkspaceEdit =
        throw NotImplementedError("workspaceWillDeleteFiles not implemented")

    override suspend fun textDocumentMoniker(params: MonikerParams): List<Moniker> =
        throw NotImplementedError("textDocumentMoniker not implemented")

    override suspend fun textDocumentPrepareTypeHierarchy(
        params: TypeHierarchyPrepareParams
    ): List<TypeHierarchyItem> =
        throw NotImplementedError("textDocumentPrepareTypeHierarchy not implemented")

    override suspend fun typeHierarchySupertypes(
        params: TypeHierarchySupertypesParams
    ): List<TypeHierarchyItem> =
        throw NotImplementedError("typeHierarchySupertypes not implemented")

    override suspend fun typeHierarchySubtypes(
        params: TypeHierarchySubtypesParams
    ): List<TypeHierarchyItem> = throw NotImplementedError("typeHierarchySubtypes not implemented")

    override suspend fun textDocumentInlineValue(params: InlineValueParams): List<InlineValue> =
        throw NotImplementedError("textDocumentInlineValue not implemented")

    override suspend fun textDocumentInlayHint(params: InlayHintParams): List<InlayHint> =
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
    ): kotlinx.serialization.json.JsonElement =
        throw NotImplementedError("textDocumentInlineCompletion not implemented")

    override suspend fun initialize(params: InitializeParams): InitializeResult =
        throw NotImplementedError("initialize not implemented")

    override suspend fun shutdown(): Nothing? =
        throw NotImplementedError("shutdown not implemented")

    override suspend fun textDocumentWillSaveWaitUntil(
        params: WillSaveTextDocumentParams
    ): List<TextEdit> = throw NotImplementedError("textDocumentWillSaveWaitUntil not implemented")

    override suspend fun textDocumentCompletion(
        params: CompletionParams
    ): kotlinx.serialization.json.JsonElement =
        throw NotImplementedError("textDocumentCompletion not implemented")

    override suspend fun completionItemResolve(params: CompletionItem): CompletionItem =
        throw NotImplementedError("completionItemResolve not implemented")

    override suspend fun textDocumentHover(params: HoverParams): Hover =
        throw NotImplementedError("textDocumentHover not implemented")

    override suspend fun textDocumentSignatureHelp(params: SignatureHelpParams): SignatureHelp =
        throw NotImplementedError("textDocumentSignatureHelp not implemented")

    override suspend fun textDocumentDefinition(
        params: DefinitionParams
    ): kotlinx.serialization.json.JsonElement =
        throw NotImplementedError("textDocumentDefinition not implemented")

    override suspend fun textDocumentReferences(params: ReferenceParams): List<Location> =
        throw NotImplementedError("textDocumentReferences not implemented")

    override suspend fun textDocumentDocumentHighlight(
        params: DocumentHighlightParams
    ): List<DocumentHighlight> =
        throw NotImplementedError("textDocumentDocumentHighlight not implemented")

    override suspend fun textDocumentDocumentSymbol(
        params: DocumentSymbolParams
    ): kotlinx.serialization.json.JsonElement =
        throw NotImplementedError("textDocumentDocumentSymbol not implemented")

    override suspend fun textDocumentCodeAction(
        params: CodeActionParams
    ): List<TextDocumentCodeActionResult> =
        throw NotImplementedError("textDocumentCodeAction not implemented")

    override suspend fun codeActionResolve(params: CodeAction): CodeAction =
        throw NotImplementedError("codeActionResolve not implemented")

    override suspend fun workspaceSymbol(
        params: WorkspaceSymbolParams
    ): kotlinx.serialization.json.JsonElement =
        throw NotImplementedError("workspaceSymbol not implemented")

    override suspend fun workspaceSymbolResolve(params: WorkspaceSymbol): WorkspaceSymbol =
        throw NotImplementedError("workspaceSymbolResolve not implemented")

    override suspend fun textDocumentCodeLens(params: CodeLensParams): List<CodeLens> =
        throw NotImplementedError("textDocumentCodeLens not implemented")

    override suspend fun codeLensResolve(params: CodeLens): CodeLens =
        throw NotImplementedError("codeLensResolve not implemented")

    override suspend fun textDocumentDocumentLink(params: DocumentLinkParams): List<DocumentLink> =
        throw NotImplementedError("textDocumentDocumentLink not implemented")

    override suspend fun documentLinkResolve(params: DocumentLink): DocumentLink =
        throw NotImplementedError("documentLinkResolve not implemented")

    override suspend fun textDocumentFormatting(params: DocumentFormattingParams): List<TextEdit> =
        throw NotImplementedError("textDocumentFormatting not implemented")

    override suspend fun textDocumentRangeFormatting(
        params: DocumentRangeFormattingParams
    ): List<TextEdit> = throw NotImplementedError("textDocumentRangeFormatting not implemented")

    override suspend fun textDocumentRangesFormatting(
        params: DocumentRangesFormattingParams
    ): List<TextEdit> = throw NotImplementedError("textDocumentRangesFormatting not implemented")

    override suspend fun textDocumentOnTypeFormatting(
        params: DocumentOnTypeFormattingParams
    ): List<TextEdit> = throw NotImplementedError("textDocumentOnTypeFormatting not implemented")

    override suspend fun textDocumentRename(params: RenameParams): WorkspaceEdit =
        throw NotImplementedError("textDocumentRename not implemented")

    override suspend fun textDocumentPrepareRename(
        params: PrepareRenameParams
    ): PrepareRenameResult = throw NotImplementedError("textDocumentPrepareRename not implemented")

    override suspend fun workspaceExecuteCommand(params: ExecuteCommandParams): LSPAny =
        throw NotImplementedError("workspaceExecuteCommand not implemented")

    override suspend fun workspaceDidChangeWorkspaceFolders(
        params: DidChangeWorkspaceFoldersParams
    ): Unit = throw NotImplementedError("workspaceDidChangeWorkspaceFolders not implemented")

    override suspend fun windowWorkDoneProgressCancel(params: WorkDoneProgressCancelParams): Unit =
        throw NotImplementedError("windowWorkDoneProgressCancel not implemented")

    override suspend fun workspaceDidCreateFiles(params: CreateFilesParams): Unit =
        throw NotImplementedError("workspaceDidCreateFiles not implemented")

    override suspend fun workspaceDidRenameFiles(params: RenameFilesParams): Unit =
        throw NotImplementedError("workspaceDidRenameFiles not implemented")

    override suspend fun workspaceDidDeleteFiles(params: DeleteFilesParams): Unit =
        throw NotImplementedError("workspaceDidDeleteFiles not implemented")

    override suspend fun notebookDocumentDidOpen(params: DidOpenNotebookDocumentParams): Unit =
        throw NotImplementedError("notebookDocumentDidOpen not implemented")

    override suspend fun notebookDocumentDidChange(params: DidChangeNotebookDocumentParams): Unit =
        throw NotImplementedError("notebookDocumentDidChange not implemented")

    override suspend fun notebookDocumentDidSave(params: DidSaveNotebookDocumentParams): Unit =
        throw NotImplementedError("notebookDocumentDidSave not implemented")

    override suspend fun notebookDocumentDidClose(params: DidCloseNotebookDocumentParams): Unit =
        throw NotImplementedError("notebookDocumentDidClose not implemented")

    override suspend fun initialized(params: InitializedParams): Unit =
        throw NotImplementedError("initialized not implemented")

    override suspend fun exit(): Unit = throw NotImplementedError("exit not implemented")

    override suspend fun workspaceDidChangeConfiguration(
        params: DidChangeConfigurationParams
    ): Unit = throw NotImplementedError("workspaceDidChangeConfiguration not implemented")

    override suspend fun textDocumentDidOpen(params: DidOpenTextDocumentParams): Unit =
        throw NotImplementedError("textDocumentDidOpen not implemented")

    override suspend fun textDocumentDidChange(params: DidChangeTextDocumentParams): Unit =
        throw NotImplementedError("textDocumentDidChange not implemented")

    override suspend fun textDocumentDidClose(params: DidCloseTextDocumentParams): Unit =
        throw NotImplementedError("textDocumentDidClose not implemented")

    override suspend fun textDocumentDidSave(params: DidSaveTextDocumentParams): Unit =
        throw NotImplementedError("textDocumentDidSave not implemented")

    override suspend fun textDocumentWillSave(params: WillSaveTextDocumentParams): Unit =
        throw NotImplementedError("textDocumentWillSave not implemented")

    override suspend fun workspaceDidChangeWatchedFiles(params: DidChangeWatchedFilesParams): Unit =
        throw NotImplementedError("workspaceDidChangeWatchedFiles not implemented")

    override suspend fun setTrace(params: SetTraceParams): Unit =
        throw NotImplementedError("setTrace not implemented")

    override suspend fun progress(params: ProgressParams): Unit =
        throw NotImplementedError("progress not implemented")
}
