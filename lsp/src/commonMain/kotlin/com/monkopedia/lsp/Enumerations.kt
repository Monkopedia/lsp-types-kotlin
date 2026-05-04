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
 * A set of predefined token types. This set is not fixed
 * an clients can specify additional token types via the
 * corresponding client capabilities.
 *
 * @since 3.16.0
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class SemanticTokenTypes(val value: String) {
    companion object {
        val NAMESPACE = SemanticTokenTypes("namespace")

        /**
         * Represents a generic type. Acts as a fallback for types which can't be mapped to
         * a specific type like class or enum.
         */
        val TYPE = SemanticTokenTypes("type")
        val `CLASS` = SemanticTokenTypes("class")
        val ENUM = SemanticTokenTypes("enum")
        val `INTERFACE` = SemanticTokenTypes("interface")
        val STRUCT = SemanticTokenTypes("struct")
        val TYPE_PARAMETER = SemanticTokenTypes("typeParameter")
        val PARAMETER = SemanticTokenTypes("parameter")
        val VARIABLE = SemanticTokenTypes("variable")
        val PROPERTY = SemanticTokenTypes("property")
        val ENUM_MEMBER = SemanticTokenTypes("enumMember")
        val EVENT = SemanticTokenTypes("event")
        val FUNCTION = SemanticTokenTypes("function")
        val METHOD = SemanticTokenTypes("method")
        val MACRO = SemanticTokenTypes("macro")
        val KEYWORD = SemanticTokenTypes("keyword")
        val MODIFIER = SemanticTokenTypes("modifier")
        val COMMENT = SemanticTokenTypes("comment")
        val STRING = SemanticTokenTypes("string")
        val NUMBER = SemanticTokenTypes("number")
        val REGEXP = SemanticTokenTypes("regexp")
        val OPERATOR = SemanticTokenTypes("operator")

        /**
         * @since 3.17.0
         */
        val DECORATOR = SemanticTokenTypes("decorator")
    }
}

/**
 * A set of predefined token modifiers. This set is not fixed
 * an clients can specify additional token types via the
 * corresponding client capabilities.
 *
 * @since 3.16.0
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class SemanticTokenModifiers(val value: String) {
    companion object {
        val DECLARATION = SemanticTokenModifiers("declaration")
        val DEFINITION = SemanticTokenModifiers("definition")
        val READONLY = SemanticTokenModifiers("readonly")
        val STATIC = SemanticTokenModifiers("static")
        val DEPRECATED = SemanticTokenModifiers("deprecated")
        val ABSTRACT = SemanticTokenModifiers("abstract")
        val ASYNC = SemanticTokenModifiers("async")
        val MODIFICATION = SemanticTokenModifiers("modification")
        val DOCUMENTATION = SemanticTokenModifiers("documentation")
        val DEFAULT_LIBRARY = SemanticTokenModifiers("defaultLibrary")
    }
}

/**
 * The document diagnostic report kinds.
 *
 * @since 3.17.0
 */
@kotlinx.serialization.Serializable
enum class DocumentDiagnosticReportKind {
    /**
     * A diagnostic report with a full
     * set of problems.
     */
    @kotlinx.serialization.SerialName("full")
    FULL,

    /**
     * A report indicating that the last
     * returned report is still accurate.
     */
    @kotlinx.serialization.SerialName("unchanged")
    UNCHANGED
}

/**
 * Predefined error codes.
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class ErrorCodes(val value: Int) {
    companion object {
        val PARSE_ERROR = ErrorCodes(-32700)
        val INVALID_REQUEST = ErrorCodes(-32600)
        val METHOD_NOT_FOUND = ErrorCodes(-32601)
        val INVALID_PARAMS = ErrorCodes(-32602)
        val INTERNAL_ERROR = ErrorCodes(-32603)

        /**
         * Error code indicating that a server received a notification or
         * request before the server has received the `initialize` request.
         */
        val SERVER_NOT_INITIALIZED = ErrorCodes(-32002)
        val UNKNOWN_ERROR_CODE = ErrorCodes(-32001)
    }
}

