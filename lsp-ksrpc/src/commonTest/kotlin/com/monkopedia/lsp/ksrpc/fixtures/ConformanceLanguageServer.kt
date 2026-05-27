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

import com.monkopedia.lsp.BooleanOr
import com.monkopedia.lsp.CodeAction
import com.monkopedia.lsp.CodeActionParams
import com.monkopedia.lsp.CodeLens
import com.monkopedia.lsp.CodeLensParams
import com.monkopedia.lsp.Command
import com.monkopedia.lsp.CompletionItem
import com.monkopedia.lsp.CompletionItemKind
import com.monkopedia.lsp.CompletionList
import com.monkopedia.lsp.CompletionOptions
import com.monkopedia.lsp.CompletionParams
import com.monkopedia.lsp.Declaration
import com.monkopedia.lsp.DeclarationLink
import com.monkopedia.lsp.DeclarationParams
import com.monkopedia.lsp.DefaultLanguageServer
import com.monkopedia.lsp.Definition
import com.monkopedia.lsp.DefinitionLink
import com.monkopedia.lsp.DefinitionParams
import com.monkopedia.lsp.DocumentFormattingParams
import com.monkopedia.lsp.DocumentHighlight
import com.monkopedia.lsp.DocumentHighlightKind
import com.monkopedia.lsp.DocumentHighlightParams
import com.monkopedia.lsp.DocumentSymbol
import com.monkopedia.lsp.DocumentSymbolParams
import com.monkopedia.lsp.FoldingRange
import com.monkopedia.lsp.FoldingRangeKind
import com.monkopedia.lsp.FoldingRangeParams
import com.monkopedia.lsp.Hover
import com.monkopedia.lsp.HoverContents
import com.monkopedia.lsp.HoverParams
import com.monkopedia.lsp.ImplementationParams
import com.monkopedia.lsp.InitializeParams
import com.monkopedia.lsp.InitializeResult
import com.monkopedia.lsp.InitializeResultServerInfo
import com.monkopedia.lsp.InlayHint
import com.monkopedia.lsp.InlayHintParams
import com.monkopedia.lsp.Location
import com.monkopedia.lsp.LocationLink
import com.monkopedia.lsp.Position
import com.monkopedia.lsp.Range
import com.monkopedia.lsp.ReferenceParams
import com.monkopedia.lsp.RenameParams
import com.monkopedia.lsp.SemanticTokens
import com.monkopedia.lsp.SemanticTokensParams
import com.monkopedia.lsp.ServerCapabilities
import com.monkopedia.lsp.SignatureHelp
import com.monkopedia.lsp.SignatureHelpParams
import com.monkopedia.lsp.SignatureInformation
import com.monkopedia.lsp.SingleOrArray
import com.monkopedia.lsp.StringOr
import com.monkopedia.lsp.SymbolInformation
import com.monkopedia.lsp.SymbolKind
import com.monkopedia.lsp.TextDocumentCompletionResult
import com.monkopedia.lsp.TextDocumentDeclarationResult
import com.monkopedia.lsp.TextDocumentDefinitionResult
import com.monkopedia.lsp.TextDocumentDocumentSymbolResult
import com.monkopedia.lsp.TextDocumentImplementationResult
import com.monkopedia.lsp.TextDocumentTypeDefinitionResult
import com.monkopedia.lsp.TextEdit
import com.monkopedia.lsp.TypeDefinitionParams
import com.monkopedia.lsp.WorkspaceEdit
import com.monkopedia.lsp.markdown
import com.monkopedia.lsp.string

