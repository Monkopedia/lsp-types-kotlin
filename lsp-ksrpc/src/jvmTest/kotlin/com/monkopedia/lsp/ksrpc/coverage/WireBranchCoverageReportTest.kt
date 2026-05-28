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

import com.monkopedia.lsp.ksrpc.InMemoryLspIntegrationTest
import com.monkopedia.lsp.ksrpc.Lsp4jClientIntegrationTest
import com.monkopedia.lsp.ksrpc.Lsp4jConformanceInteropTest
import com.monkopedia.lsp.ksrpc.Lsp4jHostedServerInteropTest
import com.monkopedia.lsp.ksrpc.fixtures.TransportMatrixIntegrationTest
import java.io.File
import kotlin.test.assertTrue
import kotlin.test.fail
import org.junit.Test
import org.junit.runner.JUnitCore

/**
 * Wire-branch-coverage tracker (#74). Single end-of-suite test that:
 *
 * 1. Resets the [WireBranchRecorder] (and the sibling [WireMethodRecorder]) and
 *    runs the JVM integration suite in-process via [JUnitCore.runClasses], so
 *    the observed branch set is deterministic regardless of Gradle's discovery
 *    order.
 * 2. Snapshots the observed branches + introspects every sealed interface/class
 *    in `com.monkopedia.lsp.*`.
 * 3. Writes a Markdown report to
 *    `lsp-ksrpc/build/reports/wire-branch-coverage.md`.
 * 4. Asserts that the observed branches form a superset of the checked-in
 *    `wire-branch-baseline.txt`, and that the introspected `union-total` /
 *    `branch-total` haven't shrunk — so removing a branch from the model
 *    without removing it from the baseline also fails the build.
 */
class WireBranchCoverageReportTest {

    @Test
    fun `wire-branch coverage meets baseline and the report is written`() {
        WireMethodRecorder.install()
        WireBranchRecorder.install()
        WireMethodRecorder.reset()
        WireBranchRecorder.reset()

        val core = JUnitCore()
        val result = core.run(*INTEGRATION_TEST_CLASSES)
        val failureSummary = result.failures.joinToString("\n") {
            "  - ${it.description}: ${it.message}"
        }
        assertTrue(
            result.wasSuccessful(),
            "integration suite must pass before wire-branch coverage can be asserted; " +
                "failures:\n$failureSummary"
        )

        val state = WireBranchCoverageReport.snapshot()
        val reportDir = locateBuildDir().resolve("reports")
        val reportFile = reportDir.resolve("wire-branch-coverage.md")
        WireBranchCoverageReport.writeTo(reportFile, state)
        println(
            "Wire-branch-coverage report written to ${reportFile.absolutePath} " +
                "(branches ${state.coveredBranches}/${state.totalBranches}, " +
                "unions ${state.unions.size})"
        )

        val baseline = WireBranchCoverageBaseline.parse(loadBaselineText())

        // Bonus: the introspected surface must be at LEAST the baseline. If a
        // sealed interface or branch is removed from the model without also
        // dropping it from the baseline, this catches it. We assert ≥ rather
        // than == so growing the surface (adding new union branches) doesn't
        // need to bump the totals in the same PR — that's caught instead by
        // the per-branch coverage assertion below pulling the new entries
        // into the baseline once they're exercised.
        assertTrue(
            state.unions.size >= baseline.unionTotal,
            "Union surface shrank: introspected=${state.unions.size}, " +
                "baseline=${baseline.unionTotal}. If a sealed interface was " +
                "removed from the lsp model, drop it from wire-branch-baseline.txt " +
                "(union-total + any matching `branch:` lines)."
        )
        assertTrue(
            state.totalBranches >= baseline.branchTotal,
            "Branch surface shrank: introspected=${state.totalBranches}, " +
                "baseline=${baseline.branchTotal}. If a permitted subclass was " +
                "removed from the model, drop it from wire-branch-baseline.txt " +
                "(branch-total + matching `branch:` line)."
        )

        val regressed = baseline.branches - state.observed
        if (regressed.isNotEmpty()) {
            fail(
                buildString {
                    appendLine("Wire-branch coverage regression detected.")
                    appendLine("Branches no longer exercised (${regressed.size}):")
                    regressed.sorted().forEach { appendLine("  - $it") }
                    appendLine(
                        "Restore wire coverage in the conformance fixture or the " +
                            "transport-matrix / lsp4j-conformance driver, OR if the " +
                            "removal is intentional, update wire-branch-baseline.txt."
                    )
                    appendLine("Full report: ${reportFile.absolutePath}")
                }
            )
        }
    }

    private fun loadBaselineText(): String {
        val resource = javaClass.classLoader.getResourceAsStream("wire-branch-baseline.txt")
            ?: error(
                "wire-branch-baseline.txt missing from jvmTest resources " +
                    "(expected at lsp-ksrpc/src/jvmTest/resources/wire-branch-baseline.txt)"
            )
        return resource.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    private fun locateBuildDir(): File {
        val cwd = File(System.getProperty("user.dir") ?: ".")
        val candidate = cwd.resolve("build")
        if (cwd.resolve("build.gradle.kts").isFile && cwd.name == "lsp-ksrpc") return candidate
        val moduleDir = cwd.resolve("lsp-ksrpc")
        if (moduleDir.resolve("build.gradle.kts").isFile) return moduleDir.resolve("build")
        return candidate
    }

    private companion object {
        /**
         * The same set of JVM integration test classes that the method-coverage
         * tracker drives. Keeping the list in sync ensures the branch and
         * method reports describe the same observation window.
         */
        val INTEGRATION_TEST_CLASSES: Array<Class<*>> = arrayOf(
            InMemoryLspIntegrationTest::class.java,
            Lsp4jClientIntegrationTest::class.java,
            Lsp4jConformanceInteropTest::class.java,
            Lsp4jHostedServerInteropTest::class.java,
            TransportMatrixIntegrationTest::class.java
        )
    }
}
