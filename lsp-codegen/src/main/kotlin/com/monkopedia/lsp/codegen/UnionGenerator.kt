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
// Discriminator condition for an all-optional "catch-all" union branch — one with no
// required field to key on. Such a branch accepts any object (including `{}`), so the
// generator emits it as the union's unconditional `else` rather than a throwing default.
private const val FALLBACK_DISCRIMINATOR = "/* fallback */ obj.isNotEmpty()"

class UnionGenerator(private val resolver: TypeResolver) {

    /** Sealed interfaces emitted: name -> source. */
    private val emittedSealedInterfaces = mutableMapOf<String, String>()

    /** Map from concrete struct name to list of sealed interfaces it implements. */
    val structureInterfaces = mutableMapOf<String, MutableList<String>>()

    /** Map from enum name to list of sealed interfaces it implements (STRUCT_OR_ENUM). */
    val enumInterfaces = mutableMapOf<String, MutableList<String>>()

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
                // The single (non-array) item; its type may be a reference
                // (Location | Location[]) or a base type (string | string[]).
                val single = cls.nonNullItems.first { it !is LspType.Array }
                "SingleOrArray<${resolver.resolve(single, contextName)}>"
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

            UnionCategory.MIXED_REF_LITERAL -> {
                val name = topLevelAliasName ?: contextName.ifEmpty { "" }
                if (name.isEmpty()) {
                    "JsonElement"
                } else {
                    emitMixedRefLiteralSealed(name, cls.nonNullItems)
                    name
                }
            }

            UnionCategory.STRUCT_OR_ENUM -> {
                val name = topLevelAliasName ?: contextName.ifEmpty { "" }
                if (name.isEmpty()) {
                    "JsonElement"
                } else {
                    emitStructOrEnumSealed(name, cls.nonNullItems)
                    name
                }
            }

            UnionCategory.REF_PLUS_SINGLE_ARRAY -> {
                val name = topLevelAliasName ?: contextName.ifEmpty { "" }
                if (name.isEmpty()) {
                    "JsonElement"
                } else {
                    emitRefPlusSingleArraySealed(name, cls.nonNullItems)
                    name
                }
            }

            UnionCategory.ARRAY_REF_UNION -> {
                val name = topLevelAliasName ?: contextName.ifEmpty { "" }
                if (name.isEmpty()) {
                    "JsonElement"
                } else {
                    emitArrayRefUnionSealed(name, cls.nonNullItems)
                    name
                }
            }

