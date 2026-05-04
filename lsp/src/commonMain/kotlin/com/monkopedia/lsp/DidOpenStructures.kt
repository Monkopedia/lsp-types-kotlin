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
 * The params sent in an open notebook document notification.
 *
 * @since 3.17.0
 */
@Serializable
data class DidOpenNotebookDocumentParams(
    /**
     * The notebook document that got opened.
     */
    val notebookDocument: NotebookDocument,
    /**
     * The text documents that represent the content
     * of a notebook cell.
     */
    val cellTextDocuments: List<TextDocumentItem>
)

/**
 * The parameters sent in an open text document notification
 */
@Serializable
data class DidOpenTextDocumentParams(
    /**
     * The document that was opened.
     */
    val textDocument: TextDocumentItem
)
