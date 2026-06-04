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
 * LSP Language Client interface — methods the server calls on the client.
 */
interface LanguageClient {

    /**
     * The `workspace/workspaceFolders` is sent from the server to the client to fetch the open workspace folders.
     */
    suspend fun workspaceWorkspaceFolders(): List<WorkspaceFolder>?

    /**
     * The 'workspace/configuration' request is sent from the server to the client to fetch a certain
     * configuration setting.
     *
     * This pull model replaces the old push model where the client signaled configuration change via an
     * event. If the server still needs to react to configuration changes (since the server caches the
     * result of `workspace/configuration` requests) the server should register for an empty configuration
     * change event and empty the cache if such an event is received.
     */
    suspend fun workspaceConfiguration(params: ConfigurationParams): List<LSPAny>

    /**
     * @since 3.18.0
     * @proposed
     */
    suspend fun workspaceFoldingRangeRefresh(): Nothing?

    /**
     * The `window/workDoneProgress/create` request is sent from the server to the client to initiate progress
     * reporting from the server.
     */
    suspend fun windowWorkDoneProgressCreate(params: WorkDoneProgressCreateParams): Nothing?

    /**
     * @since 3.16.0
     */
    suspend fun workspaceSemanticTokensRefresh(): Nothing?

    /**
     * A request to show a document. This request might open an
     * external program depending on the value of the URI to open.
     * For example a request to open `https://code.visualstudio.com/`
     * will very likely open the URI in a WEB browser.
     *
     * @since 3.16.0
     */
    suspend fun windowShowDocument(params: ShowDocumentParams): ShowDocumentResult

    /**
     * @since 3.17.0
     */
    suspend fun workspaceInlineValueRefresh(): Nothing?

    /**
     * @since 3.17.0
     */
    suspend fun workspaceInlayHintRefresh(): Nothing?

    /**
     * The diagnostic refresh request definition.
     *
     * @since 3.17.0
     */
    suspend fun workspaceDiagnosticRefresh(): Nothing?

    /**
     * The `client/registerCapability` request is sent from the server to the client to register a new capability
     * handler on the client side.
     */
    suspend fun clientRegisterCapability(params: RegistrationParams): Nothing?

    /**
     * The `client/unregisterCapability` request is sent from the server to the client to unregister a previously registered capability
     * handler on the client side.
     */
    suspend fun clientUnregisterCapability(params: UnregistrationParams): Nothing?

    /**
     * The show message request is sent from the server to the client to show a message
     * and a set of options actions to the user.
     */
    suspend fun windowShowMessageRequest(params: ShowMessageRequestParams): MessageActionItem?

    /**
     * A request to refresh all code actions
     *
     * @since 3.16.0
     */
    suspend fun workspaceCodeLensRefresh(): Nothing?

    /**
     * A request sent from the server to the client to modified certain resources.
     */
    suspend fun workspaceApplyEdit(params: ApplyWorkspaceEditParams): ApplyWorkspaceEditResult

    /**
     * The show message notification is sent from a server to a client to ask
     * the client to display a particular message in the user interface.
     */
    suspend fun windowShowMessage(params: ShowMessageParams)

    /**
     * The log message notification is sent from the server to the client to ask
     * the client to log a particular message.
     */
    suspend fun windowLogMessage(params: LogMessageParams)

    /**
     * The telemetry event notification is sent from the server to the client to ask
     * the client to log telemetry data.
     */
    suspend fun telemetryEvent(params: LSPAny)

    /**
     * Diagnostics notification are sent from the server to the client to signal
     * results of validation runs.
     */
    suspend fun textDocumentPublishDiagnostics(params: PublishDiagnosticsParams)

    suspend fun logTrace(params: LogTraceParams)

    suspend fun progress(params: ProgressParams)

    companion object {
        const val WORKSPACE_WORKSPACE_FOLDERS: String = "workspace/workspaceFolders"
        const val WORKSPACE_CONFIGURATION: String = "workspace/configuration"
        const val WORKSPACE_FOLDING_RANGE_REFRESH: String = "workspace/foldingRange/refresh"
        const val WINDOW_WORK_DONE_PROGRESS_CREATE: String = "window/workDoneProgress/create"
        const val WORKSPACE_SEMANTIC_TOKENS_REFRESH: String = "workspace/semanticTokens/refresh"
        const val WINDOW_SHOW_DOCUMENT: String = "window/showDocument"
        const val WORKSPACE_INLINE_VALUE_REFRESH: String = "workspace/inlineValue/refresh"
        const val WORKSPACE_INLAY_HINT_REFRESH: String = "workspace/inlayHint/refresh"
        const val WORKSPACE_DIAGNOSTIC_REFRESH: String = "workspace/diagnostic/refresh"
        const val CLIENT_REGISTER_CAPABILITY: String = "client/registerCapability"
        const val CLIENT_UNREGISTER_CAPABILITY: String = "client/unregisterCapability"
        const val WINDOW_SHOW_MESSAGE_REQUEST: String = "window/showMessageRequest"
        const val WORKSPACE_CODE_LENS_REFRESH: String = "workspace/codeLens/refresh"
        const val WORKSPACE_APPLY_EDIT: String = "workspace/applyEdit"
        const val WINDOW_SHOW_MESSAGE: String = "window/showMessage"
        const val WINDOW_LOG_MESSAGE: String = "window/logMessage"
        const val TELEMETRY_EVENT: String = "telemetry/event"
        const val TEXT_DOCUMENT_PUBLISH_DIAGNOSTICS: String = "textDocument/publishDiagnostics"
        const val LOG_TRACE: String = "$/logTrace"
        const val PROGRESS: String = "$/progress"
    }
}
