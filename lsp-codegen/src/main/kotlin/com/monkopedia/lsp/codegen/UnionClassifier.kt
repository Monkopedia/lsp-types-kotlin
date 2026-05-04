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
 * Categorizes LSP union ("or") types into Kotlin generation strategies.
 */
enum class UnionCategory {
    /** `T | null` — handled as nullable, no special generation needed. */
    T_OR_NULL,

    /** `boolean | OptionsType(s)` — generate `BooleanOr<T>`. Dominant in ServerCapabilities. */
    BOOLEAN_OR_OPTIONS,

    /** `T | T[]` — generate `SingleOrArray<T>`. */
    T_OR_ARRAY_T,

    /** `Ref | Ref | ...` — sealed interface with named branches. */
    NAMED_REFERENCES,

    /** `Literal | Literal | ...` — generate named data classes + sealed interface. */
    LITERAL_UNION,

    /** `string | T` — generate `StringOr<T>`. */
    STRING_OR,

    /** `integer | string` — `IntOrString`. */
    INT_OR_STRING,

    /** Mixed primitives or too complex — keep as `JsonElement`. */
    KEEP_JSON_ELEMENT,
}

/**
 * Result of classifying a union type. Carries metadata needed by the generator.
 */
data class UnionClassification(
    val category: UnionCategory,
    /** Items with `null` stripped (the union's "real" members). */
    val nonNullItems: List<LspType>,
    /** True if the original union included `null` (i.e., property should be nullable). */
    val isNullable: Boolean,
)

/**
 * Classifies a union type. Examines item kinds to determine which generation
 * strategy applies. [resolver] is consulted to distinguish struct-references from
 * enum-references (the latter are treated as primitives).
 */
fun classifyUnion(type: LspType.Or, resolver: TypeResolver? = null): UnionClassification {
    val items = type.items
    val nonNull = items.filter { !(it is LspType.Base && it.name == "null") }
    val isNullable = nonNull.size < items.size

    fun isStructRef(t: LspType): Boolean =
        t is LspType.Reference && (resolver == null || resolver.isStructure(t.name))

    val category = when {
        nonNull.isEmpty() -> UnionCategory.T_OR_NULL
        nonNull.size == 1 -> UnionCategory.T_OR_NULL
        isBoolPlusObjects(nonNull, ::isStructRef) -> UnionCategory.BOOLEAN_OR_OPTIONS
        isTOrArrayT(nonNull) -> UnionCategory.T_OR_ARRAY_T
        nonNull.all { isStructRef(it) } -> UnionCategory.NAMED_REFERENCES
        isAllLiterals(nonNull) -> UnionCategory.LITERAL_UNION
        isIntOrString(nonNull) -> UnionCategory.INT_OR_STRING
        isStringPlusOther(nonNull, ::isStructRef) -> UnionCategory.STRING_OR
        else -> UnionCategory.KEEP_JSON_ELEMENT
    }

    return UnionClassification(category, nonNull, isNullable)
}

private fun isBoolPlusObjects(items: List<LspType>, isStructRef: (LspType) -> Boolean): Boolean {
    val booleanCount = items.count { it is LspType.Base && it.name == "boolean" }
    if (booleanCount != 1) return false
    val others = items.filter { !(it is LspType.Base && it.name == "boolean") }
    // The other items must all be struct references or literals (i.e., object-shaped).
    return others.isNotEmpty() && others.all { isStructRef(it) || it is LspType.Literal }
}

private fun isTOrArrayT(items: List<LspType>): Boolean {
    if (items.size != 2) return false
    val refs = items.filterIsInstance<LspType.Reference>()
    val arrays = items.filterIsInstance<LspType.Array>()
    if (refs.size != 1 || arrays.size != 1) return false
    val arrayElement = arrays[0].element
    return arrayElement is LspType.Reference && arrayElement.name == refs[0].name
}

private fun isAllLiterals(items: List<LspType>): Boolean =
    items.all { it is LspType.Literal }

private fun isIntOrString(items: List<LspType>): Boolean {
    if (items.size != 2) return false
    val baseNames = items.filterIsInstance<LspType.Base>().map { it.name }.toSet()
    return baseNames == setOf("integer", "string") ||
        baseNames == setOf("uinteger", "string")
}

private fun isStringPlusOther(items: List<LspType>, isStructRef: (LspType) -> Boolean): Boolean {
    if (items.size != 2) return false
    val hasString = items.any { it is LspType.Base && it.name == "string" }
    val hasObject = items.any { isStructRef(it) || it is LspType.Literal }
    return hasString && hasObject
}