            UnionCategory.STRING_OR -> {
                val other = cls.nonNullItems
                    .firstOrNull { !isStringLike(it) }
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

        // Single-options case: BooleanOr<HoverOptions>, or BooleanOr<String> for
        // a base-type branch (e.g. `boolean | string`).
        if (nonBoolean.size == 1) {
            val onlyType = nonBoolean[0]
            if (onlyType is LspType.Reference || onlyType is LspType.Base) {
                return "BooleanOr<${resolver.resolve(onlyType, contextName)}>"
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
                        val emittedElse = emitDiscriminationCases(this, branchTypes, name)
                        if (!emittedElse) {
                            line(
                                "else -> throw SerializationException(" +
                                    "\"Unknown $name variant: \$obj\")"
                            )
                        }
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
    ): Boolean {
        val branches = branchTypes.mapNotNull { name ->
            resolver.getStructure(name)?.let { name to resolver.collectAllProperties(it) }
        }
        val cast = "as DeserializationStrategy<$sealedName>"

        data class Info(
            val name: String,
            val kindValue: String?,
            val required: Set<String>,
            val all: Set<String>
        )
        val infos = branches.map { (name, props) ->
            val kindType = props.firstOrNull { it.name == "kind" }?.type
            Info(
                name = name,
                kindValue = (kindType as? LspType.StringLiteral)?.value,
                required = props.filter { !it.optional }.map { it.name }.toSet(),
                all = props.map { it.name }.toSet()
            )
        }

        // Fast path: every branch carries a distinct `kind` string-literal — the
        // spec's intended discriminator (e.g. the DocumentDiagnosticReport family,
        // "full" vs "unchanged"). Key on the kind VALUE for all branches.
        if (infos.all { it.kindValue != null } &&
            infos.mapNotNull { it.kindValue }.toSet().size == infos.size &&
            infos.isNotEmpty()
        ) {
            for (info in infos) {
                w.line(
                    "(obj[\"kind\"] as? JsonPrimitive)?.contentOrNull == " +
                        "\"${info.kindValue}\" -> ${info.name}.serializer() $cast"
                )
            }
            return false
        }

        // Initial discriminator (the long-standing strategy): a required field
        // unique to this branch, else its first required field, else a catch-all.
        // Branches whose required-field sets fully overlap collide here; those are
        // resolved below. Non-colliding branches are left exactly as before.
        class Case(var discriminator: String, var priority: Int)
        val cases = infos.associateWithTo(LinkedHashMap()) { info ->
            val otherRequired = infos.filter { it !== info }.flatMap { it.required }.toSet()
            val unique = info.required - otherRequired
            when {
                unique.isNotEmpty() ->
                    Case("\"${unique.first()}\" in obj", 100 + info.required.size)

                info.required.isNotEmpty() ->
                    Case("\"${info.required.first()}\" in obj", info.required.size)

                else -> Case(FALLBACK_DISCRIMINATOR, 0)
            }
        }

        // Resolve collisions: two branches keyed identically means one is
        // unreachable. Intervene ONLY on the colliding branches.
        cases.entries.groupBy { it.value.discriminator }
            .filterValues { it.size > 1 }
            .forEach { (_, entries) ->
                val group = entries.map { it.key }
                val kinds = group.mapNotNull { it.kindValue }
                if (kinds.size == group.size && kinds.toSet().size == group.size) {
                    // All branches carry distinct `kind` literals — discriminate on
                    // the kind VALUE (e.g. CreateFile/RenameFile/DeleteFile).
                    group.forEach { info ->
                        cases.getValue(info).discriminator =
                            "(obj[\"kind\"] as? JsonPrimitive)?.contentOrNull == \"${info.kindValue}\""
                        cases.getValue(info).priority = 1000
                    }
                } else {
                    // Subtype case: one branch extends another with extra (often
                    // optional) fields (e.g. NotebookDocumentSyncRegistrationOptions
                    // adds optional `id`). Key the larger branches on a field unique
                    // within the group; leave the smallest as the catch-all.
                    val ordered = group.sortedByDescending { it.all.size }
                    for (info in ordered.dropLast(1)) {
                        val othersInGroup = group.filter { it !== info }.flatMap { it.all }.toSet()
                        val extra = (info.all - othersInGroup).firstOrNull()
                        requireNotNull(extra) {
                            "Cannot disambiguate union $sealedName branches " +
                                "${group.map { it.name }} — no distinguishing field"
                        }
                        cases.getValue(info).discriminator = "\"$extra\" in obj"
                        cases.getValue(info).priority = 200 + info.all.size
                    }
                }
            }

        // Guard: any remaining collision means a branch is still unreachable.
        val remaining = cases.entries.groupBy {
            it.value.discriminator
        }.filterValues { it.size > 1 }
        require(remaining.isEmpty()) {
            "Union $sealedName has unreachable branches: " +
                remaining.map { (cond, cs) -> "$cond <- ${cs.map { it.key.name }}" }
        }

        val ordered = cases.entries.sortedByDescending { it.value.priority }
        // The all-optional catch-all branch (keyed `obj.isNotEmpty()`) accepts ANY object,
        // including `{}` — emit it as the unconditional `else` so an empty object (e.g.
        // gopls' `inlayHintProvider: {}`, a spec-valid all-optional Options) resolves to the
        // base Options branch instead of hitting the caller's throwing else.
        val fallback = ordered.firstOrNull { it.value.discriminator == FALLBACK_DISCRIMINATOR }
        for ((info, case) in ordered) {
            if (case === fallback?.value) continue
            w.line("${case.discriminator} -> ${info.name}.serializer() $cast")
        }
        return if (fallback != null) {
            w.line("else -> ${fallback.key.name}.serializer() $cast")
            true
        } else {
            false
        }
    }

    /**
     * Emit a sealed interface for a union mixing struct references and inline
     * literals (e.g. `Range | { insert: Range; replace: Range }`). Struct refs
     * implement the interface via [structureInterfaces] (as NAMED_REFERENCES does);
     * literal branches become generated classes via [literalBranches] (as
     * LITERAL_UNION does). Discrimination spans both kinds.
     */
    private fun emitMixedRefLiteralSealed(name: String, branches: List<LspType>) {
        if (name in emittedSealedInterfaces) return

        val refs = branches.filterIsInstance<LspType.Reference>()
        val literals = branches.filterIsInstance<LspType.Literal>()
        if (refs.size + literals.size != branches.size) return // unexpected branch kind

        val literalNames = pickLiteralBranchNames(name, literals)

        // Register implementers: existing struct refs + generated literal classes.
        for (ref in refs) {
            structureInterfaces.getOrPut(ref.name) { mutableListOf() }.add(name)
        }
        for ((branchName, lit) in literalNames.zip(literals)) {
            literalBranches[branchName] = lit.value.properties
            structureInterfaces.getOrPut(branchName) { mutableListOf() }.add(name)
        }

        // Required-field sets per branch, for discrimination.
        data class Branch(val typeName: String, val required: Set<String>)
        val all = refs.map { ref ->
            val props = resolver.getStructure(ref.name)
                ?.let { resolver.collectAllProperties(it) }.orEmpty()
            Branch(ref.name, props.filter { !it.optional }.map { it.name }.toSet())
        } + literalNames.zip(literals).map { (branchName, lit) ->
            Branch(branchName, lit.value.properties.filter { !it.optional }.map { it.name }.toSet())
        }

        val cast = "as DeserializationStrategy<$name>"
        data class Case(val expr: String, val priority: Int)
        val cases = all.map { b ->
            val others = all.filter { it !== b }.flatMap { it.required }.toSet()
            val unique = b.required - others
            val serializer = "${b.typeName}.serializer() $cast"
            when {
                unique.isNotEmpty() ->
                    Case("\"${unique.first()}\" in obj -> $serializer", 100 + b.required.size)

                b.required.isNotEmpty() ->
                    Case("\"${b.required.first()}\" in obj -> $serializer", b.required.size)

                else ->
                    Case("$FALLBACK_DISCRIMINATOR -> $serializer", 0)
            }
        }.sortedByDescending { it.priority }

        val w = CodeWriter()
        w.line("/**")
        w.line(" * Sealed interface for the LSP union: ${all.joinToString(" | ") { it.typeName }}.")
        w.line(" */")
        w.line("@Serializable(with = ${name}Serializer::class)")
        w.line("sealed interface $name")
        w.line()
        w.line("object ${name}Serializer :")
        w.indent {
            line("JsonContentPolymorphicSerializer<$name>($name::class) {")
            indent {
                line("override fun selectDeserializer(")
                indent { line("element: JsonElement") }
                line("): DeserializationStrategy<$name> {")
                indent {
                    line("val obj = element.jsonObject")
                    line("return when {")
                    indent {
                        // An all-optional fallback branch accepts any object incl. `{}`;
                        // emit it as the unconditional `else` rather than throwing (see
                        // emitDiscriminationCases for the rationale).
                        val fallback = cases.firstOrNull {
                            it.expr.startsWith("$FALLBACK_DISCRIMINATOR ->")
                        }
                        for (case in cases) {
                            if (case === fallback) continue
                            line(case.expr)
                        }
                        if (fallback != null) {
                            line("else -> ${fallback.expr.substringAfter("-> ")}")
                        } else {
                            line(
                                "else -> throw SerializationException(" +
                                    "\"Unknown $name variant: \$obj\")"
                            )
                        }
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
     * Emit a sealed interface for a `struct | enum` union (e.g.
     * `TextDocumentSyncOptions | TextDocumentSyncKind`). The struct implements it
     * via [structureInterfaces]; the enum (a `@Serializable` enum/value class)
     * implements it via [enumInterfaces]. Discrimination is by JSON shape — an
     * object decodes as the struct, anything else (an int/string primitive) as
     * the enum.
     */
    private fun emitStructOrEnumSealed(name: String, branches: List<LspType>) {
        if (name in emittedSealedInterfaces) return
        val refs = branches.filterIsInstance<LspType.Reference>()
        val structRef = refs.firstOrNull { resolver.isStructure(it.name) } ?: return
        val enumRef = refs.firstOrNull { resolver.isEnum(it.name) } ?: return

        structureInterfaces.getOrPut(structRef.name) { mutableListOf() }.add(name)
        enumInterfaces.getOrPut(enumRef.name) { mutableListOf() }.add(name)

        val cast = "as DeserializationStrategy<$name>"
        val w = CodeWriter()
        w.line("/**")
        w.line(" * Sealed interface for the LSP union: ${structRef.name} | ${enumRef.name}.")
        w.line(" */")
        w.line("@Serializable(with = ${name}Serializer::class)")
        w.line("sealed interface $name")
        w.line()
        w.line("object ${name}Serializer :")
        w.indent {
            line("JsonContentPolymorphicSerializer<$name>($name::class) {")
            indent {
                line("override fun selectDeserializer(")
                indent { line("element: JsonElement") }
                line("): DeserializationStrategy<$name> {")
                indent {
                    line("return if (element is JsonObject) {")
                    indent { line("${structRef.name}.serializer() $cast") }
                    line("} else {")
                    indent { line("${enumRef.name}.serializer() $cast") }
                    line("}")
                }
                line("}")
            }
            line("}")
        }
        emittedSealedInterfaces[name] = w.toString()
    }

    /**
     * Emit a sealed interface for an `A | X | X[]` union (e.g.
     * `MarkupContent | MarkedString | MarkedString[]`). Three @JvmInline value-class
     * branches wrap A, X and List<X>. Discrimination: a JSON array → the list branch;
     * a JSON object carrying all of A's required fields → the A branch; otherwise → X
     * (which may itself be a string-or-object handled by X's own serializer).
     */
    private fun emitRefPlusSingleArraySealed(name: String, branches: List<LspType>) {
        if (name in emittedSealedInterfaces) return
        val array = branches.filterIsInstance<LspType.Array>().firstOrNull() ?: return
        val element = array.element
        val refA = branches.firstOrNull {
            it !is LspType.Array && it != element &&
                resolver.isStructure((it as? LspType.Reference)?.name ?: "")
        } as? LspType.Reference ?: return

        val xType = resolver.resolve(element, name)
        val aName = refA.name
        val xSimple = xType.substringBefore('<').substringAfterLast('.')
        val aBranch = "${aName.substringAfterLast('.')}Value"
        val xBranch = "${xSimple}Value"
        val xArrayBranch = "${xSimple}Array"

        val aRequired = resolver.getStructure(aName)
            ?.let { resolver.collectAllProperties(it) }
            ?.filter { !it.optional }?.map { it.name }.orEmpty()
        val aDiscriminator = if (aRequired.isEmpty()) {
            "element is JsonObject"
        } else {
            "element is JsonObject && " + aRequired.joinToString(" && ") { "\"$it\" in element" }
        }

        val cast = "as DeserializationStrategy<$name>"
        val w = CodeWriter()
        w.line("/**")
        w.line(" * Sealed interface for the LSP union: $aName | $xType | $xType\\[\\].")
        w.line(" */")
        w.line("@Serializable(with = ${name}Serializer::class)")
        w.block("sealed interface $name") {
            line("@Serializable")
            line("@JvmInline")
            line("value class $aBranch(val value: $aName) : $name")
            line()
            line("@Serializable")
            line("@JvmInline")
            line("value class $xBranch(val value: $xType) : $name")
            line()
            line("@Serializable")
            line("@JvmInline")
            line("value class $xArrayBranch(val value: List<$xType>) : $name")
            line()
            line("companion object")
        }
        w.line()
        w.line("object ${name}Serializer :")
        w.indent {
            line("JsonContentPolymorphicSerializer<$name>($name::class) {")
            indent {
                line("override fun selectDeserializer(")
                indent { line("element: JsonElement") }
                line("): DeserializationStrategy<$name> {")
                indent {
                    line("return when {")
                    indent {
                        line("element is JsonArray -> $name.$xArrayBranch.serializer() $cast")
                        line("$aDiscriminator ->")
                        indent { line("$name.$aBranch.serializer() $cast") }
                        line("else -> $name.$xBranch.serializer() $cast")
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
     * Emit a sealed interface for a method-result-style union of refs/aliases and
     * arrays-of-refs (e.g. `CompletionItem[] | CompletionList`,
     * `Definition | DefinitionLink[]`, `SymbolInformation[] | DocumentSymbol[]`).
     * One @JvmInline value-class branch per union item. The serializer discriminates
     * a JSON object → the object branch, and a JSON array → the array branch whose
     * element carries a distinguishing required field (a branch that is also
     * object-capable — i.e. an alias to `T | T[]` — is the array default). The
     * classifier only routes here when the branches are actually distinguishable.
     */
    private fun emitArrayRefUnionSealed(name: String, items: List<LspType>) {
        if (name in emittedSealedInterfaces) return

        data class Branch(
            val branchName: String,
            val valueType: String,
            val arrayElementStruct: Structure?,
            val objectCapable: Boolean
        )

        fun aliasArrayElement(ref: LspType.Reference): LspType? {
            val target = resolver.getAlias(ref.name)?.type
            if (target is LspType.Or &&
                classifyUnion(target, resolver).category == UnionCategory.T_OR_ARRAY_T
            ) {
                return target.items.firstOrNull { it !is LspType.Array }
            }
            return null
        }

        val branches = items.map { item ->
            if (item is LspType.Array) {
                val elemType = resolver.resolve(item.element, name)
                val simple = elemType.substringAfterLast('.').substringBefore('<')
                Branch(
                    "${simple}Array",
                    "List<$elemType>",
                    resolver.underlyingStructure(item.element),
                    false
                )
            } else {
                val valueType = resolver.resolve(item, name)
                val simple = valueType.substringAfterLast('.').substringBefore('<')
                val arrayElem = (item as? LspType.Reference)?.let { aliasArrayElement(it) }
                Branch(
                    "${simple}Value",
                    valueType,
                    arrayElem?.let {
                        resolver.underlyingStructure(it)
                    },
                    true
                )
            }
        }

        fun required(s: Structure) =
            resolver.collectAllProperties(s).filter { !it.optional }.map { it.name }

        val arrayBranches = branches.filter { it.arrayElementStruct != null }
        val keyed = arrayBranches.mapNotNull { b ->
            val others = arrayBranches.filter { it !== b }
                .flatMap { required(it.arrayElementStruct!!) }.toSet()
            (required(b.arrayElementStruct!!).toSet() - others).firstOrNull()?.let { b to it }
        }
        val defaultArrayBranch =
            arrayBranches.firstOrNull { it.objectCapable }
                ?: arrayBranches.firstOrNull { b -> keyed.none { it.first === b } }
                ?: arrayBranches.first()
        val objectBranch = branches.firstOrNull { it.objectCapable }

        val cast = "as DeserializationStrategy<$name>"
        val w = CodeWriter()
        w.line("/**")
        w.line(" * Sealed interface for an LSP method-result union (value-class branches).")
        w.line(" */")
        w.line("@Serializable(with = ${name}Serializer::class)")
        w.block("sealed interface $name") {
            branches.forEachIndexed { i, b ->
                if (i > 0) line()
                line("@Serializable")
                line("@JvmInline")
                line("value class ${b.branchName}(val value: ${b.valueType}) : $name")
            }
        }
        w.line()
        w.line("object ${name}Serializer :")
        w.indent {
            line("JsonContentPolymorphicSerializer<$name>($name::class) {")
            indent {
                line("override fun selectDeserializer(")
                indent { line("element: JsonElement") }
                line("): DeserializationStrategy<$name> {")
                indent {
                    line("return when {")
                    indent {
                        if (objectBranch != null) {
                            line(
                                "element is JsonObject -> " +
                                    "$name.${objectBranch.branchName}.serializer() $cast"
                            )
                        }
                        for ((b, field) in keyed) {
                            if (b === defaultArrayBranch) continue
                            line(
                                "element is JsonArray && (element.firstOrNull() as? JsonObject)" +
                                    "?.containsKey(\"$field\") == true ->"
                            )
                            indent { line("$name.${b.branchName}.serializer() $cast") }
                        }
                        line(
                            "element is JsonArray -> " +
                                "$name.${defaultArrayBranch.branchName}.serializer() $cast"
                        )
                        line("else ->")
                        indent {
                            line("throw SerializationException(\"Unexpected $name: \$element\")")
                        }
                    }
                    line("}")
                }
                line("}")
            }
            line("}")
        }
        emittedSealedInterfaces[name] = w.toString()
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
