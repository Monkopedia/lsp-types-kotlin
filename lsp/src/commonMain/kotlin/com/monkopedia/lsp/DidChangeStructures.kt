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
 * The parameters of a `workspace/didChangeWorkspaceFolders` notification.
 */
@Serializable
data class DidChangeWorkspaceFoldersParams(
    /**
     * The actual workspace folder change event.
     */
    val event: WorkspaceFoldersChangeEvent
)

/**
 * The params sent in a change notebook document notification.
 *
 * @since 3.17.0
 */
@Serializable
data class DidChangeNotebookDocumentParams(
    /**
     * The notebook document that did change. The version number points
     * to the version after all provided changes have been applied. If
     * only the text document content of a cell changes the notebook version
     * doesn't necessarily have to change.
     */
    val notebookDocument: VersionedNotebookDocumentIdentifier,
    /**
     * The actual changes to the notebook document.
     *
     * The changes describe single state changes to the notebook document.
     * So if there are two changes c1 (at array index 0) and c2 (at array
     * index 1) for a notebook in state S then c1 moves the notebook from
     * S to S' and c2 from S' to S''. So c1 is computed on the state S and
     * c2 is computed on the state S'.
     *
     * To mirror the content of a notebook using change events use the following approach:
     * - start with the same initial content
     * - apply the 'notebookDocument/didChange' notifications in the order you receive them.
     * - apply the `NotebookChangeEvent`s in a single notification in the order
     *   you receive them.
     */
    val change: NotebookDocumentChangeEvent
)

/**
 * The parameters of a change configuration notification.
 */
@Serializable
data class DidChangeConfigurationParams(
    /**
     * The actual changed settings
     */
    val settings: LSPAny
)

@Serializable
data class DidChangeConfigurationRegistrationOptions(val section: JsonElement? = null)

/**
 * The change text document notification's parameters.
 */
@Serializable
data class DidChangeTextDocumentParams(
    /**
     * The document that did change. The version number points
     * to the version after all provided content changes have
     * been applied.
     */
    val textDocument: VersionedTextDocumentIdentifier,
    /**
     * The actual content changes. The content changes describe single state changes
     * to the document. So if there are two content changes c1 (at array index 0) and
     * c2 (at array index 1) for a document in state S then c1 moves the document from
     * S to S' and c2 from S' to S''. So c1 is computed on the state S and c2 is computed
     * on the state S'.
     *
     * To mirror the content of a document using change events use the following approach:
     * - start with the same initial content
     * - apply the 'textDocument/didChange' notifications in the order you receive them.
     * - apply the `TextDocumentContentChangeEvent`s in a single notification in the order
     *   you receive them.
     */
    val contentChanges: List<TextDocumentContentChangeEvent>
)

/**
 * The watched files change notification's parameters.
 */
@Serializable
data class DidChangeWatchedFilesParams(
    /**
     * The actual file events.
     */
    val changes: List<FileEvent>
)

/**
 * Describe options to be used when registered for text document change events.
 */
@Serializable
data class DidChangeWatchedFilesRegistrationOptions(
    /**
     * The watchers to register.
     */
    val watchers: List<FileSystemWatcher>
)

@Serializable
data class DidChangeConfigurationClientCapabilities(
    /**
     * Did change configuration notification supports dynamic registration.
     */
    val dynamicRegistration: Boolean? = null
)

@Serializable
data class DidChangeWatchedFilesClientCapabilities(
    /**
     * Did change watched files notification supports dynamic registration. Please note
     * that the current protocol doesn't support static configuration for file changes
     * from the server side.
     */
    val dynamicRegistration: Boolean? = null,
    /**
     * Whether the client has support for {@link  RelativePattern relative pattern}
     * or not.
     *
     * @since 3.17.0
     */
    val relativePatternSupport: Boolean? = null
)
