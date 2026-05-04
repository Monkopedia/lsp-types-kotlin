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
 * Generates Kotlin code for LSP union types.
 *
 * Three output kinds:
 * 1. Reusable wrapper types (`BooleanOr<T>`, `SingleOrArray<T>`, `StringOr<T>`, `IntOrString`):
 *    these live in hand-written `UnionTypes.kt` — no generation needed, just resolve to them.
 * 2. Per-type sealed interfaces for `NAMED_REFERENCES` and `LITERAL_UNION` unions: these
 *    are emitted into `Unions.kt` and existing data classes implement them via
 *    [structureInterfaces] map for category 2 (NAMED_REFERENCES).
 * 3. Generated literal-branch data classes for LITERAL_UNION cases.
 */
class UnionGenerator(private val resolver: TypeResolver) {

    /** Sealed interfaces emitted: name -> source. */
    private val emittedSealedInterfaces = mutableMapOf<String, String>()

    /** Map from concrete struct name to list of sealed interfaces it implements. */
    val structureInterfaces = mutableMapOf<String, MutableList<String>>()

    /**
     * Resolve a union to its Kotlin type string and register any necessary
     * sealed interfaces / branch classes.
     *
     * [contextName] is used to derive a name for inline unions (e.g., `InlayHintLabel`).
     * [topLevelAliasName] is set when the union is a top-level typeAlias declaration —
     * in that case, the sealed interface uses the alias name directly.
     */
    fun resolveUnion(
        type: LspType.Or,
        contextName: String,
        topLevelAliasName: String? = null
    ): String {
        val cls = classifyUnion(type, resolver)
        return when (cls.category) {
            UnionCategory.T_OR_NULL ->
                resolver.resolve(cls.nonNullItems[0], contextName)

            UnionCategory.BOOLEAN_OR_OPTIONS -> resolveBooleanOr(cls, contextName)

            UnionCategory.T_OR_ARRAY_T -> {
                val refItem = cls.nonNullItems.filterIsInstance<LspType.Reference>()[0]
                "SingleOrArray<${refItem.name}>"
            }

            UnionCategory.NAMED_REFERENCES -> {
                val name = topLevelAliasName ?: contextName.ifEmpty { "" }
                if (name.isEmpty()) {
                    "JsonElement"
                } else {
                    emitNamedReferenceSealed(name, cls.nonNullItems)
                    name
                }
            }

            UnionCategory.LITERAL_UNION -> {
                val name = topLevelAliasName ?: contextName.ifEmpty { "" }
                if (name.isEmpty()) {
                    "JsonElement"
                } else {
                    emitLiteralUnionSealed(name, cls.nonNullItems)
                    name
                }
            }

            UnionCategory.STRING_OR -> {
                val other = cls.nonNullItems
                    .firstOrNull { it !is LspType.Base || it.name != "string" }
                    ?: return "JsonElement"
                // Use a suffix so an inline literal in the other branch doesn't collide
                // with the parent alias/context name (would create a recursive typealias).
                val subContext = if (contextName.isNotEmpty()) "${contextName}Object" else ""
                val tName = resolver.resolve(other, subContext)
                "StringOr<$tName>"
            }

            UnionCategory.INT_OR_STRING -> "IntOrString"

            UnionCategory.KEEP_JSON_ELEMENT -> "JsonElement"
        }
    }

    /**
     * Get all generated sealed interfaces and helper code as files to write.
     */
    fun generateUnionsFile(): String {
        val w = CodeWriter()
        for ((_, src) in emittedSealedInterfaces.entries.sortedBy { it.key }) {
            w.line(src)
            w.line()
        }
        return w.toString()
    }

    /**
     * Get all generated literal branch classes (a per-name map for emission alongside
     * structures or in a dedicated file).
     */
    val literalBranches = mutableMapOf<String, List<Property>>()