/**
 * Deterministic conformance fixture server: every typed-union result method
 * returns a canned value chosen by the request's *position line* (or, for hover,
 * the line). Downstream integration tests (#45/#46/#47) drive this server with
 * the well-known inputs below and assert the concrete branch that round-trips
 * back over the wire.
 *
 * The fixture keys branch selection off [HoverParams.position]`.line` (and the
 * analogous `position.line` of the other text-document requests). Line numbers
 * are stable contract; do not renumber them without updating the dependent
 * tests.
 *
 * ## Union-branch selection table
 *
 * `textDocument/hover` — result [Hover], `contents: HoverContents`:
 *
 * | position.line | branch                               |
 * |---------------|--------------------------------------|
 * | 0             | [HoverContents.MarkupContentValue]   |
 * | 1             | [HoverContents.MarkedStringValue]    |
 * | 2             | [HoverContents.MarkedStringArray]    |
 *
 * `textDocument/definition` — [TextDocumentDefinitionResult]:
 *
 * | position.line | branch                                                      |
 * |---------------|-------------------------------------------------------------|
 * | 0             | [TextDocumentDefinitionResult.DefinitionValue] (single [Location], i.e. [SingleOrArray.Single]) |
 * | 1             | [TextDocumentDefinitionResult.DefinitionValue] ([Location]`[]`, i.e. [SingleOrArray.Multiple])  |
 * | 2             | [TextDocumentDefinitionResult.DefinitionLinkArray] ([LocationLink]`[]`) |
 *
 * `textDocument/declaration` — [TextDocumentDeclarationResult]: same line → branch
 * mapping as definition (single / array / link-array).
 *
 * `textDocument/typeDefinition` — [TextDocumentTypeDefinitionResult]: same mapping.
 *
 * `textDocument/implementation` — [TextDocumentImplementationResult]: same mapping.
 *
 * `textDocument/completion` — [TextDocumentCompletionResult]:
 *
 * | position.line | branch                                            |
 * |---------------|---------------------------------------------------|
 * | 0             | [TextDocumentCompletionResult.CompletionListValue] |
 * | 1             | [TextDocumentCompletionResult.CompletionItemArray] |
 *
 * `textDocument/documentSymbol` — [TextDocumentDocumentSymbolResult]: keyed off
 * the document URI rather than a position (the request has no position):
 *
 * | textDocument.uri ends with | branch                                              |
 * |----------------------------|-----------------------------------------------------|
 * | `#hierarchical` (default)  | [TextDocumentDocumentSymbolResult.DocumentSymbolArray] |
 * | `#flat`                    | [TextDocumentDocumentSymbolResult.SymbolInformationArray] |
 *
 * `textDocument/references` — always a [Location]`[]`.
 *
 * The remaining server methods (signatureHelp, formatting, rename, codeAction,
 * codeLens, foldingRange, semanticTokens, inlayHint, documentHighlight) return
 * simple well-formed canned values; they are not branch-exhaustive but never
 * throw.
 *
 * Use the [Uri] / [Lines] constants below from tests so the contract stays in
 * one place.
 */
open class ConformanceLanguageServer : DefaultLanguageServer() {

    /** Well-known document URIs the fixture recognises. */
    object Uri {
        const val MAIN = "file:///conformance/main.kt"
        const val HIERARCHICAL_SYMBOLS = "file:///conformance/symbols.kt#hierarchical"
        const val FLAT_SYMBOLS = "file:///conformance/symbols.kt#flat"
    }

    /** Well-known request lines that select union branches (see class KDoc). */
    object Lines {
        const val SINGLE = 0
        const val ARRAY = 1
        const val LINK = 2
    }

    private fun pos(line: Int, character: Int = 0): Position =
        Position(line = line.toUInt(), character = character.toUInt())

    private fun range(line: Int): Range = Range(start = pos(line, 0), end = pos(line, 4))

    private fun location(uri: String, line: Int): Location =
        Location(uri = uri, range = range(line))

    private fun locationLink(uri: String, line: Int): LocationLink = LocationLink(
        originSelectionRange = range(line),
        targetUri = uri,
        targetRange = range(line + 10),
        targetSelectionRange = range(line + 10)
    )

    override suspend fun initialize(params: InitializeParams): InitializeResult = InitializeResult(
        capabilities = ServerCapabilities(
            hoverProvider = BooleanOr.BooleanValue(true),
            definitionProvider = BooleanOr.BooleanValue(true),
            declarationProvider = BooleanOr.BooleanValue(true),
            typeDefinitionProvider = BooleanOr.BooleanValue(true),
            implementationProvider = BooleanOr.BooleanValue(true),
            referencesProvider = BooleanOr.BooleanValue(true),
            documentSymbolProvider = BooleanOr.BooleanValue(true),
            completionProvider = CompletionOptions(),
            signatureHelpProvider = com.monkopedia.lsp.SignatureHelpOptions(),
            documentHighlightProvider = BooleanOr.BooleanValue(true),
            documentFormattingProvider = BooleanOr.BooleanValue(true),
            renameProvider = BooleanOr.BooleanValue(true),
            codeActionProvider = BooleanOr.BooleanValue(true),
            codeLensProvider = com.monkopedia.lsp.CodeLensOptions(),
            foldingRangeProvider = BooleanOr.BooleanValue(true),
            inlayHintProvider = BooleanOr.BooleanValue(true)
        ),
        serverInfo = InitializeResultServerInfo(
            name = "ConformanceLanguageServer",
            version = "1.0.0"
        )
    )

