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
import kotlin.concurrent.thread
import kotlin.system.exitProcess
import kotlinx.serialization.json.Json

/** Hard timeout — abort if generation takes longer than this. */
private const val GENERATION_TIMEOUT_SECONDS = 60L

/**
 * Main entry point for the LSP code generator.
 *
 * Usage: Main <metaModel.json path> <output directory>
 *
 * Reads metaModel.json and generates Kotlin source files into the output directory
 * under the com/monkopedia/lsp/ package structure.
 */
fun main(args: Array<String>) {
    // Watchdog: kill the process if generation hangs.
    val mainThread = Thread.currentThread()
    val watchdog = thread(isDaemon = true, name = "codegen-watchdog") {
        try {
            Thread.sleep(GENERATION_TIMEOUT_SECONDS * 1000)
        } catch (_: InterruptedException) {
            return@thread // Generation completed normally
        }
        System.err.println(
            "ERROR: codegen exceeded ${GENERATION_TIMEOUT_SECONDS}s timeout — aborting."
        )
        System.err.println("Main thread stack trace:")
        mainThread.stackTrace.forEach { System.err.println("  at $it") }
        exitProcess(2)
    }

    try {
        runGeneration(args)
    } finally {
        watchdog.interrupt()
    }
}

private fun runGeneration(args: Array<String>) {
    require(args.size in 2..3) {
        "Usage: lsp-codegen <metaModel.json> <types-output-dir> [services-output-dir]"
    }

    val metaModelFile = File(args[0])
    val outputDir = File(args[1])
    val servicesOutputDir = if (args.size >= 3) File(args[2]) else null

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
    val unionGen = UnionGenerator(resolver)
    resolver.unionGenerator = unionGen

    // Pre-classify all unions so we know which sealed interfaces each
    // structure needs to implement before generating the data classes.
    System.err.println("Pre-classifying unions...")
    preClassifyUnions(model, resolver, unionGen)

    val structGen = StructureGenerator(resolver, unionGen.structureInterfaces)
    val enumGen = EnumGenerator(resolver)
    val aliasGen = TypeAliasGenerator(resolver)
    // Group structures by namespace prefix for per-file organization.
    // Structures starting with _ are internal helpers, group with their parent.
    val structureGroups = groupByNamespace(model.structures.map { it.name })

    // --- Base types ---
    writeFile(packageDir, "Base.kt") {
        appendLine(fileHeader(LSP_PACKAGE))
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

    val structureImports = listOf(
        "kotlinx.serialization.EncodeDefault",
        "kotlinx.serialization.SerialName",
        "kotlinx.serialization.Serializable",
        "kotlinx.serialization.json.JsonArray",
        "kotlinx.serialization.json.JsonElement"
    )

    // --- Enumerations ---
    writeFile(packageDir, "Enumerations.kt") {
        appendLine(
            fileHeader(
                LSP_PACKAGE,
                imports = listOf(
                    "kotlin.jvm.JvmInline",
                    "kotlinx.serialization.SerialName",
                    "kotlinx.serialization.Serializable"
                )
            )
        )
        for (enum in model.enumerations) {
            appendLine(enumGen.generate(enum))
            appendLine()
        }
    }

    // --- Type aliases ---
    writeFile(packageDir, "TypeAliases.kt") {
        appendLine(
            fileHeader(
                LSP_PACKAGE,
                imports = listOf("kotlinx.serialization.json.JsonElement")
            )
        )
        for (alias in model.typeAliases) {
            val src = aliasGen.generate(alias)
            if (src.isNotBlank()) {
                appendLine(src)
                appendLine()
            }
        }
    }

    // --- Structures — one file per namespace group ---
    for ((group, names) in structureGroups) {
        val structures = names.mapNotNull { name ->
            model.structures.find { it.name == name }
        }
        writeFile(packageDir, "${group}Structures.kt") {
            appendLine(fileHeader(LSP_PACKAGE, imports = structureImports))
            for (struct in structures) {
                System.err.println("  generating ${struct.name}")
                appendLine(structGen.generate(struct))
                appendLine()
            }
        }
    }

    // --- Service interfaces ---
    // Clean interfaces (no transport annotations) live in :lsp alongside the types.
    // Ksrpc-annotated subinterfaces and Default* base classes live in :lsp-ksrpc.
    //
    // Both are emitted BEFORE InlineLiterals / UnionBranches / Unions so any
    // sealed interfaces or inline literals discovered while resolving
    // request/notification result/param types get included.
    val serviceGen = ServiceGenerator(resolver, model)

    // Clean interfaces — always emitted into :lsp (the types module).
    val cleanImports = listOf("kotlinx.serialization.json.JsonElement")
    writeFile(packageDir, "LanguageServer.kt") {
        appendLine(fileHeader(LSP_PACKAGE, imports = cleanImports))
        appendLine(serviceGen.generateCleanServer())
    }
    writeFile(packageDir, "LanguageClient.kt") {
        appendLine(fileHeader(LSP_PACKAGE, imports = cleanImports))
        appendLine(serviceGen.generateCleanClient())
    }

    // Ksrpc subinterfaces and defaults — emitted into :lsp-ksrpc when configured.
    if (servicesOutputDir != null) {
        val servicesPackageDir = File(servicesOutputDir, "com/monkopedia/lsp")
        servicesPackageDir.mkdirs()

        val ksrpcImports = listOf(
            "com.monkopedia.ksrpc.RpcService",
            "com.monkopedia.ksrpc.annotation.KsMethod",
            "com.monkopedia.ksrpc.annotation.KsNotification",
            "com.monkopedia.ksrpc.annotation.KsService",
            "kotlinx.serialization.json.JsonElement"
        )
        val defaultImports = listOf("kotlinx.serialization.json.JsonElement")

        writeFile(servicesPackageDir, "KsrpcLanguageServer.kt") {
            appendLine(fileHeader(LSP_PACKAGE, imports = ksrpcImports))
            appendLine(serviceGen.generateKsrpcServer())
        }
        writeFile(servicesPackageDir, "KsrpcLanguageClient.kt") {
            appendLine(fileHeader(LSP_PACKAGE, imports = ksrpcImports))
            appendLine(serviceGen.generateKsrpcClient())
        }
        writeFile(servicesPackageDir, "DefaultLanguageServer.kt") {
            appendLine(fileHeader(LSP_PACKAGE, imports = defaultImports))
            appendLine(serviceGen.generateDefaultServer())
        }
        writeFile(servicesPackageDir, "DefaultLanguageClient.kt") {
            appendLine(fileHeader(LSP_PACKAGE, imports = defaultImports))
            appendLine(serviceGen.generateDefaultClient())
        }
        println("Generated services in ${servicesPackageDir.absolutePath}")
    }

    // Emit any remaining inline literals that weren't emitted with their parent
    if (resolver.inlineLiterals.isNotEmpty()) {
        writeFile(packageDir, "InlineLiterals.kt") {
            appendLine(fileHeader(LSP_PACKAGE, imports = structureImports))
            for ((name, props) in resolver.inlineLiterals.toMap()) {
                val w = CodeWriter()
                structGen.generateClass(w, name, props, null, null)
                appendLine(w.toString())
                appendLine()
            }
        }
        resolver.inlineLiterals.clear()
    }

    // --- Generated literal-branch data classes (for LITERAL_UNION sealed interfaces) ---
    if (unionGen.literalBranches.isNotEmpty()) {
        writeFile(packageDir, "UnionBranches.kt") {
            appendLine(fileHeader(LSP_PACKAGE, imports = structureImports))
            for ((name, props) in unionGen.literalBranches.toMap()) {
                val w = CodeWriter()
                structGen.generateClass(w, name, props, null, null)
                appendLine(w.toString())
                appendLine()
            }
        }
    }

    // --- Sealed interfaces and serializers for unions ---
    val unionsSrc = unionGen.generateUnionsFile()
    if (unionsSrc.isNotBlank()) {
        writeFile(packageDir, "Unions.kt") {
            appendLine(
                fileHeader(
                    LSP_PACKAGE,
                    imports = listOf(
                        "kotlinx.serialization.DeserializationStrategy",
                        "kotlinx.serialization.SerializationException",
                        "kotlinx.serialization.Serializable",
                        "kotlinx.serialization.json.JsonContentPolymorphicSerializer",
                        "kotlinx.serialization.json.JsonElement",
                        "kotlinx.serialization.json.JsonPrimitive",
                        "kotlinx.serialization.json.contentOrNull",
                        "kotlinx.serialization.json.jsonObject"
                    )
                )
            )
            appendLine(unionsSrc)
        }
    }

    println("Generated files in ${packageDir.absolutePath}")
}

/**
 * Walk the model and ask the UnionGenerator to register sealed interfaces for every
 * `NAMED_REFERENCES` and `LITERAL_UNION` it finds. This pre-pass populates the
 * `structureInterfaces` map so structures know which interfaces to implement.
 */
private fun preClassifyUnions(model: MetaModel, resolver: TypeResolver, unionGen: UnionGenerator) {
    fun visit(contextName: String, type: LspType, topLevelAliasName: String? = null) {
        when (type) {
            is LspType.Or -> {
                val cls = classifyUnion(type, resolver)
                if (cls.category == UnionCategory.NAMED_REFERENCES ||
                    cls.category == UnionCategory.LITERAL_UNION ||
                    cls.category == UnionCategory.MIXED_REF_LITERAL ||
                    cls.category == UnionCategory.BOOLEAN_OR_OPTIONS
                ) {
                    unionGen.resolveUnion(type, contextName, topLevelAliasName)
                }
                // Recurse into the union's items in case nested unions exist.
                cls.nonNullItems.forEach { visit(contextName, it) }
            }

            is LspType.Array -> visit(contextName, type.element)

            is LspType.Map -> visit(contextName, type.value)

            is LspType.Literal ->
                type.value.properties.forEach {
                    visit(
                        "$contextName${it.name.replaceFirstChar { c -> c.uppercase() }}",
                        it.type
                    )
                }

            else -> {}
        }
    }

    for (s in model.structures) {
        for (p in s.properties) {
            visit(
                "${s.name}${p.name.replaceFirstChar { it.uppercase() }}",
                p.type
            )
        }
    }
    for (a in model.typeAliases) {
        visit(a.name, a.type, topLevelAliasName = a.name)
    }
    // Walk request/notification result/param/error types so unions there get
    // sealed interfaces too.
    for (req in model.requests) {
        val methodKey = req.method.toContextName()
        visit("${methodKey}Result", req.result)
        req.params?.let { visit("${methodKey}Params", it) }
        req.errorData?.let { visit("${methodKey}ErrorData", it) }
    }
    for (notif in model.notifications) {
        val methodKey = notif.method.toContextName()
        notif.params?.let { visit("${methodKey}Params", it) }
    }
}

/** Convert a wire method name to a PascalCase context prefix (matches ServiceGenerator). */
private fun String.toContextName(): String {
    val cleaned = removePrefix("$/")
    return cleaned.split("/").mapIndexed { i, part ->
        if (i == 0) {
            part.replaceFirstChar { c -> c.uppercase() }
        } else {
            part.replaceFirstChar { c -> c.uppercase() }
        }
    }.joinToString("")
}

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