@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class LSPErrorCodes(val value: Int) {
    companion object {
        /**
         * A request failed but it was syntactically correct, e.g the
         * method name was known and the parameters were valid. The error
         * message should contain human readable information about why
         * the request failed.
         *
         * @since 3.17.0
         */
        val REQUEST_FAILED = LSPErrorCodes(-32803)

        /**
         * The server cancelled the request. This error code should
         * only be used for requests that explicitly support being
         * server cancellable.
         *
         * @since 3.17.0
         */
        val SERVER_CANCELLED = LSPErrorCodes(-32802)

        /**
         * The server detected that the content of a document got
         * modified outside normal conditions. A server should
         * NOT send this error code if it detects a content change
         * in it unprocessed messages. The result even computed
         * on an older state might still be useful for the client.
         *
         * If a client decides that a result is not of any use anymore
         * the client should cancel the request.
         */
        val CONTENT_MODIFIED = LSPErrorCodes(-32801)

        /**
         * The client has canceled a request and a server has detected
         * the cancel.
         */
        val REQUEST_CANCELLED = LSPErrorCodes(-32800)
    }
}

/**
 * A set of predefined range kinds.
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class FoldingRangeKind(val value: String) {
    companion object {
        /**
         * Folding range for a comment
         */
        val COMMENT = FoldingRangeKind("comment")

        /**
         * Folding range for an import or include
         */
        val IMPORTS = FoldingRangeKind("imports")

        /**
         * Folding range for a region (e.g. `#region`)
         */
        val REGION = FoldingRangeKind("region")
    }
}

/**
 * A symbol kind.
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class SymbolKind(val value: UInt) {
    companion object {
        val FILE = SymbolKind(1u)
        val MODULE = SymbolKind(2u)
        val NAMESPACE = SymbolKind(3u)
        val PACKAGE = SymbolKind(4u)
        val `CLASS` = SymbolKind(5u)
        val METHOD = SymbolKind(6u)
        val PROPERTY = SymbolKind(7u)
        val FIELD = SymbolKind(8u)
        val CONSTRUCTOR = SymbolKind(9u)
        val ENUM = SymbolKind(10u)
        val `INTERFACE` = SymbolKind(11u)
        val FUNCTION = SymbolKind(12u)
        val VARIABLE = SymbolKind(13u)
        val CONSTANT = SymbolKind(14u)
        val STRING = SymbolKind(15u)
        val NUMBER = SymbolKind(16u)
        val BOOLEAN = SymbolKind(17u)
        val ARRAY = SymbolKind(18u)
        val OBJECT = SymbolKind(19u)
        val KEY = SymbolKind(20u)
        val NULL = SymbolKind(21u)
        val ENUM_MEMBER = SymbolKind(22u)
        val STRUCT = SymbolKind(23u)
        val EVENT = SymbolKind(24u)
        val OPERATOR = SymbolKind(25u)
        val TYPE_PARAMETER = SymbolKind(26u)
    }
}

/**
 * Symbol tags are extra annotations that tweak the rendering of a symbol.
 *
 * @since 3.16
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class SymbolTag(val value: UInt) {
    companion object {
        /**
         * Render a symbol as obsolete, usually using a strike-out.
         */
        val DEPRECATED = SymbolTag(1u)
    }
}

/**
 * Moniker uniqueness level to define scope of the moniker.
 *
 * @since 3.16.0
 */
@kotlinx.serialization.Serializable
enum class UniquenessLevel {
    /**
     * The moniker is only unique inside a document
     */
    @kotlinx.serialization.SerialName("document")
    DOCUMENT,

    /**
     * The moniker is unique inside a project for which a dump got created
     */
    @kotlinx.serialization.SerialName("project")
    PROJECT,

    /**
     * The moniker is unique inside the group to which a project belongs
     */
    @kotlinx.serialization.SerialName("group")
    GROUP,

    /**
     * The moniker is unique inside the moniker scheme.
     */
    @kotlinx.serialization.SerialName("scheme")
    SCHEME,

    /**
     * The moniker is globally unique
     */
    @kotlinx.serialization.SerialName("global")
    GLOBAL
}

/**
 * The moniker kind.
 *
 * @since 3.16.0
 */
@kotlinx.serialization.Serializable
enum class MonikerKind {
    /**
     * The moniker represent a symbol that is imported into a project
     */
    @kotlinx.serialization.SerialName("import")
    IMPORT,

