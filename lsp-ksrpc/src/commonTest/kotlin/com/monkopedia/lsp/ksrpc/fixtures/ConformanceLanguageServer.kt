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

import com.monkopedia.lsp.ApplyWorkspaceEditParams
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
import com.monkopedia.lsp.ConfigurationItem
import com.monkopedia.lsp.ConfigurationParams
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
import com.monkopedia.lsp.IntOrString
import com.monkopedia.lsp.KsrpcLanguageClient
import com.monkopedia.lsp.LSPAny
import com.monkopedia.lsp.LanguageClient
import com.monkopedia.lsp.LanguageServer
import com.monkopedia.lsp.LinkedEditingRangeParams
import com.monkopedia.lsp.LinkedEditingRanges
import com.monkopedia.lsp.Location
import com.monkopedia.lsp.LocationLink
import com.monkopedia.lsp.LogMessageParams
import com.monkopedia.lsp.LogTraceParams
import com.monkopedia.lsp.MessageActionItem
import com.monkopedia.lsp.MessageType
import com.monkopedia.lsp.Moniker
import com.monkopedia.lsp.MonikerKind
import com.monkopedia.lsp.MonikerParams
import com.monkopedia.lsp.Position
import com.monkopedia.lsp.PrepareRenameParams
import com.monkopedia.lsp.PrepareRenameResult
import com.monkopedia.lsp.PrepareRenameResultRange
import com.monkopedia.lsp.ProgressParams
import com.monkopedia.lsp.PublishDiagnosticsParams
import com.monkopedia.lsp.Range
import com.monkopedia.lsp.ReferenceParams
import com.monkopedia.lsp.Registration
import com.monkopedia.lsp.RegistrationParams
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
import com.monkopedia.lsp.ShowDocumentParams
import com.monkopedia.lsp.ShowMessageParams
import com.monkopedia.lsp.ShowMessageRequestParams
import com.monkopedia.lsp.SignatureHelp
import com.monkopedia.lsp.SignatureHelpParams
import com.monkopedia.lsp.SignatureInformation
import com.monkopedia.lsp.SingleOrArray
import com.monkopedia.lsp.StringOr
import com.monkopedia.lsp.SymbolInformation
import com.monkopedia.lsp.SymbolKind
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
import com.monkopedia.lsp.Unregistration
import com.monkopedia.lsp.UnregistrationParams
import com.monkopedia.lsp.WillSaveTextDocumentParams
import com.monkopedia.lsp.WorkDoneProgressCancelParams
import com.monkopedia.lsp.WorkDoneProgressCreateParams
import com.monkopedia.lsp.WorkspaceDiagnosticParams
import com.monkopedia.lsp.WorkspaceDiagnosticReport
import com.monkopedia.lsp.WorkspaceEdit
import com.monkopedia.lsp.WorkspaceFullDocumentDiagnosticReport
import com.monkopedia.lsp.WorkspaceSymbol
import com.monkopedia.lsp.WorkspaceSymbolParams
import com.monkopedia.lsp.markdown
import com.monkopedia.lsp.string
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

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
 * into [notificationsBuffer]. Tests can drain [drainNotifications] or observe
 * via [notificationFlow] to assert that a notification arrived. The key is the
 * LSP method name (e.g. `textDocument/didChange`).
 *
 * ## Server → client trigger surface (issue #64)
 *
 * To exercise the *client*-side wire surface — methods the server calls back on
 * the client — open [Triggers.ALL] via `textDocument/didOpen`. The fixture then
 * issues one call against every server-initiated client method listed in
 * [ClientMethods.ALL], in that order, against its [client] stub (which tests
 * must assign to the result of `connectAsLspServer`). Each successful issue is
 * recorded in [issuedClientCalls] and re-emitted on [issuedClientCallsFlow], so
 * tests can either snapshot the list or suspend on the flow.
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

    private suspend fun record(method: String, paramsSummary: String, params: Any? = null) {
        notificationMutex.withLock {
            notificationsBuffer += NotificationReceipt(method, paramsSummary)
        }
        notificationFlow.tryEmit(NotificationReceipt(method, paramsSummary))
        ConformanceWireRecorder.observeServer(method)
        if (params != null) ConformanceWireRecorder.observeValue(params)
    }

    /**
     * Wire-coverage observation hook for request handlers (issues #66, #74).
     * Notifications route through [record]; requests don't take a params summary
     * so they call this directly. Fires into the shared [ConformanceWireRecorder]
     * so the JVM coverage tracker can record every server-side method that
     * arrived over the wire and every typed param the handler received.
     * Defaults to a no-op on targets that haven't installed a recorder.
     */
    private fun observeRequest(method: String, params: Any? = null) {
        ConformanceWireRecorder.observeServer(method)
        if (params != null) ConformanceWireRecorder.observeValue(params)
    }

    /**
     * Wire-coverage observation hook for handler return values (issue #74).
     * Walks the typed result through [ConformanceWireRecorder.observeValue] so
     * the JVM-side branch recorder can record every sealed-interface subclass
     * appearing in the response, then returns the value unchanged.
     */
    private fun <T> observeResult(value: T): T {
        ConformanceWireRecorder.observeValue(value)
        return value
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

    /**
     * Well-known trigger URIs that, on `textDocument/didOpen`, cause the server to
     * initiate the documented sequence of server → client calls so wire tests can
     * exercise the full client surface (issue #64). See [emitAllClientTriggers].
     */
    object Triggers {
        /**
         * `textDocument/didOpen` with this URI drives [emitAllClientTriggers]: the
         * server issues one call against every server-initiated client method that
         * tests can observe over the wire.
         */
        const val ALL = "file:///__triggers/all"
    }

    /**
     * Plain-text name of every server-initiated client call the fixture issues from
     * [emitAllClientTriggers], in the order it issues them. The corresponding entry
     * is appended to [issuedClientCalls] (and emitted on [issuedClientCallsFlow])
     * immediately after the client side returns/acknowledges, so tests can both
     * snapshot the list after the trigger or suspend on the flow.
     */
    object ClientMethods {
        const val WORKSPACE_CONFIGURATION = "workspace/configuration"
        const val WORKSPACE_WORKSPACE_FOLDERS = "workspace/workspaceFolders"
        const val WORKSPACE_APPLY_EDIT = "workspace/applyEdit"
        const val WINDOW_SHOW_MESSAGE_REQUEST = "window/showMessageRequest"
        const val WINDOW_SHOW_DOCUMENT = "window/showDocument"
        const val CLIENT_REGISTER_CAPABILITY = "client/registerCapability"
        const val CLIENT_UNREGISTER_CAPABILITY = "client/unregisterCapability"
        const val WINDOW_WORK_DONE_PROGRESS_CREATE = "window/workDoneProgress/create"
        const val PROGRESS = "\$/progress"
        const val WORKSPACE_CODE_LENS_REFRESH = "workspace/codeLens/refresh"
        const val WORKSPACE_SEMANTIC_TOKENS_REFRESH = "workspace/semanticTokens/refresh"
        const val WORKSPACE_INLAY_HINT_REFRESH = "workspace/inlayHint/refresh"
        const val WORKSPACE_INLINE_VALUE_REFRESH = "workspace/inlineValue/refresh"
        const val WORKSPACE_DIAGNOSTIC_REFRESH = "workspace/diagnostic/refresh"
        const val WORKSPACE_FOLDING_RANGE_REFRESH = "workspace/foldingRange/refresh"
        const val TELEMETRY_EVENT = "telemetry/event"
        const val LOG_TRACE = "\$/logTrace"
        const val WINDOW_SHOW_MESSAGE = "window/showMessage"
        const val WINDOW_LOG_MESSAGE = "window/logMessage"
        const val TEXT_DOCUMENT_PUBLISH_DIAGNOSTICS = "textDocument/publishDiagnostics"

        /** The full ordered list of method names [emitAllClientTriggers] issues. */
        val ALL: List<String> = listOf(
            WORKSPACE_CONFIGURATION,
            WORKSPACE_WORKSPACE_FOLDERS,
            WORKSPACE_APPLY_EDIT,
            WINDOW_SHOW_MESSAGE_REQUEST,
            WINDOW_SHOW_DOCUMENT,
            CLIENT_REGISTER_CAPABILITY,
            CLIENT_UNREGISTER_CAPABILITY,
            WINDOW_WORK_DONE_PROGRESS_CREATE,
            PROGRESS,
            WORKSPACE_CODE_LENS_REFRESH,
            WORKSPACE_SEMANTIC_TOKENS_REFRESH,
            WORKSPACE_INLAY_HINT_REFRESH,
            WORKSPACE_INLINE_VALUE_REFRESH,
            WORKSPACE_DIAGNOSTIC_REFRESH,
            WORKSPACE_FOLDING_RANGE_REFRESH,
            TELEMETRY_EVENT,
            LOG_TRACE,
            WINDOW_SHOW_MESSAGE,
            WINDOW_LOG_MESSAGE,
            TEXT_DOCUMENT_PUBLISH_DIAGNOSTICS
        )
    }

    /**
     * Client stub the fixture should drive on triggers. Tests that wire a connection
     * (`connectAsLspServer` returns the client stub) assign it here so
     * [emitAllClientTriggers] can call back. Volatile so tests can publish across
     * threads.
     */
    @kotlin.concurrent.Volatile
    var client: KsrpcLanguageClient? = null

    private val _issuedClientCalls = mutableListOf<String>()
    private val _issuedClientCallsFlow = MutableSharedFlow<String>(
        replay = TRIGGER_REPLAY,
        extraBufferCapacity = TRIGGER_BUFFER
    )

    /**
     * Append-only record of server-initiated client method names the fixture has
     * issued (in issue order). Populated by [emitAllClientTriggers].
     */
    val issuedClientCalls: List<String> get() = _issuedClientCalls.toList()

    /**
     * Hot stream of server-initiated client method names. Replays recent issues so
     * a test that subscribes after the trigger fires can still observe them.
     */
    val issuedClientCallsFlow: SharedFlow<String> get() = _issuedClientCallsFlow

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

    override suspend fun initialize(params: InitializeParams): InitializeResult {
        observeRequest(LanguageServer.INITIALIZE, params)
        return observeResult(
            InitializeResult(
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
        )
    }

    override suspend fun shutdown(): Nothing? {
        observeRequest(LanguageServer.SHUTDOWN)
        return observeResult(null)
    }

    // region union-branch-exhaustive methods

    override suspend fun textDocumentHover(params: HoverParams): Hover {
        observeRequest(LanguageServer.TEXT_DOCUMENT_HOVER, params)
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
        return observeResult(
            Hover(contents = contents, range = range(params.position.line.toInt()))
        )
    }

    override suspend fun textDocumentDefinition(
        params: DefinitionParams
    ): TextDocumentDefinitionResult {
        observeRequest(LanguageServer.TEXT_DOCUMENT_DEFINITION, params)
        return observeResult(
            when (params.position.line.toInt()) {
                Lines.SINGLE -> TextDocumentDefinitionResult.DefinitionValue(
                    singleDefinition(Uri.MAIN, 0)
                )

                Lines.ARRAY -> TextDocumentDefinitionResult.DefinitionValue(
                    arrayDefinition(Uri.MAIN)
                )

                else -> TextDocumentDefinitionResult.DefinitionLinkArray(linkDefinitions(Uri.MAIN))
            }
        )
    }

    override suspend fun textDocumentDeclaration(
        params: DeclarationParams
    ): TextDocumentDeclarationResult {
        observeRequest(LanguageServer.TEXT_DOCUMENT_DECLARATION, params)
        return observeResult(
            when (params.position.line.toInt()) {
                Lines.SINGLE -> TextDocumentDeclarationResult.DeclarationValue(
                    singleDeclaration(Uri.MAIN, 0)
                )

                Lines.ARRAY -> TextDocumentDeclarationResult.DeclarationValue(
                    arrayDeclaration(Uri.MAIN)
                )

                else -> TextDocumentDeclarationResult.DeclarationLinkArray(
                    linkDeclarations(Uri.MAIN)
                )
            }
        )
    }

    override suspend fun textDocumentTypeDefinition(
        params: TypeDefinitionParams
    ): TextDocumentTypeDefinitionResult {
        observeRequest(LanguageServer.TEXT_DOCUMENT_TYPE_DEFINITION, params)
        return observeResult(
            when (params.position.line.toInt()) {
                Lines.SINGLE -> TextDocumentTypeDefinitionResult.DefinitionValue(
                    singleDefinition(Uri.MAIN, 0)
                )

                Lines.ARRAY -> TextDocumentTypeDefinitionResult.DefinitionValue(
                    arrayDefinition(Uri.MAIN)
                )

                else -> TextDocumentTypeDefinitionResult.DefinitionLinkArray(
                    linkDefinitions(Uri.MAIN)
                )
            }
        )
    }

    override suspend fun textDocumentImplementation(
        params: ImplementationParams
    ): TextDocumentImplementationResult {
        observeRequest(LanguageServer.TEXT_DOCUMENT_IMPLEMENTATION, params)
        return observeResult(
            when (params.position.line.toInt()) {
                Lines.SINGLE -> TextDocumentImplementationResult.DefinitionValue(
                    singleDefinition(Uri.MAIN, 0)
                )

                Lines.ARRAY -> TextDocumentImplementationResult.DefinitionValue(
                    arrayDefinition(Uri.MAIN)
                )

                else -> TextDocumentImplementationResult.DefinitionLinkArray(
                    linkDefinitions(Uri.MAIN)
                )
            }
        )
    }

    override suspend fun textDocumentReferences(params: ReferenceParams): List<Location> {
        observeRequest(LanguageServer.TEXT_DOCUMENT_REFERENCES, params)
        return observeResult(
            listOf(location(Uri.MAIN, 0), location(Uri.MAIN, 1), location(Uri.MAIN, 2))
        )
    }

    override suspend fun textDocumentCompletion(
        params: CompletionParams
    ): TextDocumentCompletionResult {
        observeRequest(LanguageServer.TEXT_DOCUMENT_COMPLETION, params)
        return observeResult(
            when (params.position.line.toInt()) {
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
        )
    }

    override suspend fun textDocumentDocumentSymbol(
        params: DocumentSymbolParams
    ): TextDocumentDocumentSymbolResult {
        observeRequest(LanguageServer.TEXT_DOCUMENT_DOCUMENT_SYMBOL, params)
        return observeResult(
            if (params.textDocument.uri.endsWith("#flat")) {
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
        )
    }

    // endregion

    // region simple well-formed canned methods

    override suspend fun textDocumentSignatureHelp(params: SignatureHelpParams): SignatureHelp {
        observeRequest(LanguageServer.TEXT_DOCUMENT_SIGNATURE_HELP, params)
        return observeResult(
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
        )
    }

    override suspend fun textDocumentDocumentHighlight(
        params: DocumentHighlightParams
    ): List<DocumentHighlight> {
        observeRequest(LanguageServer.TEXT_DOCUMENT_DOCUMENT_HIGHLIGHT, params)
        return observeResult(
            listOf(
                DocumentHighlight(range = range(0), kind = DocumentHighlightKind.TEXT),
                DocumentHighlight(range = range(1), kind = DocumentHighlightKind.WRITE)
            )
        )
    }

    override suspend fun textDocumentFormatting(params: DocumentFormattingParams): List<TextEdit> {
        observeRequest(LanguageServer.TEXT_DOCUMENT_FORMATTING, params)
        return observeResult(listOf(TextEdit(range = range(0), newText = "formatted\n")))
    }

    override suspend fun textDocumentRename(params: RenameParams): WorkspaceEdit {
        observeRequest(LanguageServer.TEXT_DOCUMENT_RENAME, params)
        return observeResult(
            WorkspaceEdit(
                changes = mapOf(
                    Uri.MAIN to listOf(TextEdit(range = range(0), newText = params.newName))
                )
            )
        )
    }

    override suspend fun textDocumentCodeAction(
        params: CodeActionParams
    ): List<com.monkopedia.lsp.TextDocumentCodeActionResult> {
        observeRequest(LanguageServer.TEXT_DOCUMENT_CODE_ACTION, params)
        return observeResult(
            listOf(
                CodeAction(title = "Canned quick fix"),
                Command(title = "Canned command", command = "conformance.command")
            )
        )
    }

    override suspend fun textDocumentCodeLens(params: CodeLensParams): List<CodeLens> {
        observeRequest(LanguageServer.TEXT_DOCUMENT_CODE_LENS, params)
        return observeResult(
            listOf(
                CodeLens(
                    range = range(0),
                    command = Command(title = "Canned lens", command = "conformance.lens")
                )
            )
        )
    }

    override suspend fun textDocumentFoldingRange(params: FoldingRangeParams): List<FoldingRange> {
        observeRequest(LanguageServer.TEXT_DOCUMENT_FOLDING_RANGE, params)
        return observeResult(
            listOf(
                FoldingRange(startLine = 0u, endLine = 5u, kind = FoldingRangeKind.REGION)
            )
        )
    }

    override suspend fun textDocumentSemanticTokensFull(
        params: SemanticTokensParams
    ): SemanticTokens {
        observeRequest(LanguageServer.TEXT_DOCUMENT_SEMANTIC_TOKENS_FULL, params)
        return observeResult(
            SemanticTokens(
                resultId = "conformance-1",
                data = listOf(0u, 0u, 4u, 0u, 0u)
            )
        )
    }

    override suspend fun textDocumentInlayHint(params: InlayHintParams): List<InlayHint> {
        observeRequest(LanguageServer.TEXT_DOCUMENT_INLAY_HINT, params)
        return observeResult(
            listOf(
                InlayHint(
                    position = pos(0, 4),
                    label = StringOr.StringValue(": Int")
                )
            )
        )
    }

    override suspend fun inlayHintResolve(params: InlayHint): InlayHint {
        observeRequest(LanguageServer.INLAY_HINT_RESOLVE, params)
        return observeResult(
            params.copy(
                tooltip = StringOr.StringValue("resolved tooltip")
            )
        )
    }

    override suspend fun textDocumentDocumentColor(
        params: DocumentColorParams
    ): List<ColorInformation> {
        observeRequest(LanguageServer.TEXT_DOCUMENT_DOCUMENT_COLOR, params)
        return observeResult(
            listOf(
                ColorInformation(
                    range = range(0),
                    color = Color(red = 1.0, green = 0.0, blue = 0.0, alpha = 1.0)
                )
            )
        )
    }

    override suspend fun textDocumentColorPresentation(
        params: ColorPresentationParams
    ): List<ColorPresentation> {
        observeRequest(LanguageServer.TEXT_DOCUMENT_COLOR_PRESENTATION, params)
        return observeResult(
            listOf(
                ColorPresentation(label = "red")
            )
        )
    }

    override suspend fun textDocumentSelectionRange(
        params: SelectionRangeParams
    ): List<SelectionRange> {
        observeRequest(LanguageServer.TEXT_DOCUMENT_SELECTION_RANGE, params)
        return observeResult(
            listOf(
                SelectionRange(range = range(0), parent = SelectionRange(range = range(0)))
            )
        )
    }

    override suspend fun textDocumentDocumentLink(params: DocumentLinkParams): List<DocumentLink> {
        observeRequest(LanguageServer.TEXT_DOCUMENT_DOCUMENT_LINK, params)
        return observeResult(
            listOf(
                DocumentLink(range = range(0), target = "https://example.com/conformance")
            )
        )
    }

    override suspend fun documentLinkResolve(params: DocumentLink): DocumentLink {
        observeRequest(LanguageServer.DOCUMENT_LINK_RESOLVE, params)
        return observeResult(
            params.copy(
                tooltip = "resolved link tooltip"
            )
        )
    }

    override suspend fun textDocumentRangeFormatting(
        params: DocumentRangeFormattingParams
    ): List<TextEdit> {
        observeRequest(LanguageServer.TEXT_DOCUMENT_RANGE_FORMATTING, params)
        return observeResult(listOf(TextEdit(range = range(0), newText = "range-formatted\n")))
    }

    override suspend fun textDocumentRangesFormatting(
        params: DocumentRangesFormattingParams
    ): List<TextEdit> {
        observeRequest(LanguageServer.TEXT_DOCUMENT_RANGES_FORMATTING, params)
        return observeResult(listOf(TextEdit(range = range(0), newText = "ranges-formatted\n")))
    }

    override suspend fun textDocumentOnTypeFormatting(
        params: DocumentOnTypeFormattingParams
    ): List<TextEdit> {
        observeRequest(LanguageServer.TEXT_DOCUMENT_ON_TYPE_FORMATTING, params)
        return observeResult(listOf(TextEdit(range = range(0), newText = "on-type-formatted\n")))
    }

    override suspend fun textDocumentPrepareRename(
        params: PrepareRenameParams
    ): PrepareRenameResult {
        observeRequest(LanguageServer.TEXT_DOCUMENT_PREPARE_RENAME, params)
        return observeResult(
            PrepareRenameResultRange(
                range = range(params.position.line.toInt()),
                placeholder = "newName"
            )
        )
    }

    override suspend fun textDocumentWillSaveWaitUntil(
        params: WillSaveTextDocumentParams
    ): List<TextEdit> {
        observeRequest(LanguageServer.TEXT_DOCUMENT_WILL_SAVE_WAIT_UNTIL, params)
        return observeResult(listOf(TextEdit(range = range(0), newText = "// pre-save\n")))
    }

    override suspend fun completionItemResolve(params: CompletionItem): CompletionItem {
        observeRequest(LanguageServer.COMPLETION_ITEM_RESOLVE, params)
        return observeResult(params.copy(detail = "resolved: ${params.label}"))
    }

    override suspend fun codeActionResolve(params: CodeAction): CodeAction {
        observeRequest(LanguageServer.CODE_ACTION_RESOLVE, params)
        return observeResult(params.copy(isPreferred = true))
    }

    override suspend fun codeLensResolve(params: CodeLens): CodeLens {
        observeRequest(LanguageServer.CODE_LENS_RESOLVE, params)
        return observeResult(
            params.copy(
                command = Command(title = "Resolved lens", command = "conformance.lens.resolved")
            )
        )
    }

    override suspend fun textDocumentSemanticTokensFullDelta(
        params: SemanticTokensDeltaParams
    ): TextDocumentSemanticTokensFullDeltaResult {
        observeRequest(LanguageServer.TEXT_DOCUMENT_SEMANTIC_TOKENS_FULL_DELTA, params)
        return observeResult(
            SemanticTokens(
                resultId = "conformance-delta-${params.previousResultId}",
                data = listOf(0u, 0u, 4u, 0u, 0u)
            )
        )
    }

    override suspend fun textDocumentSemanticTokensRange(
        params: SemanticTokensRangeParams
    ): SemanticTokens {
        observeRequest(LanguageServer.TEXT_DOCUMENT_SEMANTIC_TOKENS_RANGE, params)
        return observeResult(
            SemanticTokens(
                resultId = "conformance-range",
                data = listOf(0u, 0u, 4u, 0u, 0u)
            )
        )
    }

    override suspend fun textDocumentLinkedEditingRange(
        params: LinkedEditingRangeParams
    ): LinkedEditingRanges {
        observeRequest(LanguageServer.TEXT_DOCUMENT_LINKED_EDITING_RANGE, params)
        return observeResult(
            LinkedEditingRanges(
                ranges = listOf(range(0), range(1)),
                wordPattern = "[a-zA-Z_][a-zA-Z0-9_]*"
            )
        )
    }

    override suspend fun textDocumentMoniker(params: MonikerParams): List<Moniker> {
        observeRequest(LanguageServer.TEXT_DOCUMENT_MONIKER, params)
        return observeResult(
            listOf(
                Moniker(
                    scheme = "tsc",
                    identifier = "conformance#symbol",
                    unique = UniquenessLevel.SCHEME,
                    kind = MonikerKind.LOCAL
                )
            )
        )
    }

    override suspend fun textDocumentInlineValue(params: InlineValueParams): List<InlineValue> {
        observeRequest(LanguageServer.TEXT_DOCUMENT_INLINE_VALUE, params)
        return observeResult(listOf(InlineValueText(range = range(0), text = "42")))
    }

    override suspend fun textDocumentInlineCompletion(
        params: InlineCompletionParams
    ): TextDocumentInlineCompletionResult {
        observeRequest(LanguageServer.TEXT_DOCUMENT_INLINE_COMPLETION, params)
        return observeResult(
            TextDocumentInlineCompletionResult.InlineCompletionListValue(
                InlineCompletionList(
                    items = listOf(
                        InlineCompletionItem(
                            insertText = StringOr.StringValue("inline-completion")
                        )
                    )
                )
            )
        )
    }

    override suspend fun textDocumentDiagnostic(
        params: DocumentDiagnosticParams
    ): DocumentDiagnosticReport {
        observeRequest(LanguageServer.TEXT_DOCUMENT_DIAGNOSTIC, params)
        return observeResult(
            RelatedFullDocumentDiagnosticReport(
                kind = "full",
                resultId = "conformance-doc-diag",
                items = listOf(
                    Diagnostic(range = range(0), message = "canned doc diagnostic")
                )
            )
        )
    }

    override suspend fun workspaceDiagnostic(
        params: WorkspaceDiagnosticParams
    ): WorkspaceDiagnosticReport {
        observeRequest(LanguageServer.WORKSPACE_DIAGNOSTIC, params)
        return observeResult(
            WorkspaceDiagnosticReport(
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
        )
    }

    // ---- Call hierarchy ----

    override suspend fun textDocumentPrepareCallHierarchy(
        params: CallHierarchyPrepareParams
    ): List<CallHierarchyItem> {
        observeRequest(LanguageServer.TEXT_DOCUMENT_PREPARE_CALL_HIERARCHY, params)
        return observeResult(listOf(callHierarchyItem("preparedCall")))
    }

    override suspend fun callHierarchyIncomingCalls(
        params: CallHierarchyIncomingCallsParams
    ): List<CallHierarchyIncomingCall> {
        observeRequest(LanguageServer.CALL_HIERARCHY_INCOMING_CALLS, params)
        return observeResult(
            listOf(
                CallHierarchyIncomingCall(
                    from = callHierarchyItem("incomingCaller"),
                    fromRanges = listOf(range(0))
                )
            )
        )
    }

    override suspend fun callHierarchyOutgoingCalls(
        params: CallHierarchyOutgoingCallsParams
    ): List<CallHierarchyOutgoingCall> {
        observeRequest(LanguageServer.CALL_HIERARCHY_OUTGOING_CALLS, params)
        return observeResult(
            listOf(
                CallHierarchyOutgoingCall(
                    to = callHierarchyItem("outgoingCallee"),
                    fromRanges = listOf(range(0))
                )
            )
        )
    }

    // ---- Type hierarchy ----

    override suspend fun textDocumentPrepareTypeHierarchy(
        params: TypeHierarchyPrepareParams
    ): List<TypeHierarchyItem> {
        observeRequest(LanguageServer.TEXT_DOCUMENT_PREPARE_TYPE_HIERARCHY, params)
        return observeResult(listOf(typeHierarchyItem("preparedType")))
    }

    override suspend fun typeHierarchySupertypes(
        params: TypeHierarchySupertypesParams
    ): List<TypeHierarchyItem> {
        observeRequest(LanguageServer.TYPE_HIERARCHY_SUPERTYPES, params)
        return observeResult(listOf(typeHierarchyItem("Supertype")))
    }

    override suspend fun typeHierarchySubtypes(
        params: TypeHierarchySubtypesParams
    ): List<TypeHierarchyItem> {
        observeRequest(LanguageServer.TYPE_HIERARCHY_SUBTYPES, params)
        return observeResult(listOf(typeHierarchyItem("Subtype")))
    }

    // ---- Workspace operations ----

    override suspend fun workspaceSymbol(params: WorkspaceSymbolParams): LSPAny {
        observeRequest(LanguageServer.WORKSPACE_SYMBOL, params)
        // workspace/symbol's result is a JsonElement union (SymbolInformation[] |
        // WorkspaceSymbol[]). Emit a SymbolInformation[] for simplicity — lsp4j
        // accepts it.
        return observeResult(
            kotlinx.serialization.json.buildJsonArray {
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
        )
    }

    override suspend fun workspaceSymbolResolve(params: WorkspaceSymbol): WorkspaceSymbol {
        observeRequest(LanguageServer.WORKSPACE_SYMBOL_RESOLVE, params)
        return observeResult(params.copy(containerName = "resolved"))
    }

    override suspend fun workspaceExecuteCommand(params: ExecuteCommandParams): LSPAny {
        observeRequest(LanguageServer.WORKSPACE_EXECUTE_COMMAND, params)
        return observeResult(
            buildJsonObject {
                put("command", JsonPrimitive(params.command))
                put("status", JsonPrimitive("ok"))
            }
        )
    }

    override suspend fun workspaceWillCreateFiles(params: CreateFilesParams): WorkspaceEdit {
        observeRequest(LanguageServer.WORKSPACE_WILL_CREATE_FILES, params)
        return observeResult(
            WorkspaceEdit(
                changes = mapOf(
                    Uri.MAIN to listOf(TextEdit(range = range(0), newText = "// will-create\n"))
                )
            )
        )
    }

    override suspend fun workspaceWillRenameFiles(params: RenameFilesParams): WorkspaceEdit {
        observeRequest(LanguageServer.WORKSPACE_WILL_RENAME_FILES, params)
        return observeResult(
            WorkspaceEdit(
                changes = mapOf(
                    Uri.MAIN to listOf(TextEdit(range = range(0), newText = "// will-rename\n"))
                )
            )
        )
    }

    override suspend fun workspaceWillDeleteFiles(params: DeleteFilesParams): WorkspaceEdit {
        observeRequest(LanguageServer.WORKSPACE_WILL_DELETE_FILES, params)
        return observeResult(
            WorkspaceEdit(
                changes = mapOf(
                    Uri.MAIN to listOf(TextEdit(range = range(0), newText = "// will-delete\n"))
                )
            )
        )
    }

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
        record("initialized", "", params = params)
    }

    override suspend fun exit() {
        record("exit", "")
    }

    override suspend fun setTrace(params: SetTraceParams) {
        record("\$/setTrace", params.value.name, params = params)
    }

    override suspend fun progress(params: ProgressParams) {
        record("\$/progress", "token=${params.token}", params = params)
    }

    override suspend fun windowWorkDoneProgressCancel(params: WorkDoneProgressCancelParams) {
        record("window/workDoneProgress/cancel", "token=${params.token}", params = params)
    }

    /**
     * Records the notification receipt and, when the opened URI matches a well-known
     * [Triggers] URI, fires the corresponding server → client sequence so wire tests
     * can exercise the full client surface (issue #64). Subclasses are free to
     * override and call `super.textDocumentDidOpen(...)` to keep this behaviour, or
     * replace it.
     */
    override suspend fun textDocumentDidOpen(params: DidOpenTextDocumentParams) {
        record("textDocument/didOpen", "uri=${params.textDocument.uri}", params = params)
        if (params.textDocument.uri == Triggers.ALL) {
            emitAllClientTriggers()
        }
    }

    override suspend fun textDocumentDidChange(params: DidChangeTextDocumentParams) {
        record("textDocument/didChange", "uri=${params.textDocument.uri}", params = params)
    }

    override suspend fun textDocumentDidClose(params: DidCloseTextDocumentParams) {
        record("textDocument/didClose", "uri=${params.textDocument.uri}", params = params)
    }

    override suspend fun textDocumentDidSave(params: DidSaveTextDocumentParams) {
        record("textDocument/didSave", "uri=${params.textDocument.uri}", params = params)
    }

    override suspend fun textDocumentWillSave(params: WillSaveTextDocumentParams) {
        record("textDocument/willSave", "uri=${params.textDocument.uri}", params = params)
    }

    override suspend fun workspaceDidChangeConfiguration(params: DidChangeConfigurationParams) {
        record(
            "workspace/didChangeConfiguration",
            params.settings.toString().take(64),
            params = params
        )
    }

    override suspend fun workspaceDidChangeWatchedFiles(params: DidChangeWatchedFilesParams) {
        record(
            "workspace/didChangeWatchedFiles",
            "changes=${params.changes.size}",
            params = params
        )
    }

    override suspend fun workspaceDidChangeWorkspaceFolders(
        params: DidChangeWorkspaceFoldersParams
    ) {
        record(
            "workspace/didChangeWorkspaceFolders",
            "added=${params.event.added.size},removed=${params.event.removed.size}",
            params = params
        )
    }

    override suspend fun workspaceDidCreateFiles(params: CreateFilesParams) {
        record("workspace/didCreateFiles", "files=${params.files.size}", params = params)
    }

    override suspend fun workspaceDidRenameFiles(params: RenameFilesParams) {
        record("workspace/didRenameFiles", "files=${params.files.size}", params = params)
    }

    override suspend fun workspaceDidDeleteFiles(params: DeleteFilesParams) {
        record("workspace/didDeleteFiles", "files=${params.files.size}", params = params)
    }

    override suspend fun notebookDocumentDidOpen(params: DidOpenNotebookDocumentParams) {
        record(
            "notebookDocument/didOpen",
            "uri=${params.notebookDocument.uri}",
            params = params
        )
    }

    override suspend fun notebookDocumentDidChange(params: DidChangeNotebookDocumentParams) {
        record(
            "notebookDocument/didChange",
            "uri=${params.notebookDocument.uri}",
            params = params
        )
    }

    override suspend fun notebookDocumentDidSave(params: DidSaveNotebookDocumentParams) {
        record(
            "notebookDocument/didSave",
            "uri=${params.notebookDocument.uri}",
            params = params
        )
    }

    override suspend fun notebookDocumentDidClose(params: DidCloseNotebookDocumentParams) {
        record(
            "notebookDocument/didClose",
            "uri=${params.notebookDocument.uri}",
            params = params
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

    // region server -> client trigger surface (issue #64)

    /**
     * Issue one call against every server-initiated client method the fixture can
     * exercise over the wire, in the order documented by [ClientMethods.ALL]. Each
     * successful issue appends the method name to [issuedClientCalls] and emits on
     * [issuedClientCallsFlow]. Throws if [client] has not been set — tests that use
     * the trigger MUST assign the client stub returned from `connectAsLspServer`.
     */
    suspend fun emitAllClientTriggers() {
        val c = client
            ?: error("ConformanceLanguageServer.client must be assigned before triggering")

        // workspace/configuration (request → list)
        c.workspaceConfiguration(
            ConfigurationParams(
                items = listOf(
                    ConfigurationItem(scopeUri = Uri.MAIN, section = "conformance.section")
                )
            )
        )
        recordClientCall(ClientMethods.WORKSPACE_CONFIGURATION)

        // workspace/workspaceFolders (request)
        c.workspaceWorkspaceFolders()
        recordClientCall(ClientMethods.WORKSPACE_WORKSPACE_FOLDERS)

        // workspace/applyEdit (request)
        c.workspaceApplyEdit(
            ApplyWorkspaceEditParams(
                label = "trigger",
                edit = WorkspaceEdit(
                    changes = mapOf(
                        Uri.MAIN to listOf(
                            TextEdit(range = range(0), newText = "triggered\n")
                        )
                    )
                )
            )
        )
        recordClientCall(ClientMethods.WORKSPACE_APPLY_EDIT)

        // window/showMessageRequest (request)
        c.windowShowMessageRequest(
            ShowMessageRequestParams(
                type = MessageType.INFO,
                message = "trigger",
                actions = listOf(
                    MessageActionItem(title = "OK"),
                    MessageActionItem(title = "Cancel")
                )
            )
        )
        recordClientCall(ClientMethods.WINDOW_SHOW_MESSAGE_REQUEST)

        // window/showDocument (request)
        c.windowShowDocument(
            ShowDocumentParams(uri = Uri.MAIN, takeFocus = true)
        )
        recordClientCall(ClientMethods.WINDOW_SHOW_DOCUMENT)

        // client/registerCapability + client/unregisterCapability (requests)
        c.clientRegisterCapability(
            RegistrationParams(
                registrations = listOf(
                    Registration(
                        id = "conformance-registration",
                        method = "workspace/didChangeWatchedFiles"
                    )
                )
            )
        )
        recordClientCall(ClientMethods.CLIENT_REGISTER_CAPABILITY)

        c.clientUnregisterCapability(
            UnregistrationParams(
                unregisterations = listOf(
                    Unregistration(
                        id = "conformance-registration",
                        method = "workspace/didChangeWatchedFiles"
                    )
                )
            )
        )
        recordClientCall(ClientMethods.CLIENT_UNREGISTER_CAPABILITY)

        // window/workDoneProgress/create + $/progress
        c.windowWorkDoneProgressCreate(
            WorkDoneProgressCreateParams(
                token = IntOrString.StringValue(TRIGGER_PROGRESS_TOKEN)
            )
        )
        recordClientCall(ClientMethods.WINDOW_WORK_DONE_PROGRESS_CREATE)

        // Encode the WorkDoneProgressBegin value as a portable JsonElement: this
        // file lives in commonTest and runs on mingwX64 too, which does not have
        // the jsonrpc helpers / the LSP_JSON Json instance available. The shape
        // matches WorkDoneProgressBegin's wire form so lsp4j (and our own client
        // stub) can parse it as a WorkDoneProgressNotification.
        val begin = buildJsonObject {
            put("kind", "begin")
            put("title", "Conformance triggers")
        }
        c.progress(
            ProgressParams(
                token = IntOrString.StringValue(TRIGGER_PROGRESS_TOKEN),
                value = begin
            )
        )
        recordClientCall(ClientMethods.PROGRESS)

        // workspace/*/refresh family (requests with empty params)
        c.workspaceCodeLensRefresh()
        recordClientCall(ClientMethods.WORKSPACE_CODE_LENS_REFRESH)

        c.workspaceSemanticTokensRefresh()
        recordClientCall(ClientMethods.WORKSPACE_SEMANTIC_TOKENS_REFRESH)

        c.workspaceInlayHintRefresh()
        recordClientCall(ClientMethods.WORKSPACE_INLAY_HINT_REFRESH)

        c.workspaceInlineValueRefresh()
        recordClientCall(ClientMethods.WORKSPACE_INLINE_VALUE_REFRESH)

        c.workspaceDiagnosticRefresh()
        recordClientCall(ClientMethods.WORKSPACE_DIAGNOSTIC_REFRESH)

        c.workspaceFoldingRangeRefresh()
        recordClientCall(ClientMethods.WORKSPACE_FOLDING_RANGE_REFRESH)

        // telemetry/event + $/logTrace (notifications)
        c.telemetryEvent(JsonPrimitive("conformance-telemetry"))
        recordClientCall(ClientMethods.TELEMETRY_EVENT)

        c.logTrace(
            LogTraceParams(message = "conformance trace", verbose = "verbose detail")
        )
        recordClientCall(ClientMethods.LOG_TRACE)

        // window/showMessage + window/logMessage (notifications)
        c.windowShowMessage(
            ShowMessageParams(type = MessageType.INFO, message = "conformance show")
        )
        recordClientCall(ClientMethods.WINDOW_SHOW_MESSAGE)

        c.windowLogMessage(
            LogMessageParams(type = MessageType.LOG, message = "conformance log")
        )
        recordClientCall(ClientMethods.WINDOW_LOG_MESSAGE)

        // textDocument/publishDiagnostics (notification)
        c.textDocumentPublishDiagnostics(
            PublishDiagnosticsParams(
                uri = Uri.MAIN,
                diagnostics = listOf(
                    Diagnostic(range = range(0), message = "trigger diagnostic")
                )
            )
        )
        recordClientCall(ClientMethods.TEXT_DOCUMENT_PUBLISH_DIAGNOSTICS)
    }

    private suspend fun recordClientCall(method: String) {
        _issuedClientCalls += method
        _issuedClientCallsFlow.emit(method)
    }

    // endregion

    private companion object {
        const val TRIGGER_REPLAY = 32
        const val TRIGGER_BUFFER = 64
        const val TRIGGER_PROGRESS_TOKEN = "conformance-trigger-progress"
    }
}
