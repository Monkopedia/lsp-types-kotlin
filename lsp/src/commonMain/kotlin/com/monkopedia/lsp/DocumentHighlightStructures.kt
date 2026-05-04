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
 * Parameters for a {@link DocumentHighlightRequest}.
 */
@kotlinx.serialization.Serializable
data class DocumentHighlightParams(
    /**
     * The text document.
     */
    val textDocument: TextDocumentIdentifier,
    /**
     * The position inside the text document.
     */
    val position: Position,
    /**
     * An optional token that a server can use to report work done progress.
     */
    val workDoneToken: ProgressToken? = null,
    /**
     * An optional token that a server can use to report partial results (e.g. streaming) to
     * the client.
     */
    val partialResultToken: ProgressToken? = null
)

/**
 * A document highlight is a range inside a text document which deserves
 * special attention. Usually a document highlight is visualized by changing
 * the background color of its range.
 */
@kotlinx.serialization.Serializable
data class DocumentHighlight(
    /**
     * The range this highlight applies to.
     */
    val range: Range,
    /**
     * The highlight kind, default is {@link DocumentHighlightKind.Text text}.
     */
    val kind: DocumentHighlightKind? = null
)

/**
 * Registration options for a {@link DocumentHighlightRequest}.
 */
@kotlinx.serialization.Serializable
data class DocumentHighlightRegistrationOptions(
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector?,
    val workDoneProgress: Boolean? = null
)

/**
 * Provider options for a {@link DocumentHighlightRequest}.
 */
@kotlinx.serialization.Serializable
data class DocumentHighlightOptions(val workDoneProgress: Boolean? = null)

/**
 * Client Capabilities for a {@link DocumentHighlightRequest}.
 */
@kotlinx.serialization.Serializable
data class DocumentHighlightClientCapabilities(
    /**
     * Whether document highlight supports dynamic registration.
     */
    val dynamicRegistration: Boolean? = null
)
