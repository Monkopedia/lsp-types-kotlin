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
 * LSP Language Client interface — methods the server calls on the client.
 */
@KsService
interface LanguageClient : RpcService {

    /**
     * The `workspace/workspaceFolders` is sent from the server to the client to fetch the open workspace folders.
     */
    @KsMethod("workspace/workspaceFolders")
    suspend fun workspaceWorkspaceFolders(): List<WorkspaceFolder>

    /**
     * The 'workspace/configuration' request is sent from the server to the client to fetch a certain
     * configuration setting.
     *
     * This pull model replaces the old push model where the client signaled configuration change via an
     * event. If the server still needs to react to configuration changes (since the server caches the
     * result of `workspace/configuration` requests) the server should register for an empty configuration
     * change event and empty the cache if such an event is received.
     */
    @KsMethod("workspace/configuration")
    suspend fun workspaceConfiguration(params: ConfigurationParams): List<LSPAny>

    /**
     * @since 3.18.0
     * @proposed
     */
    @KsMethod("workspace/foldingRange/refresh")
    suspend fun workspaceFoldingRangeRefresh(): Nothing?

    /**
     * The `window/workDoneProgress/create` request is sent from the server to the client to initiate progress
     * reporting from the server.
     */
    @KsMethod("window/workDoneProgress/create")
    suspend fun windowWorkDoneProgressCreate(params: WorkDoneProgressCreateParams): Nothing?

    /**
     * @since 3.16.0
     */
    @KsMethod("workspace/semanticTokens/refresh")
    suspend fun workspaceSemanticTokensRefresh(): Nothing?

    /**
     * A request to show a document. This request might open an
     * external program depending on the value of the URI to open.
     * For example a request to open `https://code.visualstudio.com/`
     * will very likely open the URI in a WEB browser.
     *
     * @since 3.16.0
     */
    @KsMethod("window/showDocument")
    suspend fun windowShowDocument(params: ShowDocumentParams): ShowDocumentResult

    /**
     * @since 3.17.0
     */
    @KsMethod("workspace/inlineValue/refresh")
    suspend fun workspaceInlineValueRefresh(): Nothing?

    /**
     * @since 3.17.0
     */
    @KsMethod("workspace/inlayHint/refresh")
    suspend fun workspaceInlayHintRefresh(): Nothing?

    /**
     * The diagnostic refresh request definition.
     *
     * @since 3.17.0
     */
    @KsMethod("workspace/diagnostic/refresh")
    suspend fun workspaceDiagnosticRefresh(): Nothing?

    /**
     * The `client/registerCapability` request is sent from the server to the client to register a new capability
     * handler on the client side.
     */
    @KsMethod("client/registerCapability")
    suspend fun clientRegisterCapability(params: RegistrationParams): Nothing?

    /**
     * The `client/unregisterCapability` request is sent from the server to the client to unregister a previously registered capability
     * handler on the client side.
     */
    @KsMethod("client/unregisterCapability")
    suspend fun clientUnregisterCapability(params: UnregistrationParams): Nothing?

    /**
     * The show message request is sent from the server to the client to show a message
     * and a set of options actions to the user.
     */
    @KsMethod("window/showMessageRequest")
    suspend fun windowShowMessageRequest(params: ShowMessageRequestParams): MessageActionItem

    /**
     * A request to refresh all code actions
     *
     * @since 3.16.0
     */
    @KsMethod("workspace/codeLens/refresh")
    suspend fun workspaceCodeLensRefresh(): Nothing?

    /**
     * A request sent from the server to the client to modified certain resources.
     */
    @KsMethod("workspace/applyEdit")
    suspend fun workspaceApplyEdit(params: ApplyWorkspaceEditParams): ApplyWorkspaceEditResult

    /**
     * The show message notification is sent from a server to a client to ask
     * the client to display a particular message in the user interface.
     */
    @KsMethod("window/showMessage")
    @KsNotification
    suspend fun windowShowMessage(params: ShowMessageParams)

    /**
     * The log message notification is sent from the server to the client to ask
     * the client to log a particular message.
     */
    @KsMethod("window/logMessage")
    @KsNotification
    suspend fun windowLogMessage(params: LogMessageParams)

    /**
     * The telemetry event notification is sent from the server to the client to ask
     * the client to log telemetry data.
     */
    @KsMethod("telemetry/event")
    @KsNotification
    suspend fun telemetryEvent(params: LSPAny)

    /**
     * Diagnostics notification are sent from the server to the client to signal
     * results of validation runs.
     */
    @KsMethod("textDocument/publishDiagnostics")
    @KsNotification
    suspend fun textDocumentPublishDiagnostics(params: PublishDiagnosticsParams)

    @KsMethod("$/logTrace")
    @KsNotification
    suspend fun logTrace(params: LogTraceParams)

    @KsMethod("$/progress")
    @KsNotification
    suspend fun progress(params: ProgressParams)
}
