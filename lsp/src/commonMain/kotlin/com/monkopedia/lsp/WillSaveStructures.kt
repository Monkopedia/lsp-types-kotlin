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
 * The parameters sent in a will save text document notification.
 */
@Serializable
data class WillSaveTextDocumentParams(
    /**
     * The document that will be saved.
     */
    val textDocument: TextDocumentIdentifier,
    /**
     * The 'TextDocumentSaveReason'.
     */
    val reason: TextDocumentSaveReason
)
