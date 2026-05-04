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
 * General client capabilities.
 *
 * @since 3.16.0
 */
@Serializable
data class GeneralClientCapabilities(
    /**
     * Client capability that signals how the client
     * handles stale requests (e.g. a request
     * for which the client will not process the response
     * anymore since the information is outdated).
     *
     * @since 3.17.0
     */
    val staleRequestSupport: GeneralClientCapabilitiesStaleRequestSupport? = null,
    /**
     * Client capabilities specific to regular expressions.
     *
     * @since 3.16.0
     */
    val regularExpressions: RegularExpressionsClientCapabilities? = null,
    /**
     * Client capabilities specific to the client's markdown parser.
     *
     * @since 3.16.0
     */
    val markdown: MarkdownClientCapabilities? = null,
    /**
     * The position encodings supported by the client. Client and server
     * have to agree on the same position encoding to ensure that offsets
     * (e.g. character position in a line) are interpreted the same on both
     * sides.
     *
     * To keep the protocol backwards compatible the following applies: if
     * the value 'utf-16' is missing from the array of position encodings
     * servers can assume that the client supports UTF-16. UTF-16 is
     * therefore a mandatory encoding.
     *
     * If omitted it defaults to ['utf-16'].
     *
     * Implementation considerations: since the conversion from one encoding
     * into another requires the content of the file / line the conversion
     * is best done where the file is read which is usually on the server
     * side.
     *
     * @since 3.17.0
     */
    val positionEncodings: List<PositionEncodingKind>? = null
)

@Serializable
data class GeneralClientCapabilitiesStaleRequestSupport(
    /**
     * The client will actively cancel the request.
     */
    val cancel: Boolean,
    /**
     * The list of requests for which the client
     * will retry the request if it receives a
     * response with error code `ContentModified`
     */
    val retryOnContentModified: List<String>
)
