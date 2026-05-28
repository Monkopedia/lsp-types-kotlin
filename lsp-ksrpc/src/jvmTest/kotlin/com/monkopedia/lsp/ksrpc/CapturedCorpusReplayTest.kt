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

import com.monkopedia.lsp.CompletionItem
import com.monkopedia.lsp.ConfigurationParams
import com.monkopedia.lsp.DefinitionParams
import com.monkopedia.lsp.DidChangeConfigurationParams
import com.monkopedia.lsp.DidChangeTextDocumentParams
import com.monkopedia.lsp.DidChangeWatchedFilesParams
import com.monkopedia.lsp.DidCloseTextDocumentParams
import com.monkopedia.lsp.DidOpenTextDocumentParams
import com.monkopedia.lsp.DidSaveTextDocumentParams
import com.monkopedia.lsp.DocumentDiagnosticParams
import com.monkopedia.lsp.DocumentDiagnosticReport
import com.monkopedia.lsp.DocumentLinkParams
import com.monkopedia.lsp.DocumentSymbolParams
import com.monkopedia.lsp.FoldingRangeParams
import com.monkopedia.lsp.Hover
import com.monkopedia.lsp.HoverParams
import com.monkopedia.lsp.InitializeParams
import com.monkopedia.lsp.InitializeResult
import com.monkopedia.lsp.InitializedParams
import com.monkopedia.lsp.InlayHintParams
import com.monkopedia.lsp.Location
import com.monkopedia.lsp.LogMessageParams
import com.monkopedia.lsp.LogTraceParams
import com.monkopedia.lsp.ProgressParams
import com.monkopedia.lsp.PublishDiagnosticsParams
import com.monkopedia.lsp.ReferenceParams
import com.monkopedia.lsp.RegistrationParams
import com.monkopedia.lsp.SelectionRangeParams
import com.monkopedia.lsp.SemanticTokens
import com.monkopedia.lsp.SemanticTokensParams
import com.monkopedia.lsp.SemanticTokensRangeParams
import com.monkopedia.lsp.ShowMessageParams
import com.monkopedia.lsp.ShowMessageRequestParams
import com.monkopedia.lsp.SignatureHelp
import com.monkopedia.lsp.SignatureHelpParams
import com.monkopedia.lsp.TextDocumentCompletionResult
import com.monkopedia.lsp.TextDocumentDefinitionResult
import com.monkopedia.lsp.TextDocumentDocumentSymbolResult
import com.monkopedia.lsp.UnregistrationParams
import com.monkopedia.lsp.WorkDoneProgressCancelParams
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Replay-side of the record-and-replay real-payload corpus (#62).
 *
 * Auto-discovers every JSON file committed under
 * `src/jvmTest/resources/captured/<server>/<direction>/<method>__<seq>.json` —
 * payloads recorded from REAL external LSP servers (clangd, pyright, gopls,
 * typescript-language-server, rust-analyzer) by [RealServerClientRoleTest]
 * when run with `-Plsp.captureCorpus=true` — and validates round-trip stability:
 *
 *   1. Decode the file with the serializer registered for `(direction, method)`.
 *   2. Assert the decoded value is a non-null instance of the expected type.
 *   3. Re-encode and re-decode; assert the second decode equals the first.
 *
 * Methods/directions that aren't in [SERIALIZER_TABLE] yet are SKIPPED with a
 * warning rather than failed — the test is purposely additive so a freshly
 * captured method that nobody's wired into the table doesn't gate the per-PR
 * pipeline. Add an entry to [SERIALIZER_TABLE] to start asserting on a new
 * payload type.
 *
 * Hermetic: this test reads only the committed fixtures and runs WITHOUT any
 * external server / `-Plsp.requireRealServers`, so the per-PR path stays fast.
 */
class CapturedCorpusReplayTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    @Test
    fun `every captured fixture round-trips through its declared serializer`() {
        val root = locateCorpusRoot()
            ?: run {
                println("[corpus] no captured/ resource directory found; skipping replay")
                return
            }
        val files = root.walkTopDown()
            .filter { it.isFile && it.extension == "json" }
            .toList()
        if (files.isEmpty()) {
            println("[corpus] captured/ contains no fixtures yet; skipping replay")
            return
        }

        var roundTripped = 0
        var skippedUnknown = 0
        val unknown = mutableSetOf<Pair<String, String>>()
        val failures = mutableListOf<String>()

        for (file in files) {
            val descriptor = describe(root, file) ?: continue
            val entry = SERIALIZER_TABLE[descriptor.direction to descriptor.method]
            if (entry == null) {
                skippedUnknown += 1
                unknown += descriptor.direction to descriptor.method
                continue
            }
            val text = file.readText()
            val outcome = runCatching {
                entry.roundTrip(json, text)
            }
            if (outcome.isFailure) {
                failures += buildString {
                    append(file.relativeTo(root).path)
                    append(": ")
                    append(outcome.exceptionOrNull()?.message ?: "unknown failure")
                }
            } else {
                roundTripped += 1
            }
        }

        if (unknown.isNotEmpty()) {
            println(
                "[corpus] skipped ${unknown.size} (direction,method) combinations " +
                    "without a serializer table entry: ${unknown.sortedBy {
                        "${it.first}|${it.second}"
                    }}"
            )
        }
        println("[corpus] round-tripped $roundTripped, skipped $skippedUnknown unknown")

        assertTrue(
            failures.isEmpty(),
            "captured corpus round-trip failures:\n${failures.joinToString("\n")}"
        )
    }

    private data class Descriptor(val server: String, val direction: String, val method: String)

    private fun describe(root: File, file: File): Descriptor? {
        val rel = file.relativeTo(root).path.replace(File.separatorChar, '/')
        // <server>/<direction>/<method>__<seq>.json
        val parts = rel.split('/')
        if (parts.size != 3) return null
        val (server, direction, leaf) = parts
        val stem = leaf.removeSuffix(".json")
        val sepIdx = stem.lastIndexOf("__")
        if (sepIdx <= 0) return null
        val methodSanitized = stem.substring(0, sepIdx)
        val method = methodSanitized
            .replace("dollar_", "$/")
            .replace('_', '/')
        return Descriptor(server, direction, method)
    }

    private fun locateCorpusRoot(): File? {
        // Resource resolution from the test classpath; falls back to source dir
        // when run from an IDE that hasn't copied resources yet.
        val resourceUrl = javaClass.classLoader.getResource("captured")
        if (resourceUrl != null) {
            val asFile = runCatching { File(resourceUrl.toURI()) }.getOrNull()
            if (asFile?.isDirectory == true) return asFile
        }
        val candidates = listOf(
            "lsp-ksrpc/src/jvmTest/resources/captured",
            "src/jvmTest/resources/captured"
        )
        for (path in candidates) {
            val f = File(path)
            if (f.isDirectory) return f
        }
        return null
    }

    /**
     * One round-trip strategy per `(direction, method)` row. Wraps a
     * `KSerializer<T>` and asserts the decoded result is non-null AND that a
     * second decode of the re-encoded JSON equals the first.
     */
    private interface RoundTrip {
        fun roundTrip(json: Json, payload: String)
    }

    private class TypedRoundTrip<T : Any>(val typeName: String, val serializer: KSerializer<T>) :
        RoundTrip {
        override fun roundTrip(json: Json, payload: String) {
            val first = json.decodeFromString(serializer, payload)
            assertNotNull(first, "decoded null from non-null payload ($typeName)")
            val reEncoded = json.encodeToString(serializer, first)
            val second = json.decodeFromString(serializer, reEncoded)
            assertNotNull(second, "second decode null from re-encoded payload ($typeName)")
            // Compare via the JsonElement projection rather than object equality
            // — some LSP types (e.g. `InitializedParams`) are intentionally empty
            // and don't override `equals`, so we'd get reference-equality false
            // negatives otherwise. Encoded-form equality is the stronger and more
            // relevant invariant for wire stability anyway.
            val encodedTwice = json.encodeToString(serializer, second)
            assertEquals(
                json.parseToJsonElement(reEncoded),
                json.parseToJsonElement(encodedTwice),
                "round-trip not stable for $typeName"
            )
        }
    }

    private companion object {
        // (direction, wireMethod) -> round-trip strategy. Methods with no
        // structured payload (e.g. `shutdown`, `exit`) are deliberately absent;
        // their fixtures fall through to the skip-and-warn branch.
        @Suppress("UNCHECKED_CAST")
        val SERIALIZER_TABLE: Map<Pair<String, String>, RoundTrip> = buildMap {
            // ---- client → server REQUESTS (params) ----
            put(
                "request" to "initialize",
                TypedRoundTrip("InitializeParams", InitializeParams.serializer())
            )
            put(
                "request" to "textDocument/hover",
                TypedRoundTrip("HoverParams", HoverParams.serializer())
            )
            put(
                "request" to "textDocument/definition",
                TypedRoundTrip("DefinitionParams", DefinitionParams.serializer())
            )
            put(
                "request" to "textDocument/references",
                TypedRoundTrip("ReferenceParams", ReferenceParams.serializer())
            )
            put(
                "request" to "textDocument/documentSymbol",
                TypedRoundTrip("DocumentSymbolParams", DocumentSymbolParams.serializer())
            )
            put(
                "request" to "textDocument/signatureHelp",
                TypedRoundTrip("SignatureHelpParams", SignatureHelpParams.serializer())
            )
            put(
                "request" to "textDocument/foldingRange",
                TypedRoundTrip("FoldingRangeParams", FoldingRangeParams.serializer())
            )
            put(
                "request" to "textDocument/selectionRange",
                TypedRoundTrip("SelectionRangeParams", SelectionRangeParams.serializer())
            )
            put(
                "request" to "textDocument/documentLink",
                TypedRoundTrip("DocumentLinkParams", DocumentLinkParams.serializer())
            )
            put(
                "request" to "textDocument/semanticTokens/full",
                TypedRoundTrip("SemanticTokensParams", SemanticTokensParams.serializer())
            )
            put(
                "request" to "textDocument/semanticTokens/range",
                TypedRoundTrip("SemanticTokensRangeParams", SemanticTokensRangeParams.serializer())
            )
            put(
                "request" to "textDocument/inlayHint",
                TypedRoundTrip("InlayHintParams", InlayHintParams.serializer())
            )
            put(
                "request" to "textDocument/diagnostic",
                TypedRoundTrip("DocumentDiagnosticParams", DocumentDiagnosticParams.serializer())
            )
            put(
                "request" to "completionItem/resolve",
                TypedRoundTrip("CompletionItem", CompletionItem.serializer())
            )

            // ---- client → server NOTIFICATIONS (params) ----
            put(
                "notification" to "initialized",
                TypedRoundTrip("InitializedParams", InitializedParams.serializer())
            )
            put(
                "notification" to "textDocument/didOpen",
                TypedRoundTrip("DidOpenTextDocumentParams", DidOpenTextDocumentParams.serializer())
            )
            put(
                "notification" to "textDocument/didChange",
                TypedRoundTrip(
                    "DidChangeTextDocumentParams",
                    DidChangeTextDocumentParams.serializer()
                )
            )
            put(
                "notification" to "textDocument/didClose",
                TypedRoundTrip(
                    "DidCloseTextDocumentParams",
                    DidCloseTextDocumentParams.serializer()
                )
            )
            put(
                "notification" to "textDocument/didSave",
                TypedRoundTrip("DidSaveTextDocumentParams", DidSaveTextDocumentParams.serializer())
            )
            put(
                "notification" to "workspace/didChangeConfiguration",
                TypedRoundTrip(
                    "DidChangeConfigurationParams",
                    DidChangeConfigurationParams.serializer()
                )
            )
            put(
                "notification" to "workspace/didChangeWatchedFiles",
                TypedRoundTrip(
                    "DidChangeWatchedFilesParams",
                    DidChangeWatchedFilesParams.serializer()
                )
            )
            put(
                "notification" to "window/workDoneProgress/cancel",
                TypedRoundTrip(
                    "WorkDoneProgressCancelParams",
                    WorkDoneProgressCancelParams.serializer()
                )
            )
            put(
                "notification" to "\$/progress",
                TypedRoundTrip("ProgressParams", ProgressParams.serializer())
            )

            // ---- server → client RESPONSES (result) ----
            put(
                "response" to "initialize",
                TypedRoundTrip("InitializeResult", InitializeResult.serializer())
            )
            put("response" to "textDocument/hover", TypedRoundTrip("Hover", Hover.serializer()))
            put(
                "response" to "textDocument/definition",
                TypedRoundTrip(
                    "TextDocumentDefinitionResult",
                    TextDocumentDefinitionResult.serializer()
                )
            )
            put(
                "response" to "textDocument/references",
                TypedRoundTrip("List<Location>", ListSerializer(Location.serializer()))
            )
            put(
                "response" to "textDocument/documentSymbol",
                TypedRoundTrip(
                    "TextDocumentDocumentSymbolResult",
                    TextDocumentDocumentSymbolResult.serializer()
                )
            )
            put(
                "response" to "textDocument/completion",
                TypedRoundTrip(
                    "TextDocumentCompletionResult",
                    TextDocumentCompletionResult.serializer()
                )
            )
            put(
                "response" to "completionItem/resolve",
                TypedRoundTrip("CompletionItem", CompletionItem.serializer())
            )
            put(
                "response" to "textDocument/signatureHelp",
                TypedRoundTrip("SignatureHelp", SignatureHelp.serializer())
            )
            put(
                "response" to "textDocument/semanticTokens/full",
                TypedRoundTrip("SemanticTokens", SemanticTokens.serializer())
            )
            put(
                "response" to "textDocument/semanticTokens/range",
                TypedRoundTrip("SemanticTokens", SemanticTokens.serializer())
            )
            put(
                "response" to "textDocument/diagnostic",
                TypedRoundTrip("DocumentDiagnosticReport", DocumentDiagnosticReport.serializer())
            )

            // ---- server → client REQUESTS (params; server-initiated) ----
            put(
                "clientRequest" to "workspace/configuration",
                TypedRoundTrip("ConfigurationParams", ConfigurationParams.serializer())
            )
            put(
                "clientRequest" to "client/registerCapability",
                TypedRoundTrip("RegistrationParams", RegistrationParams.serializer())
            )
            put(
                "clientRequest" to "client/unregisterCapability",
                TypedRoundTrip("UnregistrationParams", UnregistrationParams.serializer())
            )
            put(
                "clientRequest" to "window/showMessageRequest",
                TypedRoundTrip("ShowMessageRequestParams", ShowMessageRequestParams.serializer())
            )

            // ---- server → client NOTIFICATIONS (params) ----
            put(
                "clientNotification" to "window/logMessage",
                TypedRoundTrip("LogMessageParams", LogMessageParams.serializer())
            )
            put(
                "clientNotification" to "window/showMessage",
                TypedRoundTrip("ShowMessageParams", ShowMessageParams.serializer())
            )
            put(
                "clientNotification" to "textDocument/publishDiagnostics",
                TypedRoundTrip("PublishDiagnosticsParams", PublishDiagnosticsParams.serializer())
            )
            put(
                "clientNotification" to "\$/progress",
                TypedRoundTrip("ProgressParams", ProgressParams.serializer())
            )
            put(
                "clientNotification" to "\$/logTrace",
                TypedRoundTrip("LogTraceParams", LogTraceParams.serializer())
            )
        }
    }
}
