/*
 * Copyright 2026 Jason Monk
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
package com.monkopedia.lsp.ksrpc.coverage

/**
 * Renders a covered/total pair as the `N/M (P%)` totals-line fragment used by
 * both the wire-method (#66) and wire-branch (#74) Markdown reports.
 *
 * An empty surface renders as `n/a` rather than dividing by zero.
 */
internal fun ratio(covered: Int, total: Int): String {
    val pct = if (total == 0) "n/a" else "${covered * 100 / total}%"
    return "$covered/$total ($pct)"
}
