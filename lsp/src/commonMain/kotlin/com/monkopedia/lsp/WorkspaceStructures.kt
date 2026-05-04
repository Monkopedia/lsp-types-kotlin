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
 * A workspace folder inside a client.
 */
@Serializable
data class WorkspaceFolder(
    /**
     * The associated URI for this workspace folder.
     */
    val uri: URI,
    /**
     * The name of the workspace folder. Used to refer to this
     * workspace folder in the user interface.
     */
    val name: String
)

/**
 * A workspace edit represents changes to many resources managed in the workspace. The edit
 * should either provide `changes` or `documentChanges`. If documentChanges are present
 * they are preferred over `changes` if the client can handle versioned document edits.
 *
 * Since version 3.13.0 a workspace edit can contain resource operations as well. If resource
 * operations are present clients need to execute the operations in the order in which they
 * are provided. So a workspace edit for example can consist of the following two changes:
 * (1) a create file a.txt and (2) a text document edit which insert text into file a.txt.
 *
 * An invalid sequence (e.g. (1) delete file a.txt and (2) insert text into file a.txt) will
 * cause failure of the operation. How the client recovers from the failure is described by
 * the client capability: `workspace.workspaceEdit.failureHandling`
 */
@Serializable
data class WorkspaceEdit(
    /**
     * Holds changes to existing resources.
     */
    val changes: Map<DocumentUri, List<TextEdit>>? = null,
    /**
     * Depending on the client capability `workspace.workspaceEdit.resourceOperations` document changes
     * are either an array of `TextDocumentEdit`s to express changes to n different text documents
     * where each text document edit addresses a specific version of a text document. Or it can contain
     * above `TextDocumentEdit`s mixed with create, rename and delete file / folder operations.
     *
     * Whether a client supports versioned document edits is expressed via
     * `workspace.workspaceEdit.documentChanges` client capability.
     *
     * If a client neither supports `documentChanges` nor `workspace.workspaceEdit.resourceOperations` then
     * only plain `TextEdit`s using the `changes` property are supported.
     */
    val documentChanges: List<WorkspaceEditDocumentChanges>? = null,
    /**
     * A map of change annotations that can be referenced in `AnnotatedTextEdit`s or create, rename and
     * delete file / folder operations.
     *
     * Whether clients honor this property depends on the client capability `workspace.changeAnnotationSupport`.
     *
     * @since 3.16.0
     */
    val changeAnnotations: Map<ChangeAnnotationIdentifier, ChangeAnnotation>? = null
)

/**
 * Parameters of the workspace diagnostic request.
 *
 * @since 3.17.0
 */
@Serializable
data class WorkspaceDiagnosticParams(
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
     * The additional identifier provided during registration.
     */
    val identifier: String? = null,
    /**
     * The currently known diagnostic reports with their
     * previous result ids.
     */
    val previousResultIds: List<PreviousResultId>
)

/**
 * A workspace diagnostic report.
 *
 * @since 3.17.0
 */
@Serializable
data class WorkspaceDiagnosticReport(val items: List<WorkspaceDocumentDiagnosticReport>)

/**
 * A partial result for a workspace diagnostic report.
 *
 * @since 3.17.0
 */
@Serializable
data class WorkspaceDiagnosticReportPartialResult(
    val items: List<WorkspaceDocumentDiagnosticReport>
)

/**
 * The parameters of a {@link WorkspaceSymbolRequest}.
 */
@Serializable
data class WorkspaceSymbolParams(
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
     * A query string to filter symbols by. Clients may send an empty
     * string here to request all symbols.
     */
    val query: String
)

/**
 * A special workspace symbol that supports locations without a range.
 *
 * See also SymbolInformation.
 *
 * @since 3.17.0
 */
@Serializable
data class WorkspaceSymbol(
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
     * The location of the symbol. Whether a server is allowed to
     * return a location without a range depends on the client
     * capability `workspace.symbol.resolveSupport`.
     *
     * See SymbolInformation#location for more details.
     */
    val location: JsonElement,
    /**
     * A data entry field that is preserved on a workspace symbol between a
     * workspace symbol request and a workspace symbol resolve request.
     */
    @SerialName("data") val `data`: LSPAny? = null
)

/**
 * Registration options for a {@link WorkspaceSymbolRequest}.
 */
