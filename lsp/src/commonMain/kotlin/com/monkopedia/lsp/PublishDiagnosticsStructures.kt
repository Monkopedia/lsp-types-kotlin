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
 * The publish diagnostic notification's parameters.
 */
@Serializable
data class PublishDiagnosticsParams(
    /**
     * The URI for which diagnostic information is reported.
     */
    val uri: DocumentUri,
    /**
     * Optional the version number of the document the diagnostics are published for.
     *
     * @since 3.15.0
     */
    val version: Int? = null,
    /**
     * An array of diagnostic information items.
     */
    val diagnostics: List<Diagnostic>
)

/**
 * The publish diagnostic client capabilities.
 */
@Serializable
data class PublishDiagnosticsClientCapabilities(
    /**
     * Whether the clients accepts diagnostics with related information.
     */
    val relatedInformation: Boolean? = null,
    /**
     * Client supports the tag property to provide meta data about a diagnostic.
     * Clients supporting tags have to handle unknown tags gracefully.
     *
     * @since 3.15.0
     */
    val tagSupport: PublishDiagnosticsClientCapabilitiesTagSupport? = null,
    /**
     * Whether the client interprets the version property of the
     * `textDocument/publishDiagnostics` notification's parameter.
     *
     * @since 3.15.0
     */
    val versionSupport: Boolean? = null,
    /**
     * Client supports a codeDescription property
     *
     * @since 3.16.0
     */
    val codeDescriptionSupport: Boolean? = null,
    /**
     * Whether code action supports the `data` property which is
     * preserved between a `textDocument/publishDiagnostics` and
     * `textDocument/codeAction` request.
     *
     * @since 3.16.0
     */
    val dataSupport: Boolean? = null
)

@Serializable
data class PublishDiagnosticsClientCapabilitiesTagSupport(
    /**
     * The tags supported by the client.
     */
    val valueSet: List<DiagnosticTag>
)
