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

@kotlinx.serialization.Serializable
data class DeclarationParams(
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

@kotlinx.serialization.Serializable
data class DeclarationRegistrationOptions(
    val workDoneProgress: Boolean? = null,
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector?,
    /**
     * The id used to register the request. The id can be used to deregister
     * the request again. See also Registration#id.
     */
    val id: String? = null
) : ServerCapabilitiesDeclarationProviderOptions

@kotlinx.serialization.Serializable
data class DeclarationOptions(val workDoneProgress: Boolean? = null) :
    ServerCapabilitiesDeclarationProviderOptions

/**
 * @since 3.14.0
 */
@kotlinx.serialization.Serializable
data class DeclarationClientCapabilities(
    /**
     * Whether declaration supports dynamic registration. If this is set to `true`
     * the client supports the new `DeclarationRegistrationOptions` return value
     * for the corresponding server capability as well.
     */
    val dynamicRegistration: Boolean? = null,
    /**
     * The client supports additional metadata in the form of declaration links.
     */
    val linkSupport: Boolean? = null
)
