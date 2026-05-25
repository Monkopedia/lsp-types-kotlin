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

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement

/**
 * General text document registration options.
 */
@Serializable
data class TextDocumentRegistrationOptions(
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector?
)

/**
 * Describe options to be used when registered for text document change events.
 */
@Serializable
data class TextDocumentChangeRegistrationOptions(
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector?,
    /**
     * How documents are synced to the server.
     */
    val syncKind: TextDocumentSyncKind
)

/**
 * Save registration options.
 */
@Serializable
data class TextDocumentSaveRegistrationOptions(
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector?,
    /**
     * The client is supposed to include the content on save.
     */
    val includeText: Boolean? = null
)

/**
 * A parameter literal used in requests to pass a text document and a position inside that
 * document.
 */
@Serializable
data class TextDocumentPositionParams(
    /**
     * The text document.
     */
    val textDocument: TextDocumentIdentifier,
    /**
     * The position inside the text document.
     */
    val position: Position
)

/**
 * A literal to identify a text document in the client.
 */
@Serializable
data class TextDocumentIdentifier(
    /**
     * The text document's uri.
     */
    val uri: DocumentUri
)

/**
 * Describes textual changes on a text document. A TextDocumentEdit describes all changes
 * on a document version Si and after they are applied move the document to version Si+1.
 * So the creator of a TextDocumentEdit doesn't need to sort the array of edits or do any
 * kind of ordering. However the edits must be non overlapping.
 */
@Serializable
data class TextDocumentEdit(
    /**
     * The text document to change.
     */
    val textDocument: OptionalVersionedTextDocumentIdentifier,
    /**
     * The edits to be applied.
     *
     * @since 3.16.0 - support for AnnotatedTextEdit. This is guarded using a
     * client capability.
     */
    val edits: List<TextDocumentEditEdits>
) : WorkspaceEditDocumentChanges

/**
 * An item to transfer a text document from the client to the
 * server.
 */
@Serializable
data class TextDocumentItem(
    /**
     * The text document's uri.
     */
    val uri: DocumentUri,
    /**
     * The text document's language identifier.
     */
    val languageId: String,
    /**
     * The version number of this document (it will increase after each
     * change, including undo/redo).
     */
    val version: Int,
    /**
     * The content of the opened text document.
     */
    val text: String
)

@Serializable
data class TextDocumentSyncOptions(
    /**
     * Open and close notifications are sent to the server. If omitted open close notification should not
     * be sent.
     */
    val openClose: Boolean? = null,
    /**
     * Change notifications are sent to the server. See TextDocumentSyncKind.None, TextDocumentSyncKind.Full
     * and TextDocumentSyncKind.Incremental. If omitted it defaults to TextDocumentSyncKind.None.
     */
    val change: TextDocumentSyncKind? = null,
    /**
     * If present will save notifications are sent to the server. If omitted the notification should not be
     * sent.
     */
    val willSave: Boolean? = null,
    /**
     * If present will save wait until requests are sent to the server. If omitted the request should not be
     * sent.
     */
    val willSaveWaitUntil: Boolean? = null,
    /**
     * If present save notifications are sent to the server. If omitted the notification should not be
     * sent.
     */
    val save: BooleanOr<SaveOptions>? = null
) : ServerCapabilitiesTextDocumentSync

/**
 * Text document specific client capabilities.
 */
