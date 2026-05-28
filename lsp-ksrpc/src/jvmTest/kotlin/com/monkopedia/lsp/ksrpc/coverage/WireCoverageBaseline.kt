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
package com.monkopedia.lsp.ksrpc.coverage

/**
 * Parsed representation of `lsp-ksrpc/src/jvmTest/resources/wire-coverage-baseline.txt`.
 *
 * The baseline file format:
 *
 * ```
 * # Comment lines start with '#'.
 * # Two required header lines anchor the totals: any add to the underlying
 * # interface that isn't covered by the suite fails the build.
 * server-total: 73
 * client-total: 20
 * # Per-method coverage baseline. Each line records one method name (LSP wire
 * # format) that the suite MUST exercise. A line of "server: <wire/name>" is
 * # a baselined server method; "client: <wire/name>" is a baselined client
 * # method. Blank lines and comments (#) are ignored.
 * server: initialize
 * client: window/showMessage
 * ```
 *
 * Edit by re-running the suite, copying the produced wire-coverage.md totals,
 * and updating the per-method lines (the report's table is the source of
 * truth — only methods marked covered should appear here).
 */
internal data class WireCoverageBaseline(
    val serverTotal: Int,
    val clientTotal: Int,
    val server: Set<String>,
    val client: Set<String>
) {

    companion object {
        fun parse(text: String): WireCoverageBaseline {
            var serverTotal: Int? = null
            var clientTotal: Int? = null
            val server = linkedSetOf<String>()
            val client = linkedSetOf<String>()
            for ((lineNo, raw) in text.lines().withIndex()) {
                val line = raw.trim()
                if (line.isEmpty() || line.startsWith("#")) continue
                val colon = line.indexOf(':')
                require(colon > 0) {
                    "wire-coverage-baseline.txt:${lineNo + 1}: expected 'key: value', got '$raw'"
                }
                val key = line.substring(0, colon).trim()
                val value = line.substring(colon + 1).trim()
                when (key) {
                    "server-total" -> serverTotal = value.toInt()

                    "client-total" -> clientTotal = value.toInt()

                    "server" -> server.add(value)

                    "client" -> client.add(value)

                    else -> error(
                        "wire-coverage-baseline.txt:${lineNo + 1}: " +
                            "unknown key '$key' (expected server-total|client-total|server|client)"
                    )
                }
            }
            return WireCoverageBaseline(
                serverTotal = checkNotNull(serverTotal) {
                    "wire-coverage-baseline.txt missing 'server-total:' line"
                },
                clientTotal = checkNotNull(clientTotal) {
                    "wire-coverage-baseline.txt missing 'client-total:' line"
                },
                server = server,
                client = client
            )
        }
    }
}
