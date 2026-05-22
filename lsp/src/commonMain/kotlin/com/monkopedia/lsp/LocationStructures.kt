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
 * Represents a location inside a resource, such as a line
 * inside a text file.
 */
@Serializable
data class Location(val uri: DocumentUri, val range: Range) : WorkspaceSymbolLocation

/**
 * Represents the connection of two locations. Provides additional metadata over normal {@link Location locations},
 * including an origin range.
 */
@Serializable
data class LocationLink(
    /**
     * Span of the origin of this link.
     *
     * Used as the underlined span for mouse interaction. Defaults to the word range at
     * the definition position.
     */
    val originSelectionRange: Range? = null,
    /**
     * The target resource identifier of this link.
     */
    val targetUri: DocumentUri,
    /**
     * The full target range of this link. If the target for example is a symbol then target range is the
     * range enclosing this symbol not including leading/trailing whitespace but everything else
     * like comments. This information is typically used to highlight the range in the editor.
     */
    val targetRange: Range,
    /**
     * The range that should be selected and revealed when this link is being followed, e.g the name of a function.
     * Must be contained by the `targetRange`. See also `DocumentSymbol#range`
     */
    val targetSelectionRange: Range
)
