/*
 * Copyright 2026 Jason Monk
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
import kotlinx.serialization.json.Json

/**
 * Deserialization + round-trip tests with JSON payloads taken VERBATIM from the
 * microsoft/lsprotocol Python test suite.
 *
 * Each test cites the upstream source via `repo:path#L<lines>` in a comment.
 *
 * Repo: https://github.com/microsoft/lsprotocol
 */
class LsprotocolSampleTest {

    private val json = Json { ignoreUnknownKeys = true }

    private inline fun <reified T> roundTrip(payload: String): T {
        val decoded = json.decodeFromString<T>(payload)
        val reencoded = json.encodeToString(
            kotlinx.serialization.serializer<T>(),
            decoded
        )
        val redecoded = json.decodeFromString<T>(reencoded)
        assertEquals(decoded, redecoded, "round-trip equality failed")
        return decoded
    }

    // microsoft/lsprotocol:tests/python/requests/test_inlay_hint_resolve_request.py#L18-L24
    // inlayHint/resolve params with the string-label branch of InlayHint.label.
    @Test
    fun `InlayHint with string label from lsprotocol inlayHint resolve test`() {
        val result = roundTrip<InlayHint>(
            """{
                "position": {"line": 6, "character": 5},
                "label": "a label",
                "kind": 1,
                "paddingLeft": false,
                "paddingRight": true
            }"""
        )
        assertEquals(6u, result.position.line)
        val label = result.label
        assertIs<StringOr.StringValue>(label)
        assertEquals("a label", label.value)
        assertEquals(InlayHintKind.TYPE, result.kind)
        assertEquals(false, result.paddingLeft)
        assertEquals(true, result.paddingRight)
    }

    // microsoft/lsprotocol:tests/python/requests/test_inlay_hint_resolve_request.py#L46-L55
    // inlayHint/resolve params with the InlayHintLabelPart[] branch (multiple parts, one with tooltip).
    @Test
    fun `InlayHint with multi-part label from lsprotocol inlayHint resolve test`() {
        val result = roundTrip<InlayHint>(
            """{
                "position": {"line": 6, "character": 5},
                "label": [
                    {"value": "part 1"},
                    {"value": "part 2", "tooltip": "a tooltip"}
                ],
                "kind": 1,
                "paddingLeft": false,
                "paddingRight": true
            }"""
        )
        val label = result.label
        assertIs<StringOr.Value<List<InlayHintLabelPart>>>(label)
        assertEquals(2, label.value.size)
        assertEquals("part 1", label.value[0].value)
        assertEquals("part 2", label.value[1].value)
        val tooltip = label.value[1].tooltip
        assertIs<StringOr.StringValue>(tooltip)
        assertEquals("a tooltip", tooltip.value)
    }

    // microsoft/lsprotocol:tests/python/requests/test_workspace_symbols_request.py#L12-L16
    // WorkspaceSymbol result with a bare-uri location and no range.
    @Test
    fun `WorkspaceSymbol with bare uri from lsprotocol workspace symbols test`() {
        val result = roundTrip<WorkspaceSymbol>(
            """{"name": "test", "kind": 1, "location": {"uri": "test"}}"""
        )
        assertEquals("test", result.name)
        assertEquals(SymbolKind.FILE, result.kind)
        val loc = result.location
        assertIs<WorkspaceSymbolLocationUri>(loc)
        assertEquals("test", loc.uri)
    }

    // microsoft/lsprotocol:tests/python/requests/test_workspace_symbols_request.py#L19-L33
    // WorkspaceSymbol result with a `Location` (uri + range) — covers the Location branch.
    @Test
    fun `WorkspaceSymbol with Location from lsprotocol workspace symbols test`() {
        val result = roundTrip<WorkspaceSymbol>(
            """{
                "name": "test",
                "kind": 1,
                "location": {
                    "uri": "test",
                    "range": {
                        "start": {"line": 1, "character": 1},
                        "end": {"line": 1, "character": 1}
                    }
                }
            }"""
        )
        assertEquals("test", result.name)
        val loc = result.location
        assertIs<Location>(loc)
        assertEquals("test", loc.uri)
        assertEquals(1u, loc.range.start.line)
    }

    // microsoft/lsprotocol:tests/python/requests/test_workspace_symbols_request.py#L34-L38
    // WorkspaceSymbol with `data` field set.
    @Test
    fun `WorkspaceSymbol with data field from lsprotocol workspace symbols test`() {
        val result = roundTrip<WorkspaceSymbol>(
            """{"name": "test", "kind": 1, "location": {"uri": "test"}, "data": 1}"""
        )
        assertEquals("test", result.name)
        // `data` is LSPAny — verify decoded value preserved.
        val data = result.`data`
        assertEquals(json.parseToJsonElement("1"), data)
    }

    // microsoft/lsprotocol:tests/python/notifications/test_progress.py#L22-L43
    // $/progress notification params with a `begin` value (percentage 0).
    @Test
    fun `ProgressParams with WorkDoneProgressBegin from lsprotocol progress test`() {
        val result = roundTrip<ProgressParams>(
            """{
                "token": "id1",
                "value": {
                    "title": "Begin Progress",
                    "kind": "begin",
                    "percentage": 0
                }
            }"""
        )
        val token = result.token
        assertIs<IntOrString.StringValue>(token)
        assertEquals("id1", token.value)
        val begin = json.decodeFromJsonElement(
            WorkDoneProgressBegin.serializer(),
            result.value
        )
        assertEquals("Begin Progress", begin.title)
        assertEquals(0u, begin.percentage)
    }

    // microsoft/lsprotocol:tests/python/notifications/test_progress.py#L44-L65
    // $/progress with a `report` value carrying message + percentage.
    @Test
    fun `ProgressParams with WorkDoneProgressReport from lsprotocol progress test`() {
        val result = roundTrip<ProgressParams>(
            """{
                "token": "id1",
                "value": {
                    "kind": "report",
                    "message": "Still going",
                    "percentage": 50
                }
            }"""
        )
        val report = json.decodeFromJsonElement(
            WorkDoneProgressReport.serializer(),
            result.value
        )
        assertEquals("Still going", report.message)
        assertEquals(50u, report.percentage)
    }

    // microsoft/lsprotocol:tests/python/notifications/test_progress.py#L66-L86
    // $/progress with an `end` value carrying a final message.
    @Test
    fun `ProgressParams with WorkDoneProgressEnd from lsprotocol progress test`() {
        val result = roundTrip<ProgressParams>(
            """{
                "token": "id1",
                "value": {
                    "kind": "end",
                    "message": "Finished"
                }
            }"""
        )
        val end = json.decodeFromJsonElement(
            WorkDoneProgressEnd.serializer(),
            result.value
        )
        assertEquals("Finished", end.message)
    }
}
