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
 * @since 3.16.0
 */
@Serializable
data class SemanticTokensParams(
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
    val textDocument: TextDocumentIdentifier
)

/**
 * @since 3.16.0
 */
@Serializable
data class SemanticTokens(
    /**
     * An optional result id. If provided and clients support delta updating
     * the client will include the result id in the next semantic token request.
     * A server can then instead of computing all semantic tokens again simply
     * send a delta.
     */
    val resultId: String? = null,
    /**
     * The actual tokens.
     */
    @SerialName("data") val `data`: List<UInt>
) : TextDocumentSemanticTokensFullDeltaResult

/**
 * @since 3.16.0
 */
@Serializable
data class SemanticTokensPartialResult(@SerialName("data") val `data`: List<UInt>)

/**
 * @since 3.16.0
 */
@Serializable
data class SemanticTokensRegistrationOptions(
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector?,
    val workDoneProgress: Boolean? = null,
    /**
     * The legend used by the server
     */
    val legend: SemanticTokensLegend,
    /**
     * Server supports providing semantic tokens for a specific range
     * of a document.
     */
    val range: BooleanOr<JsonElement>? = null,
    /**
     * Server supports providing semantic tokens for a full document.
     */
    val full: BooleanOr<JsonElement>? = null,
    /**
     * The id used to register the request. The id can be used to deregister
     * the request again. See also Registration#id.
     */
    val id: String? = null
) : ServerCapabilitiesSemanticTokensProvider

/**
 * @since 3.16.0
 */
@Serializable
data class SemanticTokensDeltaParams(
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
     * The result id of a previous response. The result Id can either point to a full response
     * or a delta response depending on what was received last.
     */
    val previousResultId: String
)

/**
 * @since 3.16.0
 */
@Serializable
data class SemanticTokensDelta(
    val resultId: String? = null,
    /**
     * The semantic token edits to transform a previous result into a new result.
     */
    val edits: List<SemanticTokensEdit>
) : TextDocumentSemanticTokensFullDeltaResult

/**
 * @since 3.16.0
 */
@Serializable
data class SemanticTokensDeltaPartialResult(val edits: List<SemanticTokensEdit>)

/**
 * @since 3.16.0
 */
@Serializable
data class SemanticTokensRangeParams(
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
     * The range the semantic tokens are requested for.
     */
    val range: Range
)

/**
 * @since 3.16.0
 */
@Serializable
data class SemanticTokensOptions(
    val workDoneProgress: Boolean? = null,
    /**
     * The legend used by the server
     */
    val legend: SemanticTokensLegend,
    /**
     * Server supports providing semantic tokens for a specific range
     * of a document.
     */
    val range: BooleanOr<JsonElement>? = null,
    /**
     * Server supports providing semantic tokens for a full document.
     */
    val full: BooleanOr<JsonElement>? = null
) : ServerCapabilitiesSemanticTokensProvider

/**
 * @since 3.16.0
 */
@Serializable
data class SemanticTokensEdit(
    /**
     * The start offset of the edit.
     */
    val start: UInt,
    /**
     * The count of elements to remove.
     */
    val deleteCount: UInt,
    /**
     * The elements to insert.
     */
    @SerialName("data") val `data`: List<UInt>? = null
)

/**
 * @since 3.16.0
 */
@Serializable
data class SemanticTokensLegend(
    /**
     * The token types a server uses.
     */
    val tokenTypes: List<String>,
    /**
     * The token modifiers a server uses.
     */
    val tokenModifiers: List<String>
)

/**
 * @since 3.16.0
 */
@Serializable
data class SemanticTokensWorkspaceClientCapabilities(
    /**
     * Whether the client implementation supports a refresh request sent from
     * the server to the client.
     *
     * Note that this event is global and will force the client to refresh all
     * semantic tokens currently shown. It should be used with absolute care
     * and is useful for situation where a server for example detects a project
     * wide change that requires such a calculation.
     */
    val refreshSupport: Boolean? = null
)

/**
 * @since 3.16.0
 */
@Serializable
data class SemanticTokensClientCapabilities(
    /**
     * Whether implementation supports dynamic registration. If this is set to `true`
     * the client supports the new `(TextDocumentRegistrationOptions & StaticRegistrationOptions)`
     * return value for the corresponding server capability as well.
     */
    val dynamicRegistration: Boolean? = null,
    /**
     * Which requests the client supports and might send to the server
     * depending on the server's capability. Please note that clients might not
     * show semantic tokens or degrade some of the user experience if a range
     * or full request is advertised by the client but not provided by the
     * server. If for example the client capability `requests.full` and
     * `request.range` are both set to true but the server only provides a
     * range provider the client might not render a minimap correctly or might
     * even decide to not show any semantic tokens at all.
     */
    val requests: SemanticTokensClientCapabilitiesRequests,
    /**
     * The token types that the client supports.
     */
    val tokenTypes: List<String>,
    /**
     * The token modifiers that the client supports.
     */
    val tokenModifiers: List<String>,
    /**
     * The token formats the clients supports.
     */
    val formats: List<TokenFormat>,
    /**
     * Whether the client supports tokens that can overlap each other.
     */
    val overlappingTokenSupport: Boolean? = null,
    /**
     * Whether the client supports tokens that can span multiple lines.
     */
    val multilineTokenSupport: Boolean? = null,
    /**
     * Whether the client allows the server to actively cancel a
     * semantic token request, e.g. supports returning
     * LSPErrorCodes.ServerCancelled. If a server does the client
     * needs to retrigger the request.
     *
     * @since 3.17.0
     */
    val serverCancelSupport: Boolean? = null,
    /**
     * Whether the client uses semantic tokens to augment existing
     * syntax tokens. If set to `true` client side created syntax
     * tokens and semantic tokens are both used for colorization. If
     * set to `false` the client only uses the returned semantic tokens
     * for colorization.
     *
     * If the value is `undefined` then the client behavior is not
     * specified.
     *
     * @since 3.17.0
     */
    val augmentsSyntaxTokens: Boolean? = null
)

@Serializable
data class SemanticTokensClientCapabilitiesRequests(
    /**
     * The client will send the `textDocument/semanticTokens/range` request if
     * the server provides a corresponding handler.
     */
    val range: BooleanOr<JsonElement>? = null,
    /**
     * The client will send the `textDocument/semanticTokens/full` request if
     * the server provides a corresponding handler.
     */
    val full: BooleanOr<JsonElement>? = null
)
