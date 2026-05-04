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
 * LSP Language Server interface — methods the client calls on the server.
 */
@com.monkopedia.ksrpc.annotation.KsService
interface LanguageServer : com.monkopedia.ksrpc.RpcService {

    /**
     * A request to resolve the implementation locations of a symbol at a given text
     * document position. The request's parameter is of type {@link TextDocumentPositionParams}
     * the response is of type {@link Definition} or a Thenable that resolves to such.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/implementation")
    suspend fun textDocumentImplementation(
        params: ImplementationParams
    ): kotlinx.serialization.json.JsonElement

    /**
     * A request to resolve the type definition locations of a symbol at a given text
     * document position. The request's parameter is of type {@link TextDocumentPositionParams}
     * the response is of type {@link Definition} or a Thenable that resolves to such.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/typeDefinition")
    suspend fun textDocumentTypeDefinition(
        params: TypeDefinitionParams
    ): kotlinx.serialization.json.JsonElement

    /**
     * A request to list all color symbols found in a given text document. The request's
     * parameter is of type {@link DocumentColorParams} the
     * response is of type {@link ColorInformation ColorInformation[]} or a Thenable
     * that resolves to such.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/documentColor")
    suspend fun textDocumentDocumentColor(params: DocumentColorParams): List<ColorInformation>

    /**
     * A request to list all presentation for a color. The request's
     * parameter is of type {@link ColorPresentationParams} the
     * response is of type {@link ColorInformation ColorInformation[]} or a Thenable
     * that resolves to such.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/colorPresentation")
    suspend fun textDocumentColorPresentation(
        params: ColorPresentationParams
    ): List<ColorPresentation>

    /**
     * A request to provide folding ranges in a document. The request's
     * parameter is of type {@link FoldingRangeParams}, the
     * response is of type {@link FoldingRangeList} or a Thenable
     * that resolves to such.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/foldingRange")
    suspend fun textDocumentFoldingRange(params: FoldingRangeParams): List<FoldingRange>

    /**
     * A request to resolve the type definition locations of a symbol at a given text
     * document position. The request's parameter is of type {@link TextDocumentPositionParams}
     * the response is of type {@link Declaration} or a typed array of {@link DeclarationLink}
     * or a Thenable that resolves to such.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/declaration")
    suspend fun textDocumentDeclaration(
        params: DeclarationParams
    ): kotlinx.serialization.json.JsonElement

    /**
     * A request to provide selection ranges in a document. The request's
     * parameter is of type {@link SelectionRangeParams}, the
     * response is of type {@link SelectionRange SelectionRange[]} or a Thenable
     * that resolves to such.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/selectionRange")
    suspend fun textDocumentSelectionRange(params: SelectionRangeParams): List<SelectionRange>

    /**
     * A request to result a `CallHierarchyItem` in a document at a given position.
     * Can be used as an input to an incoming or outgoing call hierarchy.
     *
     * @since 3.16.0
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/prepareCallHierarchy")
    suspend fun textDocumentPrepareCallHierarchy(
        params: CallHierarchyPrepareParams
    ): List<CallHierarchyItem>

    /**
     * A request to resolve the incoming calls for a given `CallHierarchyItem`.
     *
     * @since 3.16.0
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("callHierarchy/incomingCalls")
    suspend fun callHierarchyIncomingCalls(
        params: CallHierarchyIncomingCallsParams
    ): List<CallHierarchyIncomingCall>

    /**
     * A request to resolve the outgoing calls for a given `CallHierarchyItem`.
     *
     * @since 3.16.0
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("callHierarchy/outgoingCalls")
    suspend fun callHierarchyOutgoingCalls(
        params: CallHierarchyOutgoingCallsParams
    ): List<CallHierarchyOutgoingCall>

    /**
     * @since 3.16.0
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/semanticTokens/full")
    suspend fun textDocumentSemanticTokensFull(params: SemanticTokensParams): SemanticTokens

    /**
     * @since 3.16.0
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/semanticTokens/full/delta")
    suspend fun textDocumentSemanticTokensFullDelta(
        params: SemanticTokensDeltaParams
    ): TextDocumentSemanticTokensFullDeltaResult

    /**
     * @since 3.16.0
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/semanticTokens/range")
    suspend fun textDocumentSemanticTokensRange(params: SemanticTokensRangeParams): SemanticTokens

    /**
     * A request to provide ranges that can be edited together.
     *
     * @since 3.16.0
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/linkedEditingRange")
    suspend fun textDocumentLinkedEditingRange(
        params: LinkedEditingRangeParams
    ): LinkedEditingRanges

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
    @com.monkopedia.ksrpc.annotation.KsMethod("workspace/willCreateFiles")
    suspend fun workspaceWillCreateFiles(params: CreateFilesParams): WorkspaceEdit

    /**
     * The will rename files request is sent from the client to the server before files are actually
     * renamed as long as the rename is triggered from within the client.
     *
     * @since 3.16.0
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("workspace/willRenameFiles")
    suspend fun workspaceWillRenameFiles(params: RenameFilesParams): WorkspaceEdit

    /**
     * The did delete files notification is sent from the client to the server when
     * files were deleted from within the client.
     *
     * @since 3.16.0
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("workspace/willDeleteFiles")
    suspend fun workspaceWillDeleteFiles(params: DeleteFilesParams): WorkspaceEdit

    /**
     * A request to get the moniker of a symbol at a given text document position.
     * The request parameter is of type {@link TextDocumentPositionParams}.
     * The response is of type {@link Moniker Moniker[]} or `null`.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/moniker")
    suspend fun textDocumentMoniker(params: MonikerParams): List<Moniker>

    /**
     * A request to result a `TypeHierarchyItem` in a document at a given position.
     * Can be used as an input to a subtypes or supertypes type hierarchy.
     *
     * @since 3.17.0
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/prepareTypeHierarchy")
    suspend fun textDocumentPrepareTypeHierarchy(
        params: TypeHierarchyPrepareParams
    ): List<TypeHierarchyItem>

    /**
     * A request to resolve the supertypes for a given `TypeHierarchyItem`.
     *
     * @since 3.17.0
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("typeHierarchy/supertypes")
    suspend fun typeHierarchySupertypes(
        params: TypeHierarchySupertypesParams
    ): List<TypeHierarchyItem>

    /**
     * A request to resolve the subtypes for a given `TypeHierarchyItem`.
     *
     * @since 3.17.0
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("typeHierarchy/subtypes")
    suspend fun typeHierarchySubtypes(params: TypeHierarchySubtypesParams): List<TypeHierarchyItem>

    /**
     * A request to provide inline values in a document. The request's parameter is of
     * type {@link InlineValueParams}, the response is of type
     * {@link InlineValue InlineValue[]} or a Thenable that resolves to such.
     *
     * @since 3.17.0
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/inlineValue")
    suspend fun textDocumentInlineValue(params: InlineValueParams): List<InlineValue>

    /**
     * A request to provide inlay hints in a document. The request's parameter is of
     * type {@link InlayHintsParams}, the response is of type
     * {@link InlayHint InlayHint[]} or a Thenable that resolves to such.
     *
     * @since 3.17.0
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/inlayHint")
    suspend fun textDocumentInlayHint(params: InlayHintParams): List<InlayHint>

    /**
     * A request to resolve additional properties for an inlay hint.
     * The request's parameter is of type {@link InlayHint}, the response is
     * of type {@link InlayHint} or a Thenable that resolves to such.
     *
     * @since 3.17.0
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("inlayHint/resolve")
    suspend fun inlayHintResolve(params: InlayHint): InlayHint

    /**
     * The document diagnostic request definition.
     *
     * @since 3.17.0
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/diagnostic")
    // errorData: DiagnosticServerCancellationData
    suspend fun textDocumentDiagnostic(params: DocumentDiagnosticParams): DocumentDiagnosticReport

    /**
     * The workspace diagnostic request definition.
     *
     * @since 3.17.0
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("workspace/diagnostic")
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
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/inlineCompletion")
    suspend fun textDocumentInlineCompletion(
        params: InlineCompletionParams
    ): kotlinx.serialization.json.JsonElement

    /**
     * The initialize request is sent from the client to the server.
     * It is sent once as the request after starting up the server.
     * The requests parameter is of type {@link InitializeParams}
     * the response if of type {@link InitializeResult} of a Thenable that
     * resolves to such.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("initialize")
    // errorData: InitializeError
    suspend fun initialize(params: InitializeParams): InitializeResult

    /**
     * A shutdown request is sent from the client to the server.
     * It is sent once when the client decides to shutdown the
     * server. The only notification that is sent after a shutdown request
     * is the exit event.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("shutdown")
    suspend fun shutdown(): Nothing?

    /**
     * A document will save request is sent from the client to the server before
     * the document is actually saved. The request can return an array of TextEdits
     * which will be applied to the text document before it is saved. Please note that
     * clients might drop results if computing the text edits took too long or if a
     * server constantly fails on this request. This is done to keep the save fast and
     * reliable.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/willSaveWaitUntil")
    suspend fun textDocumentWillSaveWaitUntil(params: WillSaveTextDocumentParams): List<TextEdit>

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
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/completion")
    suspend fun textDocumentCompletion(
        params: CompletionParams
    ): kotlinx.serialization.json.JsonElement

    /**
     * Request to resolve additional information for a given completion item.The request's
     * parameter is of type {@link CompletionItem} the response
     * is of type {@link CompletionItem} or a Thenable that resolves to such.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("completionItem/resolve")
    suspend fun completionItemResolve(params: CompletionItem): CompletionItem

    /**
     * Request to request hover information at a given text document position. The request's
     * parameter is of type {@link TextDocumentPosition} the response is of
     * type {@link Hover} or a Thenable that resolves to such.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/hover")
    suspend fun textDocumentHover(params: HoverParams): Hover

    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/signatureHelp")
    suspend fun textDocumentSignatureHelp(params: SignatureHelpParams): SignatureHelp

    /**
     * A request to resolve the definition location of a symbol at a given text
     * document position. The request's parameter is of type {@link TextDocumentPosition}
     * the response is of either type {@link Definition} or a typed array of
     * {@link DefinitionLink} or a Thenable that resolves to such.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/definition")
    suspend fun textDocumentDefinition(
        params: DefinitionParams
    ): kotlinx.serialization.json.JsonElement

    /**
     * A request to resolve project-wide references for the symbol denoted
     * by the given text document position. The request's parameter is of
     * type {@link ReferenceParams} the response is of type
     * {@link Location Location[]} or a Thenable that resolves to such.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/references")
    suspend fun textDocumentReferences(params: ReferenceParams): List<Location>

    /**
     * Request to resolve a {@link DocumentHighlight} for a given
     * text document position. The request's parameter is of type {@link TextDocumentPosition}
     * the request response is an array of type {@link DocumentHighlight}
     * or a Thenable that resolves to such.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/documentHighlight")
    suspend fun textDocumentDocumentHighlight(
        params: DocumentHighlightParams
    ): List<DocumentHighlight>

    /**
     * A request to list all symbols found in a given text document. The request's
     * parameter is of type {@link TextDocumentIdentifier} the
     * response is of type {@link SymbolInformation SymbolInformation[]} or a Thenable
     * that resolves to such.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/documentSymbol")
    suspend fun textDocumentDocumentSymbol(
        params: DocumentSymbolParams
    ): kotlinx.serialization.json.JsonElement

    /**
     * A request to provide commands for the given text document and range.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/codeAction")
    suspend fun textDocumentCodeAction(params: CodeActionParams): List<TextDocumentCodeActionResult>

    /**
     * Request to resolve additional information for a given code action.The request's
     * parameter is of type {@link CodeAction} the response
     * is of type {@link CodeAction} or a Thenable that resolves to such.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("codeAction/resolve")
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
    @com.monkopedia.ksrpc.annotation.KsMethod("workspace/symbol")
    suspend fun workspaceSymbol(
        params: WorkspaceSymbolParams
    ): kotlinx.serialization.json.JsonElement

    /**
     * A request to resolve the range inside the workspace
     * symbol's location.
     *
     * @since 3.17.0
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("workspaceSymbol/resolve")
    suspend fun workspaceSymbolResolve(params: WorkspaceSymbol): WorkspaceSymbol

    /**
     * A request to provide code lens for the given text document.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/codeLens")
    suspend fun textDocumentCodeLens(params: CodeLensParams): List<CodeLens>

    /**
     * A request to resolve a command for a given code lens.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("codeLens/resolve")
    suspend fun codeLensResolve(params: CodeLens): CodeLens

    /**
     * A request to provide document links
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/documentLink")
    suspend fun textDocumentDocumentLink(params: DocumentLinkParams): List<DocumentLink>

    /**
     * Request to resolve additional information for a given document link. The request's
     * parameter is of type {@link DocumentLink} the response
     * is of type {@link DocumentLink} or a Thenable that resolves to such.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("documentLink/resolve")
    suspend fun documentLinkResolve(params: DocumentLink): DocumentLink

    /**
     * A request to format a whole document.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/formatting")
    suspend fun textDocumentFormatting(params: DocumentFormattingParams): List<TextEdit>

    /**
     * A request to format a range in a document.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/rangeFormatting")
    suspend fun textDocumentRangeFormatting(params: DocumentRangeFormattingParams): List<TextEdit>

    /**
     * A request to format ranges in a document.
     *
     * @since 3.18.0
     * @proposed
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/rangesFormatting")
    suspend fun textDocumentRangesFormatting(params: DocumentRangesFormattingParams): List<TextEdit>

    /**
     * A request to format a document on type.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/onTypeFormatting")
    suspend fun textDocumentOnTypeFormatting(params: DocumentOnTypeFormattingParams): List<TextEdit>

    /**
     * A request to rename a symbol.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/rename")
    suspend fun textDocumentRename(params: RenameParams): WorkspaceEdit

    /**
     * A request to test and perform the setup necessary for a rename.
     *
     * @since 3.16 - support for default behavior
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/prepareRename")
    suspend fun textDocumentPrepareRename(params: PrepareRenameParams): PrepareRenameResult

    /**
     * A request send from the client to the server to execute a command. The request might return
     * a workspace edit which the client will apply to the workspace.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("workspace/executeCommand")
    suspend fun workspaceExecuteCommand(params: ExecuteCommandParams): LSPAny

    /**
     * The `workspace/didChangeWorkspaceFolders` notification is sent from the client to the server when the workspace
     * folder configuration changes.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("workspace/didChangeWorkspaceFolders")
    @com.monkopedia.ksrpc.annotation.KsNotification
    suspend fun workspaceDidChangeWorkspaceFolders(params: DidChangeWorkspaceFoldersParams)

    /**
     * The `window/workDoneProgress/cancel` notification is sent from  the client to the server to cancel a progress
     * initiated on the server side.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("window/workDoneProgress/cancel")
    @com.monkopedia.ksrpc.annotation.KsNotification
    suspend fun windowWorkDoneProgressCancel(params: WorkDoneProgressCancelParams)

    /**
     * The did create files notification is sent from the client to the server when
     * files were created from within the client.
     *
     * @since 3.16.0
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("workspace/didCreateFiles")
    @com.monkopedia.ksrpc.annotation.KsNotification
    suspend fun workspaceDidCreateFiles(params: CreateFilesParams)

    /**
     * The did rename files notification is sent from the client to the server when
     * files were renamed from within the client.
     *
     * @since 3.16.0
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("workspace/didRenameFiles")
    @com.monkopedia.ksrpc.annotation.KsNotification
    suspend fun workspaceDidRenameFiles(params: RenameFilesParams)

    /**
     * The will delete files request is sent from the client to the server before files are actually
     * deleted as long as the deletion is triggered from within the client.
     *
     * @since 3.16.0
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("workspace/didDeleteFiles")
    @com.monkopedia.ksrpc.annotation.KsNotification
    suspend fun workspaceDidDeleteFiles(params: DeleteFilesParams)

    /**
     * A notification sent when a notebook opens.
     *
     * @since 3.17.0
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("notebookDocument/didOpen")
    @com.monkopedia.ksrpc.annotation.KsNotification
    suspend fun notebookDocumentDidOpen(params: DidOpenNotebookDocumentParams)

    @com.monkopedia.ksrpc.annotation.KsMethod("notebookDocument/didChange")
    @com.monkopedia.ksrpc.annotation.KsNotification
    suspend fun notebookDocumentDidChange(params: DidChangeNotebookDocumentParams)

    /**
     * A notification sent when a notebook document is saved.
     *
     * @since 3.17.0
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("notebookDocument/didSave")
    @com.monkopedia.ksrpc.annotation.KsNotification
    suspend fun notebookDocumentDidSave(params: DidSaveNotebookDocumentParams)

    /**
     * A notification sent when a notebook closes.
     *
     * @since 3.17.0
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("notebookDocument/didClose")
    @com.monkopedia.ksrpc.annotation.KsNotification
    suspend fun notebookDocumentDidClose(params: DidCloseNotebookDocumentParams)

    /**
     * The initialized notification is sent from the client to the
     * server after the client is fully initialized and the server
     * is allowed to send requests from the server to the client.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("initialized")
    @com.monkopedia.ksrpc.annotation.KsNotification
    suspend fun initialized(params: InitializedParams)

    /**
     * The exit event is sent from the client to the server to
     * ask the server to exit its process.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("exit")
    @com.monkopedia.ksrpc.annotation.KsNotification
    suspend fun exit()

    /**
     * The configuration change notification is sent from the client to the server
     * when the client's configuration has changed. The notification contains
     * the changed configuration as defined by the language client.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("workspace/didChangeConfiguration")
    @com.monkopedia.ksrpc.annotation.KsNotification
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
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/didOpen")
    @com.monkopedia.ksrpc.annotation.KsNotification
    suspend fun textDocumentDidOpen(params: DidOpenTextDocumentParams)

    /**
     * The document change notification is sent from the client to the server to signal
     * changes to a text document.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/didChange")
    @com.monkopedia.ksrpc.annotation.KsNotification
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
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/didClose")
    @com.monkopedia.ksrpc.annotation.KsNotification
    suspend fun textDocumentDidClose(params: DidCloseTextDocumentParams)

    /**
     * The document save notification is sent from the client to the server when
     * the document got saved in the client.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/didSave")
    @com.monkopedia.ksrpc.annotation.KsNotification
    suspend fun textDocumentDidSave(params: DidSaveTextDocumentParams)

    /**
     * A document will save notification is sent from the client to the server before
     * the document is actually saved.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("textDocument/willSave")
    @com.monkopedia.ksrpc.annotation.KsNotification
    suspend fun textDocumentWillSave(params: WillSaveTextDocumentParams)

    /**
     * The watched files notification is sent from the client to the server when
     * the client detects changes to file watched by the language client.
     */
    @com.monkopedia.ksrpc.annotation.KsMethod("workspace/didChangeWatchedFiles")
    @com.monkopedia.ksrpc.annotation.KsNotification
    suspend fun workspaceDidChangeWatchedFiles(params: DidChangeWatchedFilesParams)

    @com.monkopedia.ksrpc.annotation.KsMethod("$/setTrace")
    @com.monkopedia.ksrpc.annotation.KsNotification
    suspend fun setTrace(params: SetTraceParams)

    @com.monkopedia.ksrpc.annotation.KsMethod("$/progress")
    @com.monkopedia.ksrpc.annotation.KsNotification
    suspend fun progress(params: ProgressParams)
}
