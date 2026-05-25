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
 * The parameters of a configuration request.
 */
@Serializable
data class ConfigurationParams(val items: List<ConfigurationItem>)

/**
 * Represents a color range from a document.
 */
@Serializable
data class ColorInformation(
    /**
     * The range in the document where this color appears.
     */
    val range: Range,
    /**
     * The actual color value for this color range.
     */
    val color: Color
)

/**
 * Parameters for a {@link ColorPresentationRequest}.
 */
@Serializable
data class ColorPresentationParams(
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
     * The text document.
     */
    val textDocument: TextDocumentIdentifier,
    /**
     * The color to request presentations for.
     */
    val color: Color,
    /**
     * The range where the color would be inserted. Serves as a context.
     */
    val range: Range
)

@Serializable
data class ColorPresentation(
    /**
     * The label of this color presentation. It will be shown on the color
     * picker header. By default this is also the text that is inserted when selecting
     * this color presentation.
     */
    val label: String,
    /**
     * An {@link TextEdit edit} which is applied to a document when selecting
     * this presentation for the color.  When `falsy` the {@link ColorPresentation.label label}
     * is used.
     */
    val textEdit: TextEdit? = null,
    /**
     * An optional array of additional {@link TextEdit text edits} that are applied when
     * selecting this color presentation. Edits must not overlap with the main {@link ColorPresentation.textEdit edit} nor with themselves.
     */
    val additionalTextEdits: List<TextEdit>? = null
)

/**
 * The parameter of a `textDocument/prepareCallHierarchy` request.
 *
 * @since 3.16.0
 */
@Serializable
data class CallHierarchyPrepareParams(
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
    val workDoneToken: ProgressToken? = null
)

/**
 * Represents programming constructs like functions or constructors in the context
 * of call hierarchy.
 *
 * @since 3.16.0
 */
@Serializable
data class CallHierarchyItem(
    /**
     * The name of this item.
     */
    val name: String,
    /**
     * The kind of this item.
     */
    val kind: SymbolKind,
    /**
     * Tags for this item.
     */
    val tags: List<SymbolTag>? = null,
    /**
     * More detail for this item, e.g. the signature of a function.
     */
    val detail: String? = null,
    /**
     * The resource identifier of this item.
     */
    val uri: DocumentUri,
    /**
     * The range enclosing this symbol not including leading/trailing whitespace but everything else, e.g. comments and code.
     */
    val range: Range,
    /**
     * The range that should be selected and revealed when this symbol is being picked, e.g. the name of a function.
     * Must be contained by the {@link CallHierarchyItem.range `range`}.
     */
    val selectionRange: Range,
    /**
     * A data entry field that is preserved between a call hierarchy prepare and
     * incoming calls or outgoing calls requests.
     */
    @SerialName("data") val `data`: LSPAny? = null
)

/**
 * Call hierarchy options used during static or dynamic registration.
 *
 * @since 3.16.0
 */
@Serializable
data class CallHierarchyRegistrationOptions(
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector?,
    val workDoneProgress: Boolean? = null,
    /**
     * The id used to register the request. The id can be used to deregister
     * the request again. See also Registration#id.
     */
    val id: String? = null
) : ServerCapabilitiesCallHierarchyProviderOptions

/**
 * The parameter of a `callHierarchy/incomingCalls` request.
 *
 * @since 3.16.0
 */
@Serializable
data class CallHierarchyIncomingCallsParams(
    /**
     * An optional token that a server can use to report work done progress.
     */
    val workDoneToken: ProgressToken? = null,
    /**
     * An optional token that a server can use to report partial results (e.g. streaming) to
     * the client.
     */
    val partialResultToken: ProgressToken? = null,
    val item: CallHierarchyItem
)

/**
 * Represents an incoming call, e.g. a caller of a method or constructor.
 *
 * @since 3.16.0
 */
@Serializable
data class CallHierarchyIncomingCall(
    /**
     * The item that makes the call.
     */
    val from: CallHierarchyItem,
    /**
     * The ranges at which the calls appear. This is relative to the caller
     * denoted by {@link CallHierarchyIncomingCall.from `this.from`}.
     */
    val fromRanges: List<Range>
)

/**
 * The parameter of a `callHierarchy/outgoingCalls` request.
 *
 * @since 3.16.0
 */
@Serializable
data class CallHierarchyOutgoingCallsParams(
    /**
     * An optional token that a server can use to report work done progress.
     */
    val workDoneToken: ProgressToken? = null,
    /**
     * An optional token that a server can use to report partial results (e.g. streaming) to
     * the client.
     */
    val partialResultToken: ProgressToken? = null,
    val item: CallHierarchyItem
)

/**
 * Represents an outgoing call, e.g. calling a getter from a method or a method from a constructor etc.
 *
 * @since 3.16.0
 */
@Serializable
data class CallHierarchyOutgoingCall(
    /**
     * The item that is called.
     */
    val to: CallHierarchyItem,
    /**
     * The range at which this item is called. This is the range relative to the caller, e.g the item
     * passed to {@link CallHierarchyItemProvider.provideCallHierarchyOutgoingCalls `provideCallHierarchyOutgoingCalls`}
     * and not {@link CallHierarchyOutgoingCall.to `this.to`}.
     */
    val fromRanges: List<Range>
)

/**
 * Params to show a resource in the UI.
 *
 * @since 3.16.0
 */
@Serializable
data class ShowDocumentParams(
    /**
     * The uri to show.
     */
    val uri: URI,
    /**
     * Indicates to show the resource in an external program.
     * To show, for example, `https://code.visualstudio.com/`
     * in the default WEB browser set `external` to `true`.
     */
    val external: Boolean? = null,
    /**
     * An optional property to indicate whether the editor
     * showing the document should take focus or not.
     * Clients might ignore this property if an external
     * program is started.
     */
    val takeFocus: Boolean? = null,
    /**
     * An optional selection range if the document is a text
     * document. Clients might ignore the property if an
     * external program is started or the file is not a text
     * file.
     */
    val selection: Range? = null
)

/**
 * The result of a showDocument request.
 *
 * @since 3.16.0
 */
@Serializable
data class ShowDocumentResult(
    /**
     * A boolean indicating if the show was successful.
     */
    val success: Boolean
)

@Serializable
data class LinkedEditingRangeParams(
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
    val workDoneToken: ProgressToken? = null
)

/**
 * The result of a linked editing range request.
 *
 * @since 3.16.0
 */
@Serializable
data class LinkedEditingRanges(
    /**
     * A list of ranges that can be edited together. The ranges must have
     * identical length and contain identical text content. The ranges cannot overlap.
     */
    val ranges: List<Range>,
    /**
     * An optional word pattern (regular expression) that describes valid contents for
     * the given ranges. If no pattern is provided, the client configuration's word
     * pattern will be used.
     */
    val wordPattern: String? = null
)

@Serializable
data class LinkedEditingRangeRegistrationOptions(
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector?,
    val workDoneProgress: Boolean? = null,
    /**
     * The id used to register the request. The id can be used to deregister
     * the request again. See also Registration#id.
     */
    val id: String? = null
) : ServerCapabilitiesLinkedEditingRangeProviderOptions

/**
 * The parameters sent in notifications/requests for user-initiated creation of
 * files.
 *
 * @since 3.16.0
 */
@Serializable
data class CreateFilesParams(
    /**
     * An array of all files/folders created in this operation.
     */
    val files: List<FileCreate>
)

/**
 * The parameters sent in notifications/requests for user-initiated renames of
 * files.
 *
 * @since 3.16.0
 */
@Serializable
data class RenameFilesParams(
    /**
     * An array of all files/folders renamed in this operation. When a folder is renamed, only
     * the folder will be included, and not its children.
     */
    val files: List<FileRename>
)

