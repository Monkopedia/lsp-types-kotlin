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
package com.monkopedia.lsp.ksrpc

import com.monkopedia.lsp.ClientCapabilities
import com.monkopedia.lsp.DidOpenTextDocumentParams
import com.monkopedia.lsp.HoverContents
import com.monkopedia.lsp.HoverParams
import com.monkopedia.lsp.InitializeParams
import com.monkopedia.lsp.KsrpcLanguageServer
import com.monkopedia.lsp.Position
import com.monkopedia.lsp.TextDocumentIdentifier
import com.monkopedia.lsp.TextDocumentItem
import com.monkopedia.lsp.ksrpc.fixtures.ConformanceLanguageClient
import com.monkopedia.lsp.ksrpc.fixtures.ConformanceLanguageServer
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.close
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/**
 * Native (Kotlin/Native: linuxX64 + macOS) end-to-end smoke for the jsonrpc ksrpc
 * transport. Drives a real LSP round-trip — `initialize` → `didOpen` →
 * `hover` → `shutdown`/`exit` — over the same [asLspConnection] helper a native
 * client/server uses, exercising `Content-Length` framing, the generated
 * `@KsService` dispatch and the LSP `$/cancelRequest` convention compiled for
 * Kotlin/Native.
 *
 * ## Why in-memory channels rather than a real posix-stdio child process
 *
 * The original ambition for #47 was a native client driving an external server
 * (e.g. `clangd`) over a posix-stdio pipe and tearing the child down with a
 * process kill. That path is **blocked on #201 (ksrpc) / our #53**: the only
 * native posix-fd byte-channel primitive (ksrpc-sockets `posixFileReadChannel`)
 * reads on a dedicated thread blocked in a `read(2)` syscall that is **not**
 * coroutine-cancellable — a `withTimeout` cannot unblock it — and on EOF it
 * busy-loops rather than terminating. lsp-ksrpc also exposes no native
 * process-spawn surface (`ProcessBuilder.asLspConnection` is JVM-only). A native
 * round-trip built on that primitive can hang the build on teardown, which is
 * explicitly disallowed.
 *
 * This test therefore uses ktor in-memory [ByteChannel]s, which carry the exact
 * same jsonrpc wire bytes but are fully coroutine-driven: teardown is a channel
 * close plus a scope cancel, with no blocking syscall, so it terminates
 * deterministically and can never hang `allTests`. It proves the native
 * transport carries a real LSP round-trip; the external-process posix-stdio leg
 * remains tracked under #201/#53.
 */
class NativeStdioSmokeTest {

    @Test
    fun `native transport carries a real LSP round-trip`() = runTest {
        withInMemoryConnection { remote ->
            withTimeout(TIMEOUT_MS) {
                val init = remote.initialize(
                    InitializeParams(
                        capabilities = ClientCapabilities(),
                        processId = null,
                        rootUri = "file:///workspace"
                    )
                )
                assertNotNull(init.capabilities.hoverProvider, "server should advertise hover")

                remote.textDocumentDidOpen(
                    DidOpenTextDocumentParams(
                        textDocument = TextDocumentItem(
                            uri = ConformanceLanguageServer.Uri.MAIN,
                            languageId = "kotlin",
                            version = 1,
                            text = "fun main() {}"
                        )
                    )
                )

                val hover = remote.textDocumentHover(
                    HoverParams(
                        textDocument = TextDocumentIdentifier(ConformanceLanguageServer.Uri.MAIN),
                        position = Position(line = 0u, character = 0u)
                    )
                )
                assertTrue(
                    hover.contents is HoverContents.MarkupContentValue,
                    "line 0 hover should round-trip as MarkupContentValue, was ${hover.contents}"
                )

                // Clean shutdown/exit over the (cancellable, in-memory) transport.
                assertEquals(null, remote.shutdown())
                remote.exit()
            }
        }
    }

    /**
     * Wire a [ConformanceLanguageServer] to a [ConformanceLanguageClient] over a
     * pair of in-memory byte channels and run [block] against the resulting
     * remote-server stub. Teardown closes both channels and cancels the server's
     * receive loop — no blocking syscall, so this always terminates.
     */
    private suspend fun withInMemoryConnection(block: suspend (KsrpcLanguageServer) -> Unit) =
        withContext(Dispatchers.Default) {
            val clientToServer = ByteChannel(autoFlush = true)
            val serverToClient = ByteChannel(autoFlush = true)

            val serverJob = launch {
                val conn = (clientToServer to serverToClient).asLspConnection()
                conn.connectAsLspServer(ConformanceLanguageServer())
            }
            try {
                val conn = (serverToClient to clientToServer).asLspConnection()
                val remote = conn.connectAsLspClient(ConformanceLanguageClient())
                block(remote)
            } finally {
                clientToServer.close()
                serverToClient.close()
                serverJob.cancel()
            }
        }

    private companion object {
        const val TIMEOUT_MS = 10_000L
    }
}
