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
 * LSP Language Server interface — methods the client calls on the server.
 */
interface LanguageServer {

    /**
     * A request to resolve the implementation locations of a symbol at a given text
     * document position. The request's parameter is of type {@link TextDocumentPositionParams}
     * the response is of type {@link Definition} or a Thenable that resolves to such.
     */
    suspend fun textDocumentImplementation(
        params: ImplementationParams
    ): TextDocumentImplementationResult?

    /**
     * A request to resolve the type definition locations of a symbol at a given text
     * document position. The request's parameter is of type {@link TextDocumentPositionParams}
     * the response is of type {@link Definition} or a Thenable that resolves to such.
     */
    suspend fun textDocumentTypeDefinition(
        params: TypeDefinitionParams
    ): TextDocumentTypeDefinitionResult?

    /**
     * A request to list all color symbols found in a given text document. The request's
     * parameter is of type {@link DocumentColorParams} the
     * response is of type {@link ColorInformation ColorInformation[]} or a Thenable
     * that resolves to such.
     */
    suspend fun textDocumentDocumentColor(params: DocumentColorParams): List<ColorInformation>

    /**
     * A request to list all presentation for a color. The request's
     * parameter is of type {@link ColorPresentationParams} the
     * response is of type {@link ColorInformation ColorInformation[]} or a Thenable
     * that resolves to such.
     */
    suspend fun textDocumentColorPresentation(
        params: ColorPresentationParams
    ): List<ColorPresentation>

    /**
     * A request to provide folding ranges in a document. The request's
     * parameter is of type {@link FoldingRangeParams}, the
     * response is of type {@link FoldingRangeList} or a Thenable
     * that resolves to such.
     */
    suspend fun textDocumentFoldingRange(params: FoldingRangeParams): List<FoldingRange>?

    /**
     * A request to resolve the type definition locations of a symbol at a given text
     * document position. The request's parameter is of type {@link TextDocumentPositionParams}
     * the response is of type {@link Declaration} or a typed array of {@link DeclarationLink}
     * or a Thenable that resolves to such.
     */
    suspend fun textDocumentDeclaration(params: DeclarationParams): TextDocumentDeclarationResult?

    /**
     * A request to provide selection ranges in a document. The request's
     * parameter is of type {@link SelectionRangeParams}, the
     * response is of type {@link SelectionRange SelectionRange[]} or a Thenable
     * that resolves to such.
     */
    suspend fun textDocumentSelectionRange(params: SelectionRangeParams): List<SelectionRange>?

    /**
     * A request to result a `CallHierarchyItem` in a document at a given position.
     * Can be used as an input to an incoming or outgoing call hierarchy.
     *
     * @since 3.16.0
     */
    suspend fun textDocumentPrepareCallHierarchy(
        params: CallHierarchyPrepareParams
    ): List<CallHierarchyItem>?

    /**
     * A request to resolve the incoming calls for a given `CallHierarchyItem`.
     *
     * @since 3.16.0
     */
    suspend fun callHierarchyIncomingCalls(
        params: CallHierarchyIncomingCallsParams
    ): List<CallHierarchyIncomingCall>?

    /**
     * A request to resolve the outgoing calls for a given `CallHierarchyItem`.
     *
     * @since 3.16.0
     */
    suspend fun callHierarchyOutgoingCalls(
        params: CallHierarchyOutgoingCallsParams
    ): List<CallHierarchyOutgoingCall>?

    /**
     * @since 3.16.0
     */
    suspend fun textDocumentSemanticTokensFull(params: SemanticTokensParams): SemanticTokens?

    /**
     * @since 3.16.0
     */
    suspend fun textDocumentSemanticTokensFullDelta(
        params: SemanticTokensDeltaParams
    ): TextDocumentSemanticTokensFullDeltaResult?

    /**
     * @since 3.16.0
     */
    suspend fun textDocumentSemanticTokensRange(params: SemanticTokensRangeParams): SemanticTokens?

    /**
     * A request to provide ranges that can be edited together.
     *
     * @since 3.16.0
     */
    suspend fun textDocumentLinkedEditingRange(
        params: LinkedEditingRangeParams
    ): LinkedEditingRanges?

    /**
     * The will create files request is sent from the client to the server before files are actually
     * created as long as the creation is triggered from within the client.
     *
     * The request can return a `WorkspaceEdit` which will be applied to workspace before the
     * files are created. Hence the `WorkspaceEdit` can not manipulate the content of the file
     * to be created.
     *
     * @since 3.16.0
     */
    suspend fun workspaceWillCreateFiles(params: CreateFilesParams): WorkspaceEdit?