/**
 * The parameters sent in notifications/requests for user-initiated deletes of
 * files.
 *
 * @since 3.16.0
 */
@Serializable
data class DeleteFilesParams(
    /**
     * An array of all files/folders deleted in this operation.
     */
    val files: List<FileDelete>
)

/**
 * The parameter of a `textDocument/prepareTypeHierarchy` request.
 *
 * @since 3.17.0
 */
@Serializable
data class TypeHierarchyPrepareParams(
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
    val workDoneToken: ProgressToken? = null
)

/**
 * @since 3.17.0
 */
@Serializable
data class TypeHierarchyItem(
    /**
     * The name of this item.
     */
    val name: String,
    /**
     * The kind of this item.
     */
    val kind: SymbolKind,
    /**
     * Tags for this item.
     */
    val tags: List<SymbolTag>? = null,
    /**
     * More detail for this item, e.g. the signature of a function.
     */
    val detail: String? = null,
    /**
     * The resource identifier of this item.
     */
    val uri: DocumentUri,
    /**
     * The range enclosing this symbol not including leading/trailing whitespace
     * but everything else, e.g. comments and code.
     */
    val range: Range,
    /**
     * The range that should be selected and revealed when this symbol is being
     * picked, e.g. the name of a function. Must be contained by the
     * {@link TypeHierarchyItem.range `range`}.
     */
    val selectionRange: Range,
    /**
     * A data entry field that is preserved between a type hierarchy prepare and
     * supertypes or subtypes requests. It could also be used to identify the
     * type hierarchy in the server, helping improve the performance on
     * resolving supertypes and subtypes.
     */
    @SerialName("data") val `data`: LSPAny? = null
)

/**
 * Type hierarchy options used during static or dynamic registration.
 *
 * @since 3.17.0
 */
@Serializable
data class TypeHierarchyRegistrationOptions(
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector?,
    val workDoneProgress: Boolean? = null,
    /**
     * The id used to register the request. The id can be used to deregister
     * the request again. See also Registration#id.
     */
    val id: String? = null
) : ServerCapabilitiesTypeHierarchyProviderOptions

/**
 * The parameter of a `typeHierarchy/supertypes` request.
 *
 * @since 3.17.0
 */
@Serializable
data class TypeHierarchySupertypesParams(
    /**
     * An optional token that a server can use to report work done progress.
     */
    val workDoneToken: ProgressToken? = null,
    /**
     * An optional token that a server can use to report partial results (e.g. streaming) to
     * the client.
     */
    val partialResultToken: ProgressToken? = null,
    val item: TypeHierarchyItem
)

/**
 * The parameter of a `typeHierarchy/subtypes` request.
 *
 * @since 3.17.0
 */
@Serializable
data class TypeHierarchySubtypesParams(
    /**
     * An optional token that a server can use to report work done progress.
     */
    val workDoneToken: ProgressToken? = null,
    /**
     * An optional token that a server can use to report partial results (e.g. streaming) to
     * the client.
     */
    val partialResultToken: ProgressToken? = null,
    val item: TypeHierarchyItem
)

/**
 * Parameters of the document diagnostic request.
 *
 * @since 3.17.0
 */
@Serializable
data class DocumentDiagnosticParams(
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
     * The text document.
     */
    val textDocument: TextDocumentIdentifier,
    /**
     * The additional identifier  provided during registration.
     */
    val identifier: String? = null,
    /**
     * The result id of a previous response if provided.
     */
    val previousResultId: String? = null
)

/**
 * A partial result for a document diagnostic report.
 *
 * @since 3.17.0
 */
@Serializable
data class DocumentDiagnosticReportPartialResult(
    val relatedDocuments: Map<DocumentUri, DocumentDiagnosticReportPartialResultRelatedDocuments>
)

@Serializable
data class RegistrationParams(val registrations: List<Registration>)

@Serializable
data class UnregistrationParams(val unregisterations: List<Unregistration>)

@Serializable
data class InitializeParams(
    /**
     * An optional token that a server can use to report work done progress.
     */
    val workDoneToken: ProgressToken? = null,
    /**
     * The process Id of the parent process that started
     * the server.
     *
     * Is `null` if the process has not been started by another process.
     * If the parent process is not alive then the server should exit.
     */
    val processId: Int?,
    /**
     * Information about the client
     *
     * @since 3.15.0
     */
    val clientInfo: InitializeParamsClientInfo? = null,
    /**
     * The locale the client is currently showing the user interface
     * in. This must not necessarily be the locale of the operating
     * system.
     *
     * Uses IETF language tags as the value's syntax
     * (See https://en.wikipedia.org/wiki/IETF_language_tag)
     *
     * @since 3.16.0
     */
    val locale: String? = null,
    /**
     * The rootPath of the workspace. Is null
     * if no folder is open.
     *
     * @deprecated in favour of rootUri.
     */
    val rootPath: String? = null,
    /**
     * The rootUri of the workspace. Is null if no
     * folder is open. If both `rootPath` and `rootUri` are set
     * `rootUri` wins.
     *
     * @deprecated in favour of workspaceFolders.
     */
    val rootUri: DocumentUri?,
    /**
     * The capabilities provided by the client (editor or tool)
     */
    @EncodeDefault(
        EncodeDefault.Mode.ALWAYS
    ) val capabilities: ClientCapabilities = ClientCapabilities(),
    /**
     * User provided initialization options.
     */
    val initializationOptions: LSPAny? = null,
    /**
     * The initial trace setting. If omitted trace is disabled ('off').
     */
    val trace: TraceValues? = null,
    /**
     * The workspace folders configured in the client when the server starts.
     *
     * This property is only available if the client supports workspace folders.
     * It can be `null` if the client supports workspace folders but none are
     * configured.
     *
     * @since 3.6.0
     */
    val workspaceFolders: List<WorkspaceFolder>? = null
)

@Serializable
data class InitializeParamsClientInfo(
    /**
     * The name of the client as defined by the client.
     */
    val name: String,
    /**
     * The client's version as defined by the client.
     */
    val version: String? = null
)

/**
 * The result returned from an initialize request.
 */
@Serializable
data class InitializeResult(
    /**
     * The capabilities the language server provides.
     */
    @EncodeDefault(
        EncodeDefault.Mode.ALWAYS
    ) val capabilities: ServerCapabilities = ServerCapabilities(),
    /**
     * Information about the server.
     *
     * @since 3.15.0
     */
    val serverInfo: InitializeResultServerInfo? = null
)

@Serializable
data class InitializeResultServerInfo(
    /**
     * The name of the server as defined by the server.
     */
    val name: String,
    /**
     * The server's version as defined by the server.
     */
    val version: String? = null
)

/**
 * The data type of the ResponseError if the
 * initialize request fails.
 */
@Serializable
data class InitializeError(
    /**
     * Indicates whether the client execute the following retry logic:
     * (1) show the message provided by the ResponseError to the user
     * (2) user selects retry or cancel
     * (3) if user selected retry the initialize method is sent again.
     */
    val retry: Boolean
)

@Serializable
class InitializedParams

/**
 * The parameters of a notification message.
 */
@Serializable
data class ShowMessageParams(
    /**
     * The message type. See {@link MessageType}
     */
    @SerialName("type") val `type`: MessageType,
    /**
     * The actual message.
     */
    val message: String
)

@Serializable
data class ShowMessageRequestParams(
    /**
     * The message type. See {@link MessageType}
     */
    @SerialName("type") val `type`: MessageType,
    /**
     * The actual message.
     */
    val message: String,
    /**
     * The message action items to present.
     */
    val actions: List<MessageActionItem>? = null
)

@Serializable
data class MessageActionItem(
    /**
     * A short title like 'Retry', 'Open Log' etc.
     */
    val title: String
)

