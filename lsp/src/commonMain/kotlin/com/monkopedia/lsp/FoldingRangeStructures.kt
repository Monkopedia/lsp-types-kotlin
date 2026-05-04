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
 * Parameters for a {@link FoldingRangeRequest}.
 */
@kotlinx.serialization.Serializable
data class FoldingRangeParams(
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
 * Represents a folding range. To be valid, start and end line must be bigger than zero and smaller
 * than the number of lines in the document. Clients are free to ignore invalid ranges.
 */
@kotlinx.serialization.Serializable
data class FoldingRange(
    /**
     * The zero-based start line of the range to fold. The folded area starts after the line's last character.
     * To be valid, the end must be zero or larger and smaller than the number of lines in the document.
     */
    val startLine: UInt,
    /**
     * The zero-based character offset from where the folded range starts. If not defined, defaults to the length of the start line.
     */
    val startCharacter: UInt? = null,
    /**
     * The zero-based end line of the range to fold. The folded area ends with the line's last character.
     * To be valid, the end must be zero or larger and smaller than the number of lines in the document.
     */
    val endLine: UInt,
    /**
     * The zero-based character offset before the folded range ends. If not defined, defaults to the length of the end line.
     */
    val endCharacter: UInt? = null,
    /**
     * Describes the kind of the folding range such as `comment' or 'region'. The kind
     * is used to categorize folding ranges and used by commands like 'Fold all comments'.
     * See {@link FoldingRangeKind} for an enumeration of standardized kinds.
     */
    val kind: FoldingRangeKind? = null,
    /**
     * The text that the client should show when the specified range is
     * collapsed. If not defined or not supported by the client, a default
     * will be chosen by the client.
     *
     * @since 3.17.0
     */
    val collapsedText: String? = null
)

@kotlinx.serialization.Serializable
data class FoldingRangeRegistrationOptions(
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
) : ServerCapabilitiesFoldingRangeProviderOptions

@kotlinx.serialization.Serializable
data class FoldingRangeOptions(val workDoneProgress: Boolean? = null) :
    ServerCapabilitiesFoldingRangeProviderOptions

/**
 * Client workspace capabilities specific to folding ranges
 *
 * @since 3.18.0
 * @proposed
 */
@kotlinx.serialization.Serializable
data class FoldingRangeWorkspaceClientCapabilities(
    /**
     * Whether the client implementation supports a refresh request sent from the
     * server to the client.
     *
     * Note that this event is global and will force the client to refresh all
     * folding ranges currently shown. It should be used with absolute care and is
     * useful for situation where a server for example detects a project wide
     * change that requires such a calculation.
     *
     * @since 3.18.0
     * @proposed
     */
    val refreshSupport: Boolean? = null
)

@kotlinx.serialization.Serializable
data class FoldingRangeClientCapabilities(
    /**
     * Whether implementation supports dynamic registration for folding range
     * providers. If this is set to `true` the client supports the new
     * `FoldingRangeRegistrationOptions` return value for the corresponding
     * server capability as well.
     */
    val dynamicRegistration: Boolean? = null,
    /**
     * The maximum number of folding ranges that the client prefers to receive
     * per document. The value serves as a hint, servers are free to follow the
     * limit.
     */
    val rangeLimit: UInt? = null,
    /**
     * If set, the client signals that it only supports folding complete lines.
     * If set, client will ignore specified `startCharacter` and `endCharacter`
     * properties in a FoldingRange.
     */
    val lineFoldingOnly: Boolean? = null,
    /**
     * Specific options for the folding range kind.
     *
     * @since 3.17.0
     */
    val foldingRangeKind: FoldingRangeClientCapabilitiesFoldingRangeKind? = null,
    /**
     * Specific options for the folding range.
     *
     * @since 3.17.0
     */
    val foldingRange: FoldingRangeClientCapabilitiesFoldingRange? = null
)

@kotlinx.serialization.Serializable
data class FoldingRangeClientCapabilitiesFoldingRange(
    /**
     * If set, the client signals that it supports setting collapsedText on
     * folding ranges to display custom labels instead of the default text.
     *
     * @since 3.17.0
     */
    val collapsedText: Boolean? = null
)

@kotlinx.serialization.Serializable
data class FoldingRangeClientCapabilitiesFoldingRangeKind(
    /**
     * The folding range kind values the client supports. When this
     * property exists the client also guarantees that it will
     * handle values outside its set gracefully and falls back
     * to a default value when unknown.
     */
    val valueSet: List<FoldingRangeKind>? = null
)