    /**
     * The will rename files request is sent from the client to the server before files are actually
     * renamed as long as the rename is triggered from within the client.
     *
     * @since 3.16.0
     */
    suspend fun workspaceWillRenameFiles(params: RenameFilesParams): WorkspaceEdit?

    /**
     * The did delete files notification is sent from the client to the server when
     * files were deleted from within the client.
     *
     * @since 3.16.0
     */
    suspend fun workspaceWillDeleteFiles(params: DeleteFilesParams): WorkspaceEdit?

    /**
     * A request to get the moniker of a symbol at a given text document position.
     * The request parameter is of type {@link TextDocumentPositionParams}.
     * The response is of type {@link Moniker Moniker[]} or `null`.
     */
    suspend fun textDocumentMoniker(params: MonikerParams): List<Moniker>?

    /**
     * A request to result a `TypeHierarchyItem` in a document at a given position.
     * Can be used as an input to a subtypes or supertypes type hierarchy.
     *
     * @since 3.17.0
     */
    suspend fun textDocumentPrepareTypeHierarchy(
        params: TypeHierarchyPrepareParams
    ): List<TypeHierarchyItem>?

    /**
     * A request to resolve the supertypes for a given `TypeHierarchyItem`.
     *
     * @since 3.17.0
     */
    suspend fun typeHierarchySupertypes(
        params: TypeHierarchySupertypesParams
    ): List<TypeHierarchyItem>?

    /**
     * A request to resolve the subtypes for a given `TypeHierarchyItem`.
     *
     * @since 3.17.0
     */
    suspend fun typeHierarchySubtypes(params: TypeHierarchySubtypesParams): List<TypeHierarchyItem>?

    /**
     * A request to provide inline values in a document. The request's parameter is of
     * type {@link InlineValueParams}, the response is of type
     * {@link InlineValue InlineValue[]} or a Thenable that resolves to such.
     *
     * @since 3.17.0
     */
    suspend fun textDocumentInlineValue(params: InlineValueParams): List<InlineValue>?

    /**
     * A request to provide inlay hints in a document. The request's parameter is of
     * type {@link InlayHintsParams}, the response is of type
     * {@link InlayHint InlayHint[]} or a Thenable that resolves to such.
     *
     * @since 3.17.0
     */
    suspend fun textDocumentInlayHint(params: InlayHintParams): List<InlayHint>?

    /**
     * A request to resolve additional properties for an inlay hint.
     * The request's parameter is of type {@link InlayHint}, the response is
     * of type {@link InlayHint} or a Thenable that resolves to such.
     *
     * @since 3.17.0
     */
    suspend fun inlayHintResolve(params: InlayHint): InlayHint

    /**
     * The document diagnostic request definition.
     *
     * @since 3.17.0
     */
    // errorData: DiagnosticServerCancellationData
    suspend fun textDocumentDiagnostic(params: DocumentDiagnosticParams): DocumentDiagnosticReport

    /**
     * The workspace diagnostic request definition.
     *
     * @since 3.17.0
     */
    // errorData: DiagnosticServerCancellationData
    suspend fun workspaceDiagnostic(params: WorkspaceDiagnosticParams): WorkspaceDiagnosticReport

    /**
     * A request to provide inline completions in a document. The request's parameter is of
     * type {@link InlineCompletionParams}, the response is of type
     * {@link InlineCompletion InlineCompletion[]} or a Thenable that resolves to such.
     *
     * @since 3.18.0
     * @proposed
     */
    suspend fun textDocumentInlineCompletion(
        params: InlineCompletionParams
    ): TextDocumentInlineCompletionResult?

    /**
     * The initialize request is sent from the client to the server.
     * It is sent once as the request after starting up the server.
     * The requests parameter is of type {@link InitializeParams}
     * the response if of type {@link InitializeResult} of a Thenable that
     * resolves to such.
     */
    // errorData: InitializeError
    suspend fun initialize(params: InitializeParams): InitializeResult

    /**
     * A shutdown request is sent from the client to the server.
     * It is sent once when the client decides to shutdown the
     * server. The only notification that is sent after a shutdown request
     * is the exit event.
     */
    suspend fun shutdown(): Nothing?

    /**
     * A document will save request is sent from the client to the server before
     * the document is actually saved. The request can return an array of TextEdits
     * which will be applied to the text document before it is saved. Please note that
     * clients might drop results if computing the text edits took too long or if a
     * server constantly fails on this request. This is done to keep the save fast and
     * reliable.
     */
    suspend fun textDocumentWillSaveWaitUntil(params: WillSaveTextDocumentParams): List<TextEdit>?

