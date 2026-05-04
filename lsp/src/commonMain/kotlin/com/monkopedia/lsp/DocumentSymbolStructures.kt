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
 * Parameters for a {@link DocumentSymbolRequest}.
 */
@kotlinx.serialization.Serializable
data class DocumentSymbolParams(
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
    val textDocument: TextDocumentIdentifier
)

/**
 * Represents programming constructs like variables, classes, interfaces etc.
 * that appear in a document. Document symbols can be hierarchical and they
 * have two ranges: one that encloses its definition and one that points to
 * its most interesting range, e.g. the range of an identifier.
 */
@kotlinx.serialization.Serializable
data class DocumentSymbol(
    /**
     * The name of this symbol. Will be displayed in the user interface and therefore must not be
     * an empty string or a string only consisting of white spaces.
     */
    val name: String,
    /**
     * More detail for this symbol, e.g the signature of a function.
     */
    val detail: String? = null,
    /**
     * The kind of this symbol.
     */
    val kind: SymbolKind,
    /**
     * Tags for this document symbol.
     *
     * @since 3.16.0
     */
    val tags: List<SymbolTag>? = null,
    /**
     * Indicates if this symbol is deprecated.
     *
     * @deprecated Use tags instead
     */
    val deprecated: Boolean? = null,
    /**
     * The range enclosing this symbol not including leading/trailing whitespace but everything else
     * like comments. This information is typically used to determine if the clients cursor is
     * inside the symbol to reveal in the symbol in the UI.
     */
    val range: Range,
    /**
     * The range that should be selected and revealed when this symbol is being picked, e.g the name of a function.
     * Must be contained by the `range`.
     */
    val selectionRange: Range,
    /**
     * Children of this symbol, e.g. properties of a class.
     */
    val children: List<DocumentSymbol>? = null
)

/**
 * Registration options for a {@link DocumentSymbolRequest}.
 */
@kotlinx.serialization.Serializable
data class DocumentSymbolRegistrationOptions(
    /**
     * A document selector to identify the scope of the registration. If set to null
     * the document selector provided on the client side will be used.
     */
    val documentSelector: DocumentSelector?,
    val workDoneProgress: Boolean? = null,
    /**
     * A human-readable string that is shown when multiple outlines trees
     * are shown for the same document.
     *
     * @since 3.16.0
     */
    val label: String? = null
)

/**
 * Provider options for a {@link DocumentSymbolRequest}.
 */
@kotlinx.serialization.Serializable
data class DocumentSymbolOptions(
    val workDoneProgress: Boolean? = null,
    /**
     * A human-readable string that is shown when multiple outlines trees
     * are shown for the same document.
     *
     * @since 3.16.0
     */
    val label: String? = null
)

/**
 * Client Capabilities for a {@link DocumentSymbolRequest}.
 */
@kotlinx.serialization.Serializable
data class DocumentSymbolClientCapabilities(
    /**
     * Whether document symbol supports dynamic registration.
     */
    val dynamicRegistration: Boolean? = null,
    /**
     * Specific capabilities for the `SymbolKind` in the
     * `textDocument/documentSymbol` request.
     */
    val symbolKind: DocumentSymbolClientCapabilitiesSymbolKind? = null,
    /**
     * The client supports hierarchical document symbols.
     */
    val hierarchicalDocumentSymbolSupport: Boolean? = null,
    /**
     * The client supports tags on `SymbolInformation`. Tags are supported on
     * `DocumentSymbol` if `hierarchicalDocumentSymbolSupport` is set to true.
     * Clients supporting tags have to handle unknown tags gracefully.
     *
     * @since 3.16.0
     */
    val tagSupport: DocumentSymbolClientCapabilitiesTagSupport? = null,
    /**
     * The client supports an additional label presented in the UI when
     * registering a document symbol provider.
     *
     * @since 3.16.0
     */
    val labelSupport: Boolean? = null
)

@kotlinx.serialization.Serializable
data class DocumentSymbolClientCapabilitiesSymbolKind(
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

@kotlinx.serialization.Serializable
data class DocumentSymbolClientCapabilitiesTagSupport(
    /**
     * The tags supported by the client.
     */
    val valueSet: List<SymbolTag>
)