/**
 * The log message parameters.
 */
@Serializable
data class LogMessageParams(
    /**
     * The message type. See {@link MessageType}
     */
    @SerialName("type") val `type`: MessageType,
    /**
     * The actual message.
     */
    val message: String
)

/**
 * A text edit applicable to a text document.
 */
@Serializable
data class TextEdit(
    /**
     * The range of the text document to be manipulated. To insert
     * text into a document create a range where start === end.
     */
    val range: Range,
    /**
     * The string to be inserted. For delete operations use an
     * empty string.
     */
    val newText: String
) : CompletionItemTextEdit,
    TextDocumentEditEdits

/**
 * Parameters for a {@link ReferencesRequest}.
 */
@Serializable
data class ReferenceParams(
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
    val context: ReferenceContext
)

/**
 * Registration options for a {@link ReferencesRequest}.
 */
@Serializable
data class ReferenceRegistrationOptions(
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector?,
    val workDoneProgress: Boolean? = null
)

/**
 * Represents information about programming constructs like variables, classes,
 * interfaces etc.
 */
@Serializable
data class SymbolInformation(
    /**
     * The name of this symbol.
     */
    val name: String,
    /**
     * The kind of this symbol.
     */
    val kind: SymbolKind,
    /**
     * Tags for this symbol.
     *
     * @since 3.16.0
     */
    val tags: List<SymbolTag>? = null,
    /**
     * The name of the symbol containing this symbol. This information is for
     * user interface purposes (e.g. to render a qualifier in the user interface
     * if necessary). It can't be used to re-infer a hierarchy for the document
     * symbols.
     */
    val containerName: String? = null,
    /**
     * Indicates if this symbol is deprecated.
     *
     * @deprecated Use tags instead
     */
    val deprecated: Boolean? = null,
    /**
     * The location of this symbol. The location's range is used by a tool
     * to reveal the location in the editor. If the symbol is selected in the
     * tool the range's start information is used to position the cursor. So
     * the range usually spans more than the actual symbol's name and does
     * normally include things like visibility modifiers.
     *
     * The range doesn't have to denote a node range in the sense of an abstract
     * syntax tree. It can therefore not be used to re-construct a hierarchy of
     * the symbols.
     */
    val location: Location
)

/**
 * Represents a reference to a command. Provides a title which
 * will be used to represent a command in the UI and, optionally,
 * an array of arguments which will be passed to the command handler
 * function when invoked.
 */
@Serializable
data class Command(
    /**
     * Title of the command, like `save`.
     */
    val title: String,
    /**
     * The identifier of the actual command handler.
     */
    val command: String,
    /**
     * Arguments that the command handler should be
     * invoked with.
     */
    val arguments: List<LSPAny>? = null
) : TextDocumentCodeActionResult

/**
 * The parameters of a {@link DocumentFormattingRequest}.
 */
@Serializable
data class DocumentFormattingParams(
    /**
     * An optional token that a server can use to report work done progress.
     */
    val workDoneToken: ProgressToken? = null,
    /**
     * The document to format.
     */
    val textDocument: TextDocumentIdentifier,
    /**
     * The format options.
     */
    val options: FormattingOptions
)

/**
 * Registration options for a {@link DocumentFormattingRequest}.
 */
@Serializable
data class DocumentFormattingRegistrationOptions(
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector?,
    val workDoneProgress: Boolean? = null
)

/**
 * The parameters of a {@link DocumentRangeFormattingRequest}.
 */
@Serializable
data class DocumentRangeFormattingParams(
    /**
     * An optional token that a server can use to report work done progress.
     */
    val workDoneToken: ProgressToken? = null,
    /**
     * The document to format.
     */
    val textDocument: TextDocumentIdentifier,
    /**
     * The range to format
     */
    val range: Range,
    /**
     * The format options
     */
    val options: FormattingOptions
)

/**
 * Registration options for a {@link DocumentRangeFormattingRequest}.
 */
@Serializable
data class DocumentRangeFormattingRegistrationOptions(
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector?,
    val workDoneProgress: Boolean? = null,
    /**
     * Whether the server supports formatting multiple ranges at once.
     *
     * @since 3.18.0
     * @proposed
     */
    val rangesSupport: Boolean? = null
)

/**
 * The parameters of a {@link DocumentRangesFormattingRequest}.
 *
 * @since 3.18.0
 * @proposed
 */
@Serializable
data class DocumentRangesFormattingParams(
    /**
     * An optional token that a server can use to report work done progress.
     */
    val workDoneToken: ProgressToken? = null,
    /**
     * The document to format.
     */
    val textDocument: TextDocumentIdentifier,
    /**
     * The ranges to format
     */
    val ranges: List<Range>,
    /**
     * The format options
     */
    val options: FormattingOptions
)

/**
 * The parameters of a {@link DocumentOnTypeFormattingRequest}.
 */
@Serializable
data class DocumentOnTypeFormattingParams(
    /**
     * The document to format.
     */
    val textDocument: TextDocumentIdentifier,
    /**
     * The position around which the on type formatting should happen.
     * This is not necessarily the exact position where the character denoted
     * by the property `ch` got typed.
     */
    val position: Position,
    /**
     * The character that has been typed that triggered the formatting
     * on type request. That is not necessarily the last character that
     * got inserted into the document since the client could auto insert
     * characters as well (e.g. like automatic brace completion).
     */
    val ch: String,
    /**
     * The formatting options.
     */
    val options: FormattingOptions
)

/**
 * Registration options for a {@link DocumentOnTypeFormattingRequest}.
 */
@Serializable
data class DocumentOnTypeFormattingRegistrationOptions(
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector?,
    /**
     * A character on which formatting should be triggered, like `{`.
     */
    val firstTriggerCharacter: String,
    /**
     * More trigger characters.
     */
    val moreTriggerCharacter: List<String>? = null
)

/**
 * The parameters of a {@link RenameRequest}.
 */
@Serializable
data class RenameParams(
    /**
     * An optional token that a server can use to report work done progress.
     */
    val workDoneToken: ProgressToken? = null,
    /**
     * The document to rename.
     */
    val textDocument: TextDocumentIdentifier,
    /**
     * The position at which this request was sent.
     */
    val position: Position,
    /**
     * The new name of the symbol. If the given name is not valid the
     * request must return a {@link ResponseError} with an
     * appropriate message set.
     */
    val newName: String
)

/**
 * Registration options for a {@link RenameRequest}.
 */
@Serializable
data class RenameRegistrationOptions(
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector?,
    val workDoneProgress: Boolean? = null,
    /**
     * Renames should be checked and tested before being executed.
     *
     * @since version 3.12.0
     */
    val prepareProvider: Boolean? = null
)

@Serializable
data class PrepareRenameParams(
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
    val workDoneToken: ProgressToken? = null
)

/**
 * The parameters of a {@link ExecuteCommandRequest}.
 */
@Serializable
data class ExecuteCommandParams(
    /**
     * An optional token that a server can use to report work done progress.
     */
    val workDoneToken: ProgressToken? = null,
    /**
     * The identifier of the actual command handler.
     */
    val command: String,
    /**
     * Arguments that the command should be invoked with.
     */
    val arguments: List<LSPAny>? = null
)

/**
 * Registration options for a {@link ExecuteCommandRequest}.
 */
@Serializable
data class ExecuteCommandRegistrationOptions(
    val workDoneProgress: Boolean? = null,
    /**
     * The commands to be executed on the server
     */
    val commands: List<String>
)

/**
 * The parameters passed via an apply workspace edit request.
 */
@Serializable
data class ApplyWorkspaceEditParams(
    /**
     * An optional label of the workspace edit. This label is
     * presented in the user interface for example on an undo
     * stack to undo the workspace edit.
     */
    val label: String? = null,
    /**
     * The edits to apply.
     */
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) val edit: WorkspaceEdit = WorkspaceEdit()
)

