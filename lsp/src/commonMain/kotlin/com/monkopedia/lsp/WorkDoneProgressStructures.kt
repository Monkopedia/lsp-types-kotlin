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

@kotlinx.serialization.Serializable
data class WorkDoneProgressOptions(val workDoneProgress: Boolean? = null)

@kotlinx.serialization.Serializable
data class WorkDoneProgressCreateParams(
    /**
     * The token to be used to report progress.
     */
    val token: ProgressToken
)

@kotlinx.serialization.Serializable
data class WorkDoneProgressCancelParams(
    /**
     * The token to be used to report progress.
     */
    val token: ProgressToken
)

@kotlinx.serialization.Serializable
data class WorkDoneProgressBegin(
    val kind: String,
    /**
     * Mandatory title of the progress operation. Used to briefly inform about
     * the kind of operation being performed.
     *
     * Examples: "Indexing" or "Linking dependencies".
     */
    val title: String,
    /**
     * Controls if a cancel button should show to allow the user to cancel the
     * long running operation. Clients that don't support cancellation are allowed
     * to ignore the setting.
     */
    val cancellable: Boolean? = null,
    /**
     * Optional, more detailed associated progress message. Contains
     * complementary information to the `title`.
     *
     * Examples: "3/25 files", "project/src/module2", "node_modules/some_dep".
     * If unset, the previous progress message (if any) is still valid.
     */
    val message: String? = null,
    /**
     * Optional progress percentage to display (value 100 is considered 100%).
     * If not provided infinite progress is assumed and clients are allowed
     * to ignore the `percentage` value in subsequent report notifications.
     *
     * The value should be steadily rising. Clients are free to ignore values
     * that are not following this rule. The value range is [0, 100].
     */
    val percentage: UInt? = null
)

@kotlinx.serialization.Serializable
data class WorkDoneProgressReport(
    val kind: String,
    /**
     * Controls enablement state of a cancel button.
     *
     * Clients that don't support cancellation or don't support controlling the button's
     * enablement state are allowed to ignore the property.
     */
    val cancellable: Boolean? = null,
    /**
     * Optional, more detailed associated progress message. Contains
     * complementary information to the `title`.
     *
     * Examples: "3/25 files", "project/src/module2", "node_modules/some_dep".
     * If unset, the previous progress message (if any) is still valid.
     */
    val message: String? = null,
    /**
     * Optional progress percentage to display (value 100 is considered 100%).
     * If not provided infinite progress is assumed and clients are allowed
     * to ignore the `percentage` value in subsequent report notifications.
     *
     * The value should be steadily rising. Clients are free to ignore values
     * that are not following this rule. The value range is [0, 100].
     */
    val percentage: UInt? = null
)

@kotlinx.serialization.Serializable
data class WorkDoneProgressEnd(
    val kind: String,
    /**
     * Optional, a final message indicating to for example indicate the outcome
     * of the operation.
     */
    val message: String? = null
)

@kotlinx.serialization.Serializable
data class WorkDoneProgressParams(
    /**
     * An optional token that a server can use to report work done progress.
     */
    val workDoneToken: ProgressToken? = null
)
