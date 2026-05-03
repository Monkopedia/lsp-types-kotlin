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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.serialization.json.Json

/**
 * Deserialization tests using JSON samples from authoritative sources:
 * - LSP 3.17 specification (https://microsoft.github.io/language-server-protocol/)
 * - microsoft/lsprotocol test suite (https://github.com/microsoft/lsprotocol)
 *
 * These verify that our generated types can parse real-world LSP messages.
 */
class SpecSampleDeserializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ---- Basic types from the LSP spec ----

    @Test
    fun `Position from spec`() {
        val result = json.decodeFromString<Position>(
            """{"line": 9, "character": 5}"""
        )
        assertEquals(9u, result.line)
        assertEquals(5u, result.character)
    }

    @Test
    fun `Range from spec`() {
        val result = json.decodeFromString<Range>(
            """{
                "start": {"line": 5, "character": 23},
                "end": {"line": 6, "character": 0}
            }"""
        )
        assertEquals(5u, result.start.line)
        assertEquals(23u, result.start.character)
        assertEquals(6u, result.end.line)
        assertEquals(0u, result.end.character)
    }

    @Test
    fun `Location from spec`() {
        val result = json.decodeFromString<Location>(
            """{
                "uri": "file:///workspace/lib.ts",
                "range": {
                    "start": {"line": 20, "character": 0},
                    "end": {"line": 25, "character": 1}
                }
            }"""
        )
        assertEquals("file:///workspace/lib.ts", result.uri)
        assertEquals(20u, result.range.start.line)
    }

    @Test
    fun `MarkupContent from spec`() {
        val result = json.decodeFromString<MarkupContent>(
            """{
                "kind": "markdown",
                "value": "# Header\nSome text\n```typescript\nsomeCode();\n```"
            }"""
        )
        assertEquals(MarkupKind.MARKDOWN, result.kind)
        assertNotNull(result.value)
    }

    // ---- Lifecycle messages ----

    @Test
    fun `InitializeResult from spec`() {
        val result = json.decodeFromString<InitializeResult>(
            """{
                "capabilities": {
                    "textDocumentSync": 1,
                    "completionProvider": {},
                    "hoverProvider": true
                },
                "serverInfo": {
                    "name": "MyLanguageServer",
                    "version": "1.0"
                }
            }"""
        )
        assertNotNull(result.capabilities)
        assertNotNull(result.serverInfo)
    }

    // ---- Text synchronization ----

    @Test
    fun `DidOpenTextDocumentParams from spec`() {
        val result = json.decodeFromString<DidOpenTextDocumentParams>(
            """{
                "textDocument": {
                    "uri": "file:///workspace/file.ts",
                    "languageId": "typescript",
                    "version": 1,
                    "text": "let x = 5;"
                }
            }"""
        )
        assertEquals("file:///workspace/file.ts", result.textDocument.uri)
        assertEquals("typescript", result.textDocument.languageId)
        assertEquals(1, result.textDocument.version)
        assertEquals("let x = 5;", result.textDocument.text)
    }

    @Test
    fun `DidChangeTextDocumentParams from spec`() {
        val result = json.decodeFromString<DidChangeTextDocumentParams>(
            """{
                "textDocument": {
                    "uri": "file:///workspace/file.ts",
                    "version": 2
                },
                "contentChanges": [
                    {
                        "range": {
                            "start": {"line": 0, "character": 8},
                            "end": {"line": 0, "character": 9}
                        },
                        "text": "10"
                    }
                ]
            }"""
        )
        assertEquals(2, result.textDocument.version)
        assertEquals(1, result.contentChanges.size)
    }

    // ---- Language features ----

    @Test
    fun `CompletionList from spec`() {
        val result = json.decodeFromString<CompletionList>(
            """{
                "isIncomplete": false,
                "items": [
                    {
                        "label": "console",
                        "kind": 6,
                        "detail": "namespace console"
                    }
                ]
            }"""
        )
        assertEquals(false, result.isIncomplete)
        assertEquals(1, result.items.size)
        assertEquals("console", result.items[0].label)
        assertEquals(CompletionItemKind.VARIABLE, result.items[0].kind)
    }

    @Test
    fun `SignatureHelp from spec`() {
        val result = json.decodeFromString<SignatureHelp>(
            """{
                "signatures": [
                    {
                        "label": "function(a: number, b: string): void",
                        "parameters": [
                            {"label": "a: number"},
                            {"label": "b: string"}
                        ]
                    }
                ],
                "activeSignature": 0,
                "activeParameter": 0
            }"""
        )
        assertEquals(1, result.signatures.size)
        assertEquals(2, result.signatures[0].parameters?.size)
        assertEquals(0u, result.activeSignature)
    }

    @Test
    fun `TextEdit from spec`() {
        val result = json.decodeFromString<TextEdit>(
            """{
                "range": {
                    "start": {"line": 0, "character": 0},
                    "end": {"line": 0, "character": 5}
                },
                "newText": "const"
            }"""
        )
        assertEquals("const", result.newText)
        assertEquals(0u, result.range.start.line)
    }

    @Test
    fun `ShowMessageParams from spec`() {
        val result = json.decodeFromString<ShowMessageParams>(
            """{"type": 1, "message": "Server initialization complete"}"""
        )
        assertEquals(MessageType.ERROR, result.type)
        assertEquals("Server initialization complete", result.message)
    }

    @Test
    fun `LogMessageParams from spec`() {
        val result = json.decodeFromString<LogMessageParams>(
            """{"type": 3, "message": "Processing document: file.ts"}"""
        )
        assertEquals(MessageType.INFO, result.type)
    }

    // ---- Samples from microsoft/lsprotocol test suite ----

    @Test
    fun `Diagnostic from lsprotocol`() {
        val result = json.decodeFromString<Diagnostic>(
            """{
                "range": {
                    "start": {"line": 0, "character": 0},
                    "end": {"line": 0, "character": 0}
                },
                "message": "Missing module docstring",
                "severity": 3,
                "code": "C0114:missing-module-docstring",
                "source": "my_lint"
            }"""
        )
        assertEquals("Missing module docstring", result.message)
        assertEquals(DiagnosticSeverity.INFORMATION, result.severity)
        assertEquals("my_lint", result.source)
    }

    @Test
    fun `PublishDiagnosticsParams from lsprotocol`() {
        val result = json.decodeFromString<PublishDiagnosticsParams>(
            """{
                "uri": "something.py",
                "diagnostics": [
                    {
                        "range": {
                            "start": {"line": 0, "character": 0},
                            "end": {"line": 0, "character": 0}
                        },
                        "message": "Missing module docstring",
                        "severity": 3,
                        "code": "C0114:missing-module-docstring",
                        "source": "my_lint"
                    },
                    {
                        "range": {
                            "start": {"line": 2, "character": 6},
                            "end": {"line": 2, "character": 7}
                        },
                        "message": "Undefined variable 'x'",
                        "severity": 1,
                        "code": "E0602:undefined-variable",
                        "source": "my_lint"
                    },
                    {
                        "range": {
                            "start": {"line": 0, "character": 0},
                            "end": {"line": 0, "character": 10}
                        },
                        "message": "Unused import sys",
                        "severity": 2,
                        "code": "W0611:unused-import",
                        "source": "my_lint"
                    }
                ]
            }"""
        )
        assertEquals("something.py", result.uri)
        assertEquals(3, result.diagnostics.size)
        assertEquals(DiagnosticSeverity.ERROR, result.diagnostics[1].severity)
        assertEquals(DiagnosticSeverity.WARNING, result.diagnostics[2].severity)
    }

    @Test
    fun `CompletionItem from lsprotocol`() {
        val result = json.decodeFromString<CompletionItem>(
            """{"label": "example", "documentation": "This is documented"}"""
        )
        assertEquals("example", result.label)
    }

    @Test
    fun `CallHierarchyIncomingCall from lsprotocol`() {
        val result = json.decodeFromString<CallHierarchyIncomingCall>(
            """{
                "from": {
                    "name": "something",
                    "kind": 5,
                    "uri": "something.py",
                    "range": {
                        "start": {"line": 0, "character": 0},
                        "end": {"line": 0, "character": 10}
                    },
                    "selectionRange": {
                        "start": {"line": 0, "character": 2},
                        "end": {"line": 0, "character": 8}
                    },
                    "data": {"something": "some other"}
                },
                "fromRanges": [
                    {
                        "start": {"line": 0, "character": 0},
                        "end": {"line": 0, "character": 10}
                    },
                    {
                        "start": {"line": 12, "character": 0},
                        "end": {"line": 13, "character": 0}
                    }
                ]
            }"""
        )
        assertEquals("something", result.from.name)
        assertEquals(SymbolKind.CLASS, result.from.kind)
        assertEquals(2, result.fromRanges.size)
    }

    @Test
    fun `InlayHint with string label from lsprotocol`() {
        val result = json.decodeFromString<InlayHint>(
            """{
                "position": {"line": 6, "character": 5},
                "label": "a label",
                "kind": 1,
                "paddingLeft": false,
                "paddingRight": true
            }"""
        )
        assertEquals(6u, result.position.line)
        assertEquals(InlayHintKind.TYPE, result.kind)
        assertEquals(true, result.paddingRight)
    }

    @Test
    fun `NotebookDocumentSyncOptions from lsprotocol`() {
        val result = json.decodeFromString<NotebookDocumentSyncOptions>(
            """{"notebookSelector": [{"cells": [{"language": "python"}]}]}"""
        )
        assertEquals(1, result.notebookSelector.size)
    }

    @Test
    fun `DidOpenNotebookDocumentParams from lsprotocol`() {
        val result = json.decodeFromString<DidOpenNotebookDocumentParams>(
            """{
                "notebookDocument": {
                    "uri": "untitled:Untitled-1.ipynb?jupyter-notebook",
                    "notebookType": "jupyter-notebook",
                    "version": 0,
                    "cells": [
                        {
                            "kind": 2,
                            "document": "vscode-notebook-cell:Untitled-1.ipynb",
                            "metadata": {"custom": {"metadata": {}}}
                        }
                    ],
                    "metadata": {"custom": {"cells": []}}
                },
                "cellTextDocuments": [
                    {
                        "uri": "vscode-notebook-cell:Untitled-1.ipynb",
                        "languageId": "python",
                        "version": 1,
                        "text": ""
                    }
                ]
            }"""
        )
        assertEquals("jupyter-notebook", result.notebookDocument.notebookType)
        assertEquals(1, result.notebookDocument.cells.size)
        assertEquals(NotebookCellKind.CODE, result.notebookDocument.cells[0].kind)
    }

    // ---- Full initialize request from lsprotocol (real coc.nvim capture) ----

    @Test
    fun `full InitializeParams from coc_nvim capture`() {
        val result = json.decodeFromString<InitializeParams>(
            """{
                "processId": 1105947,
                "rootUri": "file:///home/user/src/Personal/jedi-language-server",
                "capabilities": {
                    "workspace": {
                        "applyEdit": true,
                        "workspaceEdit": {
                            "documentChanges": true,
                            "resourceOperations": ["create", "rename", "delete"],
                            "failureHandling": "undo",
                            "normalizesLineEndings": true,
                            "changeAnnotationSupport": {"groupsOnLabel": false}
                        },
                        "didChangeConfiguration": {"dynamicRegistration": true},
                        "didChangeWatchedFiles": {
                            "dynamicRegistration": true,
                            "relativePatternSupport": true
                        },
                        "codeLens": {"refreshSupport": true},
                        "executeCommand": {"dynamicRegistration": true},
                        "configuration": true,
                        "fileOperations": {
                            "dynamicRegistration": true,
                            "didCreate": true,
                            "didRename": true,
                            "didDelete": true,
                            "willCreate": true,
                            "willRename": true,
                            "willDelete": true
                        },
                        "semanticTokens": {"refreshSupport": true},
                        "inlayHint": {"refreshSupport": true},
                        "inlineValue": {"refreshSupport": true},
                        "diagnostics": {"refreshSupport": true},
                        "symbol": {
                            "dynamicRegistration": true,
                            "symbolKind": {
                                "valueSet": [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26]
                            },
                            "tagSupport": {"valueSet": [1]},
                            "resolveSupport": {"properties": ["location.range"]}
                        },
                        "workspaceFolders": true
                    },
                    "textDocument": {
                        "publishDiagnostics": {
                            "relatedInformation": true,
                            "versionSupport": true,
                            "tagSupport": {"valueSet": [1, 2]},
                            "codeDescriptionSupport": true,
                            "dataSupport": true
                        },
                        "synchronization": {
                            "dynamicRegistration": true,
                            "willSave": true,
                            "willSaveWaitUntil": true,
                            "didSave": true
                        },
                        "completion": {
                            "dynamicRegistration": true,
                            "contextSupport": true,
                            "completionItem": {
                                "snippetSupport": true,
                                "commitCharactersSupport": true,
                                "documentationFormat": ["markdown", "plaintext"],
                                "deprecatedSupport": true,
                                "preselectSupport": true,
                                "insertReplaceSupport": true,
                                "tagSupport": {"valueSet": [1]},
                                "resolveSupport": {
                                    "properties": ["documentation", "detail", "additionalTextEdits"]
                                },
                                "labelDetailsSupport": true,
                                "insertTextModeSupport": {"valueSet": [1, 2]}
                            },
                            "completionItemKind": {
                                "valueSet": [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25]
                            },
                            "insertTextMode": 2,
                            "completionList": {
                                "itemDefaults": ["commitCharacters", "editRange", "insertTextFormat", "insertTextMode"]
                            }
                        },
                        "hover": {
                            "dynamicRegistration": true,
                            "contentFormat": ["markdown", "plaintext"]
                        },
                        "signatureHelp": {
                            "dynamicRegistration": true,
                            "contextSupport": true,
                            "signatureInformation": {
                                "documentationFormat": ["markdown", "plaintext"],
                                "activeParameterSupport": true,
                                "parameterInformation": {"labelOffsetSupport": true}
                            }
                        },
                        "codeAction": {
                            "dynamicRegistration": true,
                            "isPreferredSupport": true,
                            "disabledSupport": true,
                            "dataSupport": true,
                            "honorsChangeAnnotations": false,
                            "resolveSupport": {"properties": ["edit"]},
                            "codeActionLiteralSupport": {
                                "codeActionKind": {
                                    "valueSet": ["", "quickfix", "refactor", "refactor.extract", "refactor.inline", "refactor.rewrite", "source", "source.organizeImports"]
                                }
                            }
                        },
                        "rename": {
                            "dynamicRegistration": true,
                            "prepareSupport": true,
                            "honorsChangeAnnotations": true,
                            "prepareSupportDefaultBehavior": 1
                        },
                        "foldingRange": {
                            "dynamicRegistration": true,
                            "rangeLimit": 5000,
                            "lineFoldingOnly": true,
                            "foldingRangeKind": {"valueSet": ["comment", "imports", "region"]},
                            "foldingRange": {"collapsedText": false}
                        },
                        "semanticTokens": {
                            "dynamicRegistration": true,
                            "tokenTypes": ["namespace", "type", "class", "enum", "interface", "struct", "typeParameter", "parameter", "variable", "property", "enumMember", "event", "function", "method", "macro", "keyword", "modifier", "comment", "string", "number", "regexp", "decorator", "operator"],
                            "tokenModifiers": ["declaration", "definition", "readonly", "static", "deprecated", "abstract", "async", "modification", "documentation", "defaultLibrary"],
                            "formats": ["relative"],
                            "requests": {"range": true, "full": {"delta": true}},
                            "multilineTokenSupport": false,
                            "overlappingTokenSupport": false,
                            "serverCancelSupport": true,
                            "augmentsSyntaxTokens": true
                        },
                        "inlayHint": {
                            "dynamicRegistration": true,
                            "resolveSupport": {
                                "properties": ["tooltip", "textEdits", "label.tooltip", "label.location", "label.command"]
                            }
                        }
                    },
                    "window": {
                        "showMessage": {"messageActionItem": {"additionalPropertiesSupport": true}},
                        "showDocument": {"support": true},
                        "workDoneProgress": true
                    },
                    "general": {
                        "regularExpressions": {"engine": "ECMAScript", "version": "ES2020"},
                        "markdown": {"parser": "marked", "version": "4.0.10"},
                        "positionEncodings": ["utf-16"],
                        "staleRequestSupport": {
                            "cancel": true,
                            "retryOnContentModified": ["textDocument/inlayHint", "textDocument/semanticTokens/full", "textDocument/semanticTokens/range", "textDocument/semanticTokens/full/delta"]
                        }
                    }
                },
                "trace": "verbose",
                "workspaceFolders": [
                    {
                        "uri": "file:///home/user/src/Personal/jedi-language-server",
                        "name": "jedi-language-server"
                    }
                ],
                "locale": "en_US",
                "clientInfo": {"name": "coc.nvim", "version": "0.0.82"}
            }"""
        )
        assertNotNull(result.processId)
        assertEquals("coc.nvim", result.clientInfo?.name)
        assertNotNull(result.trace)
        assertNotNull(result.workspaceFolders)
        assertNotNull(result.capabilities.textDocument)
        assertNotNull(result.capabilities.workspace)
        assertNotNull(result.capabilities.window)
        assertNotNull(result.capabilities.general)
    }

    // ---- WorkspaceEdit (rename response) ----

    @Test
    fun `WorkspaceEdit from spec rename response`() {
        val result = json.decodeFromString<WorkspaceEdit>(
            """{
                "changes": {
                    "file:///workspace/file.ts": [
                        {
                            "range": {
                                "start": {"line": 0, "character": 4},
                                "end": {"line": 0, "character": 5}
                            },
                            "newText": "newVariableName"
                        }
                    ]
                }
            }"""
        )
        val changes = result.changes
        assertNotNull(changes)
        assertEquals(1, changes.size)
    }

    // ---- CodeAction response ----

    @Test
    fun `CodeAction from spec`() {
        val result = json.decodeFromString<CodeAction>(
            """{
                "title": "Extract variable",
                "kind": "refactor",
                "command": {
                    "title": "Extract",
                    "command": "extract.var"
                }
            }"""
        )
        assertEquals("Extract variable", result.title)
        assertEquals("extract.var", result.command?.command)
    }

    // ---- CodeLens response ----

    @Test
    fun `CodeLens from spec`() {
        val result = json.decodeFromString<CodeLens>(
            """{
                "range": {
                    "start": {"line": 5, "character": 0},
                    "end": {"line": 5, "character": 8}
                },
                "command": {
                    "title": "2 references",
                    "command": "editor.action.showReferences"
                }
            }"""
        )
        assertEquals("2 references", result.command?.title)
        assertEquals(5u, result.range.start.line)
    }

    // ---- WorkspaceSymbol response ----

    @Test
    fun `SymbolInformation from lsprotocol`() {
        val result = json.decodeFromString<SymbolInformation>(
            """{
                "name": "test",
                "kind": 1,
                "location": {
                    "uri": "test",
                    "range": {
                        "start": {"line": 1, "character": 1},
                        "end": {"line": 1, "character": 1}
                    }
                },
                "deprecated": true
            }"""
        )
        assertEquals("test", result.name)
        assertEquals(SymbolKind.FILE, result.kind)
        assertEquals(true, result.deprecated)
    }

    // ---- DocumentFormattingParams ----

    @Test
    fun `DocumentFormattingParams from spec`() {
        val result = json.decodeFromString<DocumentFormattingParams>(
            """{
                "textDocument": {
                    "uri": "file:///workspace/file.ts"
                },
                "options": {
                    "tabSize": 2,
                    "insertSpaces": true
                }
            }"""
        )
        assertEquals("file:///workspace/file.ts", result.textDocument.uri)
        assertEquals(2u, result.options.tabSize)
        assertEquals(true, result.options.insertSpaces)
    }

    // ---- RenameParams ----

    @Test
    fun `RenameParams from spec`() {
        val result = json.decodeFromString<RenameParams>(
            """{
                "textDocument": {
                    "uri": "file:///workspace/file.ts"
                },
                "position": {"line": 0, "character": 5},
                "newName": "newVariableName"
            }"""
        )
        assertEquals("newVariableName", result.newName)
        assertEquals(0u, result.position.line)
    }

    // ---- WorkDoneProgressBegin ----

    @Test
    fun `WorkDoneProgressBegin from spec`() {
        val result = json.decodeFromString<WorkDoneProgressBegin>(
            """{
                "kind": "begin",
                "title": "Finding references for A#foo",
                "cancellable": false,
                "message": "Processing file X.ts",
                "percentage": 0
            }"""
        )
        assertEquals("begin", result.kind)
        assertEquals("Finding references for A#foo", result.title)
        assertEquals(false, result.cancellable)
        assertEquals(0u, result.percentage)
    }
}