/**
 * The result returned from the apply workspace edit request.
 *
 * @since 3.17 renamed from ApplyWorkspaceEditResponse
 */
@Serializable
data class ApplyWorkspaceEditResult(
    /**
     * Indicates whether the edit was applied or not.
     */
    val applied: Boolean,
    /**
     * An optional textual description for why the edit was not applied.
     * This may be used by the server for diagnostic logging or to provide
     * a suitable error for a request that triggered the edit.
     */
    val failureReason: String? = null,
    /**
     * Depending on the client's failure handling strategy `failedChange` might
     * contain the index of the change that failed. This property is only available
     * if the client signals a `failureHandlingStrategy` in its client capabilities.
     */
    val failedChange: UInt? = null
)

@Serializable
data class SetTraceParams(val value: TraceValues)

@Serializable
data class LogTraceParams(val message: String, val verbose: String? = null)

@Serializable
data class CancelParams(
    /**
     * The request id to cancel.
     */
    val id: IntOrString
)

@Serializable
data class ProgressParams(
    /**
     * The progress token provided by the client or server.
     */
    val token: ProgressToken,
    /**
     * The progress data.
     */
    val value: LSPAny
)

@Serializable
data class PartialResultParams(
    /**
     * An optional token that a server can use to report partial results (e.g. streaming) to
     * the client.
     */
    val partialResultToken: ProgressToken? = null
)

/**
 * A range in a text document expressed as (zero-based) start and end positions.
 *
 * If you want to specify a range that contains a line including the line ending
 * character(s) then use an end position denoting the start of the next line.
 * For example:
 * ```ts
 * {
 *     start: { line: 5, character: 23 }
 *     end : { line 6, character : 0 }
 * }
 * ```
 */
@Serializable
data class Range(
    /**
     * The range's start position.
     */
    val start: Position,
    /**
     * The range's end position.
     */
    val end: Position
) : CompletionListItemDefaultsEditRange,
    PrepareRenameResult

/**
 * Static registration options to be returned in the initialize
 * request.
 */
@Serializable
data class StaticRegistrationOptions(
    /**
     * The id used to register the request. The id can be used to deregister
     * the request again. See also Registration#id.
     */
    val id: String? = null
)

@Serializable
data class ConfigurationItem(
    /**
     * The scope to get the configuration section for.
     */
    val scopeUri: URI? = null,
    /**
     * The configuration section asked for.
     */
    val section: String? = null
)

/**
 * Represents a color in RGBA space.
 */
@Serializable
data class Color(
    /**
     * The red component of this color in the range [0-1].
     */
    val red: Double,
    /**
     * The green component of this color in the range [0-1].
     */
    val green: Double,
    /**
     * The blue component of this color in the range [0-1].
     */
    val blue: Double,
    /**
     * The alpha component of this color in the range [0-1].
     */
    val alpha: Double
)

/**
 * Position in a text document expressed as zero-based line and character
 * offset. Prior to 3.17 the offsets were always based on a UTF-16 string
 * representation. So a string of the form `a𐐀b` the character offset of the
 * character `a` is 0, the character offset of `𐐀` is 1 and the character
 * offset of b is 3 since `𐐀` is represented using two code units in UTF-16.
 * Since 3.17 clients and servers can agree on a different string encoding
 * representation (e.g. UTF-8). The client announces it's supported encoding
 * via the client capability [`general.positionEncodings`](https://microsoft.github.io/language-server-protocol/specifications/specification-current/#clientCapabilities).
 * The value is an array of position encodings the client supports, with
 * decreasing preference (e.g. the encoding at index `0` is the most preferred
 * one). To stay backwards compatible the only mandatory encoding is UTF-16
 * represented via the string `utf-16`. The server can pick one of the
 * encodings offered by the client and signals that encoding back to the
 * client via the initialize result's property
 * [`capabilities.positionEncoding`](https://microsoft.github.io/language-server-protocol/specifications/specification-current/#serverCapabilities). If the string value
 * `utf-16` is missing from the client's capability `general.positionEncodings`
 * servers can safely assume that the client supports UTF-16. If the server
 * omits the position encoding in its initialize result the encoding defaults
 * to the string value `utf-16`. Implementation considerations: since the
 * conversion from one encoding into another requires the content of the
 * file / line the conversion is best done where the file is read which is
 * usually on the server side.
 *
 * Positions are line end character agnostic. So you can not specify a position
 * that denotes `\r|\n` or `\n|` where `|` represents the character offset.
 *
 * @since 3.17.0 - support for negotiated position encoding.
 */
@Serializable
data class Position(
    /**
     * Line position in a document (zero-based).
     *
     * If a line number is greater than the number of lines in a document, it defaults back to the number of lines in the document.
     * If a line number is negative, it defaults to 0.
     */
    val line: UInt,
    /**
     * Character offset on a line in a document (zero-based).
     *
     * The meaning of this offset is determined by the negotiated
     * `PositionEncodingKind`.
     *
     * If the character value is greater than the line length it defaults back to the
     * line length.
     */
    val character: UInt
)

/**
 * Call hierarchy options used during static registration.
 *
 * @since 3.16.0
 */
@Serializable
data class CallHierarchyOptions(val workDoneProgress: Boolean? = null) :
    ServerCapabilitiesCallHierarchyProviderOptions

@Serializable
data class LinkedEditingRangeOptions(val workDoneProgress: Boolean? = null) :
    ServerCapabilitiesLinkedEditingRangeProviderOptions

/**
 * Represents information on a file/folder create.
 *
 * @since 3.16.0
 */
@Serializable
data class FileCreate(
    /**
     * A file:// URI for the location of the file/folder being created.
     */
    val uri: String
)

/**
 * Create file operation.
 */
@Serializable
data class CreateFile(
    /**
     * An optional annotation identifier describing the operation.
     *
     * @since 3.16.0
     */
    val annotationId: ChangeAnnotationIdentifier? = null,
    /**
     * A create
     */
    val kind: String,
    /**
     * The resource to create.
     */
    val uri: DocumentUri,
    /**
     * Additional options
     */
    val options: CreateFileOptions? = null
) : WorkspaceEditDocumentChanges

/**
 * Rename file operation
 */
@Serializable
data class RenameFile(
    /**
     * An optional annotation identifier describing the operation.
     *
     * @since 3.16.0
     */
    val annotationId: ChangeAnnotationIdentifier? = null,
    /**
     * A rename
     */
    val kind: String,
    /**
     * The old (existing) location.
     */
    val oldUri: DocumentUri,
    /**
     * The new location.
     */
    val newUri: DocumentUri,
    /**
     * Rename options.
     */
    val options: RenameFileOptions? = null
) : WorkspaceEditDocumentChanges

/**
 * Delete file operation
 */
@Serializable
data class DeleteFile(
    /**
     * An optional annotation identifier describing the operation.
     *
     * @since 3.16.0
     */
    val annotationId: ChangeAnnotationIdentifier? = null,
    /**
     * A delete
     */
    val kind: String,
    /**
     * The file to delete.
     */
    val uri: DocumentUri,
    /**
     * Delete options.
     */
    val options: DeleteFileOptions? = null
) : WorkspaceEditDocumentChanges

/**
 * Additional information that describes document changes.
 *
 * @since 3.16.0
 */
@Serializable
data class ChangeAnnotation(
    /**
     * A human-readable string describing the actual change. The string
     * is rendered prominent in the user interface.
     */
    val label: String,
    /**
     * A flag which indicates that user confirmation is needed
     * before applying the change.
     */
    val needsConfirmation: Boolean? = null,
    /**
     * A human-readable string which is rendered less prominent in
     * the user interface.
     */
    val description: String? = null
)

