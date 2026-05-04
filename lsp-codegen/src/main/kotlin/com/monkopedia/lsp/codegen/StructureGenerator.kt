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

/**
 * Generates @Serializable data classes for LSP structures.
 *
 * If the structure (by name) is registered in [structureInterfaces], the data
 * class declaration includes those sealed interfaces. This is populated by the
 * [UnionGenerator] during pre-classification.
 */
class StructureGenerator(
    private val resolver: TypeResolver,
    private val structureInterfaces: Map<String, List<String>> = emptyMap()
) {

    /**
     * Generate code for a single structure. Returns Kotlin source for the class
     * plus any inline literal classes it needs.
     */
    fun generate(structure: Structure): String {
        val w = CodeWriter()
        val allProps = resolver.collectAllProperties(structure)
        generateClass(w, structure.name, allProps, structure.documentation, structure.since)
        return w.toString()
    }

    fun generateClass(
        w: CodeWriter,
        name: String,
        properties: List<Property>,
        documentation: String?,
        since: String?,
        depth: Int = 0
    ) {
        check(depth < MAX_NESTING_DEPTH) {
            "Inline literal nesting exceeded $MAX_NESTING_DEPTH at $name — likely a cycle."
        }
        w.kdoc(documentation, since)
        val implementsClause = structureInterfaces[name].orEmpty()
            .takeIf { it.isNotEmpty() }
            ?.let { " : ${it.joinToString(", ")}" }
            ?: ""

        if (properties.isEmpty()) {
            w.line("@kotlinx.serialization.Serializable")
            w.line("class $name$implementsClause")
            return
        }

        // Snapshot which literals exist before resolving this class's properties.
        val literalsBefore = resolver.inlineLiterals.keys.toSet()

        w.line("@kotlinx.serialization.Serializable")
        w.line("data class $name(")
        w.indent {
            properties.forEachIndexed { i, prop ->
                val propContext = "${name}${prop.name.replaceFirstChar { it.uppercase() }}"
                val isNullable = prop.optional || isNullableType(prop.type)
                val kotlinType = resolver.resolve(prop.type, propContext, nullable = isNullable)
                val serialName = if (prop.name != prop.name.toKotlinPropertyName()) {
                    "@kotlinx.serialization.SerialName(\"${prop.name}\") "
                } else {
                    ""
                }
                kdoc(prop.documentation, prop.since)
                val default = if (prop.optional) " = null" else ""
                val comma = if (i < properties.lastIndex) "," else ""
                line(
                    "${serialName}val ${prop.name.toKotlinPropertyName()}: $kotlinType$default$comma"
                )
            }
        }
        w.line(")$implementsClause")

        // Emit inline literal classes that were newly registered by this class's properties.
        val newLiterals = resolver.inlineLiterals.keys - literalsBefore
        var iterations = 0
        for (litName in newLiterals.sorted()) {
            check(++iterations < MAX_LITERAL_ITERATIONS) {
                "Inline literal emission exceeded $MAX_LITERAL_ITERATIONS iterations at $name."
            }
            val litProps = resolver.inlineLiterals.remove(litName) ?: continue
            w.line()
            generateClass(w, litName, litProps, null, null, depth + 1)
        }
    }

    companion object {
        private const val MAX_NESTING_DEPTH = 20
        private const val MAX_LITERAL_ITERATIONS = 1000
    }

    private fun isNullableType(type: LspType): Boolean {
        if (type is LspType.Base && type.name == "null") return true
        if (type is LspType.Or) {
            return type.items.any { it is LspType.Base && it.name == "null" }
        }
        return false
    }
}

/** Convert LSP property name to valid Kotlin identifier. */
fun String.toKotlinPropertyName(): String {
    // Handle reserved words
    return when (this) {
        "val", "var", "fun", "class", "object", "interface", "is", "in", "as",
        "when", "if", "else", "for", "while", "do", "return", "throw", "try",
        "catch", "finally", "import", "package", "null", "true", "false",
        "this", "super", "typeof", "type", "data"
        -> "`$this`"

        else -> this
    }
}
