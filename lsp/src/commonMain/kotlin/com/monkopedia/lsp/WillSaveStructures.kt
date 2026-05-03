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
 * The parameters sent in a will save text document notification.
 */
@kotlinx.serialization.Serializable
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