@Serializable
data class TextDocumentClientCapabilities(
    /**
     * Defines which synchronization capabilities the client supports.
     */
    val synchronization: TextDocumentSyncClientCapabilities? = null,
    /**
     * Capabilities specific to the `textDocument/completion` request.
     */
    val completion: CompletionClientCapabilities? = null,
    /**
     * Capabilities specific to the `textDocument/hover` request.
     */
    val hover: HoverClientCapabilities? = null,
    /**
     * Capabilities specific to the `textDocument/signatureHelp` request.
     */
    val signatureHelp: SignatureHelpClientCapabilities? = null,
    /**
     * Capabilities specific to the `textDocument/declaration` request.
     *
     * @since 3.14.0
     */
    val declaration: DeclarationClientCapabilities? = null,
    /**
     * Capabilities specific to the `textDocument/definition` request.
     */
    val definition: DefinitionClientCapabilities? = null,
    /**
     * Capabilities specific to the `textDocument/typeDefinition` request.
     *
     * @since 3.6.0
     */
    val typeDefinition: TypeDefinitionClientCapabilities? = null,
    /**
     * Capabilities specific to the `textDocument/implementation` request.
     *
     * @since 3.6.0
     */
    val implementation: ImplementationClientCapabilities? = null,
    /**
     * Capabilities specific to the `textDocument/references` request.
     */
    val references: ReferenceClientCapabilities? = null,
    /**
     * Capabilities specific to the `textDocument/documentHighlight` request.
     */
    val documentHighlight: DocumentHighlightClientCapabilities? = null,
    /**
     * Capabilities specific to the `textDocument/documentSymbol` request.
     */
    val documentSymbol: DocumentSymbolClientCapabilities? = null,
    /**
     * Capabilities specific to the `textDocument/codeAction` request.
     */
    val codeAction: CodeActionClientCapabilities? = null,
    /**
     * Capabilities specific to the `textDocument/codeLens` request.
     */
    val codeLens: CodeLensClientCapabilities? = null,
    /**
     * Capabilities specific to the `textDocument/documentLink` request.
     */
    val documentLink: DocumentLinkClientCapabilities? = null,
    /**
     * Capabilities specific to the `textDocument/documentColor` and the
     * `textDocument/colorPresentation` request.
     *
     * @since 3.6.0
     */
    val colorProvider: DocumentColorClientCapabilities? = null,
    /**
     * Capabilities specific to the `textDocument/formatting` request.
     */
    val formatting: DocumentFormattingClientCapabilities? = null,
    /**
     * Capabilities specific to the `textDocument/rangeFormatting` request.
     */
    val rangeFormatting: DocumentRangeFormattingClientCapabilities? = null,
    /**
     * Capabilities specific to the `textDocument/onTypeFormatting` request.
     */
    val onTypeFormatting: DocumentOnTypeFormattingClientCapabilities? = null,
    /**
     * Capabilities specific to the `textDocument/rename` request.
     */
    val rename: RenameClientCapabilities? = null,
    /**
     * Capabilities specific to the `textDocument/foldingRange` request.
     *
     * @since 3.10.0
     */
    val foldingRange: FoldingRangeClientCapabilities? = null,
    /**
     * Capabilities specific to the `textDocument/selectionRange` request.
     *
     * @since 3.15.0
     */
    val selectionRange: SelectionRangeClientCapabilities? = null,
    /**
     * Capabilities specific to the `textDocument/publishDiagnostics` notification.
     */
    val publishDiagnostics: PublishDiagnosticsClientCapabilities? = null,
    /**
     * Capabilities specific to the various call hierarchy requests.
     *
     * @since 3.16.0
     */
    val callHierarchy: CallHierarchyClientCapabilities? = null,
    /**
     * Capabilities specific to the various semantic token request.
     *
     * @since 3.16.0
     */
    val semanticTokens: SemanticTokensClientCapabilities? = null,
    /**
     * Capabilities specific to the `textDocument/linkedEditingRange` request.
     *
     * @since 3.16.0
     */
    val linkedEditingRange: LinkedEditingRangeClientCapabilities? = null,
    /**
     * Client capabilities specific to the `textDocument/moniker` request.
     *
     * @since 3.16.0
     */
    val moniker: MonikerClientCapabilities? = null,
    /**
     * Capabilities specific to the various type hierarchy requests.
     *
     * @since 3.17.0
     */
    val typeHierarchy: TypeHierarchyClientCapabilities? = null,
    /**
     * Capabilities specific to the `textDocument/inlineValue` request.
     *
     * @since 3.17.0
     */
    val inlineValue: InlineValueClientCapabilities? = null,
    /**
     * Capabilities specific to the `textDocument/inlayHint` request.
     *
     * @since 3.17.0
     */
    val inlayHint: InlayHintClientCapabilities? = null,
    /**
     * Capabilities specific to the diagnostic pull model.
     *
     * @since 3.17.0
     */
    val diagnostic: DiagnosticClientCapabilities? = null,
    /**
     * Client capabilities specific to inline completions.
     *
     * @since 3.18.0
     * @proposed
     */
    val inlineCompletion: InlineCompletionClientCapabilities? = null
)

@Serializable
data class TextDocumentSyncClientCapabilities(
    /**
     * Whether text document synchronization supports dynamic registration.
     */
    val dynamicRegistration: Boolean? = null,
    /**
     * The client supports sending will save notifications.
     */
    val willSave: Boolean? = null,
    /**
     * The client supports sending a will save request and
     * waits for a response providing text edits which will
     * be applied to the document before it is saved.
     */
    val willSaveWaitUntil: Boolean? = null,
    /**
     * The client supports did save notifications.
     */
    val didSave: Boolean? = null
)
