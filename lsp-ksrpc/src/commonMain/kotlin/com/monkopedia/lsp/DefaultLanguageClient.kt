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
 * Default LanguageClient where every method throws NotImplementedError.
 * Subclass and override only what you need.
 */
open class DefaultLanguageClient : LanguageClient {

    override suspend fun workspaceWorkspaceFolders(): List<WorkspaceFolder> =
        throw NotImplementedError("workspaceWorkspaceFolders not implemented")

    override suspend fun workspaceConfiguration(params: ConfigurationParams): List<LSPAny> =
        throw NotImplementedError("workspaceConfiguration not implemented")

    override suspend fun workspaceFoldingRangeRefresh(): Nothing? =
        throw NotImplementedError("workspaceFoldingRangeRefresh not implemented")

    override suspend fun windowWorkDoneProgressCreate(
        params: WorkDoneProgressCreateParams
    ): Nothing? = throw NotImplementedError("windowWorkDoneProgressCreate not implemented")

    override suspend fun workspaceSemanticTokensRefresh(): Nothing? =
        throw NotImplementedError("workspaceSemanticTokensRefresh not implemented")

    override suspend fun windowShowDocument(params: ShowDocumentParams): ShowDocumentResult =
        throw NotImplementedError("windowShowDocument not implemented")

    override suspend fun workspaceInlineValueRefresh(): Nothing? =
        throw NotImplementedError("workspaceInlineValueRefresh not implemented")

    override suspend fun workspaceInlayHintRefresh(): Nothing? =
        throw NotImplementedError("workspaceInlayHintRefresh not implemented")

    override suspend fun workspaceDiagnosticRefresh(): Nothing? =
        throw NotImplementedError("workspaceDiagnosticRefresh not implemented")

    override suspend fun clientRegisterCapability(params: RegistrationParams): Nothing? =
        throw NotImplementedError("clientRegisterCapability not implemented")

    override suspend fun clientUnregisterCapability(params: UnregistrationParams): Nothing? =
        throw NotImplementedError("clientUnregisterCapability not implemented")

    override suspend fun windowShowMessageRequest(
        params: ShowMessageRequestParams
    ): MessageActionItem = throw NotImplementedError("windowShowMessageRequest not implemented")

    override suspend fun workspaceCodeLensRefresh(): Nothing? =
        throw NotImplementedError("workspaceCodeLensRefresh not implemented")

    override suspend fun workspaceApplyEdit(
        params: ApplyWorkspaceEditParams
    ): ApplyWorkspaceEditResult = throw NotImplementedError("workspaceApplyEdit not implemented")

    override suspend fun windowShowMessage(params: ShowMessageParams): Unit =
        throw NotImplementedError("windowShowMessage not implemented")

    override suspend fun windowLogMessage(params: LogMessageParams): Unit =
        throw NotImplementedError("windowLogMessage not implemented")

    override suspend fun telemetryEvent(params: LSPAny): Unit =
        throw NotImplementedError("telemetryEvent not implemented")

    override suspend fun textDocumentPublishDiagnostics(params: PublishDiagnosticsParams): Unit =
        throw NotImplementedError("textDocumentPublishDiagnostics not implemented")

    override suspend fun logTrace(params: LogTraceParams): Unit =
        throw NotImplementedError("logTrace not implemented")

    override suspend fun progress(params: ProgressParams): Unit =
        throw NotImplementedError("progress not implemented")
}
