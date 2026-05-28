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
import com.monkopedia.lsp.ApplyWorkspaceEditResult
import com.monkopedia.lsp.ConfigurationParams
import com.monkopedia.lsp.DefaultLanguageClient
import com.monkopedia.lsp.LSPAny
import com.monkopedia.lsp.LanguageClient
import com.monkopedia.lsp.LogMessageParams
import com.monkopedia.lsp.LogTraceParams
import com.monkopedia.lsp.MessageActionItem
import com.monkopedia.lsp.ProgressParams
import com.monkopedia.lsp.PublishDiagnosticsParams
import com.monkopedia.lsp.RegistrationParams
import com.monkopedia.lsp.ShowDocumentParams
import com.monkopedia.lsp.ShowDocumentResult
import com.monkopedia.lsp.ShowMessageParams
import com.monkopedia.lsp.ShowMessageRequestParams
import com.monkopedia.lsp.UnregistrationParams
import com.monkopedia.lsp.WorkDoneProgressCreateParams
import com.monkopedia.lsp.WorkspaceFolder
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.json.JsonNull

/**
 * Conformance fixture client: records every server → client call it receives so a
 * test can assert what the server pushed. All recorded calls land in
 * append-only lists *and* are re-emitted on hot [SharedFlow]s so a test can
 * either snapshot ([showMessages] etc.) after the fact or suspend on the flow
 * until a specific call arrives.
 *
 * Recorded notifications:
 * - `window/showMessage` → [showMessages] / [showMessageFlow]
 * - `window/logMessage` → [logMessages] / [logMessageFlow]
 * - `textDocument/publishDiagnostics` → [publishedDiagnostics] / [diagnosticsFlow]
 * - `$/progress` → [progressNotifications] / [progressFlow]
 * - `telemetry/event` → [telemetryEvents] / [telemetryEventFlow]
 * - `$/logTrace` → [logTraces] / [logTraceFlow]
 *
 * Recorded requests (issue #64 — server-initiated client surface):
 * - `workspace/configuration` → [configurationRequests]
 * - `workspace/workspaceFolders` → [workspaceFoldersRequestCount]
 * - `workspace/applyEdit` → [applyEditRequests]
 * - `window/showMessageRequest` → [showMessageRequests]
 * - `window/showDocument` → [showDocumentRequests]
 * - `client/registerCapability` → [registerCapabilityRequests]
 * - `client/unregisterCapability` → [unregisterCapabilityRequests]
 * - `window/workDoneProgress/create` → [workDoneProgressCreateRequests]
 * - `workspace/{codeLens,semanticTokens,inlayHint,inlineValue,diagnostic,foldingRange}/refresh`
 *   family → [refreshCalls] (one entry per call, by method name)
 *
 * Every recorded request returns a deterministic canned response — e.g.
 * `applyEdit` → `applied = true`, `configuration` → a list of [JsonNull] entries
 * (one per requested item), `showMessageRequest` → the first action item if any
 * (or [DEFAULT_MESSAGE_ACTION] otherwise), `workspaceFolders` → [DEFAULT_FOLDER].
 * Refresh requests succeed with `null` (their `Nothing?` LSP signature).
 *
 * The lists are not synchronized; drive the fixture from a single test coroutine
 * (the in-memory transport dispatches receives sequentially), or collect the
 * flows for cross-coroutine assertions.
 */
open class ConformanceLanguageClient : DefaultLanguageClient() {

    private val _showMessages = mutableListOf<ShowMessageParams>()
    private val _logMessages = mutableListOf<LogMessageParams>()
    private val _publishedDiagnostics = mutableListOf<PublishDiagnosticsParams>()
    private val _progressNotifications = mutableListOf<ProgressParams>()
    private val _telemetryEvents = mutableListOf<LSPAny>()
    private val _logTraces = mutableListOf<LogTraceParams>()

    private val _configurationRequests = mutableListOf<ConfigurationParams>()
    private var _workspaceFoldersRequestCount = 0
    private val _applyEditRequests = mutableListOf<ApplyWorkspaceEditParams>()
    private val _showMessageRequests = mutableListOf<ShowMessageRequestParams>()
    private val _showDocumentRequests = mutableListOf<ShowDocumentParams>()
    private val _registerCapabilityRequests = mutableListOf<RegistrationParams>()
    private val _unregisterCapabilityRequests = mutableListOf<UnregistrationParams>()
    private val _workDoneProgressCreateRequests =
        mutableListOf<WorkDoneProgressCreateParams>()
    private val _refreshCalls = mutableListOf<String>()

    /** Append-only record of `window/showMessage` notifications received. */
    val showMessages: List<ShowMessageParams> get() = _showMessages.toList()

    /** Append-only record of `window/logMessage` notifications received. */
    val logMessages: List<LogMessageParams> get() = _logMessages.toList()

    /** Append-only record of `textDocument/publishDiagnostics` notifications. */
    val publishedDiagnostics: List<PublishDiagnosticsParams>
        get() = _publishedDiagnostics.toList()