    /**
     * Request to request completion at a given text document position. The request's
     * parameter is of type {@link TextDocumentPosition} the response
     * is of type {@link CompletionItem CompletionItem[]} or {@link CompletionList}
     * or a Thenable that resolves to such.
     *
     * The request can delay the computation of the {@link CompletionItem.detail `detail`}
     * and {@link CompletionItem.documentation `documentation`} properties to the `completionItem/resolve`
     * request. However, properties that are needed for the initial sorting and filtering, like `sortText`,
     * `filterText`, `insertText`, and `textEdit`, must not be changed during resolve.
     */
    suspend fun textDocumentCompletion(params: CompletionParams): TextDocumentCompletionResult?

    /**
     * Request to resolve additional information for a given completion item.The request's
     * parameter is of type {@link CompletionItem} the response
     * is of type {@link CompletionItem} or a Thenable that resolves to such.
     */
    suspend fun completionItemResolve(params: CompletionItem): CompletionItem

    /**
     * Request to request hover information at a given text document position. The request's
     * parameter is of type {@link TextDocumentPosition} the response is of
     * type {@link Hover} or a Thenable that resolves to such.
     */
    suspend fun textDocumentHover(params: HoverParams): Hover?

    suspend fun textDocumentSignatureHelp(params: SignatureHelpParams): SignatureHelp?

    /**
     * A request to resolve the definition location of a symbol at a given text
     * document position. The request's parameter is of type {@link TextDocumentPosition}
     * the response is of either type {@link Definition} or a typed array of
     * {@link DefinitionLink} or a Thenable that resolves to such.
     */
    suspend fun textDocumentDefinition(params: DefinitionParams): TextDocumentDefinitionResult?

    /**
     * A request to resolve project-wide references for the symbol denoted
     * by the given text document position. The request's parameter is of
     * type {@link ReferenceParams} the response is of type
     * {@link Location Location[]} or a Thenable that resolves to such.
     */
    suspend fun textDocumentReferences(params: ReferenceParams): List<Location>?

    /**
     * Request to resolve a {@link DocumentHighlight} for a given
     * text document position. The request's parameter is of type {@link TextDocumentPosition}
     * the request response is an array of type {@link DocumentHighlight}
     * or a Thenable that resolves to such.
     */
    suspend fun textDocumentDocumentHighlight(
        params: DocumentHighlightParams
    ): List<DocumentHighlight>?

    /**
     * A request to list all symbols found in a given text document. The request's
     * parameter is of type {@link TextDocumentIdentifier} the
     * response is of type {@link SymbolInformation SymbolInformation[]} or a Thenable
     * that resolves to such.
     */
    suspend fun textDocumentDocumentSymbol(
        params: DocumentSymbolParams
    ): TextDocumentDocumentSymbolResult?

    /**
     * A request to provide commands for the given text document and range.
     */
    suspend fun textDocumentCodeAction(
        params: CodeActionParams
    ): List<TextDocumentCodeActionResult>?

    /**
     * Request to resolve additional information for a given code action.The request's
     * parameter is of type {@link CodeAction} the response
     * is of type {@link CodeAction} or a Thenable that resolves to such.
     */
    suspend fun codeActionResolve(params: CodeAction): CodeAction

    /**
     * A request to list project-wide symbols matching the query string given
     * by the {@link WorkspaceSymbolParams}. The response is
     * of type {@link SymbolInformation SymbolInformation[]} or a Thenable that
     * resolves to such.
     *
     * @since 3.17.0 - support for WorkspaceSymbol in the returned data. Clients
     *  need to advertise support for WorkspaceSymbols via the client capability
     *  `workspace.symbol.resolveSupport`.
     *
     *
     * @since 3.17.0 - support for WorkspaceSymbol in the returned data. Clients
need to advertise support for WorkspaceSymbols via the client capability
`workspace.symbol.resolveSupport`.
     */
    suspend fun workspaceSymbol(params: WorkspaceSymbolParams): JsonElement?

    /**
     * A request to resolve the range inside the workspace
     * symbol's location.
     *
     * @since 3.17.0
     */
    suspend fun workspaceSymbolResolve(params: WorkspaceSymbol): WorkspaceSymbol

    /**
     * A request to provide code lens for the given text document.
     */
    suspend fun textDocumentCodeLens(params: CodeLensParams): List<CodeLens>?