    /**
     * The moniker represents a symbol that is exported from a project
     */
    @kotlinx.serialization.SerialName("export")
    EXPORT,

    /**
     * The moniker represents a symbol that is local to a project (e.g. a local
     * variable of a function, a class not visible outside the project, ...)
     */
    @kotlinx.serialization.SerialName("local")
    LOCAL
}

/**
 * Inlay hint kinds.
 *
 * @since 3.17.0
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class InlayHintKind(val value: UInt) {
    companion object {
        /**
         * An inlay hint that for a type annotation.
         */
        val TYPE = InlayHintKind(1u)

        /**
         * An inlay hint that is for a parameter.
         */
        val PARAMETER = InlayHintKind(2u)
    }
}

/**
 * The message type
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class MessageType(val value: UInt) {
    companion object {
        /**
         * An error message.
         */
        val ERROR = MessageType(1u)

        /**
         * A warning message.
         */
        val WARNING = MessageType(2u)

        /**
         * An information message.
         */
        val INFO = MessageType(3u)

        /**
         * A log message.
         */
        val LOG = MessageType(4u)

        /**
         * A debug message.
         *
         * @since 3.18.0
         */
        val DEBUG = MessageType(5u)
    }
}

/**
 * Defines how the host (editor) should sync
 * document changes to the language server.
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class TextDocumentSyncKind(val value: UInt) {
    companion object {
        /**
         * Documents should not be synced at all.
         */
        val NONE = TextDocumentSyncKind(0u)

        /**
         * Documents are synced by always sending the full content
         * of the document.
         */
        val FULL = TextDocumentSyncKind(1u)

        /**
         * Documents are synced by sending the full content on open.
         * After that only incremental updates to the document are
         * send.
         */
        val INCREMENTAL = TextDocumentSyncKind(2u)
    }
}

/**
 * Represents reasons why a text document is saved.
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class TextDocumentSaveReason(val value: UInt) {
    companion object {
        /**
         * Manually triggered, e.g. by the user pressing save, by starting debugging,
         * or by an API call.
         */
        val MANUAL = TextDocumentSaveReason(1u)

        /**
         * Automatic after a delay.
         */
        val AFTER_DELAY = TextDocumentSaveReason(2u)

        /**
         * When the editor lost focus.
         */
        val FOCUS_OUT = TextDocumentSaveReason(3u)
    }
}

/**
 * The kind of a completion entry.
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class CompletionItemKind(val value: UInt) {
    companion object {
        val TEXT = CompletionItemKind(1u)
        val METHOD = CompletionItemKind(2u)
        val FUNCTION = CompletionItemKind(3u)
        val CONSTRUCTOR = CompletionItemKind(4u)
        val FIELD = CompletionItemKind(5u)
        val VARIABLE = CompletionItemKind(6u)
        val `CLASS` = CompletionItemKind(7u)
        val `INTERFACE` = CompletionItemKind(8u)
        val MODULE = CompletionItemKind(9u)
        val PROPERTY = CompletionItemKind(10u)
        val UNIT = CompletionItemKind(11u)
        val VALUE = CompletionItemKind(12u)
        val ENUM = CompletionItemKind(13u)
        val KEYWORD = CompletionItemKind(14u)
        val SNIPPET = CompletionItemKind(15u)
        val COLOR = CompletionItemKind(16u)
        val FILE = CompletionItemKind(17u)
        val REFERENCE = CompletionItemKind(18u)
        val FOLDER = CompletionItemKind(19u)
        val ENUM_MEMBER = CompletionItemKind(20u)
        val CONSTANT = CompletionItemKind(21u)
        val STRUCT = CompletionItemKind(22u)
        val EVENT = CompletionItemKind(23u)
        val OPERATOR = CompletionItemKind(24u)
        val TYPE_PARAMETER = CompletionItemKind(25u)
    }
}

/**
 * Completion item tags are extra annotations that tweak the rendering of a completion
 * item.
 *
 * @since 3.15.0
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class CompletionItemTag(val value: UInt) {
    companion object {
        /**
         * Render a completion as obsolete, usually using a strike-out.
         */
        val DEPRECATED = CompletionItemTag(1u)
    }
}

