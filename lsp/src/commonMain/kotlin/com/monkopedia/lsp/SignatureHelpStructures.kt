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
 * Parameters for a {@link SignatureHelpRequest}.
 */
@Serializable
data class SignatureHelpParams(
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
     * The signature help context. This is only available if the client specifies
     * to send this using the client capability `textDocument.signatureHelp.contextSupport === true`
     *
     * @since 3.15.0
     */
    val context: SignatureHelpContext? = null
)

/**
 * Signature help represents the signature of something
 * callable. There can be multiple signature but only one
 * active and only one active parameter.
 */
@Serializable
data class SignatureHelp(
    /**
     * One or more signatures.
     */
    val signatures: List<SignatureInformation>,
    /**
     * The active signature. If omitted or the value lies outside the
     * range of `signatures` the value defaults to zero or is ignored if
     * the `SignatureHelp` has no signatures.
     *
     * Whenever possible implementors should make an active decision about
     * the active signature and shouldn't rely on a default value.
     *
     * In future version of the protocol this property might become
     * mandatory to better express this.
     */
    val activeSignature: UInt? = null,
    /**
     * The active parameter of the active signature. If omitted or the value
     * lies outside the range of `signatures[activeSignature].parameters`
     * defaults to 0 if the active signature has parameters. If
     * the active signature has no parameters it is ignored.
     * In future version of the protocol this property might become
     * mandatory to better express the active parameter if the
     * active signature does have any.
     */
    val activeParameter: UInt? = null
)

/**
 * Registration options for a {@link SignatureHelpRequest}.
 */
@Serializable
data class SignatureHelpRegistrationOptions(
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector? = null,
    val workDoneProgress: Boolean? = null,
    /**
     * List of characters that trigger signature help automatically.
     */
    val triggerCharacters: List<String>? = null,
    /**
     * List of characters that re-trigger signature help.
     *
     * These trigger characters are only active when signature help is already showing. All trigger characters
     * are also counted as re-trigger characters.
     *
     * @since 3.15.0
     */
    val retriggerCharacters: List<String>? = null
)

/**
 * Additional information about the context in which a signature help request was triggered.
 *
 * @since 3.15.0
 */
@Serializable
data class SignatureHelpContext(
    /**
     * Action that caused signature help to be triggered.
     */
    val triggerKind: SignatureHelpTriggerKind,
    /**
     * Character that caused signature help to be triggered.
     *
     * This is undefined when `triggerKind !== SignatureHelpTriggerKind.TriggerCharacter`
     */
    val triggerCharacter: String? = null,
    /**
     * `true` if signature help was already showing when it was triggered.
     *
     * Retriggers occurs when the signature help is already active and can be caused by actions such as
     * typing a trigger character, a cursor move, or document content changes.
     */
    val isRetrigger: Boolean,
    /**
     * The currently active `SignatureHelp`.
     *
     * The `activeSignatureHelp` has its `SignatureHelp.activeSignature` field updated based on
     * the user navigating through available signatures.
     */
    val activeSignatureHelp: SignatureHelp? = null
)

/**
 * Server Capabilities for a {@link SignatureHelpRequest}.
 */
@Serializable
data class SignatureHelpOptions(
    val workDoneProgress: Boolean? = null,
    /**
     * List of characters that trigger signature help automatically.
     */
    val triggerCharacters: List<String>? = null,
    /**
     * List of characters that re-trigger signature help.
     *
     * These trigger characters are only active when signature help is already showing. All trigger characters
     * are also counted as re-trigger characters.
     *
     * @since 3.15.0
     */
    val retriggerCharacters: List<String>? = null
)

/**
 * Client Capabilities for a {@link SignatureHelpRequest}.
 */
@Serializable
data class SignatureHelpClientCapabilities(
    /**
     * Whether signature help supports dynamic registration.
     */
    val dynamicRegistration: Boolean? = null,
    /**
     * The client supports the following `SignatureInformation`
     * specific properties.
     */
    val signatureInformation: SignatureHelpClientCapabilitiesSignatureInformation? = null,
    /**
     * The client supports to send additional context information for a
     * `textDocument/signatureHelp` request. A client that opts into
     * contextSupport will also support the `retriggerCharacters` on
     * `SignatureHelpOptions`.
     *
     * @since 3.15.0
     */
    val contextSupport: Boolean? = null
)

@Serializable
data class SignatureHelpClientCapabilitiesSignatureInformation(
    /**
     * Client supports the following content formats for the documentation
     * property. The order describes the preferred format of the client.
     */
    val documentationFormat: List<MarkupKind>? = null,
    /**
     * Client capabilities specific to parameter information.
     */
    val parameterInformation: SignatureHelpClientCapabilitiesSignatureInformationParameterInformation? = null,
    /**
     * The client supports the `activeParameter` property on `SignatureInformation`
     * literal.
     *
     * @since 3.16.0
     */
    val activeParameterSupport: Boolean? = null
)

@Serializable
data class SignatureHelpClientCapabilitiesSignatureInformationParameterInformation(
    /**
     * The client supports processing label offsets instead of a
     * simple label string.
     *
     * @since 3.14.0
     */
    val labelOffsetSupport: Boolean? = null
)