/**
 * Represents information on a file/folder rename.
 *
 * @since 3.16.0
 */
@Serializable
data class FileRename(
    /**
     * A file:// URI for the original location of the file/folder being renamed.
     */
    val oldUri: String,
    /**
     * A file:// URI for the new location of the file/folder being renamed.
     */
    val newUri: String
)

/**
 * Represents information on a file/folder delete.
 *
 * @since 3.16.0
 */
@Serializable
data class FileDelete(
    /**
     * A file:// URI for the location of the file/folder being deleted.
     */
    val uri: String
)

/**
 * Type hierarchy options used during static registration.
 *
 * @since 3.17.0
 */
@Serializable
data class TypeHierarchyOptions(val workDoneProgress: Boolean? = null) :
    ServerCapabilitiesTypeHierarchyProviderOptions

/**
 * A `MarkupContent` literal represents a string value which content is interpreted base on its
 * kind flag. Currently the protocol supports `plaintext` and `markdown` as markup kinds.
 *
 * If the kind is `markdown` then the value can contain fenced code blocks like in GitHub issues.
 * See https://help.github.com/articles/creating-and-highlighting-code-blocks/#syntax-highlighting
 *
 * Here is an example how such a string can be constructed using JavaScript / TypeScript:
 * ```ts
 * let markdown: MarkdownContent = {
 *  kind: MarkupKind.Markdown,
 *  value: [
 *    '# Header',
 *    'Some text',
 *    '```typescript',
 *    'someCode();',
 *    '```'
 *  ].join('\n')
 * };
 * ```
 *
 * *Please Note* that clients might sanitize the return markdown. A client could decide to
 * remove HTML from the markdown to avoid script execution.
 */
@Serializable
data class MarkupContent(
    /**
     * The type of the Markup
     */
    val kind: MarkupKind,
    /**
     * The content itself
     */
    val value: String
)

/**
 * A full diagnostic report with a set of related documents.
 *
 * @since 3.17.0
 */
@Serializable
data class RelatedFullDocumentDiagnosticReport(
    /**
     * A full document diagnostic report.
     */
    val kind: String,
    /**
     * An optional result id. If provided it will
     * be sent on the next diagnostic request for the
     * same document.
     */
    val resultId: String? = null,
    /**
     * The actual items.
     */
    val items: List<Diagnostic>,
    /**
     * Diagnostics of related documents. This information is useful
     * in programming languages where code in a file A can generate
     * diagnostics in a file B which A depends on. An example of
     * such a language is C/C++ where marco definitions in a file
     * a.cpp and result in errors in a header file b.hpp.
     *
     * @since 3.17.0
     */
    val relatedDocuments: Map<DocumentUri, RelatedFullDocumentDiagnosticReportRelatedDocuments>? = null
) : DocumentDiagnosticReport

/**
 * An unchanged diagnostic report with a set of related documents.
 *
 * @since 3.17.0
 */
@Serializable
data class RelatedUnchangedDocumentDiagnosticReport(
    /**
     * A document diagnostic report indicating
     * no changes to the last result. A server can
     * only return `unchanged` if result ids are
     * provided.
     */
    val kind: String,
    /**
     * A result id which will be sent on the next
     * diagnostic request for the same document.
     */
    val resultId: String,
    /**
     * Diagnostics of related documents. This information is useful
     * in programming languages where code in a file A can generate
     * diagnostics in a file B which A depends on. An example of
     * such a language is C/C++ where marco definitions in a file
     * a.cpp and result in errors in a header file b.hpp.
     *
     * @since 3.17.0
     */
    val relatedDocuments: Map<DocumentUri, RelatedUnchangedDocumentDiagnosticReportRelatedDocuments>? = null
) : DocumentDiagnosticReport

/**
 * A diagnostic report with a full set of problems.
 *
 * @since 3.17.0
 */
@Serializable
data class FullDocumentDiagnosticReport(
    /**
     * A full document diagnostic report.
     */
    val kind: String,
    /**
     * An optional result id. If provided it will
     * be sent on the next diagnostic request for the
     * same document.
     */
    val resultId: String? = null,
    /**
     * The actual items.
     */
    val items: List<Diagnostic>
) : DocumentDiagnosticReportPartialResultRelatedDocuments,
    RelatedFullDocumentDiagnosticReportRelatedDocuments,
    RelatedUnchangedDocumentDiagnosticReportRelatedDocuments

/**
 * A diagnostic report indicating that the last returned
 * report is still accurate.
 *
 * @since 3.17.0
 */
@Serializable
data class UnchangedDocumentDiagnosticReport(
    /**
     * A document diagnostic report indicating
     * no changes to the last result. A server can
     * only return `unchanged` if result ids are
     * provided.
     */
    val kind: String,
    /**
     * A result id which will be sent on the next
     * diagnostic request for the same document.
     */
    val resultId: String
) : DocumentDiagnosticReportPartialResultRelatedDocuments,
    RelatedFullDocumentDiagnosticReportRelatedDocuments,
    RelatedUnchangedDocumentDiagnosticReportRelatedDocuments

/**
 * A previous result id in a workspace pull request.
 *
 * @since 3.17.0
 */
@Serializable
data class PreviousResultId(
    /**
     * The URI for which the client knowns a
     * result id.
     */
    val uri: DocumentUri,
    /**
     * The value of the previous result id.
     */
    val value: String
)

/**
 * A versioned notebook document identifier.
 *
 * @since 3.17.0
 */
@Serializable
data class VersionedNotebookDocumentIdentifier(
    /**
     * The version number of this notebook document.
     */
    val version: Int,
    /**
     * The notebook document's uri.
     */
    val uri: URI
)

/**
 * A string value used as a snippet is a template which allows to insert text
 * and to control the editor cursor when insertion happens.
 *
 * A snippet can define tab stops and placeholders with `$1`, `$2`
 * and `${3:foo}`. `$0` defines the final tab stop, it defaults to
 * the end of the snippet. Variables are defined with `$name` and
 * `${name:default value}`.
 *
 * @since 3.18.0
 * @proposed
 */
@Serializable
data class StringValue(
    /**
     * The kind of string value.
     */
    val kind: String,
    /**
     * The snippet string.
     */
    val value: String
)

/**
 * General parameters to register for a notification or to register a provider.
 */
@Serializable
data class Registration(
    /**
     * The id used to register the request. The id can be used to deregister
     * the request again.
     */
    val id: String,
    /**
     * The method / capability to register for.
     */
    val method: String,
    /**
     * Options necessary for the registration.
     */
    val registerOptions: LSPAny? = null
)

/**
 * General parameters to unregister a request or notification.
 */
@Serializable
data class Unregistration(
    /**
     * The id used to unregister the request or notification. Usually an id
     * provided during the register request.
     */
    val id: String,
    /**
     * The method to unregister for.
     */
    val method: String
)

/**
 * The initialize parameters
 */
