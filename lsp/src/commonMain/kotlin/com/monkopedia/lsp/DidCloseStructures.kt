// Auto-generated from LSP metaModel.json — do not edit manually.
// Generator: lsp-codegen

@file:Suppress(
    "unused",
    "PropertyName",
    "ktlint:standard:class-naming",
    "ktlint:standard:filename",
    "ktlint:standard:max-line-length"
)

package com.monkopedia.lsp

/**
 * The params sent in a close notebook document notification.
 *
 * @since 3.17.0
 */
@kotlinx.serialization.Serializable
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
@kotlinx.serialization.Serializable
data class DidCloseTextDocumentParams(
    /**
     * The document that was closed.
     */
    val textDocument: TextDocumentIdentifier
)
