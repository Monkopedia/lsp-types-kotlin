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

@kotlinx.serialization.Serializable
data class NotebookDocumentSyncOptionsNotebookSelectorNotebook(
    /**
     * The notebook to be synced If a string
     * value is provided it matches against the
     * notebook type. '*' matches every notebook.
     */
    val notebook: kotlinx.serialization.json.JsonElement,
    /**
     * The cells of the matching notebook to be synced.
     */
    val cells: List<NotebookDocumentSyncOptionsNotebookSelectorNotebookCells>? = null
) : NotebookDocumentSyncOptionsNotebookSelector

@kotlinx.serialization.Serializable
data class NotebookDocumentSyncOptionsNotebookSelectorNotebookCells(val language: String)

@kotlinx.serialization.Serializable
data class NotebookDocumentSyncOptionsNotebookSelectorCells(
    /**
     * The notebook to be synced If a string
     * value is provided it matches against the
     * notebook type. '*' matches every notebook.
     */
    val notebook: kotlinx.serialization.json.JsonElement? = null,
    /**
     * The cells of the matching notebook to be synced.
     */
    val cells: List<NotebookDocumentSyncOptionsNotebookSelectorCellsCells>
) : NotebookDocumentSyncOptionsNotebookSelector

@kotlinx.serialization.Serializable
data class NotebookDocumentSyncOptionsNotebookSelectorCellsCells(val language: String)

@kotlinx.serialization.Serializable
data class TextDocumentContentChangeEventRange(
    /**
     * The range of the document that changed.
     */
    val range: Range,
    /**
     * The optional length of the range that got replaced.
     *
     * @deprecated use range instead.
     */
    val rangeLength: UInt? = null,
    /**
     * The new text for the provided range.
     */
    val text: String
) : TextDocumentContentChangeEvent

@kotlinx.serialization.Serializable
data class TextDocumentContentChangeEventVariant(
    /**
     * The new text of the whole document.
     */
    val text: String
) : TextDocumentContentChangeEvent

@kotlinx.serialization.Serializable
data class TextDocumentFilterLanguage(
    /**
     * A language id, like `typescript`.
     */
    val language: String,
    /**
     * A Uri {@link Uri.scheme scheme}, like `file` or `untitled`.
     */
    val scheme: String? = null,
    /**
     * A glob pattern, like **​/&#42;.{ts,js}. See TextDocumentFilter for examples.
     */
    val pattern: String? = null
) : TextDocumentFilter

@kotlinx.serialization.Serializable
data class TextDocumentFilterScheme(
    /**
     * A language id, like `typescript`.
     */
    val language: String? = null,
    /**
     * A Uri {@link Uri.scheme scheme}, like `file` or `untitled`.
     */
    val scheme: String,
    /**
     * A glob pattern, like **​/&#42;.{ts,js}. See TextDocumentFilter for examples.
     */
    val pattern: String? = null
) : TextDocumentFilter

@kotlinx.serialization.Serializable
data class TextDocumentFilterPattern(
    /**
     * A language id, like `typescript`.
     */
    val language: String? = null,
    /**
     * A Uri {@link Uri.scheme scheme}, like `file` or `untitled`.
     */
    val scheme: String? = null,
    /**
     * A glob pattern, like **​/&#42;.{ts,js}. See TextDocumentFilter for examples.
     */
    val pattern: String
) : TextDocumentFilter

@kotlinx.serialization.Serializable
data class NotebookDocumentFilterNotebookType(
    /**
     * The type of the enclosing notebook.
     */
    val notebookType: String,
    /**
     * A Uri {@link Uri.scheme scheme}, like `file` or `untitled`.
     */
    val scheme: String? = null,
    /**
     * A glob pattern.
     */
    val pattern: String? = null
) : NotebookDocumentFilter

@kotlinx.serialization.Serializable
data class NotebookDocumentFilterScheme(
    /**
     * The type of the enclosing notebook.
     */
    val notebookType: String? = null,
    /**
     * A Uri {@link Uri.scheme scheme}, like `file` or `untitled`.
     */
    val scheme: String,
    /**
     * A glob pattern.
     */
    val pattern: String? = null
) : NotebookDocumentFilter

@kotlinx.serialization.Serializable
data class NotebookDocumentFilterPattern(
    /**
     * The type of the enclosing notebook.
     */
    val notebookType: String? = null,
    /**
     * A Uri {@link Uri.scheme scheme}, like `file` or `untitled`.
     */
    val scheme: String? = null,
    /**
     * A glob pattern.
     */
    val pattern: String
) : NotebookDocumentFilter

@kotlinx.serialization.Serializable
data class NotebookDocumentSyncRegistrationOptionsNotebookSelectorNotebook(
    /**
     * The notebook to be synced If a string
     * value is provided it matches against the
     * notebook type. '*' matches every notebook.
     */
    val notebook: kotlinx.serialization.json.JsonElement,
    /**
     * The cells of the matching notebook to be synced.
     */
    val cells: List<NotebookDocumentSyncRegistrationOptionsNotebookSelectorNotebookCells>? = null
) : NotebookDocumentSyncRegistrationOptionsNotebookSelector

@kotlinx.serialization.Serializable
data class NotebookDocumentSyncRegistrationOptionsNotebookSelectorNotebookCells(
    val language: String
)

@kotlinx.serialization.Serializable
data class NotebookDocumentSyncRegistrationOptionsNotebookSelectorCells(
    /**
     * The notebook to be synced If a string
     * value is provided it matches against the
     * notebook type. '*' matches every notebook.
     */
    val notebook: kotlinx.serialization.json.JsonElement? = null,
    /**
     * The cells of the matching notebook to be synced.
     */
    val cells: List<NotebookDocumentSyncRegistrationOptionsNotebookSelectorCellsCells>
) : NotebookDocumentSyncRegistrationOptionsNotebookSelector

@kotlinx.serialization.Serializable
data class NotebookDocumentSyncRegistrationOptionsNotebookSelectorCellsCells(val language: String)
