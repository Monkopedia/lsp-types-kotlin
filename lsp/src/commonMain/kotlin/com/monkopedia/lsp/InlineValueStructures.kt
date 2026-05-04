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
 * A parameter literal used in inline value requests.
 *
 * @since 3.17.0
 */
@Serializable
data class InlineValueParams(
    /**
     * An optional token that a server can use to report work done progress.
     */
    val workDoneToken: ProgressToken? = null,
    /**
     * The text document.
     */
    val textDocument: TextDocumentIdentifier,
    /**
     * The document range for which inline values should be computed.
     */
    val range: Range,
    /**
     * Additional information about the context in which inline values were
     * requested.
     */
    val context: InlineValueContext
)

/**
 * Inline value options used during static or dynamic registration.
 *
 * @since 3.17.0
 */
@Serializable
data class InlineValueRegistrationOptions(
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
) : ServerCapabilitiesInlineValueProviderOptions

/**
 * @since 3.17.0
 */
@Serializable
data class InlineValueContext(
    /**
     * The stack frame (as a DAP Id) where the execution has stopped.
     */
    val frameId: Int,
    /**
     * The document range where execution has stopped.
     * Typically the end position of the range denotes the line where the inline values are shown.
     */
    val stoppedLocation: Range
)

/**
 * Provide inline value as text.
 *
 * @since 3.17.0
 */
@Serializable
data class InlineValueText(
    /**
     * The document range for which the inline value applies.
     */
    val range: Range,
    /**
     * The text of the inline value.
     */
    val text: String
) : InlineValue

/**
 * Provide inline value through a variable lookup.
 * If only a range is specified, the variable name will be extracted from the underlying document.
 * An optional variable name can be used to override the extracted name.
 *
 * @since 3.17.0
 */
@Serializable
data class InlineValueVariableLookup(
    /**
     * The document range for which the inline value applies.
     * The range is used to extract the variable name from the underlying document.
     */
    val range: Range,
    /**
     * If specified the name of the variable to look up.
     */
    val variableName: String? = null,
    /**
     * How to perform the lookup.
     */
    val caseSensitiveLookup: Boolean
) : InlineValue

/**
 * Provide an inline value through an expression evaluation.
 * If only a range is specified, the expression will be extracted from the underlying document.
 * An optional expression can be used to override the extracted expression.
 *
 * @since 3.17.0
 */
@Serializable
data class InlineValueEvaluatableExpression(
    /**
     * The document range for which the inline value applies.
     * The range is used to extract the evaluatable expression from the underlying document.
     */
    val range: Range,
    /**
     * If specified the expression overrides the extracted expression.
     */
    val expression: String? = null
) : InlineValue

/**
 * Inline value options used during static registration.
 *
 * @since 3.17.0
 */
@Serializable
data class InlineValueOptions(val workDoneProgress: Boolean? = null) :
    ServerCapabilitiesInlineValueProviderOptions

/**
 * Client workspace capabilities specific to inline values.
 *
 * @since 3.17.0
 */
@Serializable
data class InlineValueWorkspaceClientCapabilities(
    /**
     * Whether the client implementation supports a refresh request sent from the
     * server to the client.
     *
     * Note that this event is global and will force the client to refresh all
     * inline values currently shown. It should be used with absolute care and is
     * useful for situation where a server for example detects a project wide
     * change that requires such a calculation.
     */
    val refreshSupport: Boolean? = null
)

/**
 * Client capabilities specific to inline values.
 *
 * @since 3.17.0
 */
@Serializable
data class InlineValueClientCapabilities(
    /**
     * Whether implementation supports dynamic registration for inline value providers.
     */
    val dynamicRegistration: Boolean? = null
)
