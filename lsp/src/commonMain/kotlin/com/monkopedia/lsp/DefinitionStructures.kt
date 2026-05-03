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
 * Parameters for a {@link DefinitionRequest}.
 */
@kotlinx.serialization.Serializable
data class DefinitionParams(
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
 * Registration options for a {@link DefinitionRequest}.
 */
@kotlinx.serialization.Serializable
data class DefinitionRegistrationOptions(
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector??,
    val workDoneProgress: Boolean? = null
)

/**
 * Server Capabilities for a {@link DefinitionRequest}.
 */
@kotlinx.serialization.Serializable
data class DefinitionOptions(val workDoneProgress: Boolean? = null)

/**
 * Client Capabilities for a {@link DefinitionRequest}.
 */
@kotlinx.serialization.Serializable
data class DefinitionClientCapabilities(
    /**
     * Whether definition supports dynamic registration.
     */
    val dynamicRegistration: Boolean? = null,
    /**
     * The client supports additional metadata in the form of definition links.
     *
     * @since 3.14.0
     */
    val linkSupport: Boolean? = null
)