@Serializable
data class _InitializeParams(
    /**
     * An optional token that a server can use to report work done progress.
     */
    val workDoneToken: ProgressToken? = null,
    /**
     * The process Id of the parent process that started
     * the server.
     *
     * Is `null` if the process has not been started by another process.
     * If the parent process is not alive then the server should exit.
     */
    val processId: Int?,
    /**
     * Information about the client
     *
     * @since 3.15.0
     */
    val clientInfo: _InitializeParamsClientInfo? = null,
    /**
     * The locale the client is currently showing the user interface
     * in. This must not necessarily be the locale of the operating
     * system.
     *
     * Uses IETF language tags as the value's syntax
     * (See https://en.wikipedia.org/wiki/IETF_language_tag)
     *
     * @since 3.16.0
     */
    val locale: String? = null,
    /**
     * The rootPath of the workspace. Is null
     * if no folder is open.
     *
     * @deprecated in favour of rootUri.
     */
    val rootPath: String? = null,
    /**
     * The rootUri of the workspace. Is null if no
     * folder is open. If both `rootPath` and `rootUri` are set
     * `rootUri` wins.
     *
     * @deprecated in favour of workspaceFolders.
     */
    val rootUri: DocumentUri?,
    /**
     * The capabilities provided by the client (editor or tool)
     */
    @EncodeDefault(
        EncodeDefault.Mode.ALWAYS
    ) val capabilities: ClientCapabilities = ClientCapabilities(),
    /**
     * User provided initialization options.
     */
    val initializationOptions: LSPAny? = null,
    /**
     * The initial trace setting. If omitted trace is disabled ('off').
     */
    val trace: TraceValues? = null
)

@Serializable
data class _InitializeParamsClientInfo(
    /**
     * The name of the client as defined by the client.
     */
    val name: String,
    /**
     * The client's version as defined by the client.
     */
    val version: String? = null
)

/**
 * Defines the capabilities provided by a language
 * server.
 */
@Serializable
data class ServerCapabilities(
    /**
     * The position encoding the server picked from the encodings offered
     * by the client via the client capability `general.positionEncodings`.
     *
     * If the client didn't provide any position encodings the only valid
     * value that a server can return is 'utf-16'.
     *
     * If omitted it defaults to 'utf-16'.
     *
     * @since 3.17.0
     */
    val positionEncoding: PositionEncodingKind? = null,
    /**
     * Defines how text documents are synced. Is either a detailed structure
     * defining each notification or for backwards compatibility the
     * TextDocumentSyncKind number.
     */
    val textDocumentSync: ServerCapabilitiesTextDocumentSync? = null,
    /**
     * Defines how notebook documents are synced.
     *
     * @since 3.17.0
     */
    val notebookDocumentSync: ServerCapabilitiesNotebookDocumentSync? = null,
    /**
     * The server provides completion support.
     */
    val completionProvider: CompletionOptions? = null,
    /**
     * The server provides hover support.
     */
    val hoverProvider: BooleanOr<HoverOptions>? = null,
    /**
     * The server provides signature help support.
     */
    val signatureHelpProvider: SignatureHelpOptions? = null,
    /**
     * The server provides Goto Declaration support.
     */
    val declarationProvider: BooleanOr<ServerCapabilitiesDeclarationProviderOptions>? = null,
    /**
     * The server provides goto definition support.
     */
    val definitionProvider: BooleanOr<DefinitionOptions>? = null,
    /**
     * The server provides Goto Type Definition support.
     */
    val typeDefinitionProvider: BooleanOr<ServerCapabilitiesTypeDefinitionProviderOptions>? = null,
    /**
     * The server provides Goto Implementation support.
     */
    val implementationProvider: BooleanOr<ServerCapabilitiesImplementationProviderOptions>? = null,
    /**
     * The server provides find references support.
     */
    val referencesProvider: BooleanOr<ReferenceOptions>? = null,
    /**
     * The server provides document highlight support.
     */
    val documentHighlightProvider: BooleanOr<DocumentHighlightOptions>? = null,
    /**
     * The server provides document symbol support.
     */
    val documentSymbolProvider: BooleanOr<DocumentSymbolOptions>? = null,
    /**
     * The server provides code actions. CodeActionOptions may only be
     * specified if the client states that it supports
     * `codeActionLiteralSupport` in its initial `initialize` request.
     */
    val codeActionProvider: BooleanOr<CodeActionOptions>? = null,
    /**
     * The server provides code lens.
     */
    val codeLensProvider: CodeLensOptions? = null,
    /**
     * The server provides document link support.
     */
    val documentLinkProvider: DocumentLinkOptions? = null,
    /**
     * The server provides color provider support.
     */
    val colorProvider: BooleanOr<ServerCapabilitiesColorProviderOptions>? = null,
    /**
     * The server provides workspace symbol support.
     */
    val workspaceSymbolProvider: BooleanOr<WorkspaceSymbolOptions>? = null,
    /**
     * The server provides document formatting.
     */
    val documentFormattingProvider: BooleanOr<DocumentFormattingOptions>? = null,
    /**
     * The server provides document range formatting.
     */
    val documentRangeFormattingProvider: BooleanOr<DocumentRangeFormattingOptions>? = null,
    /**
     * The server provides document formatting on typing.
     */
    val documentOnTypeFormattingProvider: DocumentOnTypeFormattingOptions? = null,
    /**
     * The server provides rename support. RenameOptions may only be
     * specified if the client states that it supports
     * `prepareSupport` in its initial `initialize` request.
     */
    val renameProvider: BooleanOr<RenameOptions>? = null,
    /**
     * The server provides folding provider support.
     */
    val foldingRangeProvider: BooleanOr<ServerCapabilitiesFoldingRangeProviderOptions>? = null,
    /**
     * The server provides selection range support.
     */
    val selectionRangeProvider: BooleanOr<ServerCapabilitiesSelectionRangeProviderOptions>? = null,
    /**
     * The server provides execute command support.
     */
    val executeCommandProvider: ExecuteCommandOptions? = null,
    /**
     * The server provides call hierarchy support.
     *
     * @since 3.16.0
     */
    val callHierarchyProvider: BooleanOr<ServerCapabilitiesCallHierarchyProviderOptions>? = null,
    /**
     * The server provides linked editing range support.
     *
     * @since 3.16.0
     */
    val linkedEditingRangeProvider: BooleanOr<ServerCapabilitiesLinkedEditingRangeProviderOptions>? = null,
    /**
     * The server provides semantic tokens support.
     *
     * @since 3.16.0
     */
    val semanticTokensProvider: ServerCapabilitiesSemanticTokensProvider? = null,
    /**
     * The server provides moniker support.
     *
     * @since 3.16.0
     */
    val monikerProvider: BooleanOr<ServerCapabilitiesMonikerProviderOptions>? = null,
    /**
     * The server provides type hierarchy support.
     *
     * @since 3.17.0
     */
    val typeHierarchyProvider: BooleanOr<ServerCapabilitiesTypeHierarchyProviderOptions>? = null,
    /**
     * The server provides inline values.
     *
     * @since 3.17.0
     */
    val inlineValueProvider: BooleanOr<ServerCapabilitiesInlineValueProviderOptions>? = null,
    /**
     * The server provides inlay hints.
     *
     * @since 3.17.0
     */
    val inlayHintProvider: BooleanOr<ServerCapabilitiesInlayHintProviderOptions>? = null,
    /**
     * The server has support for pull model diagnostics.
     *
     * @since 3.17.0
     */
    val diagnosticProvider: ServerCapabilitiesDiagnosticProvider? = null,
    /**
     * Inline completion options used during static registration.
     *
     * @since 3.18.0
     * @proposed
     */
    val inlineCompletionProvider: BooleanOr<InlineCompletionOptions>? = null,
    /**
     * Workspace specific server capabilities.
     */
    val workspace: ServerCapabilitiesWorkspace? = null,
    /**
     * Experimental server capabilities.
     */
    val experimental: LSPAny? = null
)

@Serializable
data class ServerCapabilitiesWorkspace(
    /**
     * The server supports workspace folder.
     *
     * @since 3.6.0
     */
    val workspaceFolders: WorkspaceFoldersServerCapabilities? = null,
    /**
     * The server is interested in notifications/requests for operations on files.
     *
     * @since 3.16.0
     */
    val fileOperations: FileOperationOptions? = null
)

/**
 * A text document identifier to denote a specific version of a text document.
 */
@Serializable
data class VersionedTextDocumentIdentifier(
    /**
     * The text document's uri.
     */
    val uri: DocumentUri,
    /**
     * The version number of this document.
     */
    val version: Int
)

