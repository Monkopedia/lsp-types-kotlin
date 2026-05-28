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
import com.monkopedia.lsp.CodeAction
import com.monkopedia.lsp.CodeActionParams
import com.monkopedia.lsp.CodeLens
import com.monkopedia.lsp.CodeLensParams
import com.monkopedia.lsp.Command
import com.monkopedia.lsp.CompletionItem
import com.monkopedia.lsp.CompletionItemKind
import com.monkopedia.lsp.CompletionList
import com.monkopedia.lsp.CompletionOptions
import com.monkopedia.lsp.CompletionParams
import com.monkopedia.lsp.ConfigurationItem
import com.monkopedia.lsp.ConfigurationParams
import com.monkopedia.lsp.Declaration
import com.monkopedia.lsp.DeclarationLink
import com.monkopedia.lsp.DeclarationParams
import com.monkopedia.lsp.DefaultLanguageServer
import com.monkopedia.lsp.Definition
import com.monkopedia.lsp.DefinitionLink
import com.monkopedia.lsp.DefinitionParams
import com.monkopedia.lsp.DidOpenTextDocumentParams
import com.monkopedia.lsp.DocumentFormattingParams
import com.monkopedia.lsp.DocumentHighlight
import com.monkopedia.lsp.DocumentHighlightKind
import com.monkopedia.lsp.DocumentHighlightParams
import com.monkopedia.lsp.DocumentSymbol
import com.monkopedia.lsp.DocumentSymbolParams
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
import com.monkopedia.lsp.InlayHint
import com.monkopedia.lsp.InlayHintParams
import com.monkopedia.lsp.IntOrString
import com.monkopedia.lsp.KsrpcLanguageClient
import com.monkopedia.lsp.Location
import com.monkopedia.lsp.LocationLink
import com.monkopedia.lsp.LogMessageParams
import com.monkopedia.lsp.LogTraceParams
import com.monkopedia.lsp.MessageActionItem
import com.monkopedia.lsp.MessageType
import com.monkopedia.lsp.Position
import com.monkopedia.lsp.ProgressParams
import com.monkopedia.lsp.Range
import com.monkopedia.lsp.ReferenceParams
import com.monkopedia.lsp.Registration
import com.monkopedia.lsp.RegistrationParams
import com.monkopedia.lsp.RenameParams
import com.monkopedia.lsp.SemanticTokens
import com.monkopedia.lsp.SemanticTokensParams
import com.monkopedia.lsp.ServerCapabilities
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
import com.monkopedia.lsp.TextDocumentTypeDefinitionResult
import com.monkopedia.lsp.TextEdit
import com.monkopedia.lsp.TypeDefinitionParams
import com.monkopedia.lsp.Unregistration
import com.monkopedia.lsp.UnregistrationParams
import com.monkopedia.lsp.WorkDoneProgressCreateParams
import com.monkopedia.lsp.WorkspaceEdit
import com.monkopedia.lsp.markdown
import com.monkopedia.lsp.string
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
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
            WINDOW_LOG_MESSAGE
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
     * On `textDocument/didOpen`, if the opened URI matches a well-known [Triggers]
     * URI, fire the corresponding server → client sequence so wire tests can
     * exercise the full client surface. Subclasses are free to override and call
     * `super.textDocumentDidOpen(...)` to keep this behaviour, or replace it.
     */
    override suspend fun textDocumentDidOpen(params: DidOpenTextDocumentParams) {
        if (params.textDocument.uri == Triggers.ALL) {
            emitAllClientTriggers()
        }
    }

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
        record(ClientMethods.WORKSPACE_CONFIGURATION)

        // workspace/workspaceFolders (request)
        c.workspaceWorkspaceFolders()
        record(ClientMethods.WORKSPACE_WORKSPACE_FOLDERS)

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
        record(ClientMethods.WORKSPACE_APPLY_EDIT)

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
        record(ClientMethods.WINDOW_SHOW_MESSAGE_REQUEST)

        // window/showDocument (request)
        c.windowShowDocument(
            ShowDocumentParams(uri = Uri.MAIN, takeFocus = true)
        )
        record(ClientMethods.WINDOW_SHOW_DOCUMENT)

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
        record(ClientMethods.CLIENT_REGISTER_CAPABILITY)

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
        record(ClientMethods.CLIENT_UNREGISTER_CAPABILITY)

        // window/workDoneProgress/create + $/progress
        c.windowWorkDoneProgressCreate(
            WorkDoneProgressCreateParams(
                token = IntOrString.StringValue(TRIGGER_PROGRESS_TOKEN)
            )
        )
        record(ClientMethods.WINDOW_WORK_DONE_PROGRESS_CREATE)

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
        record(ClientMethods.PROGRESS)

        // workspace/*/refresh family (requests with empty params)
        c.workspaceCodeLensRefresh()
        record(ClientMethods.WORKSPACE_CODE_LENS_REFRESH)

        c.workspaceSemanticTokensRefresh()
        record(ClientMethods.WORKSPACE_SEMANTIC_TOKENS_REFRESH)

        c.workspaceInlayHintRefresh()
        record(ClientMethods.WORKSPACE_INLAY_HINT_REFRESH)

        c.workspaceInlineValueRefresh()
        record(ClientMethods.WORKSPACE_INLINE_VALUE_REFRESH)

        c.workspaceDiagnosticRefresh()
        record(ClientMethods.WORKSPACE_DIAGNOSTIC_REFRESH)

        c.workspaceFoldingRangeRefresh()
        record(ClientMethods.WORKSPACE_FOLDING_RANGE_REFRESH)

        // telemetry/event + $/logTrace (notifications)
        c.telemetryEvent(JsonPrimitive("conformance-telemetry"))
        record(ClientMethods.TELEMETRY_EVENT)

        c.logTrace(
            LogTraceParams(message = "conformance trace", verbose = "verbose detail")
        )
        record(ClientMethods.LOG_TRACE)

        // window/showMessage + window/logMessage (notifications)
        c.windowShowMessage(
            ShowMessageParams(type = MessageType.INFO, message = "conformance show")
        )
        record(ClientMethods.WINDOW_SHOW_MESSAGE)

        c.windowLogMessage(
            LogMessageParams(type = MessageType.LOG, message = "conformance log")
        )
        record(ClientMethods.WINDOW_LOG_MESSAGE)
    }

    private suspend fun record(method: String) {
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
