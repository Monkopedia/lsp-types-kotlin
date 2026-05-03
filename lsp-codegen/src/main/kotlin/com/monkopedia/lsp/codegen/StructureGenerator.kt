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
 */
class StructureGenerator(private val resolver: TypeResolver) {

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
        since: String?
    ) {
        w.kdoc(documentation, since)
        if (properties.isEmpty()) {
            w.line("@kotlinx.serialization.Serializable")
            w.line("class $name")
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
        w.line(")")

        // Emit inline literal classes that were newly registered by this class's properties.
        val newLiterals = resolver.inlineLiterals.keys - literalsBefore
        for (litName in newLiterals.sorted()) {
            val litProps = resolver.inlineLiterals.remove(litName) ?: continue
            w.line()
            generateClass(w, litName, litProps, null, null)
        }
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