/**
 * Save options.
 */
@Serializable
data class SaveOptions(
    /**
     * The client is supposed to include the content on save.
     */
    val includeText: Boolean? = null
)

/**
 * An event describing a file change.
 */
@Serializable
data class FileEvent(
    /**
     * The file's uri.
     */
    val uri: DocumentUri,
    /**
     * The change type.
     */
    @SerialName("type") val `type`: FileChangeType
)

@Serializable
data class FileSystemWatcher(
    /**
     * The glob pattern to watch. See {@link GlobPattern glob pattern} for more detail.
     *
     * @since 3.17.0 support for relative patterns.
     */
    val globPattern: GlobPattern,
    /**
     * The kind of events of interest. If omitted it defaults
     * to WatchKind.Create | WatchKind.Change | WatchKind.Delete
     * which is 7.
     */
    val kind: WatchKind? = null
)

/**
 * A special text edit to provide an insert and a replace operation.
 *
 * @since 3.16.0
 */
@Serializable
data class InsertReplaceEdit(
    /**
     * The string to be inserted.
     */
    val newText: String,
    /**
     * The range if the insert is requested
     */
    val insert: Range,
    /**
     * The range if the replace is requested.
     */
    val replace: Range
) : CompletionItemTextEdit

/**
 * Represents the signature of something callable. A signature
 * can have a label, like a function-name, a doc-comment, and
 * a set of parameters.
 */
@Serializable
data class SignatureInformation(
    /**
     * The label of this signature. Will be shown in
     * the UI.
     */
    val label: String,
    /**
     * The human-readable doc-comment of this signature. Will be shown
     * in the UI but can be omitted.
     */
    val documentation: StringOr<MarkupContent>? = null,
    /**
     * The parameters of this signature.
     */
    val parameters: List<ParameterInformation>? = null,
    /**
     * The index of the active parameter.
     *
     * If provided, this is used in place of `SignatureHelp.activeParameter`.
     *
     * @since 3.16.0
     */
    val activeParameter: UInt? = null
)

/**
 * Value-object that contains additional information when
 * requesting references.
 */
@Serializable
data class ReferenceContext(
    /**
     * Include the declaration of the current symbol.
     */
    val includeDeclaration: Boolean
)

/**
 * Reference options.
 */
@Serializable
data class ReferenceOptions(val workDoneProgress: Boolean? = null)

/**
 * A base for all symbol information.
 */
@Serializable
data class BaseSymbolInformation(
    /**
     * The name of this symbol.
     */
    val name: String,
    /**
     * The kind of this symbol.
     */
    val kind: SymbolKind,
    /**
     * Tags for this symbol.
     *
     * @since 3.16.0
     */
    val tags: List<SymbolTag>? = null,
    /**
     * The name of the symbol containing this symbol. This information is for
     * user interface purposes (e.g. to render a qualifier in the user interface
     * if necessary). It can't be used to re-infer a hierarchy for the document
     * symbols.
     */
    val containerName: String? = null
)

/**
 * Value-object describing what options formatting should use.
 */
@Serializable
data class FormattingOptions(
    /**
     * Size of a tab in spaces.
     */
    val tabSize: UInt,
    /**
     * Prefer spaces over tabs.
     */
    val insertSpaces: Boolean,
    /**
     * Trim trailing whitespace on a line.
     *
     * @since 3.15.0
     */
    val trimTrailingWhitespace: Boolean? = null,
    /**
     * Insert a newline character at the end of the file if one does not exist.
     *
     * @since 3.15.0
     */
    val insertFinalNewline: Boolean? = null,
    /**
     * Trim all newlines after the final newline at the end of the file.
     *
     * @since 3.15.0
     */
    val trimFinalNewlines: Boolean? = null
)

/**
 * Provider options for a {@link DocumentFormattingRequest}.
 */
@Serializable
data class DocumentFormattingOptions(val workDoneProgress: Boolean? = null)

/**
 * Provider options for a {@link DocumentRangeFormattingRequest}.
 */
@Serializable
data class DocumentRangeFormattingOptions(
    val workDoneProgress: Boolean? = null,
    /**
     * Whether the server supports formatting multiple ranges at once.
     *
     * @since 3.18.0
     * @proposed
     */
    val rangesSupport: Boolean? = null
)

/**
 * Provider options for a {@link DocumentOnTypeFormattingRequest}.
 */
@Serializable
data class DocumentOnTypeFormattingOptions(
    /**
     * A character on which formatting should be triggered, like `{`.
     */
    val firstTriggerCharacter: String,
    /**
     * More trigger characters.
     */
    val moreTriggerCharacter: List<String>? = null
)

/**
 * Provider options for a {@link RenameRequest}.
 */
@Serializable
data class RenameOptions(
    val workDoneProgress: Boolean? = null,
    /**
     * Renames should be checked and tested before being executed.
     *
     * @since version 3.12.0
     */
    val prepareProvider: Boolean? = null
)

/**
 * The server capabilities of a {@link ExecuteCommandRequest}.
 */
@Serializable
data class ExecuteCommandOptions(
    val workDoneProgress: Boolean? = null,
    /**
     * The commands to be executed on the server
     */
    val commands: List<String>
)

/**
 * A text document identifier to optionally denote a specific version of a text document.
 */
@Serializable
data class OptionalVersionedTextDocumentIdentifier(
    /**
     * The text document's uri.
     */
    val uri: DocumentUri,
    /**
     * The version number of this document. If a versioned text document identifier
     * is sent from the server to the client and the file is not open in the editor
     * (the server has not received an open notification before) the server can send
     * `null` to indicate that the version is unknown and the content on disk is the
     * truth (as specified with document content ownership).
     */
    val version: Int?
)

/**
 * A special text edit with an additional change annotation.
 *
 * @since 3.16.0.
 */
@Serializable
data class AnnotatedTextEdit(
    /**
     * The range of the text document to be manipulated. To insert
     * text into a document create a range where start === end.
     */
    val range: Range,
    /**
     * The string to be inserted. For delete operations use an
     * empty string.
     */
    val newText: String,
    /**
     * The actual identifier of the change annotation
     */
    val annotationId: ChangeAnnotationIdentifier
) : TextDocumentEditEdits

/**
 * A generic resource operation.
 */
@Serializable
data class ResourceOperation(
    /**
     * The resource operation kind.
     */
    val kind: String,
    /**
     * An optional annotation identifier describing the operation.
     *
     * @since 3.16.0
     */
    val annotationId: ChangeAnnotationIdentifier? = null
)

/**
 * Options to create a file.
 */
@Serializable
data class CreateFileOptions(
    /**
     * Overwrite existing file. Overwrite wins over `ignoreIfExists`
     */
    val overwrite: Boolean? = null,
    /**
     * Ignore if exists.
     */
    val ignoreIfExists: Boolean? = null
)

/**
 * Rename file options
 */
@Serializable
data class RenameFileOptions(
    /**
     * Overwrite target if existing. Overwrite wins over `ignoreIfExists`
     */
    val overwrite: Boolean? = null,
    /**
     * Ignores if target exists.
     */
    val ignoreIfExists: Boolean? = null
)

/**
 * Delete file options
 */
@Serializable
data class DeleteFileOptions(
    /**
     * Delete the content recursively if a folder is denoted.
     */
    val recursive: Boolean? = null,
    /**
     * Ignore the operation if the file doesn't exist.
     */
    val ignoreIfNotExists: Boolean? = null
)

/**
 * Describes the currently selected completion item.
 *
 * @since 3.18.0
 * @proposed
 */
@Serializable
data class SelectedCompletionInfo(
    /**
     * The range that will be replaced if this completion item is accepted.
     */
    val range: Range,
    /**
     * The text the range will be replaced with if this completion is accepted.
     */
    val text: String
)

