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

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject

/**
 * Sealed interface for the LSP union type: TextEdit | InsertReplaceEdit.
 */
@Serializable(with = CompletionItemTextEditSerializer::class)
sealed interface CompletionItemTextEdit

object CompletionItemTextEditSerializer :
    JsonContentPolymorphicSerializer<CompletionItemTextEdit>(CompletionItemTextEdit::class) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<CompletionItemTextEdit> {
        val obj = element.jsonObject
        return when {
            "range" in obj -> TextEdit.serializer() as DeserializationStrategy<CompletionItemTextEdit>
            "insert" in obj -> InsertReplaceEdit.serializer() as DeserializationStrategy<CompletionItemTextEdit>
            else -> throw SerializationException("Unknown CompletionItemTextEdit variant: $obj")
        }
    }
}

/**
 * Sealed interface for the LSP union type: RelatedFullDocumentDiagnosticReport | RelatedUnchangedDocumentDiagnosticReport.
 */
@Serializable(with = DocumentDiagnosticReportSerializer::class)
sealed interface DocumentDiagnosticReport

object DocumentDiagnosticReportSerializer :
    JsonContentPolymorphicSerializer<DocumentDiagnosticReport>(DocumentDiagnosticReport::class) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<DocumentDiagnosticReport> {
        val obj = element.jsonObject
        return when {
            (obj["kind"] as? JsonPrimitive)?.contentOrNull == "full" -> RelatedFullDocumentDiagnosticReport.serializer() as DeserializationStrategy<DocumentDiagnosticReport>

            (obj["kind"] as? JsonPrimitive)?.contentOrNull == "unchanged" -> RelatedUnchangedDocumentDiagnosticReport.serializer() as DeserializationStrategy<DocumentDiagnosticReport>

            else -> throw SerializationException(
                "Unknown DocumentDiagnosticReport variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: FullDocumentDiagnosticReport | UnchangedDocumentDiagnosticReport.
 */
@Serializable(with = DocumentDiagnosticReportPartialResultRelatedDocumentsSerializer::class)
sealed interface DocumentDiagnosticReportPartialResultRelatedDocuments

object DocumentDiagnosticReportPartialResultRelatedDocumentsSerializer :
    JsonContentPolymorphicSerializer<DocumentDiagnosticReportPartialResultRelatedDocuments>(
        DocumentDiagnosticReportPartialResultRelatedDocuments::class
    ) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<DocumentDiagnosticReportPartialResultRelatedDocuments> {
        val obj = element.jsonObject
        return when {
            (obj["kind"] as? JsonPrimitive)?.contentOrNull == "full" -> FullDocumentDiagnosticReport.serializer() as DeserializationStrategy<DocumentDiagnosticReportPartialResultRelatedDocuments>

            (obj["kind"] as? JsonPrimitive)?.contentOrNull == "unchanged" -> UnchangedDocumentDiagnosticReport.serializer() as DeserializationStrategy<DocumentDiagnosticReportPartialResultRelatedDocuments>

            else -> throw SerializationException(
                "Unknown DocumentDiagnosticReportPartialResultRelatedDocuments variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: InlineValueText | InlineValueVariableLookup | InlineValueEvaluatableExpression.
 */
@Serializable(with = InlineValueSerializer::class)
sealed interface InlineValue

object InlineValueSerializer :
    JsonContentPolymorphicSerializer<InlineValue>(InlineValue::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<InlineValue> {
        val obj = element.jsonObject
        return when {
            "text" in obj -> InlineValueText.serializer() as DeserializationStrategy<InlineValue>
            "caseSensitiveLookup" in obj -> InlineValueVariableLookup.serializer() as DeserializationStrategy<InlineValue>
            "range" in obj -> InlineValueEvaluatableExpression.serializer() as DeserializationStrategy<InlineValue>
            else -> throw SerializationException("Unknown InlineValue variant: $obj")
        }
    }
}

/**
 * Sealed interface for the LSP literal union: NotebookDocumentFilter.
 * Branches: NotebookDocumentFilterNotebookType, NotebookDocumentFilterScheme, NotebookDocumentFilterPattern.
 */
@Serializable(with = NotebookDocumentFilterSerializer::class)
sealed interface NotebookDocumentFilter

object NotebookDocumentFilterSerializer :
    JsonContentPolymorphicSerializer<NotebookDocumentFilter>(NotebookDocumentFilter::class) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<NotebookDocumentFilter> {
        val obj = element.jsonObject
        return when {
            "notebookType" in obj -> NotebookDocumentFilterNotebookType.serializer() as DeserializationStrategy<NotebookDocumentFilter>
            "scheme" in obj -> NotebookDocumentFilterScheme.serializer() as DeserializationStrategy<NotebookDocumentFilter>
            "pattern" in obj -> NotebookDocumentFilterPattern.serializer() as DeserializationStrategy<NotebookDocumentFilter>
            else -> throw SerializationException("Unknown NotebookDocumentFilter variant: $obj")
        }
    }
}

/**
 * Sealed interface for the LSP literal union: NotebookDocumentSyncOptionsNotebookSelector.
 * Branches: NotebookDocumentSyncOptionsNotebookSelectorNotebook, NotebookDocumentSyncOptionsNotebookSelectorCells.
 */
@Serializable(with = NotebookDocumentSyncOptionsNotebookSelectorSerializer::class)
sealed interface NotebookDocumentSyncOptionsNotebookSelector

object NotebookDocumentSyncOptionsNotebookSelectorSerializer :
    JsonContentPolymorphicSerializer<NotebookDocumentSyncOptionsNotebookSelector>(
        NotebookDocumentSyncOptionsNotebookSelector::class
    ) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<NotebookDocumentSyncOptionsNotebookSelector> {
        val obj = element.jsonObject
        return when {
            "notebook" in obj -> NotebookDocumentSyncOptionsNotebookSelectorNotebook.serializer() as DeserializationStrategy<NotebookDocumentSyncOptionsNotebookSelector>

            "cells" in obj -> NotebookDocumentSyncOptionsNotebookSelectorCells.serializer() as DeserializationStrategy<NotebookDocumentSyncOptionsNotebookSelector>

            else -> throw SerializationException(
                "Unknown NotebookDocumentSyncOptionsNotebookSelector variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP literal union: NotebookDocumentSyncRegistrationOptionsNotebookSelector.
 * Branches: NotebookDocumentSyncRegistrationOptionsNotebookSelectorNotebook, NotebookDocumentSyncRegistrationOptionsNotebookSelectorCells.
 */
@Serializable(with = NotebookDocumentSyncRegistrationOptionsNotebookSelectorSerializer::class)
sealed interface NotebookDocumentSyncRegistrationOptionsNotebookSelector

object NotebookDocumentSyncRegistrationOptionsNotebookSelectorSerializer :
    JsonContentPolymorphicSerializer<NotebookDocumentSyncRegistrationOptionsNotebookSelector>(
        NotebookDocumentSyncRegistrationOptionsNotebookSelector::class
    ) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<NotebookDocumentSyncRegistrationOptionsNotebookSelector> {
        val obj = element.jsonObject
        return when {
            "notebook" in obj -> NotebookDocumentSyncRegistrationOptionsNotebookSelectorNotebook.serializer() as DeserializationStrategy<NotebookDocumentSyncRegistrationOptionsNotebookSelector>

            "cells" in obj -> NotebookDocumentSyncRegistrationOptionsNotebookSelectorCells.serializer() as DeserializationStrategy<NotebookDocumentSyncRegistrationOptionsNotebookSelector>

            else -> throw SerializationException(
                "Unknown NotebookDocumentSyncRegistrationOptionsNotebookSelector variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: FullDocumentDiagnosticReport | UnchangedDocumentDiagnosticReport.
 */
@Serializable(with = RelatedFullDocumentDiagnosticReportRelatedDocumentsSerializer::class)
sealed interface RelatedFullDocumentDiagnosticReportRelatedDocuments

object RelatedFullDocumentDiagnosticReportRelatedDocumentsSerializer :
    JsonContentPolymorphicSerializer<RelatedFullDocumentDiagnosticReportRelatedDocuments>(
        RelatedFullDocumentDiagnosticReportRelatedDocuments::class
    ) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<RelatedFullDocumentDiagnosticReportRelatedDocuments> {
        val obj = element.jsonObject
        return when {
            (obj["kind"] as? JsonPrimitive)?.contentOrNull == "full" -> FullDocumentDiagnosticReport.serializer() as DeserializationStrategy<RelatedFullDocumentDiagnosticReportRelatedDocuments>

            (obj["kind"] as? JsonPrimitive)?.contentOrNull == "unchanged" -> UnchangedDocumentDiagnosticReport.serializer() as DeserializationStrategy<RelatedFullDocumentDiagnosticReportRelatedDocuments>

            else -> throw SerializationException(
                "Unknown RelatedFullDocumentDiagnosticReportRelatedDocuments variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: FullDocumentDiagnosticReport | UnchangedDocumentDiagnosticReport.
 */
@Serializable(with = RelatedUnchangedDocumentDiagnosticReportRelatedDocumentsSerializer::class)
sealed interface RelatedUnchangedDocumentDiagnosticReportRelatedDocuments

object RelatedUnchangedDocumentDiagnosticReportRelatedDocumentsSerializer :
    JsonContentPolymorphicSerializer<RelatedUnchangedDocumentDiagnosticReportRelatedDocuments>(
        RelatedUnchangedDocumentDiagnosticReportRelatedDocuments::class
    ) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<RelatedUnchangedDocumentDiagnosticReportRelatedDocuments> {
        val obj = element.jsonObject
        return when {
            (obj["kind"] as? JsonPrimitive)?.contentOrNull == "full" -> FullDocumentDiagnosticReport.serializer() as DeserializationStrategy<RelatedUnchangedDocumentDiagnosticReportRelatedDocuments>

            (obj["kind"] as? JsonPrimitive)?.contentOrNull == "unchanged" -> UnchangedDocumentDiagnosticReport.serializer() as DeserializationStrategy<RelatedUnchangedDocumentDiagnosticReportRelatedDocuments>

            else -> throw SerializationException(
                "Unknown RelatedUnchangedDocumentDiagnosticReportRelatedDocuments variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: CallHierarchyOptions | CallHierarchyRegistrationOptions.
 */
@Serializable(with = ServerCapabilitiesCallHierarchyProviderOptionsSerializer::class)
sealed interface ServerCapabilitiesCallHierarchyProviderOptions

object ServerCapabilitiesCallHierarchyProviderOptionsSerializer :
    JsonContentPolymorphicSerializer<ServerCapabilitiesCallHierarchyProviderOptions>(
        ServerCapabilitiesCallHierarchyProviderOptions::class
    ) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<ServerCapabilitiesCallHierarchyProviderOptions> {
        val obj = element.jsonObject
        return when {
            /* fallback */
            obj.isNotEmpty() -> CallHierarchyOptions.serializer() as DeserializationStrategy<ServerCapabilitiesCallHierarchyProviderOptions>

            "documentSelector" in obj -> CallHierarchyRegistrationOptions.serializer() as DeserializationStrategy<ServerCapabilitiesCallHierarchyProviderOptions>

            else -> throw SerializationException(
                "Unknown ServerCapabilitiesCallHierarchyProviderOptions variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: DocumentColorOptions | DocumentColorRegistrationOptions.
 */
@Serializable(with = ServerCapabilitiesColorProviderOptionsSerializer::class)
sealed interface ServerCapabilitiesColorProviderOptions

object ServerCapabilitiesColorProviderOptionsSerializer :
    JsonContentPolymorphicSerializer<ServerCapabilitiesColorProviderOptions>(
        ServerCapabilitiesColorProviderOptions::class
    ) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<ServerCapabilitiesColorProviderOptions> {
        val obj = element.jsonObject
        return when {
            /* fallback */
            obj.isNotEmpty() -> DocumentColorOptions.serializer() as DeserializationStrategy<ServerCapabilitiesColorProviderOptions>

            "documentSelector" in obj -> DocumentColorRegistrationOptions.serializer() as DeserializationStrategy<ServerCapabilitiesColorProviderOptions>

            else -> throw SerializationException(
                "Unknown ServerCapabilitiesColorProviderOptions variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: DeclarationOptions | DeclarationRegistrationOptions.
 */
@Serializable(with = ServerCapabilitiesDeclarationProviderOptionsSerializer::class)
sealed interface ServerCapabilitiesDeclarationProviderOptions

object ServerCapabilitiesDeclarationProviderOptionsSerializer :
    JsonContentPolymorphicSerializer<ServerCapabilitiesDeclarationProviderOptions>(
        ServerCapabilitiesDeclarationProviderOptions::class
    ) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<ServerCapabilitiesDeclarationProviderOptions> {
        val obj = element.jsonObject
        return when {
            /* fallback */
            obj.isNotEmpty() -> DeclarationOptions.serializer() as DeserializationStrategy<ServerCapabilitiesDeclarationProviderOptions>

            "documentSelector" in obj -> DeclarationRegistrationOptions.serializer() as DeserializationStrategy<ServerCapabilitiesDeclarationProviderOptions>

            else -> throw SerializationException(
                "Unknown ServerCapabilitiesDeclarationProviderOptions variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: DiagnosticOptions | DiagnosticRegistrationOptions.
 */
@Serializable(with = ServerCapabilitiesDiagnosticProviderSerializer::class)
sealed interface ServerCapabilitiesDiagnosticProvider

object ServerCapabilitiesDiagnosticProviderSerializer :
    JsonContentPolymorphicSerializer<ServerCapabilitiesDiagnosticProvider>(
        ServerCapabilitiesDiagnosticProvider::class
    ) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<ServerCapabilitiesDiagnosticProvider> {
        val obj = element.jsonObject
        return when {
            "interFileDependencies" in obj -> DiagnosticOptions.serializer() as DeserializationStrategy<ServerCapabilitiesDiagnosticProvider>

            "documentSelector" in obj -> DiagnosticRegistrationOptions.serializer() as DeserializationStrategy<ServerCapabilitiesDiagnosticProvider>

            else -> throw SerializationException(
                "Unknown ServerCapabilitiesDiagnosticProvider variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: FoldingRangeOptions | FoldingRangeRegistrationOptions.
 */
@Serializable(with = ServerCapabilitiesFoldingRangeProviderOptionsSerializer::class)
sealed interface ServerCapabilitiesFoldingRangeProviderOptions

object ServerCapabilitiesFoldingRangeProviderOptionsSerializer :
    JsonContentPolymorphicSerializer<ServerCapabilitiesFoldingRangeProviderOptions>(
        ServerCapabilitiesFoldingRangeProviderOptions::class
    ) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<ServerCapabilitiesFoldingRangeProviderOptions> {
        val obj = element.jsonObject
        return when {
            /* fallback */
            obj.isNotEmpty() -> FoldingRangeOptions.serializer() as DeserializationStrategy<ServerCapabilitiesFoldingRangeProviderOptions>

            "documentSelector" in obj -> FoldingRangeRegistrationOptions.serializer() as DeserializationStrategy<ServerCapabilitiesFoldingRangeProviderOptions>

            else -> throw SerializationException(
                "Unknown ServerCapabilitiesFoldingRangeProviderOptions variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: ImplementationOptions | ImplementationRegistrationOptions.
 */
@Serializable(with = ServerCapabilitiesImplementationProviderOptionsSerializer::class)
sealed interface ServerCapabilitiesImplementationProviderOptions

object ServerCapabilitiesImplementationProviderOptionsSerializer :
    JsonContentPolymorphicSerializer<ServerCapabilitiesImplementationProviderOptions>(
        ServerCapabilitiesImplementationProviderOptions::class
    ) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<ServerCapabilitiesImplementationProviderOptions> {
        val obj = element.jsonObject
        return when {
            /* fallback */
            obj.isNotEmpty() -> ImplementationOptions.serializer() as DeserializationStrategy<ServerCapabilitiesImplementationProviderOptions>

            "documentSelector" in obj -> ImplementationRegistrationOptions.serializer() as DeserializationStrategy<ServerCapabilitiesImplementationProviderOptions>

            else -> throw SerializationException(
                "Unknown ServerCapabilitiesImplementationProviderOptions variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: InlayHintOptions | InlayHintRegistrationOptions.
 */
@Serializable(with = ServerCapabilitiesInlayHintProviderOptionsSerializer::class)
sealed interface ServerCapabilitiesInlayHintProviderOptions

object ServerCapabilitiesInlayHintProviderOptionsSerializer :
    JsonContentPolymorphicSerializer<ServerCapabilitiesInlayHintProviderOptions>(
        ServerCapabilitiesInlayHintProviderOptions::class
    ) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<ServerCapabilitiesInlayHintProviderOptions> {
        val obj = element.jsonObject
        return when {
            /* fallback */
            obj.isNotEmpty() -> InlayHintOptions.serializer() as DeserializationStrategy<ServerCapabilitiesInlayHintProviderOptions>

            "documentSelector" in obj -> InlayHintRegistrationOptions.serializer() as DeserializationStrategy<ServerCapabilitiesInlayHintProviderOptions>

            else -> throw SerializationException(
                "Unknown ServerCapabilitiesInlayHintProviderOptions variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: InlineValueOptions | InlineValueRegistrationOptions.
 */
@Serializable(with = ServerCapabilitiesInlineValueProviderOptionsSerializer::class)
sealed interface ServerCapabilitiesInlineValueProviderOptions

object ServerCapabilitiesInlineValueProviderOptionsSerializer :
    JsonContentPolymorphicSerializer<ServerCapabilitiesInlineValueProviderOptions>(
        ServerCapabilitiesInlineValueProviderOptions::class
    ) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<ServerCapabilitiesInlineValueProviderOptions> {
        val obj = element.jsonObject
        return when {
            /* fallback */
            obj.isNotEmpty() -> InlineValueOptions.serializer() as DeserializationStrategy<ServerCapabilitiesInlineValueProviderOptions>

            "documentSelector" in obj -> InlineValueRegistrationOptions.serializer() as DeserializationStrategy<ServerCapabilitiesInlineValueProviderOptions>

            else -> throw SerializationException(
                "Unknown ServerCapabilitiesInlineValueProviderOptions variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: LinkedEditingRangeOptions | LinkedEditingRangeRegistrationOptions.
 */
@Serializable(with = ServerCapabilitiesLinkedEditingRangeProviderOptionsSerializer::class)
sealed interface ServerCapabilitiesLinkedEditingRangeProviderOptions

object ServerCapabilitiesLinkedEditingRangeProviderOptionsSerializer :
    JsonContentPolymorphicSerializer<ServerCapabilitiesLinkedEditingRangeProviderOptions>(
        ServerCapabilitiesLinkedEditingRangeProviderOptions::class
    ) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<ServerCapabilitiesLinkedEditingRangeProviderOptions> {
        val obj = element.jsonObject
        return when {
            /* fallback */
            obj.isNotEmpty() -> LinkedEditingRangeOptions.serializer() as DeserializationStrategy<ServerCapabilitiesLinkedEditingRangeProviderOptions>

            "documentSelector" in obj -> LinkedEditingRangeRegistrationOptions.serializer() as DeserializationStrategy<ServerCapabilitiesLinkedEditingRangeProviderOptions>

            else -> throw SerializationException(
                "Unknown ServerCapabilitiesLinkedEditingRangeProviderOptions variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: MonikerOptions | MonikerRegistrationOptions.
 */
@Serializable(with = ServerCapabilitiesMonikerProviderOptionsSerializer::class)
sealed interface ServerCapabilitiesMonikerProviderOptions

object ServerCapabilitiesMonikerProviderOptionsSerializer :
    JsonContentPolymorphicSerializer<ServerCapabilitiesMonikerProviderOptions>(
        ServerCapabilitiesMonikerProviderOptions::class
    ) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<ServerCapabilitiesMonikerProviderOptions> {
        val obj = element.jsonObject
        return when {
            /* fallback */
            obj.isNotEmpty() -> MonikerOptions.serializer() as DeserializationStrategy<ServerCapabilitiesMonikerProviderOptions>

            "documentSelector" in obj -> MonikerRegistrationOptions.serializer() as DeserializationStrategy<ServerCapabilitiesMonikerProviderOptions>

            else -> throw SerializationException(
                "Unknown ServerCapabilitiesMonikerProviderOptions variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: NotebookDocumentSyncOptions | NotebookDocumentSyncRegistrationOptions.
 */
@Serializable(with = ServerCapabilitiesNotebookDocumentSyncSerializer::class)
sealed interface ServerCapabilitiesNotebookDocumentSync

object ServerCapabilitiesNotebookDocumentSyncSerializer :
    JsonContentPolymorphicSerializer<ServerCapabilitiesNotebookDocumentSync>(
        ServerCapabilitiesNotebookDocumentSync::class
    ) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<ServerCapabilitiesNotebookDocumentSync> {
        val obj = element.jsonObject
        return when {
            "notebookSelector" in obj -> NotebookDocumentSyncOptions.serializer() as DeserializationStrategy<ServerCapabilitiesNotebookDocumentSync>

            "notebookSelector" in obj -> NotebookDocumentSyncRegistrationOptions.serializer() as DeserializationStrategy<ServerCapabilitiesNotebookDocumentSync>

            else -> throw SerializationException(
                "Unknown ServerCapabilitiesNotebookDocumentSync variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: SelectionRangeOptions | SelectionRangeRegistrationOptions.
 */
@Serializable(with = ServerCapabilitiesSelectionRangeProviderOptionsSerializer::class)
sealed interface ServerCapabilitiesSelectionRangeProviderOptions

object ServerCapabilitiesSelectionRangeProviderOptionsSerializer :
    JsonContentPolymorphicSerializer<ServerCapabilitiesSelectionRangeProviderOptions>(
        ServerCapabilitiesSelectionRangeProviderOptions::class
    ) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<ServerCapabilitiesSelectionRangeProviderOptions> {
        val obj = element.jsonObject
        return when {
            /* fallback */
            obj.isNotEmpty() -> SelectionRangeOptions.serializer() as DeserializationStrategy<ServerCapabilitiesSelectionRangeProviderOptions>

            "documentSelector" in obj -> SelectionRangeRegistrationOptions.serializer() as DeserializationStrategy<ServerCapabilitiesSelectionRangeProviderOptions>

            else -> throw SerializationException(
                "Unknown ServerCapabilitiesSelectionRangeProviderOptions variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: SemanticTokensOptions | SemanticTokensRegistrationOptions.
 */
@Serializable(with = ServerCapabilitiesSemanticTokensProviderSerializer::class)
sealed interface ServerCapabilitiesSemanticTokensProvider

object ServerCapabilitiesSemanticTokensProviderSerializer :
    JsonContentPolymorphicSerializer<ServerCapabilitiesSemanticTokensProvider>(
        ServerCapabilitiesSemanticTokensProvider::class
    ) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<ServerCapabilitiesSemanticTokensProvider> {
        val obj = element.jsonObject
        return when {
            "legend" in obj -> SemanticTokensOptions.serializer() as DeserializationStrategy<ServerCapabilitiesSemanticTokensProvider>

            "documentSelector" in obj -> SemanticTokensRegistrationOptions.serializer() as DeserializationStrategy<ServerCapabilitiesSemanticTokensProvider>

            else -> throw SerializationException(
                "Unknown ServerCapabilitiesSemanticTokensProvider variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: TypeDefinitionOptions | TypeDefinitionRegistrationOptions.
 */
@Serializable(with = ServerCapabilitiesTypeDefinitionProviderOptionsSerializer::class)
sealed interface ServerCapabilitiesTypeDefinitionProviderOptions

object ServerCapabilitiesTypeDefinitionProviderOptionsSerializer :
    JsonContentPolymorphicSerializer<ServerCapabilitiesTypeDefinitionProviderOptions>(
        ServerCapabilitiesTypeDefinitionProviderOptions::class
    ) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<ServerCapabilitiesTypeDefinitionProviderOptions> {
        val obj = element.jsonObject
        return when {
            /* fallback */
            obj.isNotEmpty() -> TypeDefinitionOptions.serializer() as DeserializationStrategy<ServerCapabilitiesTypeDefinitionProviderOptions>

            "documentSelector" in obj -> TypeDefinitionRegistrationOptions.serializer() as DeserializationStrategy<ServerCapabilitiesTypeDefinitionProviderOptions>

            else -> throw SerializationException(
                "Unknown ServerCapabilitiesTypeDefinitionProviderOptions variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: TypeHierarchyOptions | TypeHierarchyRegistrationOptions.
 */
@Serializable(with = ServerCapabilitiesTypeHierarchyProviderOptionsSerializer::class)
sealed interface ServerCapabilitiesTypeHierarchyProviderOptions

object ServerCapabilitiesTypeHierarchyProviderOptionsSerializer :
    JsonContentPolymorphicSerializer<ServerCapabilitiesTypeHierarchyProviderOptions>(
        ServerCapabilitiesTypeHierarchyProviderOptions::class
    ) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<ServerCapabilitiesTypeHierarchyProviderOptions> {
        val obj = element.jsonObject
        return when {
            /* fallback */
            obj.isNotEmpty() -> TypeHierarchyOptions.serializer() as DeserializationStrategy<ServerCapabilitiesTypeHierarchyProviderOptions>

            "documentSelector" in obj -> TypeHierarchyRegistrationOptions.serializer() as DeserializationStrategy<ServerCapabilitiesTypeHierarchyProviderOptions>

            else -> throw SerializationException(
                "Unknown ServerCapabilitiesTypeHierarchyProviderOptions variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: Command | CodeAction.
 */
@Serializable(with = TextDocumentCodeActionResultSerializer::class)
sealed interface TextDocumentCodeActionResult

object TextDocumentCodeActionResultSerializer :
    JsonContentPolymorphicSerializer<TextDocumentCodeActionResult>(
        TextDocumentCodeActionResult::class
    ) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<TextDocumentCodeActionResult> {
        val obj = element.jsonObject
        return when {
            "command" in obj -> Command.serializer() as DeserializationStrategy<TextDocumentCodeActionResult>

            "title" in obj -> CodeAction.serializer() as DeserializationStrategy<TextDocumentCodeActionResult>

            else -> throw SerializationException(
                "Unknown TextDocumentCodeActionResult variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP literal union: TextDocumentContentChangeEvent.
 * Branches: TextDocumentContentChangeEventRange, TextDocumentContentChangeEventVariant.
 */
@Serializable(with = TextDocumentContentChangeEventSerializer::class)
sealed interface TextDocumentContentChangeEvent

object TextDocumentContentChangeEventSerializer :
    JsonContentPolymorphicSerializer<TextDocumentContentChangeEvent>(
        TextDocumentContentChangeEvent::class
    ) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<TextDocumentContentChangeEvent> {
        val obj = element.jsonObject
        return when {
            "range" in obj -> TextDocumentContentChangeEventRange.serializer() as DeserializationStrategy<TextDocumentContentChangeEvent>

            "text" in obj -> TextDocumentContentChangeEventVariant.serializer() as DeserializationStrategy<TextDocumentContentChangeEvent>

            else -> throw SerializationException(
                "Unknown TextDocumentContentChangeEvent variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: TextEdit | AnnotatedTextEdit.
 */
@Serializable(with = TextDocumentEditEditsSerializer::class)
sealed interface TextDocumentEditEdits

object TextDocumentEditEditsSerializer :
    JsonContentPolymorphicSerializer<TextDocumentEditEdits>(TextDocumentEditEdits::class) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<TextDocumentEditEdits> {
        val obj = element.jsonObject
        return when {
            "range" in obj -> TextEdit.serializer() as DeserializationStrategy<TextDocumentEditEdits>
            "annotationId" in obj -> AnnotatedTextEdit.serializer() as DeserializationStrategy<TextDocumentEditEdits>
            else -> throw SerializationException("Unknown TextDocumentEditEdits variant: $obj")
        }
    }
}

/**
 * Sealed interface for the LSP literal union: TextDocumentFilter.
 * Branches: TextDocumentFilterLanguage, TextDocumentFilterScheme, TextDocumentFilterPattern.
 */
@Serializable(with = TextDocumentFilterSerializer::class)
sealed interface TextDocumentFilter

object TextDocumentFilterSerializer :
    JsonContentPolymorphicSerializer<TextDocumentFilter>(TextDocumentFilter::class) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<TextDocumentFilter> {
        val obj = element.jsonObject
        return when {
            "language" in obj -> TextDocumentFilterLanguage.serializer() as DeserializationStrategy<TextDocumentFilter>
            "scheme" in obj -> TextDocumentFilterScheme.serializer() as DeserializationStrategy<TextDocumentFilter>
            "pattern" in obj -> TextDocumentFilterPattern.serializer() as DeserializationStrategy<TextDocumentFilter>
            else -> throw SerializationException("Unknown TextDocumentFilter variant: $obj")
        }
    }
}

/**
 * Sealed interface for the LSP union type: SemanticTokens | SemanticTokensDelta.
 */
@Serializable(with = TextDocumentSemanticTokensFullDeltaResultSerializer::class)
sealed interface TextDocumentSemanticTokensFullDeltaResult

object TextDocumentSemanticTokensFullDeltaResultSerializer :
    JsonContentPolymorphicSerializer<TextDocumentSemanticTokensFullDeltaResult>(
        TextDocumentSemanticTokensFullDeltaResult::class
    ) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<TextDocumentSemanticTokensFullDeltaResult> {
        val obj = element.jsonObject
        return when {
            "data" in obj -> SemanticTokens.serializer() as DeserializationStrategy<TextDocumentSemanticTokensFullDeltaResult>

            "edits" in obj -> SemanticTokensDelta.serializer() as DeserializationStrategy<TextDocumentSemanticTokensFullDeltaResult>

            else -> throw SerializationException(
                "Unknown TextDocumentSemanticTokensFullDeltaResult variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: WorkspaceFullDocumentDiagnosticReport | WorkspaceUnchangedDocumentDiagnosticReport.
 */
@Serializable(with = WorkspaceDocumentDiagnosticReportSerializer::class)
sealed interface WorkspaceDocumentDiagnosticReport

object WorkspaceDocumentDiagnosticReportSerializer :
    JsonContentPolymorphicSerializer<WorkspaceDocumentDiagnosticReport>(
        WorkspaceDocumentDiagnosticReport::class
    ) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<WorkspaceDocumentDiagnosticReport> {
        val obj = element.jsonObject
        return when {
            (obj["kind"] as? JsonPrimitive)?.contentOrNull == "full" -> WorkspaceFullDocumentDiagnosticReport.serializer() as DeserializationStrategy<WorkspaceDocumentDiagnosticReport>

            (obj["kind"] as? JsonPrimitive)?.contentOrNull == "unchanged" -> WorkspaceUnchangedDocumentDiagnosticReport.serializer() as DeserializationStrategy<WorkspaceDocumentDiagnosticReport>

            else -> throw SerializationException(
                "Unknown WorkspaceDocumentDiagnosticReport variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: TextDocumentEdit | CreateFile | RenameFile | DeleteFile.
 */
@Serializable(with = WorkspaceEditDocumentChangesSerializer::class)
sealed interface WorkspaceEditDocumentChanges

object WorkspaceEditDocumentChangesSerializer :
    JsonContentPolymorphicSerializer<WorkspaceEditDocumentChanges>(
        WorkspaceEditDocumentChanges::class
    ) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<WorkspaceEditDocumentChanges> {
        val obj = element.jsonObject
        return when {
            "textDocument" in obj -> TextDocumentEdit.serializer() as DeserializationStrategy<WorkspaceEditDocumentChanges>

            "kind" in obj -> CreateFile.serializer() as DeserializationStrategy<WorkspaceEditDocumentChanges>

            "oldUri" in obj -> RenameFile.serializer() as DeserializationStrategy<WorkspaceEditDocumentChanges>

            "kind" in obj -> DeleteFile.serializer() as DeserializationStrategy<WorkspaceEditDocumentChanges>

            else -> throw SerializationException(
                "Unknown WorkspaceEditDocumentChanges variant: $obj"
            )
        }
    }
}
