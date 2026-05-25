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

import com.monkopedia.ksrpc.RpcService
import com.monkopedia.ksrpc.annotation.KsMethod
import com.monkopedia.ksrpc.annotation.KsNotification
import com.monkopedia.ksrpc.annotation.KsService
import kotlinx.serialization.json.JsonElement

/**
 * ksrpc-annotated [LanguageServer] for use with the JSON-RPC transport.
 * Implement this (or extend [DefaultLanguageServer]) to host an LSP server
 * via [com.monkopedia.lsp.ksrpc.connectAsLspServer].
 */
@KsService
interface KsrpcLanguageServer :
    LanguageServer,
    RpcService {

    @KsMethod("textDocument/implementation")
    override suspend fun textDocumentImplementation(
        params: ImplementationParams
    ): TextDocumentImplementationResult

    @KsMethod("textDocument/typeDefinition")
    override suspend fun textDocumentTypeDefinition(
        params: TypeDefinitionParams
    ): TextDocumentTypeDefinitionResult

    @KsMethod("textDocument/documentColor")
    override suspend fun textDocumentDocumentColor(
        params: DocumentColorParams
    ): List<ColorInformation>

    @KsMethod("textDocument/colorPresentation")
    override suspend fun textDocumentColorPresentation(
        params: ColorPresentationParams
    ): List<ColorPresentation>

    @KsMethod("textDocument/foldingRange")
    override suspend fun textDocumentFoldingRange(params: FoldingRangeParams): List<FoldingRange>

    @KsMethod("textDocument/declaration")
    override suspend fun textDocumentDeclaration(
        params: DeclarationParams
    ): TextDocumentDeclarationResult

    @KsMethod("textDocument/selectionRange")
    override suspend fun textDocumentSelectionRange(
        params: SelectionRangeParams
    ): List<SelectionRange>

    @KsMethod("textDocument/prepareCallHierarchy")
    override suspend fun textDocumentPrepareCallHierarchy(
        params: CallHierarchyPrepareParams
    ): List<CallHierarchyItem>

    @KsMethod("callHierarchy/incomingCalls")
    override suspend fun callHierarchyIncomingCalls(
        params: CallHierarchyIncomingCallsParams
    ): List<CallHierarchyIncomingCall>

    @KsMethod("callHierarchy/outgoingCalls")
    override suspend fun callHierarchyOutgoingCalls(
        params: CallHierarchyOutgoingCallsParams
    ): List<CallHierarchyOutgoingCall>

    @KsMethod("textDocument/semanticTokens/full")
    override suspend fun textDocumentSemanticTokensFull(
        params: SemanticTokensParams
    ): SemanticTokens

    @KsMethod("textDocument/semanticTokens/full/delta")
    override suspend fun textDocumentSemanticTokensFullDelta(
        params: SemanticTokensDeltaParams
    ): TextDocumentSemanticTokensFullDeltaResult

    @KsMethod("textDocument/semanticTokens/range")
    override suspend fun textDocumentSemanticTokensRange(
        params: SemanticTokensRangeParams
    ): SemanticTokens

    @KsMethod("textDocument/linkedEditingRange")
    override suspend fun textDocumentLinkedEditingRange(
        params: LinkedEditingRangeParams
    ): LinkedEditingRanges

    @KsMethod("workspace/willCreateFiles")
    override suspend fun workspaceWillCreateFiles(params: CreateFilesParams): WorkspaceEdit

    @KsMethod("workspace/willRenameFiles")
    override suspend fun workspaceWillRenameFiles(params: RenameFilesParams): WorkspaceEdit

    @KsMethod("workspace/willDeleteFiles")
    override suspend fun workspaceWillDeleteFiles(params: DeleteFilesParams): WorkspaceEdit

    @KsMethod("textDocument/moniker")
    override suspend fun textDocumentMoniker(params: MonikerParams): List<Moniker>

    @KsMethod("textDocument/prepareTypeHierarchy")
    override suspend fun textDocumentPrepareTypeHierarchy(
        params: TypeHierarchyPrepareParams
    ): List<TypeHierarchyItem>

    @KsMethod("typeHierarchy/supertypes")
    override suspend fun typeHierarchySupertypes(
        params: TypeHierarchySupertypesParams
    ): List<TypeHierarchyItem>

    @KsMethod("typeHierarchy/subtypes")
    override suspend fun typeHierarchySubtypes(
        params: TypeHierarchySubtypesParams
    ): List<TypeHierarchyItem>

    @KsMethod("textDocument/inlineValue")
    override suspend fun textDocumentInlineValue(params: InlineValueParams): List<InlineValue>

    @KsMethod("textDocument/inlayHint")
    override suspend fun textDocumentInlayHint(params: InlayHintParams): List<InlayHint>

    @KsMethod("inlayHint/resolve")
    override suspend fun inlayHintResolve(params: InlayHint): InlayHint

    @KsMethod("textDocument/diagnostic")
    override suspend fun textDocumentDiagnostic(
        params: DocumentDiagnosticParams
    ): DocumentDiagnosticReport

    @KsMethod("workspace/diagnostic")
    override suspend fun workspaceDiagnostic(
        params: WorkspaceDiagnosticParams
    ): WorkspaceDiagnosticReport

    @KsMethod("textDocument/inlineCompletion")
    override suspend fun textDocumentInlineCompletion(
        params: InlineCompletionParams
    ): TextDocumentInlineCompletionResult

    @KsMethod("initialize")
    override suspend fun initialize(params: InitializeParams): InitializeResult

    @KsMethod("shutdown")
    override suspend fun shutdown(): Nothing?

    @KsMethod("textDocument/willSaveWaitUntil")
    override suspend fun textDocumentWillSaveWaitUntil(
        params: WillSaveTextDocumentParams
    ): List<TextEdit>

    @KsMethod("textDocument/completion")
    override suspend fun textDocumentCompletion(
        params: CompletionParams
    ): TextDocumentCompletionResult

    @KsMethod("completionItem/resolve")
    override suspend fun completionItemResolve(params: CompletionItem): CompletionItem

    @KsMethod("textDocument/hover")
    override suspend fun textDocumentHover(params: HoverParams): Hover

    @KsMethod("textDocument/signatureHelp")
    override suspend fun textDocumentSignatureHelp(params: SignatureHelpParams): SignatureHelp

    @KsMethod("textDocument/definition")
    override suspend fun textDocumentDefinition(
        params: DefinitionParams
    ): TextDocumentDefinitionResult

    @KsMethod("textDocument/references")
    override suspend fun textDocumentReferences(params: ReferenceParams): List<Location>

    @KsMethod("textDocument/documentHighlight")
    override suspend fun textDocumentDocumentHighlight(
        params: DocumentHighlightParams
    ): List<DocumentHighlight>

    @KsMethod("textDocument/documentSymbol")
    override suspend fun textDocumentDocumentSymbol(
        params: DocumentSymbolParams
    ): TextDocumentDocumentSymbolResult

    @KsMethod("textDocument/codeAction")
    override suspend fun textDocumentCodeAction(
        params: CodeActionParams
    ): List<TextDocumentCodeActionResult>

    @KsMethod("codeAction/resolve")
    override suspend fun codeActionResolve(params: CodeAction): CodeAction

    @KsMethod("workspace/symbol")
    override suspend fun workspaceSymbol(params: WorkspaceSymbolParams): JsonElement

    @KsMethod("workspaceSymbol/resolve")
    override suspend fun workspaceSymbolResolve(params: WorkspaceSymbol): WorkspaceSymbol

    @KsMethod("textDocument/codeLens")
    override suspend fun textDocumentCodeLens(params: CodeLensParams): List<CodeLens>

    @KsMethod("codeLens/resolve")
    override suspend fun codeLensResolve(params: CodeLens): CodeLens

    @KsMethod("textDocument/documentLink")
    override suspend fun textDocumentDocumentLink(params: DocumentLinkParams): List<DocumentLink>

    @KsMethod("documentLink/resolve")
    override suspend fun documentLinkResolve(params: DocumentLink): DocumentLink

    @KsMethod("textDocument/formatting")
    override suspend fun textDocumentFormatting(params: DocumentFormattingParams): List<TextEdit>

    @KsMethod("textDocument/rangeFormatting")
    override suspend fun textDocumentRangeFormatting(
        params: DocumentRangeFormattingParams
    ): List<TextEdit>

    @KsMethod("textDocument/rangesFormatting")
    override suspend fun textDocumentRangesFormatting(
        params: DocumentRangesFormattingParams
    ): List<TextEdit>

    @KsMethod("textDocument/onTypeFormatting")
    override suspend fun textDocumentOnTypeFormatting(
        params: DocumentOnTypeFormattingParams
    ): List<TextEdit>

    @KsMethod("textDocument/rename")
    override suspend fun textDocumentRename(params: RenameParams): WorkspaceEdit

    @KsMethod("textDocument/prepareRename")
    override suspend fun textDocumentPrepareRename(params: PrepareRenameParams): PrepareRenameResult

    @KsMethod("workspace/executeCommand")
    override suspend fun workspaceExecuteCommand(params: ExecuteCommandParams): LSPAny

    @KsMethod("workspace/didChangeWorkspaceFolders")
    @KsNotification
    override suspend fun workspaceDidChangeWorkspaceFolders(params: DidChangeWorkspaceFoldersParams)

    @KsMethod("window/workDoneProgress/cancel")
    @KsNotification
    override suspend fun windowWorkDoneProgressCancel(params: WorkDoneProgressCancelParams)

    @KsMethod("workspace/didCreateFiles")
    @KsNotification
    override suspend fun workspaceDidCreateFiles(params: CreateFilesParams)

    @KsMethod("workspace/didRenameFiles")
    @KsNotification
    override suspend fun workspaceDidRenameFiles(params: RenameFilesParams)

    @KsMethod("workspace/didDeleteFiles")
    @KsNotification
    override suspend fun workspaceDidDeleteFiles(params: DeleteFilesParams)

    @KsMethod("notebookDocument/didOpen")
    @KsNotification
    override suspend fun notebookDocumentDidOpen(params: DidOpenNotebookDocumentParams)

    @KsMethod("notebookDocument/didChange")
    @KsNotification
    override suspend fun notebookDocumentDidChange(params: DidChangeNotebookDocumentParams)

    @KsMethod("notebookDocument/didSave")
    @KsNotification
    override suspend fun notebookDocumentDidSave(params: DidSaveNotebookDocumentParams)

    @KsMethod("notebookDocument/didClose")
    @KsNotification
    override suspend fun notebookDocumentDidClose(params: DidCloseNotebookDocumentParams)

    @KsMethod("initialized")
    @KsNotification
    override suspend fun initialized(params: InitializedParams)

    @KsMethod("exit")
    @KsNotification
    override suspend fun exit()

    @KsMethod("workspace/didChangeConfiguration")
    @KsNotification
    override suspend fun workspaceDidChangeConfiguration(params: DidChangeConfigurationParams)

    @KsMethod("textDocument/didOpen")
    @KsNotification
    override suspend fun textDocumentDidOpen(params: DidOpenTextDocumentParams)

    @KsMethod("textDocument/didChange")
    @KsNotification
    override suspend fun textDocumentDidChange(params: DidChangeTextDocumentParams)

    @KsMethod("textDocument/didClose")
    @KsNotification
    override suspend fun textDocumentDidClose(params: DidCloseTextDocumentParams)

    @KsMethod("textDocument/didSave")
    @KsNotification
    override suspend fun textDocumentDidSave(params: DidSaveTextDocumentParams)

    @KsMethod("textDocument/willSave")
    @KsNotification
    override suspend fun textDocumentWillSave(params: WillSaveTextDocumentParams)

    @KsMethod("workspace/didChangeWatchedFiles")
    @KsNotification
    override suspend fun workspaceDidChangeWatchedFiles(params: DidChangeWatchedFilesParams)

    @KsMethod("$/setTrace")
    @KsNotification
    override suspend fun setTrace(params: SetTraceParams)

    @KsMethod("$/progress")
    @KsNotification
    override suspend fun progress(params: ProgressParams)
}
