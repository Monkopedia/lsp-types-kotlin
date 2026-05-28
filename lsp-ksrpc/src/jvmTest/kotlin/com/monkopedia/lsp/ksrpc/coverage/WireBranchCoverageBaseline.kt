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
 * Parsed representation of
 * `lsp-ksrpc/src/jvmTest/resources/wire-branch-baseline.txt` (#74).
 *
 * Sibling to [WireCoverageBaseline]. The file format mirrors that file's shape:
 *
 * ```
 * # Comments start with '#'.
 * # Two header lines anchor the introspected surface. Adding a new sealed
 * # interface / branch to the lsp model without driving it from the conformance
 * # fixture also fails the build via these totals.
 * union-total: 47
 * branch-total: 100
 * # Per-branch coverage baseline. Each line records one branch the suite MUST
 * # exercise, of the form `<UnionFqn>.<SubclassSimpleName>`.
 * branch: com.monkopedia.lsp.HoverContents.MarkupContentValue
 * ```
 *
 * Edit by re-running the integration suite and copying the entries marked
 * `yes` in the produced `wire-branch-coverage.md` report. Drop a branch from
 * the baseline only if removal is intentional (e.g. the underlying model
 * dropped the branch).
 */
internal data class WireBranchCoverageBaseline(
    val unionTotal: Int,
    val branchTotal: Int,
    val branches: Set<String>
) {

    companion object {
        fun parse(text: String): WireBranchCoverageBaseline {
            var unionTotal: Int? = null
            var branchTotal: Int? = null
            val branches = linkedSetOf<String>()
            for ((lineNo, raw) in text.lines().withIndex()) {
                val line = raw.trim()
                if (line.isEmpty() || line.startsWith("#")) continue
                val colon = line.indexOf(':')
                require(colon > 0) {
                    "wire-branch-baseline.txt:${lineNo + 1}: expected 'key: value', got '$raw'"
                }
                val key = line.substring(0, colon).trim()
                val value = line.substring(colon + 1).trim()
                when (key) {
                    "union-total" -> unionTotal = value.toInt()

                    "branch-total" -> branchTotal = value.toInt()

                    "branch" -> branches.add(value)

                    else -> error(
                        "wire-branch-baseline.txt:${lineNo + 1}: " +
                            "unknown key '$key' (expected union-total|branch-total|branch)"
                    )
                }
            }
            return WireBranchCoverageBaseline(
                unionTotal = checkNotNull(unionTotal) {
                    "wire-branch-baseline.txt missing 'union-total:' line"
                },
                branchTotal = checkNotNull(branchTotal) {
                    "wire-branch-baseline.txt missing 'branch-total:' line"
                },
                branches = branches
            )
        }
    }
}
