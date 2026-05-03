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
 * Resolves LSP metaModel types to Kotlin type strings.
 *
 * Tracks inline literal types that need to be emitted as separate classes.
 */
class TypeResolver(private val model: MetaModel) {
    private val structuresByName = model.structures.associateBy { it.name }
    private val enumsByName = model.enumerations.associateBy { it.name }
    private val aliasesByName = model.typeAliases.associateBy { it.name }

    /** Inline literal classes discovered during resolution, keyed by generated name. */
    val inlineLiterals = mutableMapOf<String, List<Property>>()

    /** Names already emitted — prevents duplicate emission across parent/child structures. */
    val emittedLiterals = mutableSetOf<String>()
    private var literalCounter = 0

    /**
     * Resolve an [LspType] to a Kotlin type string.
     * [context] is used to generate meaningful names for inline literals.
     */
    fun resolve(type: LspType, context: String = "", nullable: Boolean = false): String {
        val base = resolveInner(type, context)
        return if (nullable) "$base?" else base
    }

    private fun resolveInner(type: LspType, context: String): String = when (type) {
        is LspType.Base -> resolveBase(type.name)

        is LspType.Reference -> type.name

        is LspType.Array -> "List<${resolveInner(type.element, context)}>"

        is LspType.Map -> "Map<${resolveInner(
            type.key,
            context
        )}, ${resolveInner(type.value, context)}>"

        is LspType.Or -> resolveOr(type, context)

        is LspType.And -> resolveAnd(type, context)

        is LspType.Literal -> resolveLiteral(type, context)

        is LspType.StringLiteral -> "String"

        is LspType.Tuple -> "kotlinx.serialization.json.JsonArray"
    }

    private fun resolveBase(name: String): String = when (name) {
        "string" -> "String"
        "boolean" -> "Boolean"
        "integer" -> "Int"
        "uinteger" -> "UInt"
        "decimal" -> "Double"
        "DocumentUri" -> "DocumentUri"
        "URI" -> "URI"
        "null" -> "Nothing?"
        else -> error("Unknown base type: $name")
    }

    /**
     * Resolve an `or` (union) type. Several common patterns get special handling:
     * - `T | null` → `T?`
     * - Known union types that map to existing type aliases → use the alias name
     * - Otherwise → `JsonElement` (the sealed class approach is used for top-level typeAliases)
     */
    private fun resolveOr(type: LspType.Or, context: String): String {
        val items = type.items
        val nonNull = items.filter { it !is LspType.Base || it.name != "null" }

        // T | null → T?
        if (nonNull.size == 1 && nonNull.size < items.size) {
            return "${resolveInner(nonNull[0], context)}?"
        }

        // For inline or types in properties, use JsonElement as the safe fallback.
        // Top-level typeAliases with `or` get proper sealed classes generated separately.
        return "kotlinx.serialization.json.JsonElement"
    }

    private fun resolveAnd(type: LspType.And, context: String): String {
        // `and` types merge properties from all items. Generate an inline class.
        val allProps = mutableListOf<Property>()
        for (item in type.items) {
            when (item) {
                is LspType.Reference -> {
                    val struct = structuresByName[item.name]
                    if (struct != null) {
                        allProps.addAll(collectAllProperties(struct))
                    }
                }

                is LspType.Literal -> allProps.addAll(item.value.properties)

                else -> {} // shouldn't happen in practice
            }
        }
        val name = if (context.isNotEmpty()) {
            "${context}Value"
        } else {
            "AnonymousAnd${literalCounter++}"
        }
        inlineLiterals[name] = allProps
        return name
    }

    private fun resolveLiteral(type: LspType.Literal, context: String): String {
        val name = if (context.isNotEmpty()) context else "AnonymousLiteral${literalCounter++}"
        if (name !in emittedLiterals) {
            inlineLiterals.putIfAbsent(name, type.value.properties)
        }
        return name
    }

    /**
     * Collect all properties for a structure, including inherited and mixin properties.
     */
    fun collectAllProperties(structure: Structure): List<Property> {
        val props = mutableListOf<Property>()
        // Add extends properties first
        for (ext in structure.extends) {
            if (ext is LspType.Reference) {
                structuresByName[ext.name]?.let { props.addAll(collectAllProperties(it)) }
            }
        }
        // Then mixin properties
        for (mixin in structure.mixins) {
            if (mixin is LspType.Reference) {
                structuresByName[mixin.name]?.let { props.addAll(collectAllProperties(it)) }
            }
        }
        // Then own properties (may override)
        props.addAll(structure.properties)
        // Deduplicate by name, keeping last (own properties win)
        return props.reversed().distinctBy { it.name }.reversed()
    }

    fun isStructure(name: String) = name in structuresByName
    fun isEnum(name: String) = name in enumsByName
    fun isAlias(name: String) = name in aliasesByName
    fun getStructure(name: String) = structuresByName[name]
}
