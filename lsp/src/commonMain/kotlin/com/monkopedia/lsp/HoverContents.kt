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

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

/**
 * Builders for the `Hover.contents` field, which the LSP spec defines as
 * `MarkupContent | MarkedString | MarkedString[]`. The codegen exposes that as
 * a raw `JsonElement` because the three-way nested union doesn't fit cleanly
 * into the generic wrapper types — these helpers make the common shapes easy
 * to construct.
 */
@Suppress("ktlint:standard:filename")
public object HoverContents {
    private val json = Json { encodeDefaults = false }

    /**
     * Construct hover contents as a [MarkupContent] block (preferred for new
     * code; supports `markdown` or `plaintext`).
     */
    public fun markup(content: MarkupContent): kotlinx.serialization.json.JsonElement =
        json.encodeToJsonElement(MarkupContent.serializer(), content)

    /** Construct hover contents as a markdown block. */
    public fun markdown(value: String): kotlinx.serialization.json.JsonElement =
        markup(MarkupContent(kind = MarkupKind.MARKDOWN, value = value))

    /** Construct hover contents as a plaintext block. */
    public fun plaintext(value: String): kotlinx.serialization.json.JsonElement =
        markup(MarkupContent(kind = MarkupKind.PLAIN_TEXT, value = value))

    /**
     * Construct hover contents as a single string `MarkedString`.
     * (Deprecated by the LSP spec — prefer [markdown] or [plaintext].)
     */
    public fun string(value: String): kotlinx.serialization.json.JsonElement = JsonPrimitive(value)
}
