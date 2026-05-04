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
 * Parameters for a {@link HoverRequest}.
 */
@kotlinx.serialization.Serializable
data class HoverParams(
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
    val workDoneToken: ProgressToken? = null
)

/**
 * The result of a hover request.
 */
@kotlinx.serialization.Serializable
data class Hover(
    /**
     * The hover's content
     */
    val contents: kotlinx.serialization.json.JsonElement,
    /**
     * An optional range inside the text document that is used to
     * visualize the hover, e.g. by changing the background color.
     */
    val range: Range? = null
)

/**
 * Registration options for a {@link HoverRequest}.
 */
@kotlinx.serialization.Serializable
data class HoverRegistrationOptions(
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector?,
    val workDoneProgress: Boolean? = null
)

/**
 * Hover options.
 */
@kotlinx.serialization.Serializable
data class HoverOptions(val workDoneProgress: Boolean? = null)

@kotlinx.serialization.Serializable
data class HoverClientCapabilities(
    /**
     * Whether hover supports dynamic registration.
     */
    val dynamicRegistration: Boolean? = null,
    /**
     * Client supports the following content formats for the content
     * property. The order describes the preferred format of the client.
     */
    val contentFormat: List<MarkupKind>? = null
)
