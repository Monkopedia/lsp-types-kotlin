// Auto-generated from LSP metaModel.json — do not edit manually.
// Generator: lsp-codegen

@file:Suppress(
    "unused",
    "PropertyName",
    "ktlint:standard:class-naming",
    "ktlint:standard:filename",
    "ktlint:standard:max-line-length"
)

package com.monkopedia.lsp

/**
 * A parameter literal used in selection range requests.
 */
@kotlinx.serialization.Serializable
data class SelectionRangeParams(
    /**
     * An optional token that a server can use to report work done progress.
     */
    val workDoneToken: ProgressToken? = null,
    /**
     * An optional token that a server can use to report partial results (e.g. streaming) to
     * the client.
     */
    val partialResultToken: ProgressToken? = null,
    /**
     * The text document.
     */
    val textDocument: TextDocumentIdentifier,
    /**
     * The positions inside the text document.
     */
    val positions: List<Position>
)

/**
 * A selection range represents a part of a selection hierarchy. A selection range
 * may have a parent selection range that contains it.
 */
@kotlinx.serialization.Serializable
data class SelectionRange(
    /**
     * The {@link Range range} of this selection range.
     */
    val range: Range,
    /**
     * The parent selection range containing this range. Therefore `parent.range` must contain `this.range`.
     */
    val parent: SelectionRange? = null
)

@kotlinx.serialization.Serializable
data class SelectionRangeRegistrationOptions(
    val workDoneProgress: Boolean? = null,
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector??,
    /**
     * The id used to register the request. The id can be used to deregister
     * the request again. See also Registration#id.
     */
    val id: String? = null
)

@kotlinx.serialization.Serializable
data class SelectionRangeOptions(val workDoneProgress: Boolean? = null)

@kotlinx.serialization.Serializable
data class SelectionRangeClientCapabilities(
    /**
     * Whether implementation supports dynamic registration for selection range providers. If this is set to `true`
     * the client supports the new `SelectionRangeRegistrationOptions` return value for the corresponding server
     * capability as well.
     */
    val dynamicRegistration: Boolean? = null
)
