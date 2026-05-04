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
 * Completion parameters
 */
@Serializable
data class CompletionParams(
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
     * An optional token that a server can use to report partial results (e.g. streaming) to
     * the client.
     */
    val partialResultToken: ProgressToken? = null,
    /**
     * The completion context. This is only available if the client specifies
     * to send this using the client capability `textDocument.completion.contextSupport === true`
     */
    val context: CompletionContext? = null
)

/**
 * A completion item represents a text snippet that is
 * proposed to complete text that is being typed.
 */
@Serializable
data class CompletionItem(
    /**
     * The label of this completion item.
     *
     * The label property is also by default the text that
     * is inserted when selecting this completion.
     *
     * If label details are provided the label itself should
     * be an unqualified name of the completion item.
     */
    val label: String,
    /**
     * Additional details for the label
     *
     * @since 3.17.0
     */
    val labelDetails: CompletionItemLabelDetails? = null,
    /**
     * The kind of this completion item. Based of the kind
     * an icon is chosen by the editor.
     */
    val kind: CompletionItemKind? = null,
    /**
     * Tags for this completion item.
     *
     * @since 3.15.0
     */
    val tags: List<CompletionItemTag>? = null,
    /**
     * A human-readable string with additional information
     * about this item, like type or symbol information.
     */
    val detail: String? = null,
    /**
     * A human-readable string that represents a doc-comment.
     */
    val documentation: StringOr<MarkupContent>? = null,
    /**
     * Indicates if this item is deprecated.
     * @deprecated Use `tags` instead.
     */
    val deprecated: Boolean? = null,
    /**
     * Select this item when showing.
     *
     * *Note* that only one completion item can be selected and that the
     * tool / client decides which item that is. The rule is that the *first*
     * item of those that match best is selected.
     */
    val preselect: Boolean? = null,
    /**
     * A string that should be used when comparing this item
     * with other items. When `falsy` the {@link CompletionItem.label label}
     * is used.
     */
    val sortText: String? = null,
    /**
     * A string that should be used when filtering a set of
     * completion items. When `falsy` the {@link CompletionItem.label label}
     * is used.
     */
    val filterText: String? = null,
    /**
     * A string that should be inserted into a document when selecting
     * this completion. When `falsy` the {@link CompletionItem.label label}
     * is used.
     *
     * The `insertText` is subject to interpretation by the client side.
     * Some tools might not take the string literally. For example
     * VS Code when code complete is requested in this example
     * `con<cursor position>` and a completion item with an `insertText` of
     * `console` is provided it will only insert `sole`. Therefore it is
     * recommended to use `textEdit` instead since it avoids additional client
     * side interpretation.
     */
    val insertText: String? = null,
    /**
     * The format of the insert text. The format applies to both the
     * `insertText` property and the `newText` property of a provided
     * `textEdit`. If omitted defaults to `InsertTextFormat.PlainText`.
     *
     * Please note that the insertTextFormat doesn't apply to
     * `additionalTextEdits`.
     */
    val insertTextFormat: InsertTextFormat? = null,
    /**
     * How whitespace and indentation is handled during completion
     * item insertion. If not provided the clients default value depends on
     * the `textDocument.completion.insertTextMode` client capability.
     *
     * @since 3.16.0
     */
    val insertTextMode: InsertTextMode? = null,
    /**
     * An {@link TextEdit edit} which is applied to a document when selecting
     * this completion. When an edit is provided the value of
     * {@link CompletionItem.insertText insertText} is ignored.
     *
     * Most editors support two different operations when accepting a completion
     * item. One is to insert a completion text and the other is to replace an
     * existing text with a completion text. Since this can usually not be
     * predetermined by a server it can report both ranges. Clients need to
     * signal support for `InsertReplaceEdits` via the
     * `textDocument.completion.insertReplaceSupport` client capability
     * property.
     *
     * *Note 1:* The text edit's range as well as both ranges from an insert
     * replace edit must be a [single line] and they must contain the position
     * at which completion has been requested.
     * *Note 2:* If an `InsertReplaceEdit` is returned the edit's insert range
     * must be a prefix of the edit's replace range, that means it must be
     * contained and starting at the same position.
     *
     * @since 3.16.0 additional type `InsertReplaceEdit`
     */
    val textEdit: CompletionItemTextEdit? = null,
    /**
     * The edit text used if the completion item is part of a CompletionList and
     * CompletionList defines an item default for the text edit range.
     *
     * Clients will only honor this property if they opt into completion list
     * item defaults using the capability `completionList.itemDefaults`.
     *
     * If not provided and a list's default range is provided the label
     * property is used as a text.
     *
     * @since 3.17.0
     */
    val textEditText: String? = null,
    /**
     * An optional array of additional {@link TextEdit text edits} that are applied when
     * selecting this completion. Edits must not overlap (including the same insert position)
     * with the main {@link CompletionItem.textEdit edit} nor with themselves.
     *
     * Additional text edits should be used to change text unrelated to the current cursor position
     * (for example adding an import statement at the top of the file if the completion item will
     * insert an unqualified type).
     */
    val additionalTextEdits: List<TextEdit>? = null,
    /**
     * An optional set of characters that when pressed while this completion is active will accept it first and
     * then type that character. *Note* that all commit characters should have `length=1` and that superfluous
     * characters will be ignored.
     */
    val commitCharacters: List<String>? = null,
    /**
     * An optional {@link Command command} that is executed *after* inserting this completion. *Note* that
     * additional modifications to the current document should be described with the
     * {@link CompletionItem.additionalTextEdits additionalTextEdits}-property.
     */
    val command: Command? = null,
    /**
     * A data entry field that is preserved on a completion item between a
     * {@link CompletionRequest} and a {@link CompletionResolveRequest}.
     */
    @SerialName("data") val `data`: LSPAny? = null
)