    private fun resolveBooleanOr(cls: UnionClassification, contextName: String): String {
        // Branches other than `boolean`. Could be one or many.
        val nonBoolean = cls.nonNullItems
            .filter { !(it is LspType.Base && it.name == "boolean") }

        // Single-options case: BooleanOr<HoverOptions>
        if (nonBoolean.size == 1) {
            val onlyType = nonBoolean[0]
            if (onlyType is LspType.Reference) {
                return "BooleanOr<${onlyType.name}>"
            }
            // Inline literal/empty object — use JsonElement as the option type.
            return "BooleanOr<JsonElement>"
        }

        // Multi-options case: generate a sub-sealed interface for the options portion.
        // Naming: ${contextName}Options if contextName is set, else fall back to JsonElement.
        if (contextName.isEmpty()) {
            return "BooleanOr<JsonElement>"
        }
        val optionsTypeName = "${contextName}Options"
        // For multi-options, we conservatively use JsonElement to avoid emitting
        // additional sealed interfaces for now. Future work: emit a sub-sealed.
        return "BooleanOr<JsonElement>"
            .also { /* TODO: improve */ }
            .let {
                // Actually emit a sub-sealed interface so consumers get type safety.
                emitNamedReferenceSealed(optionsTypeName, nonBoolean)
                "BooleanOr<$optionsTypeName>"
            }
    }

    private fun emitNamedReferenceSealed(name: String, branches: List<LspType>) {
        if (name in emittedSealedInterfaces) return

        val branchTypes = branches.mapNotNull {
            (it as? LspType.Reference)?.name
        }
        if (branchTypes.size != branches.size) {
            // Some branches aren't simple references — skip generating an interface
            // and treat as JsonElement (caller handles fallback).
            return
        }

        // Track that each concrete struct will need to implement this interface.
        for (branch in branchTypes) {
            structureInterfaces.getOrPut(branch) { mutableListOf() }.add(name)
        }

        val w = CodeWriter()
        w.line("/**")
        w.line(" * Sealed interface for the LSP union type: ${branchTypes.joinToString(" | ")}.")
        w.line(" */")
        w.line(
            "@Serializable(with = ${name}Serializer::class)"
        )
        w.line("sealed interface $name")
        w.line()
        w.line("object ${name}Serializer :")
        w.indent {
            line(
                "JsonContentPolymorphicSerializer<$name>(" +
                    "$name::class) {"
            )
            indent {
                line("override fun selectDeserializer(")
                indent {
                    line("element: JsonElement")
                }
                line("): DeserializationStrategy<$name> {")
                indent {
                    line("val obj = element.jsonObject")
                    line("return when {")
                    indent {
                        emitDiscriminationCases(this, branchTypes, name)
                        line(
                            "else -> throw SerializationException(" +
                                "\"Unknown $name variant: \$obj\")"
                        )
                    }
                    line("}")
                }
                line("}")
            }
            line("}")
        }

        emittedSealedInterfaces[name] = w.toString()
    }

    /**
     * Emit `when` cases for discriminating between named reference branches.
     * Strategy: find unique required fields per branch.
     */
    private fun emitDiscriminationCases(
        w: CodeWriter,
        branchTypes: List<String>,
        sealedName: String
    ) {
        val branches = branchTypes.mapNotNull { name ->
            resolver.getStructure(name)?.let { name to resolver.collectAllProperties(it) }
        }
        val cast = "as DeserializationStrategy<$sealedName>"

        // Try kind-discriminator first (most reliable when branches use string literals).
        val kindMap = branches.mapNotNull { (name, props) ->
            val kindProp = props.firstOrNull { it.name == "kind" }
            val kindType = kindProp?.type
            val kindValue = (kindType as? LspType.StringLiteral)?.value
            kindValue?.let { name to it }
        }
        if (kindMap.size == branches.size && kindMap.isNotEmpty()) {
            for ((name, kindValue) in kindMap) {
                w.line(
                    "(obj[\"kind\"] as? JsonPrimitive)" +
                        "?.contentOrNull == \"$kindValue\" -> $name.serializer() $cast"
                )
            }
            return
        }

        // Fall back to unique-required-field discrimination.
        // Compute each branch's discriminator and required-field count up front,
        // then ORDER cases so that more-specific branches (those with a unique
        // required field, or more required fields) come BEFORE less-specific ones.
        // Without this, e.g. `DeclarationOptions | DeclarationRegistrationOptions`
        // (where the latter extends the former) would always pick the parent.
        data class Case(val name: String, val discriminator: String, val priority: Int)
        val cases = branches.map { (name, props) ->
            val required = props.filter { !it.optional }.map { it.name }.toSet()
            val others = branches.filter { it.first != name }
                .flatMap { it.second.filter { p -> !p.optional }.map { p -> p.name } }
                .toSet()
            val unique = required - others
            when {
                // Best: unique required field — highest priority.
                unique.isNotEmpty() ->
                    Case(name, "\"${unique.first()}\" in obj", priority = 100 + required.size)

                // Some required field but not unique — medium priority.
                required.isNotEmpty() ->
                    Case(name, "\"${required.first()}\" in obj", priority = required.size)

                // Nothing required — fallback, lowest priority.
                else -> Case(name, "/* fallback */ obj.isNotEmpty()", priority = 0)
            }
        }.sortedByDescending { it.priority }

        for (case in cases) {
            w.line("${case.discriminator} -> ${case.name}.serializer() $cast")
        }
    }