    /** Append-only record of `$/progress` notifications received. */
    val progressNotifications: List<ProgressParams> get() = _progressNotifications.toList()

    /** Append-only record of `telemetry/event` notifications received. */
    val telemetryEvents: List<LSPAny> get() = _telemetryEvents.toList()

    /** Append-only record of `$/logTrace` notifications received. */
    val logTraces: List<LogTraceParams> get() = _logTraces.toList()

    /** Append-only record of `workspace/configuration` requests received. */
    val configurationRequests: List<ConfigurationParams>
        get() = _configurationRequests.toList()

    /** Number of `workspace/workspaceFolders` requests received. */
    val workspaceFoldersRequestCount: Int get() = _workspaceFoldersRequestCount

    /** Append-only record of `workspace/applyEdit` requests received. */
    val applyEditRequests: List<ApplyWorkspaceEditParams>
        get() = _applyEditRequests.toList()

    /** Append-only record of `window/showMessageRequest` requests received. */
    val showMessageRequests: List<ShowMessageRequestParams>
        get() = _showMessageRequests.toList()

    /** Append-only record of `window/showDocument` requests received. */
    val showDocumentRequests: List<ShowDocumentParams>
        get() = _showDocumentRequests.toList()

    /** Append-only record of `client/registerCapability` requests received. */
    val registerCapabilityRequests: List<RegistrationParams>
        get() = _registerCapabilityRequests.toList()

    /** Append-only record of `client/unregisterCapability` requests received. */
    val unregisterCapabilityRequests: List<UnregistrationParams>
        get() = _unregisterCapabilityRequests.toList()

    /** Append-only record of `window/workDoneProgress/create` requests received. */
    val workDoneProgressCreateRequests: List<WorkDoneProgressCreateParams>
        get() = _workDoneProgressCreateRequests.toList()

    /**
     * Append-only ordered record of `workspace/<feature>/refresh` request method
     * names the fixture handled, one entry per call. Method names use the LSP
     * wire naming (e.g. `workspace/codeLens/refresh`).
     */
    val refreshCalls: List<String> get() = _refreshCalls.toList()

    private val _showMessageFlow = MutableSharedFlow<ShowMessageParams>(
        replay = REPLAY,
        extraBufferCapacity = BUFFER
    )
    private val _logMessageFlow = MutableSharedFlow<LogMessageParams>(
        replay = REPLAY,
        extraBufferCapacity = BUFFER
    )
    private val _diagnosticsFlow = MutableSharedFlow<PublishDiagnosticsParams>(
        replay = REPLAY,
        extraBufferCapacity = BUFFER
    )
    private val _progressFlow = MutableSharedFlow<ProgressParams>(
        replay = REPLAY,
        extraBufferCapacity = BUFFER
    )
    private val _telemetryEventFlow = MutableSharedFlow<LSPAny>(
        replay = REPLAY,
        extraBufferCapacity = BUFFER
    )
    private val _logTraceFlow = MutableSharedFlow<LogTraceParams>(
        replay = REPLAY,
        extraBufferCapacity = BUFFER
    )

    /** Hot stream of `window/showMessage` notifications (replays recent ones). */
    val showMessageFlow: SharedFlow<ShowMessageParams> get() = _showMessageFlow

    /** Hot stream of `window/logMessage` notifications (replays recent ones). */
    val logMessageFlow: SharedFlow<LogMessageParams> get() = _logMessageFlow

    /** Hot stream of `textDocument/publishDiagnostics` (replays recent ones). */
    val diagnosticsFlow: SharedFlow<PublishDiagnosticsParams> get() = _diagnosticsFlow

    /** Hot stream of `$/progress` notifications (replays recent ones). */
    val progressFlow: SharedFlow<ProgressParams> get() = _progressFlow

    /** Hot stream of `telemetry/event` notifications (replays recent ones). */
    val telemetryEventFlow: SharedFlow<LSPAny> get() = _telemetryEventFlow

    /** Hot stream of `$/logTrace` notifications (replays recent ones). */
    val logTraceFlow: SharedFlow<LogTraceParams> get() = _logTraceFlow

    override suspend fun windowShowMessage(params: ShowMessageParams) {
        observe(LanguageClient.WINDOW_SHOW_MESSAGE)
        _showMessages += params
        _showMessageFlow.emit(params)
    }

    override suspend fun windowLogMessage(params: LogMessageParams) {
        observe(LanguageClient.WINDOW_LOG_MESSAGE)
        _logMessages += params
        _logMessageFlow.emit(params)
    }

    override suspend fun textDocumentPublishDiagnostics(params: PublishDiagnosticsParams) {
        observe(LanguageClient.TEXT_DOCUMENT_PUBLISH_DIAGNOSTICS)
        _publishedDiagnostics += params
        _diagnosticsFlow.emit(params)
    }

    override suspend fun progress(params: ProgressParams) {
        observe(LanguageClient.PROGRESS)
        _progressNotifications += params
        _progressFlow.emit(params)
    }

