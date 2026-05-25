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
package com.monkopedia.lsp

import kotlin.jvm.JvmInline
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull

// Reusable union type wrappers for LSP types where a field can be one of several shapes.
//
// The LSP spec uses TypeScript union types extensively (e.g. `boolean | HoverOptions`,
// `Location | Location[]`, `string | MarkupContent`). These wrappers provide type-safe
// Kotlin representations that round-trip through kotlinx-serialization JSON.

// region BooleanOr<T>

/**
 * `boolean | T` — used for LSP capability fields like `hoverProvider: boolean | HoverOptions`.
 *
 * `false` typically means "capability not supported", `true` means "supported with defaults",
 * and `Value(t)` means "supported with specific configuration".
 */
@Serializable(with = BooleanOrSerializer::class)
sealed interface BooleanOr<out T> {
    @JvmInline value class BooleanValue(val value: Boolean) : BooleanOr<Nothing>

    @JvmInline value class Value<out T>(val value: T) : BooleanOr<T>

    public companion object {
        /** Wrap a plain boolean, e.g. `BooleanOr(true)`. */
        public operator fun invoke(value: Boolean): BooleanOr<Nothing> = BooleanValue(value)

        /** Wrap an options object, e.g. `BooleanOr.of(HoverOptions(...))`. */
        public fun <T> of(value: T): BooleanOr<T> = Value(value)
    }
}

class BooleanOrSerializer<T>(private val tSerializer: KSerializer<T>) : KSerializer<BooleanOr<T>> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("BooleanOr") { }

    override fun deserialize(decoder: Decoder): BooleanOr<T> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("BooleanOr requires JsonDecoder")
        val element = jsonDecoder.decodeJsonElement()
        return if (element is JsonPrimitive && element.booleanOrNull != null) {
            BooleanOr.BooleanValue(element.boolean)
        } else {
            BooleanOr.Value(jsonDecoder.json.decodeFromJsonElement(tSerializer, element))
        }
    }

    override fun serialize(encoder: Encoder, value: BooleanOr<T>) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw SerializationException("BooleanOr requires JsonEncoder")
        when (value) {
            is BooleanOr.BooleanValue ->
                jsonEncoder.encodeJsonElement(JsonPrimitive(value.value))

            is BooleanOr.Value -> jsonEncoder.encodeJsonElement(
                jsonEncoder.json.encodeToJsonElement(tSerializer, value.value)
            )
        }
    }
}

// endregion

// region SingleOrArray<T>

/**
 * `T | T[]` — used for LSP types like `Definition = Location | Location[]`
 * and `Hover.contents: MarkupContent | MarkedString | MarkedString[]`.
 */
@Serializable(with = SingleOrArraySerializer::class)
sealed interface SingleOrArray<out T> {
    @JvmInline value class Single<out T>(val value: T) : SingleOrArray<T>

    @JvmInline value class Multiple<out T>(val value: List<T>) : SingleOrArray<T>

    public companion object {
        /** Wrap a single value, e.g. `SingleOrArray.single(location)`. */
        public fun <T> single(value: T): SingleOrArray<T> = Single(value)

        /** Wrap a list of values, e.g. `SingleOrArray.multiple(locations)`. */
        public fun <T> multiple(values: List<T>): SingleOrArray<T> = Multiple(values)
    }
}

class SingleOrArraySerializer<T>(private val tSerializer: KSerializer<T>) :
    KSerializer<SingleOrArray<T>> {
    private val listSerializer = ListSerializer(tSerializer)

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("SingleOrArray") { }

    override fun deserialize(decoder: Decoder): SingleOrArray<T> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("SingleOrArray requires JsonDecoder")
        val element = jsonDecoder.decodeJsonElement()
        return if (element is JsonArray) {
            SingleOrArray.Multiple(jsonDecoder.json.decodeFromJsonElement(listSerializer, element))
        } else {
            SingleOrArray.Single(jsonDecoder.json.decodeFromJsonElement(tSerializer, element))
        }
    }

    override fun serialize(encoder: Encoder, value: SingleOrArray<T>) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw SerializationException("SingleOrArray requires JsonEncoder")
        when (value) {
            is SingleOrArray.Single -> jsonEncoder.encodeJsonElement(
                jsonEncoder.json.encodeToJsonElement(tSerializer, value.value)
            )

            is SingleOrArray.Multiple -> jsonEncoder.encodeJsonElement(
                jsonEncoder.json.encodeToJsonElement(listSerializer, value.value)
            )
        }
    }
}