/**
 * Defines whether the insert text in a completion item should be interpreted as
 * plain text or a snippet.
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class InsertTextFormat(val value: UInt) {
    companion object {
        /**
         * The primary text to be inserted is treated as a plain string.
         */
        val PLAIN_TEXT = InsertTextFormat(1u)

        /**
         * The primary text to be inserted is treated as a snippet.
         *
         * A snippet can define tab stops and placeholders with `$1`, `$2`
         * and `${3:foo}`. `$0` defines the final tab stop, it defaults to
         * the end of the snippet. Placeholders with equal identifiers are linked,
         * that is typing in one will update others too.
         *
         * See also: https://microsoft.github.io/language-server-protocol/specifications/specification-current/#snippet_syntax
         */
        val SNIPPET = InsertTextFormat(2u)
    }
}

/**
 * How whitespace and indentation is handled during completion
 * item insertion.
 *
 * @since 3.16.0
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class InsertTextMode(val value: UInt) {
    companion object {
        /**
         * The insertion or replace strings is taken as it is. If the
         * value is multi line the lines below the cursor will be
         * inserted using the indentation defined in the string value.
         * The client will not apply any kind of adjustments to the
         * string.
         */
        val AS_IS = InsertTextMode(1u)

        /**
         * The editor adjusts leading whitespace of new lines so that
         * they match the indentation up to the cursor of the line for
         * which the item is accepted.
         *
         * Consider a line like this: <2tabs><cursor><3tabs>foo. Accepting a
         * multi line completion item is indented using 2 tabs and all
         * following lines inserted will be indented using 2 tabs as well.
         */
        val ADJUST_INDENTATION = InsertTextMode(2u)
    }
}

/**
 * A document highlight kind.
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class DocumentHighlightKind(val value: UInt) {
    companion object {
        /**
         * A textual occurrence.
         */
        val TEXT = DocumentHighlightKind(1u)

        /**
         * Read-access of a symbol, like reading a variable.
         */
        val READ = DocumentHighlightKind(2u)

        /**
         * Write-access of a symbol, like writing to a variable.
         */
        val WRITE = DocumentHighlightKind(3u)
    }
}

/**
 * A set of predefined code action kinds
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class CodeActionKind(val value: String) {
    companion object {
        /**
         * Empty kind.
         */
        val EMPTY = CodeActionKind("")

        /**
         * Base kind for quickfix actions: 'quickfix'
         */
        val QUICK_FIX = CodeActionKind("quickfix")

        /**
         * Base kind for refactoring actions: 'refactor'
         */
        val REFACTOR = CodeActionKind("refactor")

        /**
         * Base kind for refactoring extraction actions: 'refactor.extract'
         *
         * Example extract actions:
         *
         * - Extract method
         * - Extract function
         * - Extract variable
         * - Extract interface from class
         * - ...
         */
        val REFACTOR_EXTRACT = CodeActionKind("refactor.extract")

        /**
         * Base kind for refactoring inline actions: 'refactor.inline'
         *
         * Example inline actions:
         *
         * - Inline function
         * - Inline variable
         * - Inline constant
         * - ...
         */
        val REFACTOR_INLINE = CodeActionKind("refactor.inline")

        /**
         * Base kind for refactoring rewrite actions: 'refactor.rewrite'
         *
         * Example rewrite actions:
         *
         * - Convert JavaScript function to class
         * - Add or remove parameter
         * - Encapsulate field
         * - Make method static
         * - Move method to base class
         * - ...
         */
        val REFACTOR_REWRITE = CodeActionKind("refactor.rewrite")

        /**
         * Base kind for source actions: `source`
         *
         * Source code actions apply to the entire file.
         */
        val SOURCE = CodeActionKind("source")

        /**
         * Base kind for an organize imports source action: `source.organizeImports`
         */
        val SOURCE_ORGANIZE_IMPORTS = CodeActionKind("source.organizeImports")

        /**
         * Base kind for auto-fix source actions: `source.fixAll`.
         *
         * Fix all actions automatically fix errors that have a clear fix that do not require user input.
         * They should not suppress errors or perform unsafe fixes such as generating new types or classes.
         *
         * @since 3.15.0
         */
        val SOURCE_FIX_ALL = CodeActionKind("source.fixAll")
    }
}

@kotlinx.serialization.Serializable
enum class TraceValues {
    /**
     * Turn tracing off.
     */
    @kotlinx.serialization.SerialName("off")
    OFF,