    override suspend fun telemetryEvent(params: LSPAny) {
        observe(LanguageClient.TELEMETRY_EVENT)
        _telemetryEvents += params
        _telemetryEventFlow.emit(params)
    }

    override suspend fun logTrace(params: LogTraceParams) {
        observe(LanguageClient.LOG_TRACE)
        _logTraces += params
        _logTraceFlow.emit(params)
    }

    override suspend fun workspaceConfiguration(params: ConfigurationParams): List<LSPAny> {
        observe(LanguageClient.WORKSPACE_CONFIGURATION)
        _configurationRequests += params
        // Deterministic canned response: one JsonNull per requested item so the
        // shape (a list of LSPAny) round-trips and tests can count items.
        return params.items.map { JsonNull }
    }

    override suspend fun workspaceWorkspaceFolders(): List<WorkspaceFolder> {
        observe(LanguageClient.WORKSPACE_WORKSPACE_FOLDERS)
        _workspaceFoldersRequestCount += 1
        return listOf(DEFAULT_FOLDER)
    }

    override suspend fun workspaceApplyEdit(
        params: ApplyWorkspaceEditParams
    ): ApplyWorkspaceEditResult {
        observe(LanguageClient.WORKSPACE_APPLY_EDIT)
        _applyEditRequests += params
        return ApplyWorkspaceEditResult(applied = true)
    }

    override suspend fun windowShowMessageRequest(
        params: ShowMessageRequestParams
    ): MessageActionItem {
        observe(LanguageClient.WINDOW_SHOW_MESSAGE_REQUEST)
        _showMessageRequests += params
        return params.actions?.firstOrNull() ?: DEFAULT_MESSAGE_ACTION
    }

    override suspend fun windowShowDocument(params: ShowDocumentParams): ShowDocumentResult {
        observe(LanguageClient.WINDOW_SHOW_DOCUMENT)
        _showDocumentRequests += params
        return ShowDocumentResult(success = true)
    }

    override suspend fun clientRegisterCapability(params: RegistrationParams): Nothing? {
        observe(LanguageClient.CLIENT_REGISTER_CAPABILITY)
        _registerCapabilityRequests += params
        return null
    }

    override suspend fun clientUnregisterCapability(params: UnregistrationParams): Nothing? {
        observe(LanguageClient.CLIENT_UNREGISTER_CAPABILITY)
        _unregisterCapabilityRequests += params
        return null
    }

    override suspend fun windowWorkDoneProgressCreate(
        params: WorkDoneProgressCreateParams
    ): Nothing? {
        observe(LanguageClient.WINDOW_WORK_DONE_PROGRESS_CREATE)
        _workDoneProgressCreateRequests += params
        return null
    }

    override suspend fun workspaceCodeLensRefresh(): Nothing? {
        observe(LanguageClient.WORKSPACE_CODE_LENS_REFRESH)
        _refreshCalls += "workspace/codeLens/refresh"
        return null
    }

    override suspend fun workspaceSemanticTokensRefresh(): Nothing? {
        observe(LanguageClient.WORKSPACE_SEMANTIC_TOKENS_REFRESH)
        _refreshCalls += "workspace/semanticTokens/refresh"
        return null
    }

    override suspend fun workspaceInlayHintRefresh(): Nothing? {
        observe(LanguageClient.WORKSPACE_INLAY_HINT_REFRESH)
        _refreshCalls += "workspace/inlayHint/refresh"
        return null
    }

    override suspend fun workspaceInlineValueRefresh(): Nothing? {
        observe(LanguageClient.WORKSPACE_INLINE_VALUE_REFRESH)
        _refreshCalls += "workspace/inlineValue/refresh"
        return null
    }

    override suspend fun workspaceDiagnosticRefresh(): Nothing? {
        observe(LanguageClient.WORKSPACE_DIAGNOSTIC_REFRESH)
        _refreshCalls += "workspace/diagnostic/refresh"
        return null
    }

    override suspend fun workspaceFoldingRangeRefresh(): Nothing? {
        observe(LanguageClient.WORKSPACE_FOLDING_RANGE_REFRESH)
        _refreshCalls += "workspace/foldingRange/refresh"
        return null
    }

    /**
     * Wire-coverage observation hook (issue #66). Fires into the shared
     * [ConformanceWireRecorder] so the JVM coverage tracker can record every
     * client-side method that arrived over the wire. Defaults to a no-op on
     * targets that haven't installed a recorder, keeping the fixture's public
     * contract unchanged.
     */
    private fun observe(method: String) {
        ConformanceWireRecorder.observeClient(method)
    }

    companion object {
        /** The canned [WorkspaceFolder] returned from `workspace/workspaceFolders`. */
        val DEFAULT_FOLDER: WorkspaceFolder = WorkspaceFolder(
            uri = "file:///conformance",
            name = "conformance-root"
        )

        /** The canned action returned when `showMessageRequest` has no actions. */
        val DEFAULT_MESSAGE_ACTION: MessageActionItem = MessageActionItem(title = "OK")

        private const val REPLAY = 16
        private const val BUFFER = 64
    }
}
