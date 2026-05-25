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

// Ergonomic builders for HoverContents (the generated sealed type for
// `Hover.contents = MarkupContent | MarkedString | MarkedString[]`). These keep
// the common cases concise while the type stays fully typed.

/** Hover contents as a [MarkupContent] block (preferred). */
public fun HoverContents.Companion.markup(content: MarkupContent): HoverContents =
    HoverContents.MarkupContentValue(content)

/** Hover contents as a markdown block. */
public fun HoverContents.Companion.markdown(value: String): HoverContents =
    markup(MarkupContent(kind = MarkupKind.MARKDOWN, value = value))

/** Hover contents as a plaintext block. */
public fun HoverContents.Companion.plaintext(value: String): HoverContents =
    markup(MarkupContent(kind = MarkupKind.PLAIN_TEXT, value = value))

/**
 * Hover contents as a single string `MarkedString`.
 * (Deprecated by the LSP spec — prefer [markdown] or [plaintext].)
 */
public fun HoverContents.Companion.string(value: String): HoverContents =
    HoverContents.MarkedStringValue(StringOr.StringValue(value))
