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
 * The parameters of a {@link CodeLensRequest}.
 */
@Serializable
data class CodeLensParams(
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
     * The document to request code lens for.
     */
    val textDocument: TextDocumentIdentifier
)

/**
 * A code lens represents a {@link Command command} that should be shown along with
 * source text, like the number of references, a way to run tests, etc.
 *
 * A code lens is _unresolved_ when no command is associated to it. For performance
 * reasons the creation of a code lens and resolving should be done in two stages.
 */
@Serializable
data class CodeLens(
    /**
     * The range in which this code lens is valid. Should only span a single line.
     */
    val range: Range,
    /**
     * The command this code lens represents.
     */
    val command: Command? = null,
    /**
     * A data entry field that is preserved on a code lens item between
     * a {@link CodeLensRequest} and a {@link CodeLensResolveRequest}
     */
    @SerialName("data") val `data`: LSPAny? = null
)

/**
 * Registration options for a {@link CodeLensRequest}.
 */
@Serializable
data class CodeLensRegistrationOptions(
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector?,
    val workDoneProgress: Boolean? = null,
    /**
     * Code lens has a resolve provider as well.
     */
    val resolveProvider: Boolean? = null
)

/**
 * Code Lens provider options of a {@link CodeLensRequest}.
 */
@Serializable
data class CodeLensOptions(
    val workDoneProgress: Boolean? = null,
    /**
     * Code lens has a resolve provider as well.
     */
    val resolveProvider: Boolean? = null
)

/**
 * @since 3.16.0
 */
@Serializable
data class CodeLensWorkspaceClientCapabilities(
    /**
     * Whether the client implementation supports a refresh request sent from the
     * server to the client.
     *
     * Note that this event is global and will force the client to refresh all
     * code lenses currently shown. It should be used with absolute care and is
     * useful for situation where a server for example detect a project wide
     * change that requires such a calculation.
     */
    val refreshSupport: Boolean? = null
)

/**
 * The client capabilities  of a {@link CodeLensRequest}.
 */
@Serializable
data class CodeLensClientCapabilities(
    /**
     * Whether code lens supports dynamic registration.
     */
    val dynamicRegistration: Boolean? = null
)
