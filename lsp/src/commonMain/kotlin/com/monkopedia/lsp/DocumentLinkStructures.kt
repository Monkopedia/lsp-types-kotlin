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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement

/**
 * The parameters of a {@link DocumentLinkRequest}.
 */
@Serializable
data class DocumentLinkParams(
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
     * The document to provide document links for.
     */
    val textDocument: TextDocumentIdentifier
)

/**
 * A document link is a range in a text document that links to an internal or external resource, like another
 * text document or a web site.
 */
@Serializable
data class DocumentLink(
    /**
     * The range this link applies to.
     */
    val range: Range,
    /**
     * The uri this link points to. If missing a resolve request is sent later.
     */
    val target: URI? = null,
    /**
     * The tooltip text when you hover over this link.
     *
     * If a tooltip is provided, is will be displayed in a string that includes instructions on how to
     * trigger the link, such as `{0} (ctrl + click)`. The specific instructions vary depending on OS,
     * user settings, and localization.
     *
     * @since 3.15.0
     */
    val tooltip: String? = null,
    /**
     * A data entry field that is preserved on a document link between a
     * DocumentLinkRequest and a DocumentLinkResolveRequest.
     */
    @SerialName("data") val `data`: LSPAny? = null
)

/**
 * Registration options for a {@link DocumentLinkRequest}.
 */
@Serializable
data class DocumentLinkRegistrationOptions(
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector?,
    val workDoneProgress: Boolean? = null,
    /**
     * Document links have a resolve provider as well.
     */
    val resolveProvider: Boolean? = null
)

/**
 * Provider options for a {@link DocumentLinkRequest}.
 */
@Serializable
data class DocumentLinkOptions(
    val workDoneProgress: Boolean? = null,
    /**
     * Document links have a resolve provider as well.
     */
    val resolveProvider: Boolean? = null
)

/**
 * The client capabilities of a {@link DocumentLinkRequest}.
 */
@Serializable
data class DocumentLinkClientCapabilities(
    /**
     * Whether document link supports dynamic registration.
     */
    val dynamicRegistration: Boolean? = null,
    /**
     * Whether the client supports the `tooltip` property on `DocumentLink`.
     *
     * @since 3.15.0
     */
    val tooltipSupport: Boolean? = null
)