/**
 * Represents a collection of {@link CompletionItem completion items} to be presented
 * in the editor.
 */
@Serializable
data class CompletionList(
    /**
     * This list it not complete. Further typing results in recomputing this list.
     *
     * Recomputed lists have all their items replaced (not appended) in the
     * incomplete completion sessions.
     */
    val isIncomplete: Boolean,
    /**
     * In many cases the items of an actual completion result share the same
     * value for properties like `commitCharacters` or the range of a text
     * edit. A completion list can therefore define item defaults which will
     * be used if a completion item itself doesn't specify the value.
     *
     * If a completion list specifies a default value and a completion item
     * also specifies a corresponding value the one from the item is used.
     *
     * Servers are only allowed to return default values if the client
     * signals support for this via the `completionList.itemDefaults`
     * capability.
     *
     * @since 3.17.0
     */
    val itemDefaults: CompletionListItemDefaults? = null,
    /**
     * The completion items.
     */
    val items: List<CompletionItem>
)

@Serializable
data class CompletionListItemDefaults(
    /**
     * A default commit character set.
     *
     * @since 3.17.0
     */
    val commitCharacters: List<String>? = null,
    /**
     * A default edit range.
     *
     * @since 3.17.0
     */
    val editRange: JsonElement? = null,
    /**
     * A default insert text format.
     *
     * @since 3.17.0
     */
    val insertTextFormat: InsertTextFormat? = null,
    /**
     * A default insert text mode.
     *
     * @since 3.17.0
     */
    val insertTextMode: InsertTextMode? = null,
    /**
     * A default data value.
     *
     * @since 3.17.0
     */
    @SerialName("data") val `data`: LSPAny? = null
)

/**
 * Registration options for a {@link CompletionRequest}.
 */
@Serializable
data class CompletionRegistrationOptions(
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector?,
    val workDoneProgress: Boolean? = null,
    /**
     * Most tools trigger completion request automatically without explicitly requesting
     * it using a keyboard shortcut (e.g. Ctrl+Space). Typically they do so when the user
     * starts to type an identifier. For example if the user types `c` in a JavaScript file
     * code complete will automatically pop up present `console` besides others as a
     * completion item. Characters that make up identifiers don't need to be listed here.
     *
     * If code complete should automatically be trigger on characters not being valid inside
     * an identifier (for example `.` in JavaScript) list them in `triggerCharacters`.
     */
    val triggerCharacters: List<String>? = null,
    /**
     * The list of all possible characters that commit a completion. This field can be used
     * if clients don't support individual commit characters per completion item. See
     * `ClientCapabilities.textDocument.completion.completionItem.commitCharactersSupport`
     *
     * If a server provides both `allCommitCharacters` and commit characters on an individual
     * completion item the ones on the completion item win.
     *
     * @since 3.2.0
     */
    val allCommitCharacters: List<String>? = null,
    /**
     * The server provides support to resolve additional
     * information for a completion item.
     */
    val resolveProvider: Boolean? = null,
    /**
     * The server supports the following `CompletionItem` specific
     * capabilities.
     *
     * @since 3.17.0
     */
    val completionItem: CompletionRegistrationOptionsCompletionItem? = null
)

