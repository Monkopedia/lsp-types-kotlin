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
 * ksrpc-annotated [LanguageClient] for use with the JSON-RPC transport.
 * Implement this (or extend [DefaultLanguageClient]) to host an LSP client
 * via [com.monkopedia.lsp.ksrpc.connectAsLspClient].
 */
@KsService
interface KsrpcLanguageClient :
    LanguageClient,
    RpcService {

    @KsMethod("workspace/workspaceFolders")
    override suspend fun workspaceWorkspaceFolders(): List<WorkspaceFolder>

    @KsMethod("workspace/configuration")
    override suspend fun workspaceConfiguration(params: ConfigurationParams): List<LSPAny>

    @KsMethod("workspace/foldingRange/refresh")
    override suspend fun workspaceFoldingRangeRefresh(): Nothing?

    @KsMethod("window/workDoneProgress/create")
    override suspend fun windowWorkDoneProgressCreate(
        params: WorkDoneProgressCreateParams
    ): Nothing?

    @KsMethod("workspace/semanticTokens/refresh")
    override suspend fun workspaceSemanticTokensRefresh(): Nothing?

    @KsMethod("window/showDocument")
    override suspend fun windowShowDocument(params: ShowDocumentParams): ShowDocumentResult

    @KsMethod("workspace/inlineValue/refresh")
    override suspend fun workspaceInlineValueRefresh(): Nothing?

    @KsMethod("workspace/inlayHint/refresh")
    override suspend fun workspaceInlayHintRefresh(): Nothing?

    @KsMethod("workspace/diagnostic/refresh")
    override suspend fun workspaceDiagnosticRefresh(): Nothing?

    @KsMethod("client/registerCapability")
    override suspend fun clientRegisterCapability(params: RegistrationParams): Nothing?

    @KsMethod("client/unregisterCapability")
    override suspend fun clientUnregisterCapability(params: UnregistrationParams): Nothing?

    @KsMethod("window/showMessageRequest")
    override suspend fun windowShowMessageRequest(
        params: ShowMessageRequestParams
    ): MessageActionItem

    @KsMethod("workspace/codeLens/refresh")
    override suspend fun workspaceCodeLensRefresh(): Nothing?

    @KsMethod("workspace/applyEdit")
    override suspend fun workspaceApplyEdit(
        params: ApplyWorkspaceEditParams
    ): ApplyWorkspaceEditResult

    @KsMethod("window/showMessage")
    @KsNotification
    override suspend fun windowShowMessage(params: ShowMessageParams)

    @KsMethod("window/logMessage")
    @KsNotification
    override suspend fun windowLogMessage(params: LogMessageParams)

    @KsMethod("telemetry/event")
    @KsNotification
    override suspend fun telemetryEvent(params: LSPAny)

    @KsMethod("textDocument/publishDiagnostics")
    @KsNotification
    override suspend fun textDocumentPublishDiagnostics(params: PublishDiagnosticsParams)

    @KsMethod("$/logTrace")
    @KsNotification
    override suspend fun logTrace(params: LogTraceParams)

    @KsMethod("$/progress")
    @KsNotification
    override suspend fun progress(params: ProgressParams)
}