// endregion

// region StringOr<T>

/**
 * `string | T` — used for LSP types like `CompletionItem.documentation: string | MarkupContent`
 * and `MarkedString = string | { language, value }`.
 */
@Serializable(with = StringOrSerializer::class)
sealed interface StringOr<out T> {
    @JvmInline value class StringValue(val value: String) : StringOr<Nothing>

    @JvmInline value class Value<out T>(val value: T) : StringOr<T>

    public companion object {
        /** Wrap a plain string, e.g. `StringOr("plain text")`. */
        public operator fun invoke(value: String): StringOr<Nothing> = StringValue(value)

        /** Wrap a structured value, e.g. `StringOr.of(MarkupContent(...))`. */
        public fun <T> of(value: T): StringOr<T> = Value(value)
    }
}

class StringOrSerializer<T>(private val tSerializer: KSerializer<T>) : KSerializer<StringOr<T>> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("StringOr") { }

    override fun deserialize(decoder: Decoder): StringOr<T> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("StringOr requires JsonDecoder")
        val element = jsonDecoder.decodeJsonElement()
        return if (element is JsonPrimitive && element.isString) {
            StringOr.StringValue(element.content)
        } else {
            StringOr.Value(jsonDecoder.json.decodeFromJsonElement(tSerializer, element))
        }
    }

    override fun serialize(encoder: Encoder, value: StringOr<T>) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw SerializationException("StringOr requires JsonEncoder")
        when (value) {
            is StringOr.StringValue -> jsonEncoder.encodeJsonElement(JsonPrimitive(value.value))

            is StringOr.Value -> jsonEncoder.encodeJsonElement(
                jsonEncoder.json.encodeToJsonElement(tSerializer, value.value)
            )
        }
    }
}

// endregion

// region IntOrString

/**
 * `integer | string` — used for LSP types like `ProgressToken`, `CancelParams.id`,
 * and `Diagnostic.code`.
 */
@Serializable(with = IntOrStringSerializer::class)
sealed interface IntOrString {
    @JvmInline value class IntValue(val value: Int) : IntOrString

    @JvmInline value class StringValue(val value: String) : IntOrString

    public companion object {
        /** Wrap an integer, e.g. `IntOrString(42)`. */
        public operator fun invoke(value: Int): IntOrString = IntValue(value)

        /** Wrap a string, e.g. `IntOrString("token-1")`. */
        public operator fun invoke(value: String): IntOrString = StringValue(value)
    }
}

object IntOrStringSerializer : KSerializer<IntOrString> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("IntOrString") { }

    override fun deserialize(decoder: Decoder): IntOrString {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("IntOrString requires JsonDecoder")
        val element = jsonDecoder.decodeJsonElement()
        if (element !is JsonPrimitive) {
            throw SerializationException("IntOrString expected primitive, got $element")
        }
        return when {
            element.isString -> IntOrString.StringValue(element.content)
            element.intOrNull != null -> IntOrString.IntValue(element.int)
            else -> throw SerializationException("IntOrString cannot decode: $element")
        }
    }

    override fun serialize(encoder: Encoder, value: IntOrString) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw SerializationException("IntOrString requires JsonEncoder")
        when (value) {
            is IntOrString.IntValue -> jsonEncoder.encodeJsonElement(JsonPrimitive(value.value))

            is IntOrString.StringValue ->
                jsonEncoder.encodeJsonElement(JsonPrimitive(value.value))
        }
    }
}

// endregion
