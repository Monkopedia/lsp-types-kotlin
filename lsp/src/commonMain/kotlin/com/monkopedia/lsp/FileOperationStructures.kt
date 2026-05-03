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
 * The options to register for file operations.
 *
 * @since 3.16.0
 */
@kotlinx.serialization.Serializable
data class FileOperationRegistrationOptions(
    /**
     * The actual filters.
     */
    val filters: List<FileOperationFilter>
)

/**
 * A filter to describe in which file operation requests or notifications
 * the server is interested in receiving.
 *
 * @since 3.16.0
 */
@kotlinx.serialization.Serializable
data class FileOperationFilter(
    /**
     * A Uri scheme like `file` or `untitled`.
     */
    val scheme: String? = null,
    /**
     * The actual file operation pattern.
     */
    val pattern: FileOperationPattern
)

/**
 * A pattern to describe in which file operation requests or notifications
 * the server is interested in receiving.
 *
 * @since 3.16.0
 */
@kotlinx.serialization.Serializable
data class FileOperationPattern(
    /**
     * The glob pattern to match. Glob patterns can have the following syntax:
     * - `*` to match zero or more characters in a path segment
     * - `?` to match on one character in a path segment
     * - `**` to match any number of path segments, including none
     * - `{}` to group sub patterns into an OR expression. (e.g. `**​/&#42;.{ts,js}` matches all TypeScript and JavaScript files)
     * - `[]` to declare a range of characters to match in a path segment (e.g., `example.[0-9]` to match on `example.0`, `example.1`, …)
     * - `[!...]` to negate a range of characters to match in a path segment (e.g., `example.[!0-9]` to match on `example.a`, `example.b`, but not `example.0`)
     */
    val glob: String,
    /**
     * Whether to match files or folders with this pattern.
     *
     * Matches both if undefined.
     */
    val matches: FileOperationPatternKind? = null,
    /**
     * Additional options used during matching.
     */
    val options: FileOperationPatternOptions? = null
)

/**
 * Options for notifications/requests for user operations on files.
 *
 * @since 3.16.0
 */
@kotlinx.serialization.Serializable
data class FileOperationOptions(
    /**
     * The server is interested in receiving didCreateFiles notifications.
     */
    val didCreate: FileOperationRegistrationOptions? = null,
    /**
     * The server is interested in receiving willCreateFiles requests.
     */
    val willCreate: FileOperationRegistrationOptions? = null,
    /**
     * The server is interested in receiving didRenameFiles notifications.
     */
    val didRename: FileOperationRegistrationOptions? = null,
    /**
     * The server is interested in receiving willRenameFiles requests.
     */
    val willRename: FileOperationRegistrationOptions? = null,
    /**
     * The server is interested in receiving didDeleteFiles file notifications.
     */
    val didDelete: FileOperationRegistrationOptions? = null,
    /**
     * The server is interested in receiving willDeleteFiles file requests.
     */
    val willDelete: FileOperationRegistrationOptions? = null
)

/**
 * Matching options for the file operation pattern.
 *
 * @since 3.16.0
 */
@kotlinx.serialization.Serializable
data class FileOperationPatternOptions(
    /**
     * The pattern should be matched ignoring casing.
     */
    val ignoreCase: Boolean? = null
)

/**
 * Capabilities relating to events from file operations by the user in the client.
 *
 * These events do not come from the file system, they come from user operations
 * like renaming a file in the UI.
 *
 * @since 3.16.0
 */
@kotlinx.serialization.Serializable
data class FileOperationClientCapabilities(
    /**
     * Whether the client supports dynamic registration for file requests/notifications.
     */
    val dynamicRegistration: Boolean? = null,
    /**
     * The client has support for sending didCreateFiles notifications.
     */
    val didCreate: Boolean? = null,
    /**
     * The client has support for sending willCreateFiles requests.
     */
    val willCreate: Boolean? = null,
    /**
     * The client has support for sending didRenameFiles notifications.
     */
    val didRename: Boolean? = null,
    /**
     * The client has support for sending willRenameFiles requests.
     */
    val willRename: Boolean? = null,
    /**
     * The client has support for sending didDeleteFiles notifications.
     */
    val didDelete: Boolean? = null,
    /**
     * The client has support for sending willDeleteFiles requests.
     */
    val willDelete: Boolean? = null
)
