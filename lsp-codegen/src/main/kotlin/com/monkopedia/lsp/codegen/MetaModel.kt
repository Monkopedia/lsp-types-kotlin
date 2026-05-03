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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Kotlin model of Microsoft's LSP metaModel.json schema.
 * See: https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/metaModel/
 */
@Serializable
data class MetaModel(
    val metaData: MetaData,
    val requests: List<Request>,
    val notifications: List<Notification>,
    val structures: List<Structure>,
    val enumerations: List<Enumeration>,
    val typeAliases: List<TypeAlias>
)

@Serializable
data class MetaData(val version: String)

@Serializable
data class Request(
    val method: String,
    val result: LspType,
    val messageDirection: MessageDirection,
    val params: LspType? = null,
    val partialResult: LspType? = null,
    val registrationOptions: LspType? = null,
    val errorData: LspType? = null,
    val documentation: String? = null,
    val since: String? = null,
    val proposed: Boolean? = null
)

@Serializable
data class Notification(
    val method: String,
    val messageDirection: MessageDirection,
    val params: LspType? = null,
    val registrationOptions: LspType? = null,
    val documentation: String? = null,
    val since: String? = null,
    val proposed: Boolean? = null
)

@Serializable
enum class MessageDirection {
    @SerialName("clientToServer")
    CLIENT_TO_SERVER,

    @SerialName("serverToClient")
    SERVER_TO_CLIENT,

    @SerialName("both")
    BOTH
}

@Serializable
data class Structure(
    val name: String,
    val properties: List<Property> = emptyList(),
    val extends: List<LspType> = emptyList(),
    val mixins: List<LspType> = emptyList(),
    val documentation: String? = null,
    val since: String? = null,
    val proposed: Boolean? = null
)

@Serializable
data class Property(
    val name: String,
    val type: LspType,
    val optional: Boolean = false,
    val documentation: String? = null,
    val since: String? = null,
    val proposed: Boolean? = null
)

@Serializable
data class Enumeration(
    val name: String,
    val type: LspType,
    val values: List<EnumValue>,
    val supportsCustomValues: Boolean = false,
    val documentation: String? = null,
    val since: String? = null,
    val proposed: Boolean? = null
)

@Serializable
data class EnumValue(
    val name: String,
    val value: JsonElement,
    val documentation: String? = null,
    val since: String? = null,
    val proposed: Boolean? = null
)

@Serializable
data class TypeAlias(
    val name: String,
    val type: LspType,
    val documentation: String? = null,
    val since: String? = null,
    val proposed: Boolean? = null
)

// --- Type system ---

@Serializable(with = LspTypeSerializer::class)
sealed class LspType {
    @Serializable
    data class Base(val name: String) : LspType()

    @Serializable
    data class Reference(val name: String) : LspType()

    @Serializable
    data class Array(val element: LspType) : LspType()

    @Serializable
    data class Map(val key: LspType, val value: LspType) : LspType()

    @Serializable
    data class Or(val items: List<LspType>) : LspType()

    @Serializable
    data class And(val items: List<LspType>) : LspType()

    @Serializable
    data class Literal(val value: LiteralValue) : LspType()

    @Serializable
    data class StringLiteral(val value: String) : LspType()

    @Serializable
    data class Tuple(val items: List<LspType>) : LspType()
}

@Serializable
data class LiteralValue(val properties: List<Property>)

object LspTypeSerializer : JsonContentPolymorphicSerializer<LspType>(LspType::class) {
    override fun selectDeserializer(element: JsonElement) =
        when (element.jsonObject["kind"]?.jsonPrimitive?.content) {
            "base" -> LspType.Base.serializer()
            "reference" -> LspType.Reference.serializer()
            "array" -> LspType.Array.serializer()
            "map" -> LspType.Map.serializer()
            "or" -> LspType.Or.serializer()
            "and" -> LspType.And.serializer()
            "literal" -> LspType.Literal.serializer()
            "stringLiteral" -> LspType.StringLiteral.serializer()
            "tuple" -> LspType.Tuple.serializer()
            else -> error("Unknown type kind: ${element.jsonObject["kind"]}")
        }
}