    /**
     * A request to resolve a command for a given code lens.
     */
    suspend fun codeLensResolve(params: CodeLens): CodeLens

    /**
     * A request to provide document links
     */
    suspend fun textDocumentDocumentLink(params: DocumentLinkParams): List<DocumentLink>?

    /**
     * Request to resolve additional information for a given document link. The request's
     * parameter is of type {@link DocumentLink} the response
     * is of type {@link DocumentLink} or a Thenable that resolves to such.
     */
    suspend fun documentLinkResolve(params: DocumentLink): DocumentLink

    /**
     * A request to format a whole document.
     */
    suspend fun textDocumentFormatting(params: DocumentFormattingParams): List<TextEdit>?

    /**
     * A request to format a range in a document.
     */
    suspend fun textDocumentRangeFormatting(params: DocumentRangeFormattingParams): List<TextEdit>?

    /**
     * A request to format ranges in a document.
     *
     * @since 3.18.0
     * @proposed
     */
    suspend fun textDocumentRangesFormatting(
        params: DocumentRangesFormattingParams
    ): List<TextEdit>?

    /**
     * A request to format a document on type.
     */
    suspend fun textDocumentOnTypeFormatting(
        params: DocumentOnTypeFormattingParams
    ): List<TextEdit>?

    /**
     * A request to rename a symbol.
     */
    suspend fun textDocumentRename(params: RenameParams): WorkspaceEdit?

    /**
     * A request to test and perform the setup necessary for a rename.
     *
     * @since 3.16 - support for default behavior
     */
    suspend fun textDocumentPrepareRename(params: PrepareRenameParams): PrepareRenameResult?

    /**
     * A request send from the client to the server to execute a command. The request might return
     * a workspace edit which the client will apply to the workspace.
     */
    suspend fun workspaceExecuteCommand(params: ExecuteCommandParams): LSPAny?

    /**
     * The `workspace/didChangeWorkspaceFolders` notification is sent from the client to the server when the workspace
     * folder configuration changes.
     */
    suspend fun workspaceDidChangeWorkspaceFolders(params: DidChangeWorkspaceFoldersParams)

    /**
     * The `window/workDoneProgress/cancel` notification is sent from  the client to the server to cancel a progress
     * initiated on the server side.
     */
    suspend fun windowWorkDoneProgressCancel(params: WorkDoneProgressCancelParams)

    /**
     * The did create files notification is sent from the client to the server when
     * files were created from within the client.
     *
     * @since 3.16.0
     */
    suspend fun workspaceDidCreateFiles(params: CreateFilesParams)

    /**
     * The did rename files notification is sent from the client to the server when
     * files were renamed from within the client.
     *
     * @since 3.16.0
     */
    suspend fun workspaceDidRenameFiles(params: RenameFilesParams)

    /**
     * The will delete files request is sent from the client to the server before files are actually
     * deleted as long as the deletion is triggered from within the client.
     *
     * @since 3.16.0
     */
    suspend fun workspaceDidDeleteFiles(params: DeleteFilesParams)

    /**
     * A notification sent when a notebook opens.
     *
     * @since 3.17.0
     */
    suspend fun notebookDocumentDidOpen(params: DidOpenNotebookDocumentParams)

    suspend fun notebookDocumentDidChange(params: DidChangeNotebookDocumentParams)

    /**
     * A notification sent when a notebook document is saved.
     *
     * @since 3.17.0
     */
    suspend fun notebookDocumentDidSave(params: DidSaveNotebookDocumentParams)

    /**
     * A notification sent when a notebook closes.
     *
     * @since 3.17.0
     */
    suspend fun notebookDocumentDidClose(params: DidCloseNotebookDocumentParams)

    /**
     * The initialized notification is sent from the client to the
     * server after the client is fully initialized and the server
     * is allowed to send requests from the server to the client.
     */
    suspend fun initialized(params: InitializedParams)

    /**
     * The exit event is sent from the client to the server to
     * ask the server to exit its process.
     */
    suspend fun exit()

    /**
     * The configuration change notification is sent from the client to the server
     * when the client's configuration has changed. The notification contains
     * the changed configuration as defined by the language client.
     */
    suspend fun workspaceDidChangeConfiguration(params: DidChangeConfigurationParams)

