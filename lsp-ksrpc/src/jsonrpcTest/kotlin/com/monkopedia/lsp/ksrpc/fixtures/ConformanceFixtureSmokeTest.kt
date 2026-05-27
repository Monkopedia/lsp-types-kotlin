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

import com.monkopedia.lsp.ClientCapabilities
import com.monkopedia.lsp.CompletionParams
import com.monkopedia.lsp.DeclarationParams
import com.monkopedia.lsp.DefinitionParams
import com.monkopedia.lsp.DocumentSymbolParams
import com.monkopedia.lsp.HoverContents
import com.monkopedia.lsp.HoverParams
import com.monkopedia.lsp.ImplementationParams
import com.monkopedia.lsp.InitializeParams
import com.monkopedia.lsp.KsrpcLanguageServer
import com.monkopedia.lsp.Position
import com.monkopedia.lsp.ReferenceContext
import com.monkopedia.lsp.ReferenceParams
import com.monkopedia.lsp.SingleOrArray
import com.monkopedia.lsp.TextDocumentCompletionResult
import com.monkopedia.lsp.TextDocumentDeclarationResult
import com.monkopedia.lsp.TextDocumentDefinitionResult
import com.monkopedia.lsp.TextDocumentDocumentSymbolResult
import com.monkopedia.lsp.TextDocumentIdentifier
import com.monkopedia.lsp.TextDocumentImplementationResult
import com.monkopedia.lsp.TextDocumentTypeDefinitionResult
import com.monkopedia.lsp.TypeDefinitionParams
import com.monkopedia.lsp.ksrpc.asLspConnection
import com.monkopedia.lsp.ksrpc.connectAsLspClient
import com.monkopedia.lsp.ksrpc.connectAsLspServer
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
 * Smoke test for the conformance fixtures: wires a [ConformanceLanguageServer] to
 * a [ConformanceLanguageClient] over the in-memory jsonrpc ksrpc transport and
 * asserts that each union branch documented in [ConformanceLanguageServer]'s KDoc
 * round-trips back to the expected concrete type. This is the executable contract
 * downstream tests (#45/#46/#47) build on.
 */
class ConformanceFixtureSmokeTest {

    private fun hoverAt(line: Int) = HoverParams(
        textDocument = TextDocumentIdentifier(ConformanceLanguageServer.Uri.MAIN),
        position = pos(line)
    )

    private fun pos(line: Int) = Position(line = line.toUInt(), character = 0u)

    private fun definitionAt(line: Int) = DefinitionParams(
        textDocument = TextDocumentIdentifier(ConformanceLanguageServer.Uri.MAIN),
        position = pos(line)
    )

    @Test
    fun `every documented union branch round-trips over the in-memory transport`() = runTest {
        withInMemoryConnection { remote ->
            withTimeout(TIMEOUT_MS) {
                remote.initialize(
                    InitializeParams(
                        capabilities = ClientCapabilities(),
                        processId = null,
                        rootUri = null
                    )
                ).let { init ->
                    assertNotNull(init.capabilities.hoverProvider)
                    assertNotNull(init.capabilities.definitionProvider)
                }

                assertHoverBranches(remote)
                assertDefinitionBranches(remote)
                assertDeclarationBranches(remote)
                assertTypeDefinitionBranches(remote)
                assertImplementationBranches(remote)
                assertCompletionBranches(remote)
                assertDocumentSymbolBranches(remote)
                assertReferences(remote)

                // shutdown / exit must complete cleanly.
                assertEquals(null, remote.shutdown())
                remote.exit()
            }
        }
    }

    private suspend fun assertHoverBranches(remote: KsrpcLanguageServer) {
        val markup = remote.textDocumentHover(hoverAt(ConformanceLanguageServer.Lines.SINGLE))
        assertTrue(
            markup.contents is HoverContents.MarkupContentValue,
            "line 0 hover should be MarkupContentValue, was ${markup.contents}"
        )
        val marked = remote.textDocumentHover(hoverAt(ConformanceLanguageServer.Lines.ARRAY))
        assertTrue(
            marked.contents is HoverContents.MarkedStringValue,
            "line 1 hover should be MarkedStringValue, was ${marked.contents}"
        )
        val markedArray = remote.textDocumentHover(hoverAt(ConformanceLanguageServer.Lines.LINK))
        assertTrue(
            markedArray.contents is HoverContents.MarkedStringArray,
            "line 2 hover should be MarkedStringArray, was ${markedArray.contents}"
        )
    }

    private suspend fun assertDefinitionBranches(remote: KsrpcLanguageServer) {
        val single = remote.textDocumentDefinition(
            definitionAt(ConformanceLanguageServer.Lines.SINGLE)
        )
        val singleValue = (single as TextDocumentDefinitionResult.DefinitionValue).value
        assertTrue(
            singleValue is SingleOrArray.Single,
            "line 0 definition should be a single Location"
        )

        val array = remote.textDocumentDefinition(
            definitionAt(ConformanceLanguageServer.Lines.ARRAY)
        )
        val arrayValue = (array as TextDocumentDefinitionResult.DefinitionValue).value
        assertTrue(arrayValue is SingleOrArray.Multiple, "line 1 definition should be a Location[]")

        val link = remote.textDocumentDefinition(definitionAt(ConformanceLanguageServer.Lines.LINK))
        assertTrue(
            link is TextDocumentDefinitionResult.DefinitionLinkArray,
            "line 2 definition should be a LocationLink[]"
        )
        assertEquals(2, link.value.size)
    }

    private suspend fun assertDeclarationBranches(remote: KsrpcLanguageServer) {
        fun params(line: Int) = DeclarationParams(
            textDocument = TextDocumentIdentifier(ConformanceLanguageServer.Uri.MAIN),
            position = pos(line)
        )
        val single = remote.textDocumentDeclaration(params(ConformanceLanguageServer.Lines.SINGLE))
        val singleValue = (single as TextDocumentDeclarationResult.DeclarationValue).value
        assertTrue(singleValue is SingleOrArray.Single, "line 0 declaration should be single")

        val array = remote.textDocumentDeclaration(params(ConformanceLanguageServer.Lines.ARRAY))
        val arrayValue = (array as TextDocumentDeclarationResult.DeclarationValue).value
        assertTrue(arrayValue is SingleOrArray.Multiple, "line 1 declaration should be an array")

        val link = remote.textDocumentDeclaration(params(ConformanceLanguageServer.Lines.LINK))
        assertTrue(
            link is TextDocumentDeclarationResult.DeclarationLinkArray,
            "line 2 declaration should be a link array"
        )
    }

    private suspend fun assertTypeDefinitionBranches(remote: KsrpcLanguageServer) {
        fun params(line: Int) = TypeDefinitionParams(
            textDocument = TextDocumentIdentifier(ConformanceLanguageServer.Uri.MAIN),
            position = pos(line)
        )
        val single = remote.textDocumentTypeDefinition(
            params(ConformanceLanguageServer.Lines.SINGLE)
        )
        val singleValue = (single as TextDocumentTypeDefinitionResult.DefinitionValue).value
        assertTrue(singleValue is SingleOrArray.Single, "line 0 typeDefinition should be single")

        val array = remote.textDocumentTypeDefinition(
            params(ConformanceLanguageServer.Lines.ARRAY)
        )
        val arrayValue = (array as TextDocumentTypeDefinitionResult.DefinitionValue).value
        assertTrue(arrayValue is SingleOrArray.Multiple, "line 1 typeDefinition should be an array")

        val link = remote.textDocumentTypeDefinition(params(ConformanceLanguageServer.Lines.LINK))
        assertTrue(
            link is TextDocumentTypeDefinitionResult.DefinitionLinkArray,
            "line 2 typeDefinition should be a link array"
        )
    }

    private suspend fun assertImplementationBranches(remote: KsrpcLanguageServer) {
        fun params(line: Int) = ImplementationParams(
            textDocument = TextDocumentIdentifier(ConformanceLanguageServer.Uri.MAIN),
            position = pos(line)
        )
        val single = remote.textDocumentImplementation(
            params(ConformanceLanguageServer.Lines.SINGLE)
        )
        val singleValue = (single as TextDocumentImplementationResult.DefinitionValue).value
        assertTrue(singleValue is SingleOrArray.Single, "line 0 implementation should be single")

        val array = remote.textDocumentImplementation(
            params(ConformanceLanguageServer.Lines.ARRAY)
        )
        val arrayValue = (array as TextDocumentImplementationResult.DefinitionValue).value
        assertTrue(arrayValue is SingleOrArray.Multiple, "line 1 implementation should be an array")

        val link = remote.textDocumentImplementation(params(ConformanceLanguageServer.Lines.LINK))
        assertTrue(
            link is TextDocumentImplementationResult.DefinitionLinkArray,
            "line 2 implementation should be a link array"
        )
    }

    private suspend fun assertCompletionBranches(remote: KsrpcLanguageServer) {
        fun params(line: Int) = CompletionParams(
            textDocument = TextDocumentIdentifier(ConformanceLanguageServer.Uri.MAIN),
            position = pos(line)
        )
        val list = remote.textDocumentCompletion(params(ConformanceLanguageServer.Lines.SINGLE))
        assertTrue(
            list is TextDocumentCompletionResult.CompletionListValue,
            "line 0 completion should be a CompletionList"
        )
        val array = remote.textDocumentCompletion(params(ConformanceLanguageServer.Lines.ARRAY))
        assertTrue(
            array is TextDocumentCompletionResult.CompletionItemArray,
            "line 1 completion should be a CompletionItem[]"
        )
        assertEquals(2, array.value.size)
    }

    private suspend fun assertDocumentSymbolBranches(remote: KsrpcLanguageServer) {
        val hierarchical = remote.textDocumentDocumentSymbol(
            DocumentSymbolParams(
                textDocument = TextDocumentIdentifier(
                    ConformanceLanguageServer.Uri.HIERARCHICAL_SYMBOLS
                )
            )
        )
        assertTrue(
            hierarchical is TextDocumentDocumentSymbolResult.DocumentSymbolArray,
            "#hierarchical should yield DocumentSymbol[]"
        )
        val flat = remote.textDocumentDocumentSymbol(
            DocumentSymbolParams(
                textDocument = TextDocumentIdentifier(ConformanceLanguageServer.Uri.FLAT_SYMBOLS)
            )
        )
        assertTrue(
            flat is TextDocumentDocumentSymbolResult.SymbolInformationArray,
            "#flat should yield SymbolInformation[]"
        )
    }

    private suspend fun assertReferences(remote: KsrpcLanguageServer) {
        val refs = remote.textDocumentReferences(
            ReferenceParams(
                textDocument = TextDocumentIdentifier(ConformanceLanguageServer.Uri.MAIN),
                position = pos(0),
                context = ReferenceContext(includeDeclaration = true)
            )
        )
        assertEquals(3, refs.size)
    }

    /**
     * Run [block] against a remote [KsrpcLanguageServer] stub backed by a fresh
     * [ConformanceLanguageServer], with a [ConformanceLanguageClient] on the
     * client side. The server's receive loop runs on a real dispatcher launched
     * in the test scope; channels are closed when [block] returns.
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
