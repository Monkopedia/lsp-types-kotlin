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
 * The params sent in a close notebook document notification.
 *
 * @since 3.17.0
 */
@Serializable
data class DidCloseNotebookDocumentParams(
    /**
     * The notebook document that got closed.
     */
    val notebookDocument: NotebookDocumentIdentifier,
    /**
     * The text documents that represent the content
     * of a notebook cell that got closed.
     */
    val cellTextDocuments: List<TextDocumentIdentifier>
)

/**
 * The parameters sent in a close text document notification
 */
@Serializable
data class DidCloseTextDocumentParams(
    /**
     * The document that was closed.
     */
    val textDocument: TextDocumentIdentifier
)
