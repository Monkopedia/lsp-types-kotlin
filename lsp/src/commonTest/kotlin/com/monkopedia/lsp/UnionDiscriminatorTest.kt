/*
 * Copyright 2025 Jason Monk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.monkopedia.lsp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

/**
 * Tests that the generated sealed-interface union types correctly discriminate
 * between branches when deserializing real LSP JSON payloads.
 */
class UnionDiscriminatorTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ---- DocumentDiagnosticReport — discriminates on `kind` ----

    @Test
    fun `DocumentDiagnosticReport full variant`() {
        val result = json.decodeFromString(
            DocumentDiagnosticReportSerializer,
            """{
                "kind": "full",
                "items": [
                    {
                        "range": {"start": {"line": 1, "character": 1}, "end": {"line": 1, "character": 1}},
                        "message": "diag",
                        "severity": 1
                    }
                ]
            }"""
        )
        assertIs<RelatedFullDocumentDiagnosticReport>(result)
        assertEquals(1, result.items.size)
    }

    @Test
    fun `DocumentDiagnosticReport unchanged variant`() {
        val result = json.decodeFromString(
            DocumentDiagnosticReportSerializer,
            """{"kind": "unchanged", "resultId": "abc-123"}"""
        )
        assertIs<RelatedUnchangedDocumentDiagnosticReport>(result)
        assertEquals("abc-123", result.resultId)
    }

    // ---- InlineValue — discriminates on unique required fields ----

    @Test
    fun `InlineValue text variant`() {
        val result = json.decodeFromString(
            InlineValueSerializer,
            """{
                "range": {"start": {"line": 1, "character": 0}, "end": {"line": 1, "character": 5}},
                "text": "x = 42"
            }"""
        )
        assertIs<InlineValueText>(result)
        assertEquals("x = 42", result.text)
    }

    @Test
    fun `InlineValue variableLookup variant`() {
        val result = json.decodeFromString(
            InlineValueSerializer,
            """{
                "range": {"start": {"line": 1, "character": 0}, "end": {"line": 1, "character": 5}},
                "variableName": "x",
                "caseSensitiveLookup": true
            }"""
        )
        assertIs<InlineValueVariableLookup>(result)
        assertEquals("x", result.variableName)
    }

    @Test
    fun `InlineValue evaluatableExpression variant`() {
        val result = json.decodeFromString(
            InlineValueSerializer,
            """{
                "range": {"start": {"line": 1, "character": 0}, "end": {"line": 1, "character": 5}},
                "expression": "compute()"
            }"""
        )
        assertIs<InlineValueEvaluatableExpression>(result)
        assertEquals("compute()", result.expression)
    }

    // ---- BooleanOr<T> in real ServerCapabilities ----

    @Test
    fun `ServerCapabilities hoverProvider as boolean`() {
        val result = json.decodeFromString<ServerCapabilities>(
            """{"hoverProvider": true}"""
        )
        val provider = result.hoverProvider
        assertIs<BooleanOr.BooleanValue>(provider)
        assertEquals(true, provider.value)
    }

    @Test
    fun `ServerCapabilities hoverProvider as object`() {
        val result = json.decodeFromString<ServerCapabilities>(
            """{"hoverProvider": {"workDoneProgress": true}}"""
        )
        val provider = result.hoverProvider
        assertIs<BooleanOr.Value<HoverOptions>>(provider)
        assertEquals(true, provider.value.workDoneProgress)
    }

    // ---- BooleanOr<sub-sealed> for multi-options like declarationProvider ----

    @Test
    fun `ServerCapabilities declarationProvider as boolean`() {
        val result = json.decodeFromString<ServerCapabilities>(
            """{"declarationProvider": false}"""
        )
        assertNotNull(result.declarationProvider)
        assertTrue(result.declarationProvider is BooleanOr.BooleanValue)
    }

    @Test
    fun `ServerCapabilities declarationProvider as DeclarationOptions`() {
        val result = json.decodeFromString<ServerCapabilities>(
            """{"declarationProvider": {"workDoneProgress": true}}"""
        )
        assertNotNull(result.declarationProvider)
        assertTrue(result.declarationProvider is BooleanOr.Value)
    }

    // ---- StringOr<T> for documentation fields ----

    @Test
    fun `CompletionItem documentation as plain string`() {
        val result = json.decodeFromString<CompletionItem>(
            """{"label": "x", "documentation": "Just text"}"""
        )
        val doc = result.documentation
        assertIs<StringOr.StringValue>(doc)
        assertEquals("Just text", doc.value)
    }

    @Test
    fun `CompletionItem documentation as MarkupContent`() {
        val result = json.decodeFromString<CompletionItem>(
            """{"label": "x", "documentation": {"kind": "markdown", "value": "**bold**"}}"""
        )
        val doc = result.documentation
        assertIs<StringOr.Value<MarkupContent>>(doc)
        assertEquals("**bold**", doc.value.value)
    }

    // ---- IntOrString for ProgressToken ----

    @Test
    fun `ProgressToken as integer`() {
        val result = json.decodeFromString(IntOrStringSerializer, "42")
        assertIs<IntOrString.IntValue>(result)
        assertEquals(42, result.value)
    }

    @Test
    fun `ProgressToken as string`() {
        val result = json.decodeFromString(IntOrStringSerializer, "\"my-token\"")
        assertIs<IntOrString.StringValue>(result)
        assertEquals("my-token", result.value)
    }

    // ---- Discriminator specificity ----

    @Test
    fun `declarationProvider picks RegistrationOptions when documentSelector present`() {
        val result = json.decodeFromString<ServerCapabilities>(
            """{
                "declarationProvider": {
                    "documentSelector": [{"language": "kotlin"}],
                    "id": "decl-1"
                }
            }"""
        )
        val provider = result.declarationProvider
        assertIs<BooleanOr.Value<ServerCapabilitiesDeclarationProviderOptions>>(provider)
        // The unique field `documentSelector` is on RegistrationOptions, not on
        // the parent Options — so the discriminator must pick the more specific branch.
        assertIs<DeclarationRegistrationOptions>(provider.value)
    }

    @Test
    fun `declarationProvider picks Options when no extra fields present`() {
        val result = json.decodeFromString<ServerCapabilities>(
            """{"declarationProvider": {"workDoneProgress": true}}"""
        )
        val provider = result.declarationProvider
        assertIs<BooleanOr.Value<ServerCapabilitiesDeclarationProviderOptions>>(provider)
        assertIs<DeclarationOptions>(provider.value)
    }

    // ---- Round-trip tests for round-trip stability ----

    @Test
    fun `BooleanOr round-trip preserves boolean`() {
        val original: BooleanOr<HoverOptions> = BooleanOr.BooleanValue(true)
        val encoded = json.encodeToString(BooleanOrSerializer(HoverOptions.serializer()), original)
        val decoded = json.decodeFromString(BooleanOrSerializer(HoverOptions.serializer()), encoded)
        assertTrue(decoded is BooleanOr.BooleanValue)
        assertEquals(true, decoded.value)
    }

    @Test
    fun `InlineValue round-trip preserves variant`() {
        val original: InlineValue = InlineValueText(
            range = Range(Position(1u, 0u), Position(1u, 5u)),
            text = "hello"
        )
        val encoded = json.encodeToString(InlineValueSerializer, original)
        val decoded = json.decodeFromString(InlineValueSerializer, encoded)
        assertIs<InlineValueText>(decoded)
        assertEquals("hello", decoded.text)
    }

    @Test
    fun `DocumentDiagnosticReport round-trip preserves kind`() {
        val original: DocumentDiagnosticReport = RelatedUnchangedDocumentDiagnosticReport(
            kind = "unchanged",
            resultId = "v42"
        )
        val encoded = json.encodeToString(DocumentDiagnosticReportSerializer, original)
        val decoded = json.decodeFromString(DocumentDiagnosticReportSerializer, encoded)
        assertIs<RelatedUnchangedDocumentDiagnosticReport>(decoded)
        assertEquals("v42", decoded.resultId)
    }

    // ---- MIXED_REF_LITERAL unions (struct ref | inline literal) ----

    @Test
    fun `WorkspaceSymbolLocation full Location branch`() {
        val decoded = json.decodeFromString(
            WorkspaceSymbolLocationSerializer,
            """{"uri": "file:///a.kt", "range": {"start": {"line": 1, "character": 2}, """ +
                """"end": {"line": 3, "character": 4}}}"""
        )
        assertIs<Location>(decoded)
        assertEquals("file:///a.kt", decoded.uri)
    }

    @Test
    fun `WorkspaceSymbolLocation uri-only branch`() {
        val decoded = json.decodeFromString(
            WorkspaceSymbolLocationSerializer,
            """{"uri": "file:///a.kt"}"""
        )
        assertIs<WorkspaceSymbolLocationUri>(decoded)
        assertEquals("file:///a.kt", decoded.uri)
    }

    @Test
    fun `CompletionListItemDefaultsEditRange Range branch`() {
        val decoded = json.decodeFromString(
            CompletionListItemDefaultsEditRangeSerializer,
            """{"start": {"line": 1, "character": 0}, "end": {"line": 1, "character": 5}}"""
        )
        assertIs<Range>(decoded)
    }

    @Test
    fun `CompletionListItemDefaultsEditRange insert-replace branch`() {
        val decoded = json.decodeFromString(
            CompletionListItemDefaultsEditRangeSerializer,
            """{"insert": {"start": {"line": 1, "character": 0}, """ +
                """"end": {"line": 1, "character": 2}}, """ +
                """"replace": {"start": {"line": 1, "character": 0}, """ +
                """"end": {"line": 1, "character": 5}}}"""
        )
        assertIs<CompletionListItemDefaultsEditRangeInsert>(decoded)
    }

    @Test
    fun `PrepareRenameResult discriminates its three branches`() {
        val asRange = json.decodeFromString(
            PrepareRenameResultSerializer,
            """{"start": {"line": 1, "character": 0}, "end": {"line": 1, "character": 5}}"""
        )
        assertIs<Range>(asRange)

        val asRangePlaceholder = json.decodeFromString(
            PrepareRenameResultSerializer,
            """{"range": {"start": {"line": 1, "character": 0}, """ +
                """"end": {"line": 1, "character": 5}}, "placeholder": "name"}"""
        )
        assertIs<PrepareRenameResultRange>(asRangePlaceholder)
        assertEquals("name", asRangePlaceholder.placeholder)

        val asDefault = json.decodeFromString(
            PrepareRenameResultSerializer,
            """{"defaultBehavior": true}"""
        )
        assertIs<PrepareRenameResultDefaultBehavior>(asDefault)
        assertTrue(asDefault.defaultBehavior)
    }

    // ---- string | T[] and base | base[] field shapes ----

    @Test
    fun `InlayHint label is StringOr string branch`() {
        val hint = json.decodeFromString<InlayHint>(
            """{"position": {"line": 1, "character": 2}, "label": "hello"}"""
        )
        val label = hint.label
        assertTrue(label is StringOr.StringValue)
        assertEquals("hello", label.value)
    }

    @Test
    fun `InlayHint label is StringOr label-parts branch`() {
        val hint = json.decodeFromString<InlayHint>(
            """{"position": {"line": 1, "character": 2}, """ +
                """"label": [{"value": "part1"}, {"value": "part2"}]}"""
        )
        val label = hint.label
        assertTrue(label is StringOr.Value)
        assertEquals(2, label.value.size)
        assertEquals("part1", label.value[0].value)
    }

    @Test
    fun `section is SingleOrArray of String - single and array`() {
        val single = json.decodeFromString<DidChangeConfigurationRegistrationOptions>(
            """{"section": "files"}"""
        )
        val s = single.section
        assertTrue(s is SingleOrArray.Single)
        assertEquals("files", s.value)

        val multiple = json.decodeFromString<DidChangeConfigurationRegistrationOptions>(
            """{"section": ["files", "editor"]}"""
        )
        val m = multiple.section
        assertTrue(m is SingleOrArray.Multiple)
        assertEquals(2, m.value.size)
    }

    @Test
    fun `notebook is StringOr of NotebookDocumentFilter - string and filter`() {
        val asString = json.decodeFromString<NotebookCellTextDocumentFilter>(
            """{"notebook": "jupyter-notebook"}"""
        )
        assertTrue(asString.notebook is StringOr.StringValue)

        val asFilter = json.decodeFromString<NotebookCellTextDocumentFilter>(
            """{"notebook": {"notebookType": "jupyter"}}"""
        )
        assertTrue(asFilter.notebook is StringOr.Value)
    }

    // ---- #19: boolean|base, string|tuple, struct|enum ----

    @Test
    fun `changeNotifications is BooleanOr of String - boolean and string`() {
        val asBool = json.decodeFromString<WorkspaceFoldersServerCapabilities>(
            """{"changeNotifications": true}"""
        )
        val b = asBool.changeNotifications
        assertTrue(b is BooleanOr.BooleanValue)
        assertEquals(true, b.value)

        val asString = json.decodeFromString<WorkspaceFoldersServerCapabilities>(
            """{"changeNotifications": "reg-id-1"}"""
        )
        val s = asString.changeNotifications
        assertTrue(s is BooleanOr.Value)
        assertEquals("reg-id-1", s.value)
    }

    @Test
    fun `baseUri is StringOr of WorkspaceFolder - uri and object`() {
        val asUri = json.decodeFromString<RelativePattern>(
            """{"baseUri": "file:///root", "pattern": "*.kt"}"""
        )
        val u = asUri.baseUri
        assertTrue(u is StringOr.StringValue)
        assertEquals("file:///root", u.value)

        val asFolder = json.decodeFromString<RelativePattern>(
            """{"baseUri": {"uri": "file:///root", "name": "root"}, "pattern": "*.kt"}"""
        )
        val f = asFolder.baseUri
        assertTrue(f is StringOr.Value)
        assertEquals("root", f.value.name)
    }

    @Test
    fun `ParameterInformation label is StringOr offsets - string and tuple`() {
        val asString = json.decodeFromString<ParameterInformation>(
            """{"label": "param"}"""
        )
        val s = asString.label
        assertTrue(s is StringOr.StringValue)
        assertEquals("param", s.value)

        val asOffsets = json.decodeFromString<ParameterInformation>(
            """{"label": [4, 9]}"""
        )
        val o = asOffsets.label
        assertTrue(o is StringOr.Value)
        assertEquals(listOf(4u, 9u), o.value)
    }

    @Test
    fun `textDocumentSync discriminates options object vs kind int`() {
        val asKind = json.decodeFromString(
            ServerCapabilitiesTextDocumentSyncSerializer,
            "2"
        )
        assertIs<TextDocumentSyncKind>(asKind)
        assertEquals(2u, asKind.value)

        val asOptions = json.decodeFromString(
            ServerCapabilitiesTextDocumentSyncSerializer,
            """{"openClose": true, "change": 1}"""
        )
        assertIs<TextDocumentSyncOptions>(asOptions)
    }

    // ---- WorkspaceEditDocumentChanges — kind-value + presence discrimination ----
    // Regression: CreateFile/RenameFile/DeleteFile share a `kind` field and `uri`,
    // so required-field presence collides; they must discriminate on the `kind`
    // string value. TextDocumentEdit (no `kind`) discriminates on presence.

    @Test
    fun `WorkspaceEditDocumentChanges create branch`() {
        val decoded = json.decodeFromString(
            WorkspaceEditDocumentChangesSerializer,
            """{"kind": "create", "uri": "file:///new.kt"}"""
        )
        assertIs<CreateFile>(decoded)
        assertEquals("file:///new.kt", decoded.uri)
    }

    @Test
    fun `WorkspaceEditDocumentChanges rename branch`() {
        val decoded = json.decodeFromString(
            WorkspaceEditDocumentChangesSerializer,
            """{"kind": "rename", "oldUri": "file:///a.kt", "newUri": "file:///b.kt"}"""
        )
        assertIs<RenameFile>(decoded)
        assertEquals("file:///b.kt", decoded.newUri)
    }

    @Test
    fun `WorkspaceEditDocumentChanges delete branch`() {
        val decoded = json.decodeFromString(
            WorkspaceEditDocumentChangesSerializer,
            """{"kind": "delete", "uri": "file:///gone.kt"}"""
        )
        assertIs<DeleteFile>(decoded)
        assertEquals("file:///gone.kt", decoded.uri)
    }

    @Test
    fun `WorkspaceEditDocumentChanges textDocumentEdit branch`() {
        val decoded = json.decodeFromString(
            WorkspaceEditDocumentChangesSerializer,
            """{"textDocument": {"uri": "file:///a.kt", "version": 1}, "edits": []}"""
        )
        assertIs<TextDocumentEdit>(decoded)
    }

    @Test
    fun `WorkspaceEditDocumentChanges delete round-trip preserves branch`() {
        val original: WorkspaceEditDocumentChanges =
            DeleteFile(kind = "delete", uri = "file:///gone.kt")
        val encoded = json.encodeToString(WorkspaceEditDocumentChangesSerializer, original)
        val decoded = json.decodeFromString(WorkspaceEditDocumentChangesSerializer, encoded)
        assertIs<DeleteFile>(decoded)
    }

    // ---- ServerCapabilitiesNotebookDocumentSync — subtype-by-optional-field ----
    // Regression: RegistrationOptions extends Options with only an optional `id`,
    // so required-field presence collides; the subtype must be tried first via its
    // distinguishing optional field.

    @Test
    fun `NotebookDocumentSync registration branch when id present`() {
        val decoded = json.decodeFromString(
            ServerCapabilitiesNotebookDocumentSyncSerializer,
            """{"notebookSelector": [], "id": "reg-1"}"""
        )
        assertIs<NotebookDocumentSyncRegistrationOptions>(decoded)
        assertEquals("reg-1", decoded.id)
    }

    @Test
    fun `NotebookDocumentSync options branch when no id`() {
        val decoded = json.decodeFromString(
            ServerCapabilitiesNotebookDocumentSyncSerializer,
            """{"notebookSelector": []}"""
        )
        assertIs<NotebookDocumentSyncOptions>(decoded)
    }

    // ---- Empty-object provider options — a server may advertise a capability as
    // `{}` (e.g. gopls sends `inlayHintProvider: {}`), which is a spec-valid
    // all-optional Options object. The union must fall back to the base Options
    // branch, not throw. Regression for the nightly gopls real-server failure. ----

    @Test
    fun `empty object provider options deserialize as base Options branch`() {
        assertIs<InlayHintOptions>(
            json.decodeFromString(ServerCapabilitiesInlayHintProviderOptionsSerializer, "{}")
        )
        assertIs<TypeDefinitionOptions>(
            json.decodeFromString(ServerCapabilitiesTypeDefinitionProviderOptionsSerializer, "{}")
        )
        assertIs<SelectionRangeOptions>(
            json.decodeFromString(ServerCapabilitiesSelectionRangeProviderOptionsSerializer, "{}")
        )
    }

    @Test
    fun `ServerCapabilities with empty object provider options parses`() {
        val caps = json.decodeFromString(
            ServerCapabilities.serializer(),
            """{"inlayHintProvider": {}, "typeDefinitionProvider": {}, "selectionRangeProvider": {}}"""
        )
        assertNotNull(caps.inlayHintProvider)
        assertNotNull(caps.typeDefinitionProvider)
        assertNotNull(caps.selectionRangeProvider)
    }
}