    /**
     * Trace messages only.
     */
    @kotlinx.serialization.SerialName("messages")
    MESSAGES,

    /**
     * Verbose message tracing.
     */
    @kotlinx.serialization.SerialName("verbose")
    VERBOSE
}

/**
 * Describes the content type that a client supports in various
 * result literals like `Hover`, `ParameterInfo` or `CompletionItem`.
 *
 * Please note that `MarkupKinds` must not start with a `$`. This kinds
 * are reserved for internal usage.
 */
@kotlinx.serialization.Serializable
enum class MarkupKind {
    /**
     * Plain text is supported as a content format
     */
    @kotlinx.serialization.SerialName("plaintext")
    PLAIN_TEXT,

    /**
     * Markdown is supported as a content format
     */
    @kotlinx.serialization.SerialName("markdown")
    MARKDOWN
}

/**
 * Describes how an {@link InlineCompletionItemProvider inline completion provider} was triggered.
 *
 * @since 3.18.0
 * @proposed
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class InlineCompletionTriggerKind(val value: UInt) {
    companion object {
        /**
         * Completion was triggered explicitly by a user gesture.
         */
        val INVOKED = InlineCompletionTriggerKind(0u)

        /**
         * Completion was triggered automatically while editing.
         */
        val AUTOMATIC = InlineCompletionTriggerKind(1u)
    }
}

/**
 * A set of predefined position encoding kinds.
 *
 * @since 3.17.0
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class PositionEncodingKind(val value: String) {
    companion object {
        /**
         * Character offsets count UTF-8 code units (e.g. bytes).
         */
        val UTF8 = PositionEncodingKind("utf-8")

        /**
         * Character offsets count UTF-16 code units.
         *
         * This is the default and must always be supported
         * by servers
         */
        val UTF16 = PositionEncodingKind("utf-16")

        /**
         * Character offsets count UTF-32 code units.
         *
         * Implementation note: these are the same as Unicode codepoints,
         * so this `PositionEncodingKind` may also be used for an
         * encoding-agnostic representation of character offsets.
         */
        val UTF32 = PositionEncodingKind("utf-32")
    }
}

/**
 * The file event type
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class FileChangeType(val value: UInt) {
    companion object {
        /**
         * The file got created.
         */
        val CREATED = FileChangeType(1u)

        /**
         * The file got changed.
         */
        val CHANGED = FileChangeType(2u)

        /**
         * The file got deleted.
         */
        val DELETED = FileChangeType(3u)
    }
}

@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class WatchKind(val value: UInt) {
    companion object {
        /**
         * Interested in create events.
         */
        val CREATE = WatchKind(1u)

        /**
         * Interested in change events
         */
        val CHANGE = WatchKind(2u)

        /**
         * Interested in delete events
         */
        val DELETE = WatchKind(4u)
    }
}

/**
 * The diagnostic's severity.
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class DiagnosticSeverity(val value: UInt) {
    companion object {
        /**
         * Reports an error.
         */
        val ERROR = DiagnosticSeverity(1u)

        /**
         * Reports a warning.
         */
        val WARNING = DiagnosticSeverity(2u)

        /**
         * Reports an information.
         */
        val INFORMATION = DiagnosticSeverity(3u)

        /**
         * Reports a hint.
         */
        val HINT = DiagnosticSeverity(4u)
    }
}

/**
 * The diagnostic tags.
 *
 * @since 3.15.0
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class DiagnosticTag(val value: UInt) {
    companion object {
        /**
         * Unused or unnecessary code.
         *
         * Clients are allowed to render diagnostics with this tag faded out instead of having
         * an error squiggle.
         */
        val UNNECESSARY = DiagnosticTag(1u)

        /**
         * Deprecated or obsolete code.
         *
         * Clients are allowed to rendered diagnostics with this tag strike through.
         */
        val DEPRECATED = DiagnosticTag(2u)
    }
}

