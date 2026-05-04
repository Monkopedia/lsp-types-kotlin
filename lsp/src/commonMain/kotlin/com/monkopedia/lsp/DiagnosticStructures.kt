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
 * Cancellation data returned from a diagnostic request.
 *
 * @since 3.17.0
 */
@kotlinx.serialization.Serializable
data class DiagnosticServerCancellationData(val retriggerRequest: Boolean)

/**
 * Diagnostic registration options.
 *
 * @since 3.17.0
 */
@kotlinx.serialization.Serializable
data class DiagnosticRegistrationOptions(
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector?,
    val workDoneProgress: Boolean? = null,
    /**
     * An optional identifier under which the diagnostics are
     * managed by the client.
     */
    val identifier: String? = null,
    /**
     * Whether the language has inter file dependencies meaning that
     * editing code in one file can result in a different diagnostic
     * set in another file. Inter file dependencies are common for
     * most programming languages and typically uncommon for linters.
     */
    val interFileDependencies: Boolean,
    /**
     * The server provides support for workspace diagnostics as well.
     */
    val workspaceDiagnostics: Boolean,
    /**
     * The id used to register the request. The id can be used to deregister
     * the request again. See also Registration#id.
     */
    val id: String? = null
) : ServerCapabilitiesDiagnosticProvider

/**
 * Diagnostic options.
 *
 * @since 3.17.0
 */
@kotlinx.serialization.Serializable
data class DiagnosticOptions(
    val workDoneProgress: Boolean? = null,
    /**
     * An optional identifier under which the diagnostics are
     * managed by the client.
     */
    val identifier: String? = null,
    /**
     * Whether the language has inter file dependencies meaning that
     * editing code in one file can result in a different diagnostic
     * set in another file. Inter file dependencies are common for
     * most programming languages and typically uncommon for linters.
     */
    val interFileDependencies: Boolean,
    /**
     * The server provides support for workspace diagnostics as well.
     */
    val workspaceDiagnostics: Boolean
) : ServerCapabilitiesDiagnosticProvider

/**
 * Represents a diagnostic, such as a compiler error or warning. Diagnostic objects
 * are only valid in the scope of a resource.
 */
@kotlinx.serialization.Serializable
data class Diagnostic(
    /**
     * The range at which the message applies
     */
    val range: Range,
    /**
     * The diagnostic's severity. Can be omitted. If omitted it is up to the
     * client to interpret diagnostics as error, warning, info or hint.
     */
    val severity: DiagnosticSeverity? = null,
    /**
     * The diagnostic's code, which usually appear in the user interface.
     */
    val code: IntOrString? = null,
    /**
     * An optional property to describe the error code.
     * Requires the code field (above) to be present/not null.
     *
     * @since 3.16.0
     */
    val codeDescription: CodeDescription? = null,
    /**
     * A human-readable string describing the source of this
     * diagnostic, e.g. 'typescript' or 'super lint'. It usually
     * appears in the user interface.
     */
    val source: String? = null,
    /**
     * The diagnostic's message. It usually appears in the user interface
     */
    val message: String,
    /**
     * Additional metadata about the diagnostic.
     *
     * @since 3.15.0
     */
    val tags: List<DiagnosticTag>? = null,
    /**
     * An array of related diagnostic information, e.g. when symbol-names within
     * a scope collide all definitions can be marked via this property.
     */
    val relatedInformation: List<DiagnosticRelatedInformation>? = null,
    /**
     * A data entry field that is preserved between a `textDocument/publishDiagnostics`
     * notification and `textDocument/codeAction` request.
     *
     * @since 3.16.0
     */
    @kotlinx.serialization.SerialName("data") val `data`: LSPAny? = null
)

/**
 * Represents a related message and source code location for a diagnostic. This should be
 * used to point to code locations that cause or related to a diagnostics, e.g when duplicating
 * a symbol in a scope.
 */
@kotlinx.serialization.Serializable
data class DiagnosticRelatedInformation(
    /**
     * The location of this related diagnostic information.
     */
    val location: Location,
    /**
     * The message of this related diagnostic information.
     */
    val message: String
)

/**
 * Workspace client capabilities specific to diagnostic pull requests.
 *
 * @since 3.17.0
 */
@kotlinx.serialization.Serializable
data class DiagnosticWorkspaceClientCapabilities(
    /**
     * Whether the client implementation supports a refresh request sent from
     * the server to the client.
     *
     * Note that this event is global and will force the client to refresh all
     * pulled diagnostics currently shown. It should be used with absolute care and
     * is useful for situation where a server for example detects a project wide
     * change that requires such a calculation.
     */
    val refreshSupport: Boolean? = null
)

/**
 * Client capabilities specific to diagnostic pull requests.
 *
 * @since 3.17.0
 */
@kotlinx.serialization.Serializable
data class DiagnosticClientCapabilities(
    /**
     * Whether implementation supports dynamic registration. If this is set to `true`
     * the client supports the new `(TextDocumentRegistrationOptions & StaticRegistrationOptions)`
     * return value for the corresponding server capability as well.
     */
    val dynamicRegistration: Boolean? = null,
    /**
     * Whether the clients supports related documents for document diagnostic pulls.
     */
    val relatedDocumentSupport: Boolean? = null
)
