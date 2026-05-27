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

import com.monkopedia.lsp.DefaultLanguageClient
import com.monkopedia.lsp.LogMessageParams
import com.monkopedia.lsp.ProgressParams
import com.monkopedia.lsp.PublishDiagnosticsParams
import com.monkopedia.lsp.ShowMessageParams
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

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

    /** Append-only record of `window/showMessage` notifications received. */
    val showMessages: List<ShowMessageParams> get() = _showMessages.toList()

    /** Append-only record of `window/logMessage` notifications received. */
    val logMessages: List<LogMessageParams> get() = _logMessages.toList()

    /** Append-only record of `textDocument/publishDiagnostics` notifications. */
    val publishedDiagnostics: List<PublishDiagnosticsParams>
        get() = _publishedDiagnostics.toList()

    /** Append-only record of `$/progress` notifications received. */
    val progressNotifications: List<ProgressParams> get() = _progressNotifications.toList()

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

    /** Hot stream of `window/showMessage` notifications (replays recent ones). */
    val showMessageFlow: SharedFlow<ShowMessageParams> get() = _showMessageFlow

    /** Hot stream of `window/logMessage` notifications (replays recent ones). */
    val logMessageFlow: SharedFlow<LogMessageParams> get() = _logMessageFlow

    /** Hot stream of `textDocument/publishDiagnostics` (replays recent ones). */
    val diagnosticsFlow: SharedFlow<PublishDiagnosticsParams> get() = _diagnosticsFlow

    /** Hot stream of `$/progress` notifications (replays recent ones). */
    val progressFlow: SharedFlow<ProgressParams> get() = _progressFlow

    override suspend fun windowShowMessage(params: ShowMessageParams) {
        _showMessages += params
        _showMessageFlow.emit(params)
    }

    override suspend fun windowLogMessage(params: LogMessageParams) {
        _logMessages += params
        _logMessageFlow.emit(params)
    }

    override suspend fun textDocumentPublishDiagnostics(params: PublishDiagnosticsParams) {
        _publishedDiagnostics += params
        _diagnosticsFlow.emit(params)
    }

    override suspend fun progress(params: ProgressParams) {
        _progressNotifications += params
        _progressFlow.emit(params)
    }

    private companion object {
        const val REPLAY = 16
        const val BUFFER = 64
    }
}
