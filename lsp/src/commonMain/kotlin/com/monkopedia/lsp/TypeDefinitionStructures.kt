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

@Serializable
data class TypeDefinitionParams(
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

@Serializable
data class TypeDefinitionRegistrationOptions(
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector?,
    val workDoneProgress: Boolean? = null,
    /**
     * The id used to register the request. The id can be used to deregister
     * the request again. See also Registration#id.
     */
    val id: String? = null
) : ServerCapabilitiesTypeDefinitionProviderOptions

@Serializable
data class TypeDefinitionOptions(val workDoneProgress: Boolean? = null) :
    ServerCapabilitiesTypeDefinitionProviderOptions

/**
 * Since 3.6.0
 */
@Serializable
data class TypeDefinitionClientCapabilities(
    /**
     * Whether implementation supports dynamic registration. If this is set to `true`
     * the client supports the new `TypeDefinitionRegistrationOptions` return value
     * for the corresponding server capability as well.
     */
    val dynamicRegistration: Boolean? = null,
    /**
     * The client supports additional metadata in the form of definition links.
     *
     * Since 3.14.0
     */
    val linkSupport: Boolean? = null
)