    /**
     * The document open notification is sent from the client to the server to signal
     * newly opened text documents. The document's truth is now managed by the client
     * and the server must not try to read the document's truth using the document's
     * uri. Open in this sense means it is managed by the client. It doesn't necessarily
     * mean that its content is presented in an editor. An open notification must not
     * be sent more than once without a corresponding close notification send before.
     * This means open and close notification must be balanced and the max open count
     * is one.
     */
    suspend fun textDocumentDidOpen(params: DidOpenTextDocumentParams)

    /**
     * The document change notification is sent from the client to the server to signal
     * changes to a text document.
     */
    suspend fun textDocumentDidChange(params: DidChangeTextDocumentParams)

    /**
     * The document close notification is sent from the client to the server when
     * the document got closed in the client. The document's truth now exists where
     * the document's uri points to (e.g. if the document's uri is a file uri the
     * truth now exists on disk). As with the open notification the close notification
     * is about managing the document's content. Receiving a close notification
     * doesn't mean that the document was open in an editor before. A close
     * notification requires a previous open notification to be sent.
     */
    suspend fun textDocumentDidClose(params: DidCloseTextDocumentParams)

    /**
     * The document save notification is sent from the client to the server when
     * the document got saved in the client.
     */
    suspend fun textDocumentDidSave(params: DidSaveTextDocumentParams)

    /**
     * A document will save notification is sent from the client to the server before
     * the document is actually saved.
     */
    suspend fun textDocumentWillSave(params: WillSaveTextDocumentParams)

    /**
     * The watched files notification is sent from the client to the server when
     * the client detects changes to file watched by the language client.
     */
    suspend fun workspaceDidChangeWatchedFiles(params: DidChangeWatchedFilesParams)

    suspend fun setTrace(params: SetTraceParams)

    suspend fun progress(params: ProgressParams)