@Serializable
data class CompletionRegistrationOptionsCompletionItem(
    /**
     * The server has support for completion item label
     * details (see also `CompletionItemLabelDetails`) when
     * receiving a completion item in a resolve call.
     *
     * @since 3.17.0
     */
    val labelDetailsSupport: Boolean? = null
)

/**
 * Contains additional information about the context in which a completion request is triggered.
 */
@Serializable
data class CompletionContext(
    /**
     * How the completion was triggered.
     */
    val triggerKind: CompletionTriggerKind,
    /**
     * The trigger character (a single character) that has trigger code complete.
     * Is undefined if `triggerKind !== CompletionTriggerKind.TriggerCharacter`
     */
    val triggerCharacter: String? = null
)

/**
 * Additional details for a completion item label.
 *
 * @since 3.17.0
 */
@Serializable
data class CompletionItemLabelDetails(
    /**
     * An optional string which is rendered less prominently directly after {@link CompletionItem.label label},
     * without any spacing. Should be used for function signatures and type annotations.
     */
    val detail: String? = null,
    /**
     * An optional string which is rendered less prominently after {@link CompletionItem.detail}. Should be used
     * for fully qualified names and file paths.
     */
    val description: String? = null
)

/**
 * Completion options.
 */
@Serializable
data class CompletionOptions(
    val workDoneProgress: Boolean? = null,
    /**
     * Most tools trigger completion request automatically without explicitly requesting
     * it using a keyboard shortcut (e.g. Ctrl+Space). Typically they do so when the user
     * starts to type an identifier. For example if the user types `c` in a JavaScript file
     * code complete will automatically pop up present `console` besides others as a
     * completion item. Characters that make up identifiers don't need to be listed here.
     *
     * If code complete should automatically be trigger on characters not being valid inside
     * an identifier (for example `.` in JavaScript) list them in `triggerCharacters`.
     */
    val triggerCharacters: List<String>? = null,
    /**
     * The list of all possible characters that commit a completion. This field can be used
     * if clients don't support individual commit characters per completion item. See
     * `ClientCapabilities.textDocument.completion.completionItem.commitCharactersSupport`
     *
     * If a server provides both `allCommitCharacters` and commit characters on an individual
     * completion item the ones on the completion item win.
     *
     * @since 3.2.0
     */
    val allCommitCharacters: List<String>? = null,
    /**
     * The server provides support to resolve additional
     * information for a completion item.
     */
    val resolveProvider: Boolean? = null,
    /**
     * The server supports the following `CompletionItem` specific
     * capabilities.
     *
     * @since 3.17.0
     */
    val completionItem: CompletionOptionsCompletionItem? = null
)

@Serializable
data class CompletionOptionsCompletionItem(
    /**
     * The server has support for completion item label
     * details (see also `CompletionItemLabelDetails`) when
     * receiving a completion item in a resolve call.
     *
     * @since 3.17.0
     */
    val labelDetailsSupport: Boolean? = null
)

/**
 * Completion client capabilities
 */