/**
 * Defines the capabilities provided by the client.
 */
@Serializable
data class ClientCapabilities(
    /**
     * Workspace specific client capabilities.
     */
    val workspace: WorkspaceClientCapabilities? = null,
    /**
     * Text document specific client capabilities.
     */
    val textDocument: TextDocumentClientCapabilities? = null,
    /**
     * Capabilities specific to the notebook document support.
     *
     * @since 3.17.0
     */
    val notebookDocument: NotebookDocumentClientCapabilities? = null,
    /**
     * Window specific client capabilities.
     */
    val window: WindowClientCapabilities? = null,
    /**
     * General client capabilities.
     *
     * @since 3.16.0
     */
    val general: GeneralClientCapabilities? = null,
    /**
     * Experimental client capabilities.
     */
    val experimental: LSPAny? = null
)

/**
 * Structure to capture a description for an error code.
 *
 * @since 3.16.0
 */
@Serializable
data class CodeDescription(
    /**
     * An URI to open with more information about the diagnostic error.
     */
    val href: URI
)

/**
 * Represents a parameter of a callable-signature. A parameter can
 * have a label and a doc-comment.
 */
@Serializable
data class ParameterInformation(
    /**
     * The label of this parameter information.
     *
     * Either a string or an inclusive start and exclusive end offsets within its containing
     * signature label. (see SignatureInformation.label). The offsets are based on a UTF-16
     * string representation as `Position` and `Range` does.
     *
     * *Note*: a label of type string should be a substring of its containing signature label.
     * Its intended use case is to highlight the parameter label part in the `SignatureInformation.label`.
     */
    val label: StringOr<List<UInt>>,
    /**
     * The human-readable doc-comment of this parameter. Will be shown
     * in the UI but can be omitted.
     */
    val documentation: StringOr<MarkupContent>? = null
)

@Serializable
data class ExecutionSummary(
    /**
     * A strict monotonically increasing value
     * indicating the execution order of a cell
     * inside a notebook.
     */
    val executionOrder: UInt,
    /**
     * Whether the execution was successful or
     * not if known by the client.
     */
    val success: Boolean? = null
)

/**
 * A relative pattern is a helper to construct glob patterns that are matched
 * relatively to a base URI. The common value for a `baseUri` is a workspace
 * folder root, but it can be another absolute URI as well.
 *
 * @since 3.17.0
 */
@Serializable
data class RelativePattern(
    /**
     * A workspace folder or a base URI to which this pattern will be matched
     * against relatively.
     */
    val baseUri: StringOr<WorkspaceFolder>,
    /**
     * The actual glob pattern;
     */
    val pattern: Pattern
)

/**
 * The client capabilities of a {@link ExecuteCommandRequest}.
 */
@Serializable
data class ExecuteCommandClientCapabilities(
    /**
     * Execute command supports dynamic registration.
     */
    val dynamicRegistration: Boolean? = null
)

/**
 * Client Capabilities for a {@link ReferencesRequest}.
 */
@Serializable
data class ReferenceClientCapabilities(
    /**
     * Whether references supports dynamic registration.
     */
    val dynamicRegistration: Boolean? = null
)

/**
 * Client capabilities of a {@link DocumentFormattingRequest}.
 */
@Serializable
data class DocumentFormattingClientCapabilities(
    /**
     * Whether formatting supports dynamic registration.
     */
    val dynamicRegistration: Boolean? = null
)

/**
 * Client capabilities of a {@link DocumentRangeFormattingRequest}.
 */
@Serializable
data class DocumentRangeFormattingClientCapabilities(
    /**
     * Whether range formatting supports dynamic registration.
     */
    val dynamicRegistration: Boolean? = null,
    /**
     * Whether the client supports formatting multiple ranges at once.
     *
     * @since 3.18.0
     * @proposed
     */
    val rangesSupport: Boolean? = null
)

/**
 * Client capabilities of a {@link DocumentOnTypeFormattingRequest}.
 */
@Serializable
data class DocumentOnTypeFormattingClientCapabilities(
    /**
     * Whether on type formatting supports dynamic registration.
     */
    val dynamicRegistration: Boolean? = null
)

@Serializable
data class RenameClientCapabilities(
    /**
     * Whether rename supports dynamic registration.
     */
    val dynamicRegistration: Boolean? = null,
    /**
     * Client supports testing for validity of rename operations
     * before execution.
     *
     * @since 3.12.0
     */
    val prepareSupport: Boolean? = null,
    /**
     * Client supports the default behavior result.
     *
     * The value indicates the default behavior used by the
     * client.
     *
     * @since 3.16.0
     */
    val prepareSupportDefaultBehavior: PrepareSupportDefaultBehavior? = null,
    /**
     * Whether the client honors the change annotations in
     * text edits and resource operations returned via the
     * rename request's workspace edit by for example presenting
     * the workspace edit in the user interface and asking
     * for confirmation.
     *
     * @since 3.16.0
     */
    val honorsChangeAnnotations: Boolean? = null
)

/**
 * @since 3.16.0
 */
@Serializable
data class CallHierarchyClientCapabilities(
    /**
     * Whether implementation supports dynamic registration. If this is set to `true`
     * the client supports the new `(TextDocumentRegistrationOptions & StaticRegistrationOptions)`
     * return value for the corresponding server capability as well.
     */
    val dynamicRegistration: Boolean? = null
)

/**
 * Client capabilities for the linked editing range request.
 *
 * @since 3.16.0
 */
@Serializable
data class LinkedEditingRangeClientCapabilities(
    /**
     * Whether implementation supports dynamic registration. If this is set to `true`
     * the client supports the new `(TextDocumentRegistrationOptions & StaticRegistrationOptions)`
     * return value for the corresponding server capability as well.
     */
    val dynamicRegistration: Boolean? = null
)

/**
 * @since 3.17.0
 */
@Serializable
data class TypeHierarchyClientCapabilities(
    /**
     * Whether implementation supports dynamic registration. If this is set to `true`
     * the client supports the new `(TextDocumentRegistrationOptions & StaticRegistrationOptions)`
     * return value for the corresponding server capability as well.
     */
    val dynamicRegistration: Boolean? = null
)

/**
 * Show message request client capabilities
 */
@Serializable
data class ShowMessageRequestClientCapabilities(
    /**
     * Capabilities specific to the `MessageActionItem` type.
     */
    val messageActionItem: ShowMessageRequestClientCapabilitiesMessageActionItem? = null
)

@Serializable
data class ShowMessageRequestClientCapabilitiesMessageActionItem(
    /**
     * Whether the client supports additional attributes which
     * are preserved and send back to the server in the
     * request's response.
     */
    val additionalPropertiesSupport: Boolean? = null
)

/**
 * Client capabilities for the showDocument request.
 *
 * @since 3.16.0
 */
@Serializable
data class ShowDocumentClientCapabilities(
    /**
     * The client has support for the showDocument
     * request.
     */
    val support: Boolean
)

/**
 * Client capabilities specific to regular expressions.
 *
 * @since 3.16.0
 */
@Serializable
data class RegularExpressionsClientCapabilities(
    /**
     * The engine's name.
     */
    val engine: String,
    /**
     * The engine's version.
     */
    val version: String? = null
)

/**
 * Client capabilities specific to the used markdown parser.
 *
 * @since 3.16.0
 */
@Serializable
data class MarkdownClientCapabilities(
    /**
     * The name of the parser.
     */
    val parser: String,
    /**
     * The version of the parser.
     */
    val version: String? = null,
    /**
     * A list of HTML tags that the client allows / supports in
     * Markdown.
     *
     * @since 3.17.0
     */
    val allowedTags: List<String>? = null
)