    companion object {
        const val TEXT_DOCUMENT_IMPLEMENTATION: String = "textDocument/implementation"
        const val TEXT_DOCUMENT_TYPE_DEFINITION: String = "textDocument/typeDefinition"
        const val TEXT_DOCUMENT_DOCUMENT_COLOR: String = "textDocument/documentColor"
        const val TEXT_DOCUMENT_COLOR_PRESENTATION: String = "textDocument/colorPresentation"
        const val TEXT_DOCUMENT_FOLDING_RANGE: String = "textDocument/foldingRange"
        const val TEXT_DOCUMENT_DECLARATION: String = "textDocument/declaration"
        const val TEXT_DOCUMENT_SELECTION_RANGE: String = "textDocument/selectionRange"
        const val TEXT_DOCUMENT_PREPARE_CALL_HIERARCHY: String = "textDocument/prepareCallHierarchy"
        const val CALL_HIERARCHY_INCOMING_CALLS: String = "callHierarchy/incomingCalls"
        const val CALL_HIERARCHY_OUTGOING_CALLS: String = "callHierarchy/outgoingCalls"
        const val TEXT_DOCUMENT_SEMANTIC_TOKENS_FULL: String = "textDocument/semanticTokens/full"
        const val TEXT_DOCUMENT_SEMANTIC_TOKENS_FULL_DELTA: String = "textDocument/semanticTokens/full/delta"
        const val TEXT_DOCUMENT_SEMANTIC_TOKENS_RANGE: String = "textDocument/semanticTokens/range"
        const val TEXT_DOCUMENT_LINKED_EDITING_RANGE: String = "textDocument/linkedEditingRange"
        const val WORKSPACE_WILL_CREATE_FILES: String = "workspace/willCreateFiles"
        const val WORKSPACE_WILL_RENAME_FILES: String = "workspace/willRenameFiles"
        const val WORKSPACE_WILL_DELETE_FILES: String = "workspace/willDeleteFiles"
        const val TEXT_DOCUMENT_MONIKER: String = "textDocument/moniker"
        const val TEXT_DOCUMENT_PREPARE_TYPE_HIERARCHY: String = "textDocument/prepareTypeHierarchy"
        const val TYPE_HIERARCHY_SUPERTYPES: String = "typeHierarchy/supertypes"
        const val TYPE_HIERARCHY_SUBTYPES: String = "typeHierarchy/subtypes"
        const val TEXT_DOCUMENT_INLINE_VALUE: String = "textDocument/inlineValue"
        const val TEXT_DOCUMENT_INLAY_HINT: String = "textDocument/inlayHint"
        const val INLAY_HINT_RESOLVE: String = "inlayHint/resolve"
        const val TEXT_DOCUMENT_DIAGNOSTIC: String = "textDocument/diagnostic"
        const val WORKSPACE_DIAGNOSTIC: String = "workspace/diagnostic"
        const val TEXT_DOCUMENT_INLINE_COMPLETION: String = "textDocument/inlineCompletion"
        const val INITIALIZE: String = "initialize"
        const val SHUTDOWN: String = "shutdown"
        const val TEXT_DOCUMENT_WILL_SAVE_WAIT_UNTIL: String = "textDocument/willSaveWaitUntil"
        const val TEXT_DOCUMENT_COMPLETION: String = "textDocument/completion"
        const val COMPLETION_ITEM_RESOLVE: String = "completionItem/resolve"
        const val TEXT_DOCUMENT_HOVER: String = "textDocument/hover"
        const val TEXT_DOCUMENT_SIGNATURE_HELP: String = "textDocument/signatureHelp"
        const val TEXT_DOCUMENT_DEFINITION: String = "textDocument/definition"
        const val TEXT_DOCUMENT_REFERENCES: String = "textDocument/references"
        const val TEXT_DOCUMENT_DOCUMENT_HIGHLIGHT: String = "textDocument/documentHighlight"
        const val TEXT_DOCUMENT_DOCUMENT_SYMBOL: String = "textDocument/documentSymbol"
        const val TEXT_DOCUMENT_CODE_ACTION: String = "textDocument/codeAction"
        const val CODE_ACTION_RESOLVE: String = "codeAction/resolve"
        const val WORKSPACE_SYMBOL: String = "workspace/symbol"
        const val WORKSPACE_SYMBOL_RESOLVE: String = "workspaceSymbol/resolve"
        const val TEXT_DOCUMENT_CODE_LENS: String = "textDocument/codeLens"
        const val CODE_LENS_RESOLVE: String = "codeLens/resolve"
        const val TEXT_DOCUMENT_DOCUMENT_LINK: String = "textDocument/documentLink"
        const val DOCUMENT_LINK_RESOLVE: String = "documentLink/resolve"
        const val TEXT_DOCUMENT_FORMATTING: String = "textDocument/formatting"
        const val TEXT_DOCUMENT_RANGE_FORMATTING: String = "textDocument/rangeFormatting"
        const val TEXT_DOCUMENT_RANGES_FORMATTING: String = "textDocument/rangesFormatting"
        const val TEXT_DOCUMENT_ON_TYPE_FORMATTING: String = "textDocument/onTypeFormatting"
        const val TEXT_DOCUMENT_RENAME: String = "textDocument/rename"
        const val TEXT_DOCUMENT_PREPARE_RENAME: String = "textDocument/prepareRename"
        const val WORKSPACE_EXECUTE_COMMAND: String = "workspace/executeCommand"
        const val WORKSPACE_DID_CHANGE_WORKSPACE_FOLDERS: String = "workspace/didChangeWorkspaceFolders"
        const val WINDOW_WORK_DONE_PROGRESS_CANCEL: String = "window/workDoneProgress/cancel"
        const val WORKSPACE_DID_CREATE_FILES: String = "workspace/didCreateFiles"
        const val WORKSPACE_DID_RENAME_FILES: String = "workspace/didRenameFiles"
        const val WORKSPACE_DID_DELETE_FILES: String = "workspace/didDeleteFiles"
        const val NOTEBOOK_DOCUMENT_DID_OPEN: String = "notebookDocument/didOpen"
        const val NOTEBOOK_DOCUMENT_DID_CHANGE: String = "notebookDocument/didChange"
        const val NOTEBOOK_DOCUMENT_DID_SAVE: String = "notebookDocument/didSave"
        const val NOTEBOOK_DOCUMENT_DID_CLOSE: String = "notebookDocument/didClose"
        const val INITIALIZED: String = "initialized"
        const val EXIT: String = "exit"
        const val WORKSPACE_DID_CHANGE_CONFIGURATION: String = "workspace/didChangeConfiguration"
        const val TEXT_DOCUMENT_DID_OPEN: String = "textDocument/didOpen"
        const val TEXT_DOCUMENT_DID_CHANGE: String = "textDocument/didChange"
        const val TEXT_DOCUMENT_DID_CLOSE: String = "textDocument/didClose"
        const val TEXT_DOCUMENT_DID_SAVE: String = "textDocument/didSave"
        const val TEXT_DOCUMENT_WILL_SAVE: String = "textDocument/willSave"
        const val WORKSPACE_DID_CHANGE_WATCHED_FILES: String = "workspace/didChangeWatchedFiles"
        const val SET_TRACE: String = "$/setTrace"
        const val PROGRESS: String = "$/progress"
    }
}
