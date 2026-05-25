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

    /** `Ref | Literal | ...` (a mix of struct refs and inline literals) — sealed interface. */
    MIXED_REF_LITERAL,

    /** `StructRef | EnumRef` (object | int/string-enum) — sealed interface; both branches implement it. */
    STRUCT_OR_ENUM,

    /**
     * `A | X | X[]` — a struct ref `A` plus a type `X` appearing both alone and as an
     * array (e.g. `Hover.contents = MarkupContent | MarkedString | MarkedString[]`).
     * Generated as a sealed interface with value-class branches for A, X, and List<X>.
     */
    REF_PLUS_SINGLE_ARRAY,

    /** `string | T` — generate `StringOr<T>`. */
    STRING_OR,

    /** `integer | string` — `IntOrString`. */
    INT_OR_STRING,

    /** Mixed primitives or too complex — keep as `JsonElement`. */
    KEEP_JSON_ELEMENT
}

/**
 * Result of classifying a union type. Carries metadata needed by the generator.
 */
data class UnionClassification(
    val category: UnionCategory,
    /** Items with `null` stripped (the union's "real" members). */
    val nonNullItems: List<LspType>,
    /** True if the original union included `null` (i.e., property should be nullable). */
    val isNullable: Boolean
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

    fun isEnumRef(t: LspType): Boolean =
        t is LspType.Reference && resolver != null && resolver.isEnum(t.name)

    // The non-string branch of a `string | X` union. Beyond a structure ref or
    // inline literal, this also accepts an array (`string | T[]`) and a non-enum
    // alias reference (e.g. NotebookDocumentFilter, itself a union alias) — an enum
    // ref is excluded since it serializes as a string and would be ambiguous.
    fun isStringOrOther(t: LspType): Boolean = when {
        isStructRef(t) -> true

        t is LspType.Literal -> true

        t is LspType.Array -> true

        t is LspType.Tuple -> true

        t is LspType.Reference ->
            resolver != null && resolver.isAlias(t.name) && !resolver.isEnum(t.name)

        else -> false
    }

    val category = when {
        nonNull.isEmpty() -> UnionCategory.T_OR_NULL
        nonNull.size == 1 -> UnionCategory.T_OR_NULL
        isBoolPlusObjects(nonNull, ::isStructRef) -> UnionCategory.BOOLEAN_OR_OPTIONS
        isTOrArrayT(nonNull) -> UnionCategory.T_OR_ARRAY_T
        nonNull.all { isStructRef(it) } -> UnionCategory.NAMED_REFERENCES
        isAllLiterals(nonNull) -> UnionCategory.LITERAL_UNION
        isRefLiteralMix(nonNull, ::isStructRef) -> UnionCategory.MIXED_REF_LITERAL
        isStructOrEnum(nonNull, ::isStructRef, ::isEnumRef) -> UnionCategory.STRUCT_OR_ENUM
        isRefPlusSingleArray(nonNull, ::isStructRef) -> UnionCategory.REF_PLUS_SINGLE_ARRAY
        isIntOrString(nonNull) -> UnionCategory.INT_OR_STRING
        isStringPlusOther(nonNull, ::isStringOrOther) -> UnionCategory.STRING_OR
        else -> UnionCategory.KEEP_JSON_ELEMENT
    }

    return UnionClassification(category, nonNull, isNullable)
}

private fun isBoolPlusObjects(items: List<LspType>, isStructRef: (LspType) -> Boolean): Boolean {
    val booleanCount = items.count { it is LspType.Base && it.name == "boolean" }
    if (booleanCount != 1) return false
    val others = items.filter { !(it is LspType.Base && it.name == "boolean") }
    // The other items must all be struct references, literals, or base types
    // (e.g. `boolean | string` → BooleanOr<String>).
    return others.isNotEmpty() &&
        others.all { isStructRef(it) || it is LspType.Literal || it is LspType.Base }
}

private fun isTOrArrayT(items: List<LspType>): Boolean {
    if (items.size != 2) return false
    val arrays = items.filterIsInstance<LspType.Array>()
    if (arrays.size != 1) return false
    val single = items.first { it !is LspType.Array }
    // `T | T[]` for either a reference type (Location | Location[]) or a base
    // type (string | string[]) — the array's element must match the single item.
    return typesEqual(arrays[0].element, single)
}

private fun typesEqual(a: LspType, b: LspType): Boolean = when {
    a is LspType.Reference && b is LspType.Reference -> a.name == b.name
    a is LspType.Base && b is LspType.Base -> a.name == b.name
    else -> false
}

/** A union mixing struct references and inline literals (neither all-ref nor all-literal). */
private fun isRefLiteralMix(items: List<LspType>, isStructRef: (LspType) -> Boolean): Boolean {
    if (items.size < 2) return false
    val allRefOrLiteral = items.all { isStructRef(it) || it is LspType.Literal }
    val hasRef = items.any { isStructRef(it) }
    val hasLiteral = items.any { it is LspType.Literal }
    return allRefOrLiteral && hasRef && hasLiteral
}

/** `StructRef | EnumRef` — an object branch and an enum branch (e.g. options | kind). */
private fun isStructOrEnum(
    items: List<LspType>,
    isStructRef: (LspType) -> Boolean,
    isEnumRef: (LspType) -> Boolean
): Boolean {
    if (items.size != 2) return false
    return items.count { isStructRef(it) } == 1 && items.count { isEnumRef(it) } == 1
}

/**
 * `A | X | X[]` — exactly one array whose element `X` also appears as a standalone
 * item, plus exactly one other item `A` that is a struct ref. (e.g.
 * `MarkupContent | MarkedString | MarkedString[]`.)
 */
private fun isRefPlusSingleArray(items: List<LspType>, isStructRef: (LspType) -> Boolean): Boolean {
    if (items.size != 3) return false
    val arrays = items.filterIsInstance<LspType.Array>()
    if (arrays.size != 1) return false
    val element = arrays[0].element
    val nonArrays = items.filter { it !is LspType.Array }
    val matchesElement = nonArrays.filter { typesEqual(it, element) }
    val others = nonArrays.filter { !typesEqual(it, element) }
    return matchesElement.size == 1 && others.size == 1 && isStructRef(others[0])
}

private fun isAllLiterals(items: List<LspType>): Boolean = items.all { it is LspType.Literal }

private fun isIntOrString(items: List<LspType>): Boolean {
    if (items.size != 2) return false
    val baseNames = items.filterIsInstance<LspType.Base>().map { it.name }.toSet()
    return baseNames == setOf("integer", "string") ||
        baseNames == setOf("uinteger", "string")
}

/** A wire-string base type — `string`, or the URI aliases that serialize as strings. */
internal fun isStringLike(t: LspType): Boolean =
    t is LspType.Base && t.name in setOf("string", "URI", "DocumentUri")

private fun isStringPlusOther(items: List<LspType>, isOther: (LspType) -> Boolean): Boolean {
    if (items.size != 2) return false
    val hasString = items.any { isStringLike(it) }
    val other = items.firstOrNull { !isStringLike(it) } ?: return false
    return hasString && isOther(other)
}
