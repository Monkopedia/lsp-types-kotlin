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
data class WindowClientCapabilities(
    /**
     * It indicates whether the client supports server initiated
     * progress using the `window/workDoneProgress/create` request.
     *
     * The capability also controls Whether client supports handling
     * of progress notifications. If set servers are allowed to report a
     * `workDoneProgress` property in the request specific server
     * capabilities.
     *
     * @since 3.15.0
     */
    val workDoneProgress: Boolean? = null,
    /**
     * Capabilities specific to the showMessage request.
     *
     * @since 3.16.0
     */
    val showMessage: ShowMessageRequestClientCapabilities? = null,
    /**
     * Capabilities specific to the showDocument request.
     *
     * @since 3.16.0
     */
    val showDocument: ShowDocumentClientCapabilities? = null
)
