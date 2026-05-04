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
 * Generates typealiases or sealed-interface wrappers for LSP type aliases.
 *
 * - Simple aliases (reference, base, array) → Kotlin `typealias`.
 * - Union aliases (`or`) where the classifier produces a sealed interface
 *   (NAMED_REFERENCES, LITERAL_UNION) → no typealias — the sealed interface
 *   itself IS the type, emitted by [UnionGenerator].
 * - Other unions (BOOLEAN_OR_OPTIONS, T_OR_ARRAY_T, STRING_OR, INT_OR_STRING) →
 *   typealias to the wrapper type (e.g., `typealias Definition = SingleOrArray<Location>`).
 */
class TypeAliasGenerator(private val resolver: TypeResolver) {

    fun generate(alias: TypeAlias): String {
        val w = CodeWriter()

        // For union aliases, set the top-level name so the union generator can
        // use it as the sealed interface name (e.g., `Definition`, `InlineValue`).
        if (alias.type is LspType.Or) {
            val cls = classifyUnion(alias.type, resolver)
            // Categories that produce a sealed interface keyed on the alias name —
            // skip emitting a typealias entirely (the interface IS the type).
            if (cls.category == UnionCategory.NAMED_REFERENCES ||
                cls.category == UnionCategory.LITERAL_UNION
            ) {
                resolver.topLevelAliasName = alias.name
                resolver.resolve(alias.type, alias.name)
                // No typealias emitted — UnionGenerator owns this name.
                return ""
            }
        }

        w.kdoc(alias.documentation, alias.since)
        val resolved = resolver.resolve(alias.type, alias.name)
        w.line("typealias ${alias.name} = $resolved")
        return w.toString()
    }
}