/**
 * How a completion was triggered
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class CompletionTriggerKind(val value: UInt) {
    companion object {
        /**
         * Completion was triggered by typing an identifier (24x7 code
         * complete), manual invocation (e.g Ctrl+Space) or via API.
         */
        val INVOKED = CompletionTriggerKind(1u)

        /**
         * Completion was triggered by a trigger character specified by
         * the `triggerCharacters` properties of the `CompletionRegistrationOptions`.
         */
        val TRIGGER_CHARACTER = CompletionTriggerKind(2u)

        /**
         * Completion was re-triggered as current completion list is incomplete
         */
        val TRIGGER_FOR_INCOMPLETE_COMPLETIONS = CompletionTriggerKind(3u)
    }
}

/**
 * How a signature help was triggered.
 *
 * @since 3.15.0
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class SignatureHelpTriggerKind(val value: UInt) {
    companion object {
        /**
         * Signature help was invoked manually by the user or by a command.
         */
        val INVOKED = SignatureHelpTriggerKind(1u)

        /**
         * Signature help was triggered by a trigger character.
         */
        val TRIGGER_CHARACTER = SignatureHelpTriggerKind(2u)

        /**
         * Signature help was triggered by the cursor moving or by the document content changing.
         */
        val CONTENT_CHANGE = SignatureHelpTriggerKind(3u)
    }
}

/**
 * The reason why code actions were requested.
 *
 * @since 3.17.0
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class CodeActionTriggerKind(val value: UInt) {
    companion object {
        /**
         * Code actions were explicitly requested by the user or by an extension.
         */
        val INVOKED = CodeActionTriggerKind(1u)

        /**
         * Code actions were requested automatically.
         *
         * This typically happens when current selection in a file changes, but can
         * also be triggered when file content changes.
         */
        val AUTOMATIC = CodeActionTriggerKind(2u)
    }
}

/**
 * A pattern kind describing if a glob pattern matches a file a folder or
 * both.
 *
 * @since 3.16.0
 */
@kotlinx.serialization.Serializable
enum class FileOperationPatternKind {
    /**
     * The pattern matches a file only.
     */
    @kotlinx.serialization.SerialName("file")
    FILE,

    /**
     * The pattern matches a folder only.
     */
    @kotlinx.serialization.SerialName("folder")
    FOLDER
}

/**
 * A notebook cell kind.
 *
 * @since 3.17.0
 */
@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class NotebookCellKind(val value: UInt) {
    companion object {
        /**
         * A markup-cell is formatted source that is used for display.
         */
        val MARKUP = NotebookCellKind(1u)

        /**
         * A code-cell is source code.
         */
        val CODE = NotebookCellKind(2u)
    }
}

@kotlinx.serialization.Serializable
enum class ResourceOperationKind {
    /**
     * Supports creating new files and folders.
     */
    @kotlinx.serialization.SerialName("create")
    CREATE,

    /**
     * Supports renaming existing files and folders.
     */
    @kotlinx.serialization.SerialName("rename")
    RENAME,

    /**
     * Supports deleting existing files and folders.
     */
    @kotlinx.serialization.SerialName("delete")
    DELETE
}

@kotlinx.serialization.Serializable
enum class FailureHandlingKind {
    /**
     * Applying the workspace change is simply aborted if one of the changes provided
     * fails. All operations executed before the failing operation stay executed.
     */
    @kotlinx.serialization.SerialName("abort")
    ABORT,

    /**
     * All operations are executed transactional. That means they either all
     * succeed or no changes at all are applied to the workspace.
     */
    @kotlinx.serialization.SerialName("transactional")
    TRANSACTIONAL,

    /**
     * If the workspace edit contains only textual file changes they are executed transactional.
     * If resource changes (create, rename or delete file) are part of the change the failure
     * handling strategy is abort.
     */
    @kotlinx.serialization.SerialName("textOnlyTransactional")
    TEXT_ONLY_TRANSACTIONAL,

    /**
     * The client tries to undo the operations already executed. But there is no
     * guarantee that this is succeeding.
     */
    @kotlinx.serialization.SerialName("undo")
    UNDO
}

@kotlinx.serialization.Serializable
@kotlin.jvm.JvmInline
value class PrepareSupportDefaultBehavior(val value: UInt) {
    companion object {
        /**
         * The client's default behavior is to select the identifier
         * according the to language's syntax rule.
         */
        val IDENTIFIER = PrepareSupportDefaultBehavior(1u)
    }
}

@kotlinx.serialization.Serializable
enum class TokenFormat {
    @kotlinx.serialization.SerialName("relative")
    RELATIVE
}
