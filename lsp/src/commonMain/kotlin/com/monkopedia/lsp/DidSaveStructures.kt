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
 * The params sent in a save notebook document notification.
 *
 * @since 3.17.0
 */
@kotlinx.serialization.Serializable
data class DidSaveNotebookDocumentParams(
    /**
     * The notebook document that got saved.
     */
    val notebookDocument: NotebookDocumentIdentifier
)

/**
 * The parameters sent in a save text document notification
 */
@kotlinx.serialization.Serializable
data class DidSaveTextDocumentParams(
    /**
     * The document that was saved.
     */
    val textDocument: TextDocumentIdentifier,
    /**
     * Optional the content when saved. Depends on the includeText value
     * when the save notification was requested.
     */
    val text: String? = null
)
