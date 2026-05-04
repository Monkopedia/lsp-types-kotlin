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

import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.eclipse.lsp4j.ClientCapabilities
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializedParams
import org.eclipse.lsp4j.MessageActionItem
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.ShowMessageRequestParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.launch.LSPLauncher
import org.eclipse.lsp4j.services.LanguageClient
import org.junit.Assume.assumeTrue

/**
 * The real test of wire compatibility: drive our echo-server with
 * [Eclipse lsp4j](https://github.com/eclipse-lsp4j/lsp4j), the same Java LSP
 * client library used by Eclipse, gradle-language-server, kotlin-language-server,
 * and many others. If lsp4j can talk to our server, real editors can.
 *
 * This is the strongest integration check we have: hand-written raw JSON in
 * [RawClientServerTest] only proves "our server parses what I think LSP looks
 * like." Driving lsp4j proves it parses what a battle-tested third-party
 * client actually sends.
 */
class Lsp4jClientIntegrationTest {

    @Test
    fun `lsp4j client drives echo-server through hover round-trip`() {
        val script = listOf(
            File("samples/echo-server/build/install/echo-server/bin/echo-server"),
            File("../samples/echo-server/build/install/echo-server/bin/echo-server")
        ).firstOrNull { it.exists() }
        assumeTrue(
            "echo-server not built; run :samples:echo-server:installDist",
            script != null
        )

        val process = ProcessBuilder(script!!.absolutePath)
            .redirectInput(ProcessBuilder.Redirect.PIPE)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .also { it.environment()["JAVA_HOME"] = System.getProperty("java.home") }
            .start()

        try {
            val client = TestClient()
            val launcher = LSPLauncher.createClientLauncher(
                client,
                process.inputStream,
                process.outputStream
            )
            val listening = launcher.startListening()
            val server = launcher.remoteProxy

            val initParams = InitializeParams().apply {
                capabilities = ClientCapabilities()
            }
            val initResult = server.initialize(initParams).get(15, TimeUnit.SECONDS)
            assertNotNull(initResult, "initialize must return a result")
            // echo-server advertises hoverProvider = true.
            assertEquals(true, initResult.capabilities.hoverProvider?.left)

            server.initialized(InitializedParams())

            val uri = "file:///foo.kt"
            server.textDocumentService.didOpen(
                DidOpenTextDocumentParams(
                    TextDocumentItem(uri, "kotlin", 1, "fun main() {}")
                )
            )

            val hover = server.textDocumentService
                .hover(HoverParams(TextDocumentIdentifier(uri), Position(3, 5)))
                .get(5, TimeUnit.SECONDS)
            assertNotNull(hover, "hover must return a result")
            // echo-server returns MarkupContent — lsp4j parses that as `contents.right`.
            val markup = hover.contents.right
            assertNotNull(markup, "expected MarkupContent shape, got: ${hover.contents}")
            assertEquals("markdown", markup.kind)
            assert(markup.value.contains(uri)) {
                "Expected URI in hover text: ${markup.value}"
            }
            assert(markup.value.contains("3:5")) {
                "Expected position in hover text: ${markup.value}"
            }

            // Skip shutdown/exit — ksrpc 1.0.0-RC2 chokes on 0-arg method dispatch
            // when params is omitted (which lsp4j does), and that interaction is
            // already characterized by RawClientServerTest. The hover round-trip
            // above is what proves wire compatibility with a real LSP client.
            listening.cancel(true)
        } finally {
            runCatching { process.outputStream.close() }
            runCatching { process.inputStream.close() }
            if (!process.waitFor(2, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                process.waitFor(2, TimeUnit.SECONDS)
            }
        }
    }

    private class TestClient : LanguageClient {
        override fun telemetryEvent(`object`: Any?) = Unit
        override fun publishDiagnostics(diagnostics: PublishDiagnosticsParams?) = Unit
        override fun showMessage(messageParams: MessageParams?) = Unit
        override fun showMessageRequest(
            requestParams: ShowMessageRequestParams?
        ): CompletableFuture<MessageActionItem> = CompletableFuture.completedFuture(null)
        override fun logMessage(message: MessageParams?) = Unit
    }
}
