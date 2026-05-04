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

import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject

/**
 * Sealed interface for the LSP union type: TextEdit | InsertReplaceEdit.
 */
@kotlinx.serialization.Serializable(with = CompletionItemTextEditSerializer::class)
sealed interface CompletionItemTextEdit

object CompletionItemTextEditSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<CompletionItemTextEdit>(
        CompletionItemTextEdit::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<CompletionItemTextEdit> {
        val obj = element.jsonObject
        return when {
            "range" in obj -> TextEdit.serializer() as kotlinx.serialization.DeserializationStrategy<CompletionItemTextEdit>

            "insert" in obj -> InsertReplaceEdit.serializer() as kotlinx.serialization.DeserializationStrategy<CompletionItemTextEdit>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown CompletionItemTextEdit variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: RelatedFullDocumentDiagnosticReport | RelatedUnchangedDocumentDiagnosticReport.
 */
@kotlinx.serialization.Serializable(with = DocumentDiagnosticReportSerializer::class)
sealed interface DocumentDiagnosticReport

object DocumentDiagnosticReportSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<DocumentDiagnosticReport>(
        DocumentDiagnosticReport::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<DocumentDiagnosticReport> {
        val obj = element.jsonObject
        return when {
            (obj["kind"] as? kotlinx.serialization.json.JsonPrimitive)?.contentOrNull == "full" -> RelatedFullDocumentDiagnosticReport.serializer() as kotlinx.serialization.DeserializationStrategy<DocumentDiagnosticReport>

            (obj["kind"] as? kotlinx.serialization.json.JsonPrimitive)?.contentOrNull ==
                "unchanged" -> RelatedUnchangedDocumentDiagnosticReport.serializer() as kotlinx.serialization.DeserializationStrategy<DocumentDiagnosticReport>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown DocumentDiagnosticReport variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: FullDocumentDiagnosticReport | UnchangedDocumentDiagnosticReport.
 */
@kotlinx.serialization.Serializable(
    with = DocumentDiagnosticReportPartialResultRelatedDocumentsSerializer::class
)
sealed interface DocumentDiagnosticReportPartialResultRelatedDocuments

object DocumentDiagnosticReportPartialResultRelatedDocumentsSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<DocumentDiagnosticReportPartialResultRelatedDocuments>(
        DocumentDiagnosticReportPartialResultRelatedDocuments::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<DocumentDiagnosticReportPartialResultRelatedDocuments> {
        val obj = element.jsonObject
        return when {
            (obj["kind"] as? kotlinx.serialization.json.JsonPrimitive)?.contentOrNull == "full" -> FullDocumentDiagnosticReport.serializer() as kotlinx.serialization.DeserializationStrategy<DocumentDiagnosticReportPartialResultRelatedDocuments>

            (obj["kind"] as? kotlinx.serialization.json.JsonPrimitive)?.contentOrNull ==
                "unchanged" -> UnchangedDocumentDiagnosticReport.serializer() as kotlinx.serialization.DeserializationStrategy<DocumentDiagnosticReportPartialResultRelatedDocuments>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown DocumentDiagnosticReportPartialResultRelatedDocuments variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: InlineValueText | InlineValueVariableLookup | InlineValueEvaluatableExpression.
 */
@kotlinx.serialization.Serializable(with = InlineValueSerializer::class)
sealed interface InlineValue

object InlineValueSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<InlineValue>(InlineValue::class) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<InlineValue> {
        val obj = element.jsonObject
        return when {
            "text" in obj -> InlineValueText.serializer() as kotlinx.serialization.DeserializationStrategy<InlineValue>

            "caseSensitiveLookup" in obj -> InlineValueVariableLookup.serializer() as kotlinx.serialization.DeserializationStrategy<InlineValue>

            "range" in obj -> InlineValueEvaluatableExpression.serializer() as kotlinx.serialization.DeserializationStrategy<InlineValue>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown InlineValue variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP literal union: NotebookDocumentFilter.
 * Branches: NotebookDocumentFilterNotebookType, NotebookDocumentFilterScheme, NotebookDocumentFilterPattern.
 */
@kotlinx.serialization.Serializable(with = NotebookDocumentFilterSerializer::class)
sealed interface NotebookDocumentFilter

object NotebookDocumentFilterSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<NotebookDocumentFilter>(
        NotebookDocumentFilter::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<NotebookDocumentFilter> {
        val obj = element.jsonObject
        return when {
            "notebookType" in obj -> NotebookDocumentFilterNotebookType.serializer() as kotlinx.serialization.DeserializationStrategy<NotebookDocumentFilter>

            "scheme" in obj -> NotebookDocumentFilterScheme.serializer() as kotlinx.serialization.DeserializationStrategy<NotebookDocumentFilter>

            "pattern" in obj -> NotebookDocumentFilterPattern.serializer() as kotlinx.serialization.DeserializationStrategy<NotebookDocumentFilter>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown NotebookDocumentFilter variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP literal union: NotebookDocumentSyncOptionsNotebookSelector.
 * Branches: NotebookDocumentSyncOptionsNotebookSelectorNotebook, NotebookDocumentSyncOptionsNotebookSelectorCells.
 */
@kotlinx.serialization.Serializable(
    with = NotebookDocumentSyncOptionsNotebookSelectorSerializer::class
)
sealed interface NotebookDocumentSyncOptionsNotebookSelector

object NotebookDocumentSyncOptionsNotebookSelectorSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<NotebookDocumentSyncOptionsNotebookSelector>(
        NotebookDocumentSyncOptionsNotebookSelector::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<NotebookDocumentSyncOptionsNotebookSelector> {
        val obj = element.jsonObject
        return when {
            "notebook" in obj -> NotebookDocumentSyncOptionsNotebookSelectorNotebook.serializer() as kotlinx.serialization.DeserializationStrategy<NotebookDocumentSyncOptionsNotebookSelector>

            "cells" in obj -> NotebookDocumentSyncOptionsNotebookSelectorCells.serializer() as kotlinx.serialization.DeserializationStrategy<NotebookDocumentSyncOptionsNotebookSelector>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown NotebookDocumentSyncOptionsNotebookSelector variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP literal union: NotebookDocumentSyncRegistrationOptionsNotebookSelector.
 * Branches: NotebookDocumentSyncRegistrationOptionsNotebookSelectorNotebook, NotebookDocumentSyncRegistrationOptionsNotebookSelectorCells.
 */
@kotlinx.serialization.Serializable(
    with = NotebookDocumentSyncRegistrationOptionsNotebookSelectorSerializer::class
)
sealed interface NotebookDocumentSyncRegistrationOptionsNotebookSelector

object NotebookDocumentSyncRegistrationOptionsNotebookSelectorSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<NotebookDocumentSyncRegistrationOptionsNotebookSelector>(
        NotebookDocumentSyncRegistrationOptionsNotebookSelector::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<NotebookDocumentSyncRegistrationOptionsNotebookSelector> {
        val obj = element.jsonObject
        return when {
            "notebook" in obj -> NotebookDocumentSyncRegistrationOptionsNotebookSelectorNotebook.serializer() as kotlinx.serialization.DeserializationStrategy<NotebookDocumentSyncRegistrationOptionsNotebookSelector>

            "cells" in obj -> NotebookDocumentSyncRegistrationOptionsNotebookSelectorCells.serializer() as kotlinx.serialization.DeserializationStrategy<NotebookDocumentSyncRegistrationOptionsNotebookSelector>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown NotebookDocumentSyncRegistrationOptionsNotebookSelector variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: FullDocumentDiagnosticReport | UnchangedDocumentDiagnosticReport.
 */
@kotlinx.serialization.Serializable(
    with = RelatedFullDocumentDiagnosticReportRelatedDocumentsSerializer::class
)
sealed interface RelatedFullDocumentDiagnosticReportRelatedDocuments

object RelatedFullDocumentDiagnosticReportRelatedDocumentsSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<RelatedFullDocumentDiagnosticReportRelatedDocuments>(
        RelatedFullDocumentDiagnosticReportRelatedDocuments::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<RelatedFullDocumentDiagnosticReportRelatedDocuments> {
        val obj = element.jsonObject
        return when {
            (obj["kind"] as? kotlinx.serialization.json.JsonPrimitive)?.contentOrNull == "full" -> FullDocumentDiagnosticReport.serializer() as kotlinx.serialization.DeserializationStrategy<RelatedFullDocumentDiagnosticReportRelatedDocuments>

            (obj["kind"] as? kotlinx.serialization.json.JsonPrimitive)?.contentOrNull ==
                "unchanged" -> UnchangedDocumentDiagnosticReport.serializer() as kotlinx.serialization.DeserializationStrategy<RelatedFullDocumentDiagnosticReportRelatedDocuments>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown RelatedFullDocumentDiagnosticReportRelatedDocuments variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: FullDocumentDiagnosticReport | UnchangedDocumentDiagnosticReport.
 */
@kotlinx.serialization.Serializable(
    with = RelatedUnchangedDocumentDiagnosticReportRelatedDocumentsSerializer::class
)
sealed interface RelatedUnchangedDocumentDiagnosticReportRelatedDocuments

object RelatedUnchangedDocumentDiagnosticReportRelatedDocumentsSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<RelatedUnchangedDocumentDiagnosticReportRelatedDocuments>(
        RelatedUnchangedDocumentDiagnosticReportRelatedDocuments::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<RelatedUnchangedDocumentDiagnosticReportRelatedDocuments> {
        val obj = element.jsonObject
        return when {
            (obj["kind"] as? kotlinx.serialization.json.JsonPrimitive)?.contentOrNull == "full" -> FullDocumentDiagnosticReport.serializer() as kotlinx.serialization.DeserializationStrategy<RelatedUnchangedDocumentDiagnosticReportRelatedDocuments>

            (obj["kind"] as? kotlinx.serialization.json.JsonPrimitive)?.contentOrNull ==
                "unchanged" -> UnchangedDocumentDiagnosticReport.serializer() as kotlinx.serialization.DeserializationStrategy<RelatedUnchangedDocumentDiagnosticReportRelatedDocuments>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown RelatedUnchangedDocumentDiagnosticReportRelatedDocuments variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: CallHierarchyOptions | CallHierarchyRegistrationOptions.
 */
@kotlinx.serialization.Serializable(
    with = ServerCapabilitiesCallHierarchyProviderOptionsSerializer::class
)
sealed interface ServerCapabilitiesCallHierarchyProviderOptions

object ServerCapabilitiesCallHierarchyProviderOptionsSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<ServerCapabilitiesCallHierarchyProviderOptions>(
        ServerCapabilitiesCallHierarchyProviderOptions::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesCallHierarchyProviderOptions> {
        val obj = element.jsonObject
        return when {
            /* fallback */
            obj.isNotEmpty() -> CallHierarchyOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesCallHierarchyProviderOptions>

            "documentSelector" in obj -> CallHierarchyRegistrationOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesCallHierarchyProviderOptions>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown ServerCapabilitiesCallHierarchyProviderOptions variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: DocumentColorOptions | DocumentColorRegistrationOptions.
 */
@kotlinx.serialization.Serializable(with = ServerCapabilitiesColorProviderOptionsSerializer::class)
sealed interface ServerCapabilitiesColorProviderOptions

object ServerCapabilitiesColorProviderOptionsSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<ServerCapabilitiesColorProviderOptions>(
        ServerCapabilitiesColorProviderOptions::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesColorProviderOptions> {
        val obj = element.jsonObject
        return when {
            /* fallback */
            obj.isNotEmpty() -> DocumentColorOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesColorProviderOptions>

            "documentSelector" in obj -> DocumentColorRegistrationOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesColorProviderOptions>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown ServerCapabilitiesColorProviderOptions variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: DeclarationOptions | DeclarationRegistrationOptions.
 */
@kotlinx.serialization.Serializable(
    with = ServerCapabilitiesDeclarationProviderOptionsSerializer::class
)
sealed interface ServerCapabilitiesDeclarationProviderOptions

object ServerCapabilitiesDeclarationProviderOptionsSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<ServerCapabilitiesDeclarationProviderOptions>(
        ServerCapabilitiesDeclarationProviderOptions::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesDeclarationProviderOptions> {
        val obj = element.jsonObject
        return when {
            /* fallback */
            obj.isNotEmpty() -> DeclarationOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesDeclarationProviderOptions>

            "documentSelector" in obj -> DeclarationRegistrationOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesDeclarationProviderOptions>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown ServerCapabilitiesDeclarationProviderOptions variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: DiagnosticOptions | DiagnosticRegistrationOptions.
 */
@kotlinx.serialization.Serializable(with = ServerCapabilitiesDiagnosticProviderSerializer::class)
sealed interface ServerCapabilitiesDiagnosticProvider

object ServerCapabilitiesDiagnosticProviderSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<ServerCapabilitiesDiagnosticProvider>(
        ServerCapabilitiesDiagnosticProvider::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesDiagnosticProvider> {
        val obj = element.jsonObject
        return when {
            "interFileDependencies" in obj -> DiagnosticOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesDiagnosticProvider>

            "documentSelector" in obj -> DiagnosticRegistrationOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesDiagnosticProvider>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown ServerCapabilitiesDiagnosticProvider variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: FoldingRangeOptions | FoldingRangeRegistrationOptions.
 */
@kotlinx.serialization.Serializable(
    with = ServerCapabilitiesFoldingRangeProviderOptionsSerializer::class
)
sealed interface ServerCapabilitiesFoldingRangeProviderOptions

object ServerCapabilitiesFoldingRangeProviderOptionsSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<ServerCapabilitiesFoldingRangeProviderOptions>(
        ServerCapabilitiesFoldingRangeProviderOptions::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesFoldingRangeProviderOptions> {
        val obj = element.jsonObject
        return when {
            /* fallback */
            obj.isNotEmpty() -> FoldingRangeOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesFoldingRangeProviderOptions>

            "documentSelector" in obj -> FoldingRangeRegistrationOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesFoldingRangeProviderOptions>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown ServerCapabilitiesFoldingRangeProviderOptions variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: ImplementationOptions | ImplementationRegistrationOptions.
 */
@kotlinx.serialization.Serializable(
    with = ServerCapabilitiesImplementationProviderOptionsSerializer::class
)
sealed interface ServerCapabilitiesImplementationProviderOptions

object ServerCapabilitiesImplementationProviderOptionsSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<ServerCapabilitiesImplementationProviderOptions>(
        ServerCapabilitiesImplementationProviderOptions::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesImplementationProviderOptions> {
        val obj = element.jsonObject
        return when {
            /* fallback */
            obj.isNotEmpty() -> ImplementationOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesImplementationProviderOptions>

            "documentSelector" in obj -> ImplementationRegistrationOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesImplementationProviderOptions>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown ServerCapabilitiesImplementationProviderOptions variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: InlayHintOptions | InlayHintRegistrationOptions.
 */
@kotlinx.serialization.Serializable(
    with = ServerCapabilitiesInlayHintProviderOptionsSerializer::class
)
sealed interface ServerCapabilitiesInlayHintProviderOptions

object ServerCapabilitiesInlayHintProviderOptionsSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<ServerCapabilitiesInlayHintProviderOptions>(
        ServerCapabilitiesInlayHintProviderOptions::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesInlayHintProviderOptions> {
        val obj = element.jsonObject
        return when {
            /* fallback */
            obj.isNotEmpty() -> InlayHintOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesInlayHintProviderOptions>

            "documentSelector" in obj -> InlayHintRegistrationOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesInlayHintProviderOptions>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown ServerCapabilitiesInlayHintProviderOptions variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: InlineValueOptions | InlineValueRegistrationOptions.
 */
@kotlinx.serialization.Serializable(
    with = ServerCapabilitiesInlineValueProviderOptionsSerializer::class
)
sealed interface ServerCapabilitiesInlineValueProviderOptions

object ServerCapabilitiesInlineValueProviderOptionsSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<ServerCapabilitiesInlineValueProviderOptions>(
        ServerCapabilitiesInlineValueProviderOptions::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesInlineValueProviderOptions> {
        val obj = element.jsonObject
        return when {
            /* fallback */
            obj.isNotEmpty() -> InlineValueOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesInlineValueProviderOptions>

            "documentSelector" in obj -> InlineValueRegistrationOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesInlineValueProviderOptions>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown ServerCapabilitiesInlineValueProviderOptions variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: LinkedEditingRangeOptions | LinkedEditingRangeRegistrationOptions.
 */
@kotlinx.serialization.Serializable(
    with = ServerCapabilitiesLinkedEditingRangeProviderOptionsSerializer::class
)
sealed interface ServerCapabilitiesLinkedEditingRangeProviderOptions

object ServerCapabilitiesLinkedEditingRangeProviderOptionsSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<ServerCapabilitiesLinkedEditingRangeProviderOptions>(
        ServerCapabilitiesLinkedEditingRangeProviderOptions::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesLinkedEditingRangeProviderOptions> {
        val obj = element.jsonObject
        return when {
            /* fallback */
            obj.isNotEmpty() -> LinkedEditingRangeOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesLinkedEditingRangeProviderOptions>

            "documentSelector" in obj -> LinkedEditingRangeRegistrationOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesLinkedEditingRangeProviderOptions>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown ServerCapabilitiesLinkedEditingRangeProviderOptions variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: MonikerOptions | MonikerRegistrationOptions.
 */
@kotlinx.serialization.Serializable(
    with = ServerCapabilitiesMonikerProviderOptionsSerializer::class
)
sealed interface ServerCapabilitiesMonikerProviderOptions

object ServerCapabilitiesMonikerProviderOptionsSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<ServerCapabilitiesMonikerProviderOptions>(
        ServerCapabilitiesMonikerProviderOptions::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesMonikerProviderOptions> {
        val obj = element.jsonObject
        return when {
            /* fallback */
            obj.isNotEmpty() -> MonikerOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesMonikerProviderOptions>

            "documentSelector" in obj -> MonikerRegistrationOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesMonikerProviderOptions>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown ServerCapabilitiesMonikerProviderOptions variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: NotebookDocumentSyncOptions | NotebookDocumentSyncRegistrationOptions.
 */
@kotlinx.serialization.Serializable(with = ServerCapabilitiesNotebookDocumentSyncSerializer::class)
sealed interface ServerCapabilitiesNotebookDocumentSync

object ServerCapabilitiesNotebookDocumentSyncSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<ServerCapabilitiesNotebookDocumentSync>(
        ServerCapabilitiesNotebookDocumentSync::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesNotebookDocumentSync> {
        val obj = element.jsonObject
        return when {
            "notebookSelector" in obj -> NotebookDocumentSyncOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesNotebookDocumentSync>

            "notebookSelector" in obj -> NotebookDocumentSyncRegistrationOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesNotebookDocumentSync>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown ServerCapabilitiesNotebookDocumentSync variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: SelectionRangeOptions | SelectionRangeRegistrationOptions.
 */
@kotlinx.serialization.Serializable(
    with = ServerCapabilitiesSelectionRangeProviderOptionsSerializer::class
)
sealed interface ServerCapabilitiesSelectionRangeProviderOptions

object ServerCapabilitiesSelectionRangeProviderOptionsSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<ServerCapabilitiesSelectionRangeProviderOptions>(
        ServerCapabilitiesSelectionRangeProviderOptions::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesSelectionRangeProviderOptions> {
        val obj = element.jsonObject
        return when {
            /* fallback */
            obj.isNotEmpty() -> SelectionRangeOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesSelectionRangeProviderOptions>

            "documentSelector" in obj -> SelectionRangeRegistrationOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesSelectionRangeProviderOptions>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown ServerCapabilitiesSelectionRangeProviderOptions variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: SemanticTokensOptions | SemanticTokensRegistrationOptions.
 */
@kotlinx.serialization.Serializable(
    with = ServerCapabilitiesSemanticTokensProviderSerializer::class
)
sealed interface ServerCapabilitiesSemanticTokensProvider

object ServerCapabilitiesSemanticTokensProviderSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<ServerCapabilitiesSemanticTokensProvider>(
        ServerCapabilitiesSemanticTokensProvider::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesSemanticTokensProvider> {
        val obj = element.jsonObject
        return when {
            "legend" in obj -> SemanticTokensOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesSemanticTokensProvider>

            "documentSelector" in obj -> SemanticTokensRegistrationOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesSemanticTokensProvider>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown ServerCapabilitiesSemanticTokensProvider variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: TypeDefinitionOptions | TypeDefinitionRegistrationOptions.
 */
@kotlinx.serialization.Serializable(
    with = ServerCapabilitiesTypeDefinitionProviderOptionsSerializer::class
)
sealed interface ServerCapabilitiesTypeDefinitionProviderOptions

object ServerCapabilitiesTypeDefinitionProviderOptionsSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<ServerCapabilitiesTypeDefinitionProviderOptions>(
        ServerCapabilitiesTypeDefinitionProviderOptions::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesTypeDefinitionProviderOptions> {
        val obj = element.jsonObject
        return when {
            /* fallback */
            obj.isNotEmpty() -> TypeDefinitionOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesTypeDefinitionProviderOptions>

            "documentSelector" in obj -> TypeDefinitionRegistrationOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesTypeDefinitionProviderOptions>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown ServerCapabilitiesTypeDefinitionProviderOptions variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: TypeHierarchyOptions | TypeHierarchyRegistrationOptions.
 */
@kotlinx.serialization.Serializable(
    with = ServerCapabilitiesTypeHierarchyProviderOptionsSerializer::class
)
sealed interface ServerCapabilitiesTypeHierarchyProviderOptions

object ServerCapabilitiesTypeHierarchyProviderOptionsSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<ServerCapabilitiesTypeHierarchyProviderOptions>(
        ServerCapabilitiesTypeHierarchyProviderOptions::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesTypeHierarchyProviderOptions> {
        val obj = element.jsonObject
        return when {
            /* fallback */
            obj.isNotEmpty() -> TypeHierarchyOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesTypeHierarchyProviderOptions>

            "documentSelector" in obj -> TypeHierarchyRegistrationOptions.serializer() as kotlinx.serialization.DeserializationStrategy<ServerCapabilitiesTypeHierarchyProviderOptions>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown ServerCapabilitiesTypeHierarchyProviderOptions variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: Command | CodeAction.
 */
@kotlinx.serialization.Serializable(with = TextDocumentCodeActionResultSerializer::class)
sealed interface TextDocumentCodeActionResult

object TextDocumentCodeActionResultSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<TextDocumentCodeActionResult>(
        TextDocumentCodeActionResult::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<TextDocumentCodeActionResult> {
        val obj = element.jsonObject
        return when {
            "command" in obj -> Command.serializer() as kotlinx.serialization.DeserializationStrategy<TextDocumentCodeActionResult>

            "title" in obj -> CodeAction.serializer() as kotlinx.serialization.DeserializationStrategy<TextDocumentCodeActionResult>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown TextDocumentCodeActionResult variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP literal union: TextDocumentContentChangeEvent.
 * Branches: TextDocumentContentChangeEventRange, TextDocumentContentChangeEventVariant.
 */
@kotlinx.serialization.Serializable(with = TextDocumentContentChangeEventSerializer::class)
sealed interface TextDocumentContentChangeEvent

object TextDocumentContentChangeEventSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<TextDocumentContentChangeEvent>(
        TextDocumentContentChangeEvent::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<TextDocumentContentChangeEvent> {
        val obj = element.jsonObject
        return when {
            "range" in obj -> TextDocumentContentChangeEventRange.serializer() as kotlinx.serialization.DeserializationStrategy<TextDocumentContentChangeEvent>

            "text" in obj -> TextDocumentContentChangeEventVariant.serializer() as kotlinx.serialization.DeserializationStrategy<TextDocumentContentChangeEvent>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown TextDocumentContentChangeEvent variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: TextEdit | AnnotatedTextEdit.
 */
@kotlinx.serialization.Serializable(with = TextDocumentEditEditsSerializer::class)
sealed interface TextDocumentEditEdits

object TextDocumentEditEditsSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<TextDocumentEditEdits>(
        TextDocumentEditEdits::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<TextDocumentEditEdits> {
        val obj = element.jsonObject
        return when {
            "range" in obj -> TextEdit.serializer() as kotlinx.serialization.DeserializationStrategy<TextDocumentEditEdits>

            "annotationId" in obj -> AnnotatedTextEdit.serializer() as kotlinx.serialization.DeserializationStrategy<TextDocumentEditEdits>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown TextDocumentEditEdits variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP literal union: TextDocumentFilter.
 * Branches: TextDocumentFilterLanguage, TextDocumentFilterScheme, TextDocumentFilterPattern.
 */
@kotlinx.serialization.Serializable(with = TextDocumentFilterSerializer::class)
sealed interface TextDocumentFilter

object TextDocumentFilterSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<TextDocumentFilter>(
        TextDocumentFilter::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<TextDocumentFilter> {
        val obj = element.jsonObject
        return when {
            "language" in obj -> TextDocumentFilterLanguage.serializer() as kotlinx.serialization.DeserializationStrategy<TextDocumentFilter>

            "scheme" in obj -> TextDocumentFilterScheme.serializer() as kotlinx.serialization.DeserializationStrategy<TextDocumentFilter>

            "pattern" in obj -> TextDocumentFilterPattern.serializer() as kotlinx.serialization.DeserializationStrategy<TextDocumentFilter>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown TextDocumentFilter variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: SemanticTokens | SemanticTokensDelta.
 */
@kotlinx.serialization.Serializable(
    with = TextDocumentSemanticTokensFullDeltaResultSerializer::class
)
sealed interface TextDocumentSemanticTokensFullDeltaResult

object TextDocumentSemanticTokensFullDeltaResultSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<TextDocumentSemanticTokensFullDeltaResult>(
        TextDocumentSemanticTokensFullDeltaResult::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<TextDocumentSemanticTokensFullDeltaResult> {
        val obj = element.jsonObject
        return when {
            "data" in obj -> SemanticTokens.serializer() as kotlinx.serialization.DeserializationStrategy<TextDocumentSemanticTokensFullDeltaResult>

            "edits" in obj -> SemanticTokensDelta.serializer() as kotlinx.serialization.DeserializationStrategy<TextDocumentSemanticTokensFullDeltaResult>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown TextDocumentSemanticTokensFullDeltaResult variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: WorkspaceFullDocumentDiagnosticReport | WorkspaceUnchangedDocumentDiagnosticReport.
 */
@kotlinx.serialization.Serializable(with = WorkspaceDocumentDiagnosticReportSerializer::class)
sealed interface WorkspaceDocumentDiagnosticReport

object WorkspaceDocumentDiagnosticReportSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<WorkspaceDocumentDiagnosticReport>(
        WorkspaceDocumentDiagnosticReport::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<WorkspaceDocumentDiagnosticReport> {
        val obj = element.jsonObject
        return when {
            (obj["kind"] as? kotlinx.serialization.json.JsonPrimitive)?.contentOrNull == "full" -> WorkspaceFullDocumentDiagnosticReport.serializer() as kotlinx.serialization.DeserializationStrategy<WorkspaceDocumentDiagnosticReport>

            (obj["kind"] as? kotlinx.serialization.json.JsonPrimitive)?.contentOrNull ==
                "unchanged" -> WorkspaceUnchangedDocumentDiagnosticReport.serializer() as kotlinx.serialization.DeserializationStrategy<WorkspaceDocumentDiagnosticReport>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown WorkspaceDocumentDiagnosticReport variant: $obj"
            )
        }
    }
}

/**
 * Sealed interface for the LSP union type: TextDocumentEdit | CreateFile | RenameFile | DeleteFile.
 */
@kotlinx.serialization.Serializable(with = WorkspaceEditDocumentChangesSerializer::class)
sealed interface WorkspaceEditDocumentChanges

object WorkspaceEditDocumentChangesSerializer :
    kotlinx.serialization.json.JsonContentPolymorphicSerializer<WorkspaceEditDocumentChanges>(
        WorkspaceEditDocumentChanges::class
    ) {
    override fun selectDeserializer(
        element: kotlinx.serialization.json.JsonElement
    ): kotlinx.serialization.DeserializationStrategy<WorkspaceEditDocumentChanges> {
        val obj = element.jsonObject
        return when {
            "textDocument" in obj -> TextDocumentEdit.serializer() as kotlinx.serialization.DeserializationStrategy<WorkspaceEditDocumentChanges>

            "kind" in obj -> CreateFile.serializer() as kotlinx.serialization.DeserializationStrategy<WorkspaceEditDocumentChanges>

            "oldUri" in obj -> RenameFile.serializer() as kotlinx.serialization.DeserializationStrategy<WorkspaceEditDocumentChanges>

            "kind" in obj -> DeleteFile.serializer() as kotlinx.serialization.DeserializationStrategy<WorkspaceEditDocumentChanges>

            else -> throw kotlinx.serialization.SerializationException(
                "Unknown WorkspaceEditDocumentChanges variant: $obj"
            )
        }
    }
}
