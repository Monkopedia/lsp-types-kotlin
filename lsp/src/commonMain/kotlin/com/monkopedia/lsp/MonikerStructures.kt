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
data class MonikerParams(
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
 * Moniker definition to match LSIF 0.5 moniker definition.
 *
 * @since 3.16.0
 */
@kotlinx.serialization.Serializable
data class Moniker(
    /**
     * The scheme of the moniker. For example tsc or .Net
     */
    val scheme: String,
    /**
     * The identifier of the moniker. The value is opaque in LSIF however
     * schema owners are allowed to define the structure if they want.
     */
    val identifier: String,
    /**
     * The scope in which the moniker is unique
     */
    val unique: UniquenessLevel,
    /**
     * The moniker kind if known.
     */
    val kind: MonikerKind? = null
)

@kotlinx.serialization.Serializable
data class MonikerRegistrationOptions(
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector?,
    val workDoneProgress: Boolean? = null
) : ServerCapabilitiesMonikerProviderOptions

@kotlinx.serialization.Serializable
data class MonikerOptions(val workDoneProgress: Boolean? = null) :
    ServerCapabilitiesMonikerProviderOptions

/**
 * Client capabilities specific to the moniker request.
 *
 * @since 3.16.0
 */
@kotlinx.serialization.Serializable
data class MonikerClientCapabilities(
    /**
     * Whether moniker supports dynamic registration. If this is set to `true`
     * the client supports the new `MonikerRegistrationOptions` return value
     * for the corresponding server capability as well.
     */
    val dynamicRegistration: Boolean? = null
)
