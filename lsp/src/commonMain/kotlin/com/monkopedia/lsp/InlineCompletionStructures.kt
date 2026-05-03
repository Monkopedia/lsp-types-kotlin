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
 * A parameter literal used in inline completion requests.
 *
 * @since 3.18.0
 * @proposed
 */
@kotlinx.serialization.Serializable
data class InlineCompletionParams(
    /**
     * The text document.
     */
    val textDocument: TextDocumentIdentifier,
    /**
     * The position inside the text document.
     */
    val position: Position,
    /**
     * An optional token that a server can use to report work done progress.
     */
    val workDoneToken: ProgressToken? = null,
    /**
     * Additional information about the context in which inline completions were
     * requested.
     */
    val context: InlineCompletionContext
)

/**
 * Represents a collection of {@link InlineCompletionItem inline completion items} to be presented in the editor.
 *
 * @since 3.18.0
 * @proposed
 */
@kotlinx.serialization.Serializable
data class InlineCompletionList(
    /**
     * The inline completion items
     */
    val items: List<InlineCompletionItem>
)

/**
 * An inline completion item represents a text snippet that is proposed inline to complete text that is being typed.
 *
 * @since 3.18.0
 * @proposed
 */
@kotlinx.serialization.Serializable
data class InlineCompletionItem(
    /**
     * The text to replace the range with. Must be set.
     */
    val insertText: kotlinx.serialization.json.JsonElement,
    /**
     * A text that is used to decide if this inline completion should be shown. When `falsy` the {@link InlineCompletionItem.insertText} is used.
     */
    val filterText: String? = null,
    /**
     * The range to replace. Must begin and end on the same line.
     */
    val range: Range? = null,
    /**
     * An optional {@link Command} that is executed *after* inserting this completion.
     */
    val command: Command? = null
)

/**
 * Inline completion options used during static or dynamic registration.
 *
 * @since 3.18.0
 * @proposed
 */
@kotlinx.serialization.Serializable
data class InlineCompletionRegistrationOptions(
    val workDoneProgress: Boolean? = null,
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector??,
    /**
     * The id used to register the request. The id can be used to deregister
     * the request again. See also Registration#id.
     */
    val id: String? = null
)

/**
 * Provides information about the context in which an inline completion was requested.
 *
 * @since 3.18.0
 * @proposed
 */
@kotlinx.serialization.Serializable
data class InlineCompletionContext(
    /**
     * Describes how the inline completion was triggered.
     */
    val triggerKind: InlineCompletionTriggerKind,
    /**
     * Provides information about the currently selected item in the autocomplete widget if it is visible.
     */
    val selectedCompletionInfo: SelectedCompletionInfo? = null
)

/**
 * Inline completion options used during static registration.
 *
 * @since 3.18.0
 * @proposed
 */
@kotlinx.serialization.Serializable
data class InlineCompletionOptions(val workDoneProgress: Boolean? = null)

/**
 * Client capabilities specific to inline completions.
 *
 * @since 3.18.0
 * @proposed
 */
@kotlinx.serialization.Serializable
data class InlineCompletionClientCapabilities(
    /**
     * Whether implementation supports dynamic registration for inline completion providers.
     */
    val dynamicRegistration: Boolean? = null
)