    override suspend fun shutdown(): Nothing? = null

    // region union-branch-exhaustive methods

    override suspend fun textDocumentHover(params: HoverParams): Hover {
        val contents = when (params.position.line.toInt()) {
            Lines.SINGLE -> HoverContents.markdown("**markup** hover at line 0")

            Lines.ARRAY -> HoverContents.string("marked-string hover at line 1")

            else -> HoverContents.MarkedStringArray(
                listOf(
                    StringOr.StringValue("marked one"),
                    StringOr.StringValue("marked two")
                )
            )
        }
        return Hover(contents = contents, range = range(params.position.line.toInt()))
    }

    override suspend fun textDocumentDefinition(
        params: DefinitionParams
    ): TextDocumentDefinitionResult = when (params.position.line.toInt()) {
        Lines.SINGLE -> TextDocumentDefinitionResult.DefinitionValue(
            singleDefinition(Uri.MAIN, 0)
        )

        Lines.ARRAY -> TextDocumentDefinitionResult.DefinitionValue(
            arrayDefinition(Uri.MAIN)
        )

        else -> TextDocumentDefinitionResult.DefinitionLinkArray(linkDefinitions(Uri.MAIN))
    }

    override suspend fun textDocumentDeclaration(
        params: DeclarationParams
    ): TextDocumentDeclarationResult = when (params.position.line.toInt()) {
        Lines.SINGLE -> TextDocumentDeclarationResult.DeclarationValue(
            singleDeclaration(Uri.MAIN, 0)
        )

        Lines.ARRAY -> TextDocumentDeclarationResult.DeclarationValue(
            arrayDeclaration(Uri.MAIN)
        )

        else -> TextDocumentDeclarationResult.DeclarationLinkArray(linkDeclarations(Uri.MAIN))
    }

    override suspend fun textDocumentTypeDefinition(
        params: TypeDefinitionParams
    ): TextDocumentTypeDefinitionResult = when (params.position.line.toInt()) {
        Lines.SINGLE -> TextDocumentTypeDefinitionResult.DefinitionValue(
            singleDefinition(Uri.MAIN, 0)
        )

        Lines.ARRAY -> TextDocumentTypeDefinitionResult.DefinitionValue(
            arrayDefinition(Uri.MAIN)
        )

        else -> TextDocumentTypeDefinitionResult.DefinitionLinkArray(linkDefinitions(Uri.MAIN))
    }

    override suspend fun textDocumentImplementation(
        params: ImplementationParams
    ): TextDocumentImplementationResult = when (params.position.line.toInt()) {
        Lines.SINGLE -> TextDocumentImplementationResult.DefinitionValue(
            singleDefinition(Uri.MAIN, 0)
        )

        Lines.ARRAY -> TextDocumentImplementationResult.DefinitionValue(
            arrayDefinition(Uri.MAIN)
        )

        else -> TextDocumentImplementationResult.DefinitionLinkArray(linkDefinitions(Uri.MAIN))
    }

    override suspend fun textDocumentReferences(params: ReferenceParams): List<Location> =
        listOf(location(Uri.MAIN, 0), location(Uri.MAIN, 1), location(Uri.MAIN, 2))

    override suspend fun textDocumentCompletion(
        params: CompletionParams
    ): TextDocumentCompletionResult = when (params.position.line.toInt()) {
        Lines.SINGLE -> TextDocumentCompletionResult.CompletionListValue(
            CompletionList(
                isIncomplete = false,
                items = listOf(
                    CompletionItem(label = "fromList", kind = CompletionItemKind.FUNCTION)
                )
            )
        )

        else -> TextDocumentCompletionResult.CompletionItemArray(
            listOf(
                CompletionItem(label = "fromArrayA", kind = CompletionItemKind.VALUE),
                CompletionItem(label = "fromArrayB", kind = CompletionItemKind.VALUE)
            )
        )
    }

