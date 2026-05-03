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
 * Generates typealiases or sealed-class wrappers for LSP type aliases.
 *
 * Simple aliases (reference, base, array) → Kotlin `typealias`.
 * Union aliases (`or`) → `typealias` to `JsonElement` with a doc comment
 *   listing the possible types. Proper sealed-class union types are a future
 *   enhancement — JsonElement is safe and functional for all consumers.
 */
class TypeAliasGenerator(private val resolver: TypeResolver) {

    fun generate(alias: TypeAlias): String {
        val w = CodeWriter()
        w.kdoc(alias.documentation, alias.since)
        val resolved = resolver.resolve(alias.type, alias.name)
        w.line("typealias ${alias.name} = $resolved")
        return w.toString()
    }
}
