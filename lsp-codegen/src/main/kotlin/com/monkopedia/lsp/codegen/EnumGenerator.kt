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

import kotlinx.serialization.json.jsonPrimitive

/**
 * Generates @Serializable enums and value-class wrappers for LSP enumerations.
 *
 * String-based enums without custom values → @Serializable enum class.
 * Integer/uinteger enums or enums with supportsCustomValues →
 *   @Serializable value class wrapping the raw type, with companion constants.
 */
class EnumGenerator(private val resolver: TypeResolver) {

    fun generate(enum: Enumeration): String {
        val w = CodeWriter()
        val baseType = (enum.type as LspType.Base).name

        if (enum.supportsCustomValues || baseType in setOf("integer", "uinteger")) {
            generateValueClass(w, enum, baseType)
        } else {
            generateEnumClass(w, enum)
        }
        return w.toString()
    }

    private fun generateEnumClass(w: CodeWriter, enum: Enumeration) {
        w.kdoc(enum.documentation, enum.since)
        w.line("@Serializable")
        w.block("enum class ${enum.name}") {
            enum.values.forEachIndexed { i, v ->
                val comma = if (i < enum.values.lastIndex) "," else ","
                val wireValue = v.value.jsonPrimitive.content
                kdoc(v.documentation, v.since)
                val enumName = v.name.toEnumEntryName()
                if (wireValue != enumName) {
                    line("@SerialName(\"$wireValue\")")
                }
                line("$enumName$comma")
            }
        }
    }

    private fun generateValueClass(w: CodeWriter, enum: Enumeration, baseType: String) {
        val kotlinType = when (baseType) {
            "string" -> "String"
            "integer" -> "Int"
            "uinteger" -> "UInt"
            else -> error("Unexpected enum base type: $baseType")
        }

        w.kdoc(enum.documentation, enum.since)
        w.line("@Serializable")
        w.line("@JvmInline")
        w.block("value class ${enum.name}(val value: $kotlinType)") {
            w.block("companion object") {
                for (v in enum.values) {
                    kdoc(v.documentation, v.since)
                    val rawValue = when (baseType) {
                        "string" -> "\"${v.value.jsonPrimitive.content}\""

                        "integer", "uinteger" -> {
                            val num = v.value.jsonPrimitive.content
                            if (baseType == "uinteger") "${num}u" else num
                        }

                        else -> v.value.jsonPrimitive.content
                    }
                    line("val ${v.name.toEnumEntryName()} = ${enum.name}($rawValue)")
                }
            }
        }
    }
}

/** Convert LSP enum value name to Kotlin-conventional UPPER_SNAKE_CASE. */
private fun String.toEnumEntryName(): String {
    // Handle names that are already camelCase by inserting underscores
    return replace(Regex("([a-z])([A-Z])"), "$1_$2")
        .replace(Regex("([A-Z]+)([A-Z][a-z])"), "$1_$2")
        .uppercase()
        .replace("-", "_")
        .let { name ->
            // Handle reserved words and names starting with digits
            if (name.first().isDigit() || name in setOf("CLASS", "INTERFACE")) {
                "`$name`"
            } else {
                name
            }
        }
}