    private fun emitLiteralUnionSealed(name: String, branches: List<LspType>) {
        if (name in emittedSealedInterfaces) return

        // Generate a name for each literal branch.
        val literals = branches.filterIsInstance<LspType.Literal>()
        if (literals.size != branches.size) return // mixed — skip

        // Build branch names by pickng a discriminating field name.
        val branchNames = pickLiteralBranchNames(name, literals)

        // Register each branch as a class to be emitted alongside structures.
        for ((branchName, lit) in branchNames.zip(literals)) {
            literalBranches[branchName] = lit.value.properties
            // Each branch implements the parent sealed interface.
            structureInterfaces.getOrPut(branchName) { mutableListOf() }.add(name)
        }

        val w = CodeWriter()
        w.line("/**")
        w.line(" * Sealed interface for the LSP literal union: $name.")
        w.line(" * Branches: ${branchNames.joinToString(", ")}.")
        w.line(" */")
        w.line("@Serializable(with = ${name}Serializer::class)")
        w.line("sealed interface $name")
        w.line()
        w.line("object ${name}Serializer :")
        w.indent {
            line(
                "JsonContentPolymorphicSerializer<$name>(" +
                    "$name::class) {"
            )
            indent {
                line("override fun selectDeserializer(")
                indent {
                    line("element: JsonElement")
                }
                line("): DeserializationStrategy<$name> {")
                indent {
                    line("val obj = element.jsonObject")
                    line("return when {")
                    indent {
                        emitLiteralDiscrimination(this, branchNames, literals, name)
                        line(
                            "else -> throw SerializationException(" +
                                "\"Unknown $name variant: \$obj\")"
                        )
                    }
                    line("}")
                }
                line("}")
            }
            line("}")
        }

        emittedSealedInterfaces[name] = w.toString()
    }

    private fun emitLiteralDiscrimination(
        w: CodeWriter,
        branchNames: List<String>,
        literals: List<LspType.Literal>,
        sealedName: String
    ) {
        val cast = "as DeserializationStrategy<$sealedName>"
        // Try to find a unique required field per branch.
        for ((branchName, lit) in branchNames.zip(literals)) {
            val required = lit.value.properties.filter { !it.optional }.map { it.name }.toSet()
            val others = literals.filter { it !== lit }
                .flatMap { it.value.properties.filter { p -> !p.optional }.map { p -> p.name } }
                .toSet()
            val unique = required - others
            val discriminator = unique.firstOrNull() ?: required.firstOrNull()
            if (discriminator != null) {
                w.line("\"$discriminator\" in obj -> $branchName.serializer() $cast")
            } else {
                w.line("/* fallback */ obj.isNotEmpty() -> $branchName.serializer() $cast")
            }
        }
    }

    private fun pickLiteralBranchNames(
        parentName: String,
        literals: List<LspType.Literal>
    ): List<String> {
        // Try to use the unique-required-field name per literal as the branch suffix.
        val rawSuffixes = literals.map { lit ->
            val required = lit.value.properties.filter { !it.optional }.map { it.name }
            val others = literals.filter { it !== lit }
                .flatMap { it.value.properties.filter { p -> !p.optional }.map { p -> p.name } }
                .toSet()
            val unique = required.firstOrNull { it !in others }
            unique ?: "Variant"
        }
        // De-duplicate: when suffixes collide, append an index.
        val seen = mutableMapOf<String, Int>()
        val finalSuffixes = rawSuffixes.map { s ->
            val count = seen.getOrDefault(s, 0)
            seen[s] = count + 1
            if (count == 0) s else "$s$count"
        }
        return finalSuffixes.map { suffix ->
            "${parentName}${suffix.replaceFirstChar { it.uppercase() }}"
        }
    }
}
