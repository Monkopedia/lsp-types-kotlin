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
 * A notebook document.
 *
 * @since 3.17.0
 */
@Serializable
data class NotebookDocument(
    /**
     * The notebook document's uri.
     */
    val uri: URI,
    /**
     * The type of the notebook.
     */
    val notebookType: String,
    /**
     * The version number of this document (it will increase after each
     * change, including undo/redo).
     */
    val version: Int,
    /**
     * Additional metadata stored with the notebook
     * document.
     *
     * Note: should always be an object literal (e.g. LSPObject)
     */
    val metadata: LSPObject? = null,
    /**
     * The cells of a notebook.
     */
    val cells: List<NotebookCell>
)

/**
 * A change event for a notebook document.
 *
 * @since 3.17.0
 */
@Serializable
data class NotebookDocumentChangeEvent(
    /**
     * The changed meta data if any.
     *
     * Note: should always be an object literal (e.g. LSPObject)
     */
    val metadata: LSPObject? = null,
    /**
     * Changes to cells
     */
    val cells: NotebookDocumentChangeEventCells? = null
)

@Serializable
data class NotebookDocumentChangeEventCells(
    /**
     * Changes to the cell structure to add or
     * remove cells.
     */
    val structure: NotebookDocumentChangeEventCellsStructure? = null,
    /**
     * Changes to notebook cells properties like its
     * kind, execution summary or metadata.
     */
    @SerialName("data") val `data`: List<NotebookCell>? = null,
    /**
     * Changes to the text content of notebook cells.
     */
    val textContent: List<NotebookDocumentChangeEventCellsTextContent>? = null
)

@Serializable
data class NotebookDocumentChangeEventCellsStructure(
    /**
     * The change to the cell array.
     */
    val array: NotebookCellArrayChange,
    /**
     * Additional opened cell text documents.
     */
    val didOpen: List<TextDocumentItem>? = null,
    /**
     * Additional closed cell text documents.
     */
    val didClose: List<TextDocumentIdentifier>? = null
)

@Serializable
data class NotebookDocumentChangeEventCellsTextContent(
    val document: VersionedTextDocumentIdentifier,
    val changes: List<TextDocumentContentChangeEvent>
)

/**
 * A literal to identify a notebook document in the client.
 *
 * @since 3.17.0
 */
@Serializable
data class NotebookDocumentIdentifier(
    /**
     * The notebook document's uri.
     */
    val uri: URI
)

/**
 * A notebook cell.
 *
 * A cell's document URI must be unique across ALL notebook
 * cells and can therefore be used to uniquely identify a
 * notebook cell or the cell's text document.
 *
 * @since 3.17.0
 */
@Serializable
data class NotebookCell(
    /**
     * The cell's kind
     */
    val kind: NotebookCellKind,
    /**
     * The URI of the cell's text document
     * content.
     */
    val document: DocumentUri,
    /**
     * Additional metadata stored with the cell.
     *
     * Note: should always be an object literal (e.g. LSPObject)
     */
    val metadata: LSPObject? = null,
    /**
     * Additional execution summary information
     * if supported by the client.
     */
    val executionSummary: ExecutionSummary? = null
)

/**
 * A change describing how to move a `NotebookCell`
 * array from state S to S'.
 *
 * @since 3.17.0
 */
@Serializable
data class NotebookCellArrayChange(
    /**
     * The start oftest of the cell that changed.
     */
    val start: UInt,
    /**
     * The deleted cells
     */
    val deleteCount: UInt,
    /**
     * The new cells, if any
     */
    val cells: List<NotebookCell>? = null
)

/**
 * Options specific to a notebook plus its cells
 * to be synced to the server.
 *
 * If a selector provides a notebook document
 * filter but no cell selector all cells of a
 * matching notebook document will be synced.
 *
 * If a selector provides no notebook document
 * filter but only a cell selector all notebook
 * document that contain at least one matching
 * cell will be synced.
 *
 * @since 3.17.0
 */
@Serializable
data class NotebookDocumentSyncOptions(
    /**
     * The notebooks to be synced
     */
    val notebookSelector: List<NotebookDocumentSyncOptionsNotebookSelector>,
    /**
     * Whether save notification should be forwarded to
     * the server. Will only be honored if mode === `notebook`.
     */
    val save: Boolean? = null
) : ServerCapabilitiesNotebookDocumentSync

/**
 * Registration options specific to a notebook.
 *
 * @since 3.17.0
 */
@Serializable
data class NotebookDocumentSyncRegistrationOptions(
    /**
     * The notebooks to be synced
     */
    val notebookSelector: List<NotebookDocumentSyncRegistrationOptionsNotebookSelector>,
    /**
     * Whether save notification should be forwarded to
     * the server. Will only be honored if mode === `notebook`.
     */
    val save: Boolean? = null,
    /**
     * The id used to register the request. The id can be used to deregister
     * the request again. See also Registration#id.
     */
    val id: String? = null
) : ServerCapabilitiesNotebookDocumentSync

/**
 * A notebook cell text document filter denotes a cell text
 * document by different properties.
 *
 * @since 3.17.0
 */
@Serializable
data class NotebookCellTextDocumentFilter(
    /**
     * A filter that matches against the notebook
     * containing the notebook cell. If a string
     * value is provided it matches against the
     * notebook type. '*' matches every notebook.
     */
    val notebook: JsonElement,
    /**
     * A language id like `python`.
     *
     * Will be matched against the language id of the
     * notebook cell document. '*' matches every language.
     */
    val language: String? = null
)

/**
 * Capabilities specific to the notebook document support.
 *
 * @since 3.17.0
 */
@Serializable
data class NotebookDocumentClientCapabilities(
    /**
     * Capabilities specific to notebook document synchronization
     *
     * @since 3.17.0
     */
    val synchronization: NotebookDocumentSyncClientCapabilities
)

/**
 * Notebook specific client capabilities.
 *
 * @since 3.17.0
 */
@Serializable
data class NotebookDocumentSyncClientCapabilities(
    /**
     * Whether implementation supports dynamic registration. If this is
     * set to `true` the client supports the new
     * `(TextDocumentRegistrationOptions & StaticRegistrationOptions)`
     * return value for the corresponding server capability as well.
     */
    val dynamicRegistration: Boolean? = null,
    /**
     * The client supports sending execution summary data per cell.
     */
    val executionSummarySupport: Boolean? = null
)
