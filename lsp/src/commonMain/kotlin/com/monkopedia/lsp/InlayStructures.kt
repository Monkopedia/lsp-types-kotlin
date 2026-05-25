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
 * A parameter literal used in inlay hint requests.
 *
 * @since 3.17.0
 */
@Serializable
data class InlayHintParams(
    /**
     * An optional token that a server can use to report work done progress.
     */
    val workDoneToken: ProgressToken? = null,
    /**
     * The text document.
     */
    val textDocument: TextDocumentIdentifier,
    /**
     * The document range for which inlay hints should be computed.
     */
    val range: Range
)

/**
 * Inlay hint information.
 *
 * @since 3.17.0
 */
@Serializable
data class InlayHint(
    /**
     * The position of this hint.
     *
     * If multiple hints have the same position, they will be shown in the order
     * they appear in the response.
     */
    val position: Position,
    /**
     * The label of this hint. A human readable string or an array of
     * InlayHintLabelPart label parts.
     *
     * *Note* that neither the string nor the label part can be empty.
     */
    val label: StringOr<List<InlayHintLabelPart>>,
    /**
     * The kind of this hint. Can be omitted in which case the client
     * should fall back to a reasonable default.
     */
    val kind: InlayHintKind? = null,
    /**
     * Optional text edits that are performed when accepting this inlay hint.
     *
     * *Note* that edits are expected to change the document so that the inlay
     * hint (or its nearest variant) is now part of the document and the inlay
     * hint itself is now obsolete.
     */
    val textEdits: List<TextEdit>? = null,
    /**
     * The tooltip text when you hover over this item.
     */
    val tooltip: StringOr<MarkupContent>? = null,
    /**
     * Render padding before the hint.
     *
     * Note: Padding should use the editor's background color, not the
     * background color of the hint itself. That means padding can be used
     * to visually align/separate an inlay hint.
     */
    val paddingLeft: Boolean? = null,
    /**
     * Render padding after the hint.
     *
     * Note: Padding should use the editor's background color, not the
     * background color of the hint itself. That means padding can be used
     * to visually align/separate an inlay hint.
     */
    val paddingRight: Boolean? = null,
    /**
     * A data entry field that is preserved on an inlay hint between
     * a `textDocument/inlayHint` and a `inlayHint/resolve` request.
     */
    @SerialName("data") val `data`: LSPAny? = null
)

/**
 * Inlay hint options used during static or dynamic registration.
 *
 * @since 3.17.0
 */
@Serializable
data class InlayHintRegistrationOptions(
    val workDoneProgress: Boolean? = null,
    /**
     * The server provides support to resolve additional
     * information for an inlay hint item.
     */
    val resolveProvider: Boolean? = null,
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector? = null,
    /**
     * The id used to register the request. The id can be used to deregister
     * the request again. See also Registration#id.
     */
    val id: String? = null
) : ServerCapabilitiesInlayHintProviderOptions

/**
 * An inlay hint label part allows for interactive and composite labels
 * of inlay hints.
 *
 * @since 3.17.0
 */
@Serializable
data class InlayHintLabelPart(
    /**
     * The value of this label part.
     */
    val value: String,
    /**
     * The tooltip text when you hover over this label part. Depending on
     * the client capability `inlayHint.resolveSupport` clients might resolve
     * this property late using the resolve request.
     */
    val tooltip: StringOr<MarkupContent>? = null,
    /**
     * An optional source code location that represents this
     * label part.
     *
     * The editor will use this location for the hover and for code navigation
     * features: This part will become a clickable link that resolves to the
     * definition of the symbol at the given location (not necessarily the
     * location itself), it shows the hover that shows at the given location,
     * and it shows a context menu with further code navigation commands.
     *
     * Depending on the client capability `inlayHint.resolveSupport` clients
     * might resolve this property late using the resolve request.
     */
    val location: Location? = null,
    /**
     * An optional command for this label part.
     *
     * Depending on the client capability `inlayHint.resolveSupport` clients
     * might resolve this property late using the resolve request.
     */
    val command: Command? = null
)

/**
 * Inlay hint options used during static registration.
 *
 * @since 3.17.0
 */
@Serializable
data class InlayHintOptions(
    val workDoneProgress: Boolean? = null,
    /**
     * The server provides support to resolve additional
     * information for an inlay hint item.
     */
    val resolveProvider: Boolean? = null
) : ServerCapabilitiesInlayHintProviderOptions

/**
 * Client workspace capabilities specific to inlay hints.
 *
 * @since 3.17.0
 */
@Serializable
data class InlayHintWorkspaceClientCapabilities(
    /**
     * Whether the client implementation supports a refresh request sent from
     * the server to the client.
     *
     * Note that this event is global and will force the client to refresh all
     * inlay hints currently shown. It should be used with absolute care and
     * is useful for situation where a server for example detects a project wide
     * change that requires such a calculation.
     */
    val refreshSupport: Boolean? = null
)

/**
 * Inlay hint client capabilities.
 *
 * @since 3.17.0
 */
@Serializable
data class InlayHintClientCapabilities(
    /**
     * Whether inlay hints support dynamic registration.
     */
    val dynamicRegistration: Boolean? = null,
    /**
     * Indicates which properties a client can resolve lazily on an inlay
     * hint.
     */
    val resolveSupport: InlayHintClientCapabilitiesResolveSupport? = null
)

@Serializable
data class InlayHintClientCapabilitiesResolveSupport(
    /**
     * The properties that a client can resolve lazily.
     */
    val properties: List<String>
)