@Serializable
data class WorkspaceSymbolRegistrationOptions(
    val workDoneProgress: Boolean? = null,
    /**
     * The server provides support to resolve additional
     * information for a workspace symbol.
     *
     * @since 3.17.0
     */
    val resolveProvider: Boolean? = null
)

/**
 * The workspace folder change event.
 */
@Serializable
data class WorkspaceFoldersChangeEvent(
    /**
     * The array of added workspace folders
     */
    val added: List<WorkspaceFolder>,
    /**
     * The array of the removed workspace folders
     */
    val removed: List<WorkspaceFolder>
)

@Serializable
data class WorkspaceFoldersInitializeParams(
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

/**
 * Server capabilities for a {@link WorkspaceSymbolRequest}.
 */
@Serializable
data class WorkspaceSymbolOptions(
    val workDoneProgress: Boolean? = null,
    /**
     * The server provides support to resolve additional
     * information for a workspace symbol.
     *
     * @since 3.17.0
     */
    val resolveProvider: Boolean? = null
)

/**
 * A full document diagnostic report for a workspace diagnostic result.
 *
 * @since 3.17.0
 */
@Serializable
data class WorkspaceFullDocumentDiagnosticReport(
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
     * The URI for which diagnostic information is reported.
     */
    val uri: DocumentUri,
    /**
     * The version number for which the diagnostics are reported.
     * If the document is not marked as open `null` can be provided.
     */
    val version: Int?
) : WorkspaceDocumentDiagnosticReport

/**
 * An unchanged document diagnostic report for a workspace diagnostic result.
 *
 * @since 3.17.0
 */
@Serializable
data class WorkspaceUnchangedDocumentDiagnosticReport(
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
     * The URI for which diagnostic information is reported.
     */
    val uri: DocumentUri,
    /**
     * The version number for which the diagnostics are reported.
     * If the document is not marked as open `null` can be provided.
     */
    val version: Int?
) : WorkspaceDocumentDiagnosticReport

@Serializable
data class WorkspaceFoldersServerCapabilities(
    /**
     * The server has support for workspace folders
     */
    val supported: Boolean? = null,
    /**
     * Whether the server wants to receive workspace folder
     * change notifications.
     *
     * If a string is provided the string is treated as an ID
     * under which the notification is registered on the client
     * side. The ID can be used to unregister for these events
     * using the `client/unregisterCapability` request.
     */
    val changeNotifications: JsonElement? = null
)

/**
 * Workspace specific client capabilities.
 */
@Serializable
data class WorkspaceClientCapabilities(
    /**
     * The client supports applying batch edits
     * to the workspace by supporting the request
     * 'workspace/applyEdit'
     */
    val applyEdit: Boolean? = null,
    /**
     * Capabilities specific to `WorkspaceEdit`s.
     */
    val workspaceEdit: WorkspaceEditClientCapabilities? = null,
    /**
     * Capabilities specific to the `workspace/didChangeConfiguration` notification.
     */
    val didChangeConfiguration: DidChangeConfigurationClientCapabilities? = null,
    /**
     * Capabilities specific to the `workspace/didChangeWatchedFiles` notification.
     */
    val didChangeWatchedFiles: DidChangeWatchedFilesClientCapabilities? = null,
    /**
     * Capabilities specific to the `workspace/symbol` request.
     */
    val symbol: WorkspaceSymbolClientCapabilities? = null,
    /**
     * Capabilities specific to the `workspace/executeCommand` request.
     */
    val executeCommand: ExecuteCommandClientCapabilities? = null,
    /**
     * The client has support for workspace folders.
     *
     * @since 3.6.0
     */
    val workspaceFolders: Boolean? = null,
    /**
     * The client supports `workspace/configuration` requests.
     *
     * @since 3.6.0
     */
    val configuration: Boolean? = null,
    /**
     * Capabilities specific to the semantic token requests scoped to the
     * workspace.
     *
     * @since 3.16.0.
     */
    val semanticTokens: SemanticTokensWorkspaceClientCapabilities? = null,
    /**
     * Capabilities specific to the code lens requests scoped to the
     * workspace.
     *
     * @since 3.16.0.
     */
    val codeLens: CodeLensWorkspaceClientCapabilities? = null,
    /**
     * The client has support for file notifications/requests for user operations on files.
     *
     * Since 3.16.0
     */
    val fileOperations: FileOperationClientCapabilities? = null,
    /**
     * Capabilities specific to the inline values requests scoped to the
     * workspace.
     *
     * @since 3.17.0.
     */
    val inlineValue: InlineValueWorkspaceClientCapabilities? = null,
    /**
     * Capabilities specific to the inlay hint requests scoped to the
     * workspace.
     *
     * @since 3.17.0.
     */
    val inlayHint: InlayHintWorkspaceClientCapabilities? = null,
    /**
     * Capabilities specific to the diagnostic requests scoped to the
     * workspace.
     *
     * @since 3.17.0.
     */
    val diagnostics: DiagnosticWorkspaceClientCapabilities? = null,
    /**
     * Capabilities specific to the folding range requests scoped to the workspace.
     *
     * @since 3.18.0
     * @proposed
     */
    val foldingRange: FoldingRangeWorkspaceClientCapabilities? = null
)

@Serializable
data class WorkspaceEditClientCapabilities(
    /**
     * The client supports versioned document changes in `WorkspaceEdit`s
     */
    val documentChanges: Boolean? = null,
    /**
     * The resource operations the client supports. Clients should at least
     * support 'create', 'rename' and 'delete' files and folders.
     *
     * @since 3.13.0
     */
    val resourceOperations: List<ResourceOperationKind>? = null,
    /**
     * The failure handling strategy of a client if applying the workspace edit
     * fails.
     *
     * @since 3.13.0
     */
    val failureHandling: FailureHandlingKind? = null,
    /**
     * Whether the client normalizes line endings to the client specific
     * setting.
     * If set to `true` the client will normalize line ending characters
     * in a workspace edit to the client-specified new line
     * character.
     *
     * @since 3.16.0
     */
    val normalizesLineEndings: Boolean? = null,
    /**
     * Whether the client in general supports change annotations on text edits,
     * create file, rename file and delete file changes.
     *
     * @since 3.16.0
     */
    val changeAnnotationSupport: WorkspaceEditClientCapabilitiesChangeAnnotationSupport? = null
)

@Serializable
data class WorkspaceEditClientCapabilitiesChangeAnnotationSupport(
    /**
     * Whether the client groups edits with equal labels into tree nodes,
     * for instance all edits labelled with "Changes in Strings" would
     * be a tree node.
     */
    val groupsOnLabel: Boolean? = null
)

/**
 * Client capabilities for a {@link WorkspaceSymbolRequest}.
 */
@Serializable
data class WorkspaceSymbolClientCapabilities(
    /**
     * Symbol request supports dynamic registration.
     */
    val dynamicRegistration: Boolean? = null,
    /**
     * Specific capabilities for the `SymbolKind` in the `workspace/symbol` request.
     */
    val symbolKind: WorkspaceSymbolClientCapabilitiesSymbolKind? = null,
    /**
     * The client supports tags on `SymbolInformation`.
     * Clients supporting tags have to handle unknown tags gracefully.
     *
     * @since 3.16.0
     */
    val tagSupport: WorkspaceSymbolClientCapabilitiesTagSupport? = null,
    /**
     * The client support partial workspace symbols. The client will send the
     * request `workspaceSymbol/resolve` to the server to resolve additional
     * properties.
     *
     * @since 3.17.0
     */
    val resolveSupport: WorkspaceSymbolClientCapabilitiesResolveSupport? = null
)

@Serializable
data class WorkspaceSymbolClientCapabilitiesResolveSupport(
    /**
     * The properties that a client can resolve lazily. Usually
     * `location.range`
     */
    val properties: List<String>
)

@Serializable
data class WorkspaceSymbolClientCapabilitiesSymbolKind(
    /**
     * The symbol kind values the client supports. When this
     * property exists the client also guarantees that it will
     * handle values outside its set gracefully and falls back
     * to a default value when unknown.
     *
     * If this property is not present the client only supports
     * the symbol kinds from `File` to `Array` as defined in
     * the initial version of the protocol.
     */
    val valueSet: List<SymbolKind>? = null
)

@Serializable
data class WorkspaceSymbolClientCapabilitiesTagSupport(
    /**
     * The tags supported by the client.
     */
    val valueSet: List<SymbolTag>
)