@Serializable
data class CompletionClientCapabilities(
    /**
     * Whether completion supports dynamic registration.
     */
    val dynamicRegistration: Boolean? = null,
    /**
     * The client supports the following `CompletionItem` specific
     * capabilities.
     */
    val completionItem: CompletionClientCapabilitiesCompletionItem? = null,
    val completionItemKind: CompletionClientCapabilitiesCompletionItemKind? = null,
    /**
     * Defines how the client handles whitespace and indentation
     * when accepting a completion item that uses multi line
     * text in either `insertText` or `textEdit`.
     *
     * @since 3.17.0
     */
    val insertTextMode: InsertTextMode? = null,
    /**
     * The client supports to send additional context information for a
     * `textDocument/completion` request.
     */
    val contextSupport: Boolean? = null,
    /**
     * The client supports the following `CompletionList` specific
     * capabilities.
     *
     * @since 3.17.0
     */
    val completionList: CompletionClientCapabilitiesCompletionList? = null
)

@Serializable
data class CompletionClientCapabilitiesCompletionItem(
    /**
     * Client supports snippets as insert text.
     *
     * A snippet can define tab stops and placeholders with `$1`, `$2`
     * and `${3:foo}`. `$0` defines the final tab stop, it defaults to
     * the end of the snippet. Placeholders with equal identifiers are linked,
     * that is typing in one will update others too.
     */
    val snippetSupport: Boolean? = null,
    /**
     * Client supports commit characters on a completion item.
     */
    val commitCharactersSupport: Boolean? = null,
    /**
     * Client supports the following content formats for the documentation
     * property. The order describes the preferred format of the client.
     */
    val documentationFormat: List<MarkupKind>? = null,
    /**
     * Client supports the deprecated property on a completion item.
     */
    val deprecatedSupport: Boolean? = null,
    /**
     * Client supports the preselect property on a completion item.
     */
    val preselectSupport: Boolean? = null,
    /**
     * Client supports the tag property on a completion item. Clients supporting
     * tags have to handle unknown tags gracefully. Clients especially need to
     * preserve unknown tags when sending a completion item back to the server in
     * a resolve call.
     *
     * @since 3.15.0
     */
    val tagSupport: CompletionClientCapabilitiesCompletionItemTagSupport? = null,
    /**
     * Client support insert replace edit to control different behavior if a
     * completion item is inserted in the text or should replace text.
     *
     * @since 3.16.0
     */
    val insertReplaceSupport: Boolean? = null,
    /**
     * Indicates which properties a client can resolve lazily on a completion
     * item. Before version 3.16.0 only the predefined properties `documentation`
     * and `details` could be resolved lazily.
     *
     * @since 3.16.0
     */
    val resolveSupport: CompletionClientCapabilitiesCompletionItemResolveSupport? = null,
    /**
     * The client supports the `insertTextMode` property on
     * a completion item to override the whitespace handling mode
     * as defined by the client (see `insertTextMode`).
     *
     * @since 3.16.0
     */
    val insertTextModeSupport: CompletionClientCapabilitiesCompletionItemInsertTextModeSupport? = null,
    /**
     * The client has support for completion item label
     * details (see also `CompletionItemLabelDetails`).
     *
     * @since 3.17.0
     */
    val labelDetailsSupport: Boolean? = null
)

@Serializable
data class CompletionClientCapabilitiesCompletionItemInsertTextModeSupport(
    val valueSet: List<InsertTextMode>
)

@Serializable
data class CompletionClientCapabilitiesCompletionItemResolveSupport(
    /**
     * The properties that a client can resolve lazily.
     */
    val properties: List<String>
)

@Serializable
data class CompletionClientCapabilitiesCompletionItemTagSupport(
    /**
     * The tags supported by the client.
     */
    val valueSet: List<CompletionItemTag>
)

@Serializable
data class CompletionClientCapabilitiesCompletionItemKind(
    /**
     * The completion item kind values the client supports. When this
     * property exists the client also guarantees that it will
     * handle values outside its set gracefully and falls back
     * to a default value when unknown.
     *
     * If this property is not present the client only supports
     * the completion items kinds from `Text` to `Reference` as defined in
     * the initial version of the protocol.
     */
    val valueSet: List<CompletionItemKind>? = null
)

@Serializable
data class CompletionClientCapabilitiesCompletionList(
    /**
     * The client supports the following itemDefaults on
     * a completion list.
     *
     * The value lists the supported property names of the
     * `CompletionList.itemDefaults` object. If omitted
     * no properties are supported.
     *
     * @since 3.17.0
     */
    val itemDefaults: List<String>? = null
)
