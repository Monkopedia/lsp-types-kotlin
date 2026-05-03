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
package com.monkopedia.lsp.codegen

import java.io.File
import kotlinx.serialization.json.Json

/**
 * Main entry point for the LSP code generator.
 *
 * Usage: Main <metaModel.json path> <output directory>
 *
 * Reads metaModel.json and generates Kotlin source files into the output directory
 * under the com/monkopedia/lsp/ package structure.
 */
fun main(args: Array<String>) {
    require(args.size == 2) {
        "Usage: lsp-codegen <metaModel.json> <output-dir>"
    }

    val metaModelFile = File(args[0])
    val outputDir = File(args[1])

    require(metaModelFile.exists()) { "metaModel.json not found: ${metaModelFile.absolutePath}" }

    val json = Json { ignoreUnknownKeys = true }
    val model = json.decodeFromString<MetaModel>(metaModelFile.readText())

    println(
        "LSP ${model.metaData.version}: " +
            "${model.structures.size} structures, " +
            "${model.enumerations.size} enums, " +
            "${model.typeAliases.size} aliases, " +
            "${model.requests.size} requests, " +
            "${model.notifications.size} notifications"
    )

    val packageDir = File(outputDir, "com/monkopedia/lsp")
    packageDir.mkdirs()

    val resolver = TypeResolver(model)
    val structGen = StructureGenerator(resolver)
    val enumGen = EnumGenerator(resolver)
    val aliasGen = TypeAliasGenerator(resolver)
    // Group structures by namespace prefix for per-file organization.
    // Structures starting with _ are internal helpers, group with their parent.
    val structureGroups = groupByNamespace(model.structures.map { it.name })

    // --- Base types ---
    writeFile(packageDir, "Base.kt") {
        appendLine(fileHeader(LSP_PACKAGE))
        appendLine("import kotlinx.serialization.Serializable")
        appendLine()
        appendLine("/**")
        appendLine(" * A document URI as defined by the LSP specification.")
        appendLine(" * Typically a `file://` URI string.")
        appendLine(" */")
        appendLine("typealias DocumentUri = String")
        appendLine()
        appendLine("/**")
        appendLine(" * A generic URI string.")
        appendLine(" */")
        appendLine("typealias URI = String")
    }

    // --- Enumerations ---
    writeFile(packageDir, "Enumerations.kt") {
        appendLine(fileHeader(LSP_PACKAGE))
        for (enum in model.enumerations) {
            appendLine(enumGen.generate(enum))
            appendLine()
        }
    }

    // --- Type aliases ---
    writeFile(packageDir, "TypeAliases.kt") {
        appendLine(fileHeader(LSP_PACKAGE))
        for (alias in model.typeAliases) {
            appendLine(aliasGen.generate(alias))
            appendLine()
        }
    }

    // --- Structures — one file per namespace group ---
    for ((group, names) in structureGroups) {
        val structures = names.mapNotNull { name ->
            model.structures.find { it.name == name }
        }
        writeFile(packageDir, "${group}Structures.kt") {
            appendLine(fileHeader(LSP_PACKAGE))
            for (struct in structures) {
                appendLine(structGen.generate(struct))
                appendLine()
            }
        }
    }

    // Emit any remaining inline literals that weren't emitted with their parent
    if (resolver.inlineLiterals.isNotEmpty()) {
        writeFile(packageDir, "InlineLiterals.kt") {
            appendLine(fileHeader(LSP_PACKAGE))
            for ((name, props) in resolver.inlineLiterals.toMap()) {
                val w = CodeWriter()
                structGen.generateClass(w, name, props, null, null)
                appendLine(w.toString())
                appendLine()
            }
        }
        resolver.inlineLiterals.clear()
    }

    // Service interfaces (LanguageServer/LanguageClient) are deferred until
    // ksrpc 1.0.0 ships — they need @KsService/@KsMethod/@KsNotification annotations.

    println("Generated files in ${packageDir.absolutePath}")
}

/**
 * Group structure names by a namespace prefix for file organization.
 * Names like "TextDocumentSyncOptions" → "TextDocument" group.
 * Names starting with "_" → grouped with their non-underscore parent.
 */
private fun groupByNamespace(names: List<String>): Map<String, List<String>> {
    // Well-known prefixes from the LSP spec
    val prefixes = listOf(
        "TextDocument", "Notebook", "Workspace", "Window", "General",
        "Diagnostic", "Completion", "SignatureHelp", "CodeAction", "CodeLens",
        "DocumentLink", "DocumentColor", "DocumentHighlight", "DocumentSymbol",
        "FoldingRange", "SelectionRange", "SemanticTokens", "InlineValue",
        "InlineCompletion", "Inlay", "Moniker", "WorkDoneProgress",
        "Declaration", "Definition", "TypeDefinition", "Implementation",
        "FileOperation", "DidChange", "DidOpen", "DidClose", "DidSave",
        "WillSave", "PublishDiagnostics", "Hover", "Location"
    )

    val groups = mutableMapOf<String, MutableList<String>>()
    for (name in names) {
        val cleanName = name.removePrefix("_")
        val group = prefixes.firstOrNull { cleanName.startsWith(it) } ?: "Common"
        groups.getOrPut(group) { mutableListOf() }.add(name)
    }
    return groups
}

private fun writeFile(dir: File, name: String, content: StringBuilder.() -> Unit) {
    val file = File(dir, name)
    val sb = StringBuilder()
    sb.content()
    // Collapse consecutive blank lines and trim trailing whitespace (ktlint compliance).
    val collapsed = sb.toString()
        .replace(Regex("\n{3,}"), "\n\n")
        .trimEnd()
        .plus("\n")
    file.writeText(collapsed)
    println("  wrote ${file.name}")
}