    override suspend fun textDocumentDocumentSymbol(
        params: DocumentSymbolParams
    ): TextDocumentDocumentSymbolResult = if (params.textDocument.uri.endsWith("#flat")) {
        TextDocumentDocumentSymbolResult.SymbolInformationArray(
            listOf(
                SymbolInformation(
                    name = "flatSymbol",
                    kind = SymbolKind.FUNCTION,
                    location = location(params.textDocument.uri, 0)
                )
            )
        )
    } else {
        TextDocumentDocumentSymbolResult.DocumentSymbolArray(
            listOf(
                DocumentSymbol(
                    name = "hierarchicalSymbol",
                    kind = SymbolKind.CLASS,
                    range = range(0),
                    selectionRange = range(0),
                    children = listOf(
                        DocumentSymbol(
                            name = "childMethod",
                            kind = SymbolKind.METHOD,
                            range = range(1),
                            selectionRange = range(1)
                        )
                    )
                )
            )
        )
    }

    // endregion

    // region simple well-formed canned methods

    override suspend fun textDocumentSignatureHelp(params: SignatureHelpParams): SignatureHelp =
        SignatureHelp(
            signatures = listOf(
                SignatureInformation(
                    label = "fun conformance(value: Int): Int",
                    documentation = StringOr.StringValue("canned signature")
                )
            ),
            activeSignature = 0u,
            activeParameter = 0u
        )

    override suspend fun textDocumentDocumentHighlight(
        params: DocumentHighlightParams
    ): List<DocumentHighlight> = listOf(
        DocumentHighlight(range = range(0), kind = DocumentHighlightKind.TEXT),
        DocumentHighlight(range = range(1), kind = DocumentHighlightKind.WRITE)
    )

    override suspend fun textDocumentFormatting(params: DocumentFormattingParams): List<TextEdit> =
        listOf(TextEdit(range = range(0), newText = "formatted\n"))

    override suspend fun textDocumentRename(params: RenameParams): WorkspaceEdit = WorkspaceEdit(
        changes = mapOf(
            Uri.MAIN to listOf(TextEdit(range = range(0), newText = params.newName))
        )
    )

    override suspend fun textDocumentCodeAction(
        params: CodeActionParams
    ): List<com.monkopedia.lsp.TextDocumentCodeActionResult> = listOf(
        CodeAction(title = "Canned quick fix"),
        Command(title = "Canned command", command = "conformance.command")
    )

    override suspend fun textDocumentCodeLens(params: CodeLensParams): List<CodeLens> = listOf(
        CodeLens(
            range = range(0),
            command = Command(title = "Canned lens", command = "conformance.lens")
        )
    )

    override suspend fun textDocumentFoldingRange(params: FoldingRangeParams): List<FoldingRange> =
        listOf(
            FoldingRange(startLine = 0u, endLine = 5u, kind = FoldingRangeKind.REGION)
        )

    override suspend fun textDocumentSemanticTokensFull(
        params: SemanticTokensParams
    ): SemanticTokens = SemanticTokens(
        resultId = "conformance-1",
        data = listOf(0u, 0u, 4u, 0u, 0u)
    )

    override suspend fun textDocumentInlayHint(params: InlayHintParams): List<InlayHint> = listOf(
        InlayHint(
            position = pos(0, 4),
            label = StringOr.StringValue(": Int")
        )
    )

    // endregion

    // region branch-construction helpers

    private fun singleDefinition(uri: String, line: Int): Definition =
        SingleOrArray.single(location(uri, line))

    private fun arrayDefinition(uri: String): Definition =
        SingleOrArray.multiple(listOf(location(uri, 0), location(uri, 1)))

    private fun linkDefinitions(uri: String): List<DefinitionLink> =
        listOf(locationLink(uri, 0), locationLink(uri, 1))

    private fun singleDeclaration(uri: String, line: Int): Declaration =
        SingleOrArray.single(location(uri, line))

    private fun arrayDeclaration(uri: String): Declaration =
        SingleOrArray.multiple(listOf(location(uri, 0), location(uri, 1)))

    private fun linkDeclarations(uri: String): List<DeclarationLink> =
        listOf(locationLink(uri, 0), locationLink(uri, 1))

    // endregion
}
