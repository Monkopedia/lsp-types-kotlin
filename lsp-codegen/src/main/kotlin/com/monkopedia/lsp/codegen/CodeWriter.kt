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
 * Helper for building Kotlin source files with correct indentation.
 */
class CodeWriter {
    private val sb = StringBuilder()
    private var indent = 0

    fun line(text: String = "") {
        if (text.isEmpty()) {
            sb.appendLine()
        } else {
            sb.appendLine("    ".repeat(indent) + text)
        }
    }

    fun indent(body: CodeWriter.() -> Unit) {
        indent++
        body()
        indent--
    }

    fun block(header: String, body: CodeWriter.() -> Unit) {
        line("$header {")
        indent++
        body()
        indent--
        line("}")
    }

    override fun toString(): String = sb.toString()
}

/**
 * Format a KDoc comment from LSP documentation string.
 */
fun CodeWriter.kdoc(documentation: String?, since: String? = null) {
    if (documentation == null && since == null) return
    line("/**")
    documentation?.lines()?.forEach { docLine ->
        // Escape /* and */ inside KDoc to prevent nested/unclosed comments.
        // Trim trailing whitespace to avoid ktlint no-trailing-spaces violations.
        val escaped = docLine.replace("/*", "/&#42;").replace("*/", "&#42;/")
        line(" * $escaped".trimEnd())
    }
    // Only emit @since if it's not already in the documentation text.
    if (since != null && (documentation == null || "@since $since" !in documentation)) {
        if (documentation != null) line(" *")
        line(" * @since $since")
    }
    line(" */")
}

/**
 * Emit file header with license and package declaration.
 */
fun fileHeader(packageName: String): String = buildString {
    appendLine("// Auto-generated from LSP metaModel.json — do not edit manually.")
    appendLine("// Generator: lsp-codegen")
    appendLine()
    appendLine("@file:Suppress(")
    appendLine("    \"unused\",")
    appendLine("    \"PropertyName\",")
    appendLine("    \"ktlint:standard:class-naming\",")
    appendLine("    \"ktlint:standard:filename\",")
    appendLine("    \"ktlint:standard:max-line-length\",")
    appendLine(")")
    appendLine()
    appendLine("package $packageName")
    appendLine()
}

const val LSP_PACKAGE = "com.monkopedia.lsp"
