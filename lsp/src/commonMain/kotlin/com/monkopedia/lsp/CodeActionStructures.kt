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
 * The parameters of a {@link CodeActionRequest}.
 */
@kotlinx.serialization.Serializable
data class CodeActionParams(
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
     * The document in which the command was invoked.
     */
    val textDocument: TextDocumentIdentifier,
    /**
     * The range for which the command was invoked.
     */
    val range: Range,
    /**
     * Context carrying additional information.
     */
    val context: CodeActionContext
)

/**
 * A code action represents a change that can be performed in code, e.g. to fix a problem or
 * to refactor code.
 *
 * A CodeAction must set either `edit` and/or a `command`. If both are supplied, the `edit` is applied first, then the `command` is executed.
 */
@kotlinx.serialization.Serializable
data class CodeAction(
    /**
     * A short, human-readable, title for this code action.
     */
    val title: String,
    /**
     * The kind of the code action.
     *
     * Used to filter code actions.
     */
    val kind: CodeActionKind? = null,
    /**
     * The diagnostics that this code action resolves.
     */
    val diagnostics: List<Diagnostic>? = null,
    /**
     * Marks this as a preferred action. Preferred actions are used by the `auto fix` command and can be targeted
     * by keybindings.
     *
     * A quick fix should be marked preferred if it properly addresses the underlying error.
     * A refactoring should be marked preferred if it is the most reasonable choice of actions to take.
     *
     * @since 3.15.0
     */
    val isPreferred: Boolean? = null,
    /**
     * Marks that the code action cannot currently be applied.
     *
     * Clients should follow the following guidelines regarding disabled code actions:
     *
     *   - Disabled code actions are not shown in automatic [lightbulbs](https://code.visualstudio.com/docs/editor/editingevolved#_code-action)
     *     code action menus.
     *
     *   - Disabled actions are shown as faded out in the code action menu when the user requests a more specific type
     *     of code action, such as refactorings.
     *
     *   - If the user has a [keybinding](https://code.visualstudio.com/docs/editor/refactoring#_keybindings-for-code-actions)
     *     that auto applies a code action and only disabled code actions are returned, the client should show the user an
     *     error message with `reason` in the editor.
     *
     * @since 3.16.0
     */
    val disabled: CodeActionDisabled? = null,
    /**
     * The workspace edit this code action performs.
     */
    val edit: WorkspaceEdit? = null,
    /**
     * A command this code action executes. If a code action
     * provides an edit and a command, first the edit is
     * executed and then the command.
     */
    val command: Command? = null,
    /**
     * A data entry field that is preserved on a code action between
     * a `textDocument/codeAction` and a `codeAction/resolve` request.
     *
     * @since 3.16.0
     */
    @kotlinx.serialization.SerialName("data") val `data`: LSPAny? = null
)

@kotlinx.serialization.Serializable
data class CodeActionDisabled(
    /**
     * Human readable description of why the code action is currently disabled.
     *
     * This is displayed in the code actions UI.
     */
    val reason: String
)

/**
 * Registration options for a {@link CodeActionRequest}.
 */
@kotlinx.serialization.Serializable
data class CodeActionRegistrationOptions(
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector??,
    val workDoneProgress: Boolean? = null,
    /**
     * CodeActionKinds that this server may return.
     *
     * The list of kinds may be generic, such as `CodeActionKind.Refactor`, or the server
     * may list out every specific kind they provide.
     */
    val codeActionKinds: List<CodeActionKind>? = null,
    /**
     * The server provides support to resolve additional
     * information for a code action.
     *
     * @since 3.16.0
     */
    val resolveProvider: Boolean? = null
)

/**
 * Contains additional diagnostic information about the context in which
 * a {@link CodeActionProvider.provideCodeActions code action} is run.
 */
@kotlinx.serialization.Serializable
data class CodeActionContext(
    /**
     * An array of diagnostics known on the client side overlapping the range provided to the
     * `textDocument/codeAction` request. They are provided so that the server knows which
     * errors are currently presented to the user for the given range. There is no guarantee
     * that these accurately reflect the error state of the resource. The primary parameter
     * to compute code actions is the provided range.
     */
    val diagnostics: List<Diagnostic>,
    /**
     * Requested kind of actions to return.
     *
     * Actions not of this kind are filtered out by the client before being shown. So servers
     * can omit computing them.
     */
    val only: List<CodeActionKind>? = null,
    /**
     * The reason why code actions were requested.
     *
     * @since 3.17.0
     */
    val triggerKind: CodeActionTriggerKind? = null
)

/**
 * Provider options for a {@link CodeActionRequest}.
 */
@kotlinx.serialization.Serializable
data class CodeActionOptions(
    val workDoneProgress: Boolean? = null,
    /**
     * CodeActionKinds that this server may return.
     *
     * The list of kinds may be generic, such as `CodeActionKind.Refactor`, or the server
     * may list out every specific kind they provide.
     */
    val codeActionKinds: List<CodeActionKind>? = null,
    /**
     * The server provides support to resolve additional
     * information for a code action.
     *
     * @since 3.16.0
     */
    val resolveProvider: Boolean? = null
)

/**
 * The Client Capabilities of a {@link CodeActionRequest}.
 */
@kotlinx.serialization.Serializable
data class CodeActionClientCapabilities(
    /**
     * Whether code action supports dynamic registration.
     */
    val dynamicRegistration: Boolean? = null,
    /**
     * The client support code action literals of type `CodeAction` as a valid
     * response of the `textDocument/codeAction` request. If the property is not
     * set the request can only return `Command` literals.
     *
     * @since 3.8.0
     */
    val codeActionLiteralSupport: CodeActionClientCapabilitiesCodeActionLiteralSupport? = null,
    /**
     * Whether code action supports the `isPreferred` property.
     *
     * @since 3.15.0
     */
    val isPreferredSupport: Boolean? = null,
    /**
     * Whether code action supports the `disabled` property.
     *
     * @since 3.16.0
     */
    val disabledSupport: Boolean? = null,
    /**
     * Whether code action supports the `data` property which is
     * preserved between a `textDocument/codeAction` and a
     * `codeAction/resolve` request.
     *
     * @since 3.16.0
     */
    val dataSupport: Boolean? = null,
    /**
     * Whether the client supports resolving additional code action
     * properties via a separate `codeAction/resolve` request.
     *
     * @since 3.16.0
     */
    val resolveSupport: CodeActionClientCapabilitiesResolveSupport? = null,
    /**
     * Whether the client honors the change annotations in
     * text edits and resource operations returned via the
     * `CodeAction#edit` property by for example presenting
     * the workspace edit in the user interface and asking
     * for confirmation.
     *
     * @since 3.16.0
     */
    val honorsChangeAnnotations: Boolean? = null
)

@kotlinx.serialization.Serializable
data class CodeActionClientCapabilitiesCodeActionLiteralSupport(
    /**
     * The code action kind is support with the following value
     * set.
     */
    val codeActionKind: CodeActionClientCapabilitiesCodeActionLiteralSupportCodeActionKind
)

@kotlinx.serialization.Serializable
data class CodeActionClientCapabilitiesCodeActionLiteralSupportCodeActionKind(
    /**
     * The code action kind values the client supports. When this
     * property exists the client also guarantees that it will
     * handle values outside its set gracefully and falls back
     * to a default value when unknown.
     */
    val valueSet: List<CodeActionKind>
)

@kotlinx.serialization.Serializable
data class CodeActionClientCapabilitiesResolveSupport(
    /**
     * The properties that a client can resolve lazily.
     */
    val properties: List<String>
)
