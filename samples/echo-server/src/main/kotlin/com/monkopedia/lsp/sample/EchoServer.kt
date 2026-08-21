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
package com.monkopedia.lsp.sample

import com.monkopedia.lsp.BooleanOr
import com.monkopedia.lsp.DefaultLanguageServer
import com.monkopedia.lsp.DidOpenTextDocumentParams
import com.monkopedia.lsp.Hover
import com.monkopedia.lsp.HoverContents
import com.monkopedia.lsp.HoverParams
import com.monkopedia.lsp.InitializeParams
import com.monkopedia.lsp.InitializeResult
import com.monkopedia.lsp.InitializeResultServerInfo
import com.monkopedia.lsp.InitializedParams
import com.monkopedia.lsp.LanguageClient
import com.monkopedia.lsp.LogMessageParams
import com.monkopedia.lsp.MessageType
import com.monkopedia.lsp.ServerCapabilities
import com.monkopedia.lsp.TextDocumentSyncKind
import com.monkopedia.lsp.ksrpc.LifecycleState
import com.monkopedia.lsp.ksrpc.connectAsLspServer
import com.monkopedia.lsp.ksrpc.stdInLspConnection
import com.monkopedia.lsp.markdown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * Minimal LSP server demo. Reads from stdin and writes to stdout — connect to
 * it from any LSP-capable editor by configuring it as the language server
 * binary for some made-up language.
 *
 * It tracks documents in memory and responds to hover with the URI plus
 * the position. That's it.
 *
 * Run with:
 *
 * ```
 * ./gradlew :samples:echo-server:installDist
 * samples/echo-server/build/install/echo-server/bin/echo-server
 * ```
 */
fun main(): Unit = runBlocking(Dispatchers.IO) {
    val state = LifecycleState()
    var client: LanguageClient? = null
    val documents = mutableMapOf<String, String>()

    val server = object : DefaultLanguageServer() {
        override suspend fun initialize(params: InitializeParams): InitializeResult =
            InitializeResult(
                capabilities = ServerCapabilities(
                    textDocumentSync = TextDocumentSyncKind.FULL,
                    hoverProvider = BooleanOr(true)
                ),
                serverInfo = InitializeResultServerInfo(
                    name = "echo-server",
                    version = "0.1.0"
                )
            )

        override suspend fun initialized(params: InitializedParams) {
            client?.windowLogMessage(
                LogMessageParams(
                    type = MessageType.INFO,
                    message = "echo-server: ready"
                )
            )
        }

        override suspend fun textDocumentDidOpen(params: DidOpenTextDocumentParams) {
            documents[params.textDocument.uri] = params.textDocument.text
        }

        override suspend fun textDocumentHover(params: HoverParams): Hover {
            val pos = params.position
            val uri = params.textDocument.uri
            return Hover(
                contents = HoverContents.markdown("**$uri** at ${pos.line}:${pos.character}")
            )
        }

        // `shutdown` has no default — the base class throws until you implement it.
        // Nothing to tear down here, so just acknowledge it; the phase change to
        // SHUTTING_DOWN is the wrapper's job, not ours.
        override suspend fun shutdown(): Nothing? = null

        // No `exit` override: the base class no-ops it, the wrapper advances `state`
        // to EXITED, and `main` (below) is watching for that.
    }

    val connection = stdInLspConnection()
    // Pass `state` to `connectAsLspServer` and the wiring layer keeps it current for
    // you — INITIALIZED on the `initialized` notification, SHUTTING_DOWN on the
    // `shutdown` request, EXITED on the `exit` notification. Doing it here rather than
    // inside the handlers above is what makes the phases match the spec: `initialize`
    // returning is *not* yet INITIALIZED, so a server that flips the phase from its own
    // `initialize` body is a step ahead of the client.
    client = connection.connectAsLspServer(server, state)

    // Keep `main` alive while the connection serves requests. The JSON-RPC read pump
    // runs in a connection-owned scope (issue #87 hardening) rather than as a child of
    // this `runBlocking`, so wiring the server does not implicitly block here — we must
    // wait explicitly. Waiting on the lifecycle rather than parking forever is the
    // payoff for tracking it: `exit` drives `state` to EXITED, this returns, and the
    // process ends on its own instead of being killed with `exitProcess`.
    state.awaitPhase(LifecycleState.Phase.EXITED)
}
