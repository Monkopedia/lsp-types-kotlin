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
 * Wire-coverage tracker (#66). Single end-of-suite test that:
 *
 * 1. Resets the [WireMethodRecorder] and runs the JVM integration test suite
 *    in-process via [JUnitCore.runClasses] so its observed coverage isn't
 *    affected by Gradle's test discovery / class ordering.
 * 2. Snapshots the observed coverage + introspects the surface from
 *    `LanguageServer.Companion` / `LanguageClient.Companion`.
 * 3. Writes a Markdown report to
 *    `lsp-ksrpc/build/reports/wire-coverage.md`.
 * 4. Asserts that the observed coverage is a superset of the checked-in
 *    `wire-coverage-baseline.txt`, and that the introspected surface matches
 *    the baseline `server-total` / `client-total` — so adding a new method to
 *    `LanguageServer` without driving it from the suite also fails the build.
 */
class WireCoverageReportTest {

    @Test
    fun `wire coverage meets baseline and the report is written`() {
        WireMethodRecorder.install()
        WireMethodRecorder.reset()

        val core = JUnitCore()
        val result = core.run(*INTEGRATION_TEST_CLASSES)
        // Don't let one of the underlying integration tests' failures bury the
        // coverage signal — surface them clearly first.
        val failureSummary = result.failures.joinToString("\n") {
            "  - ${it.description}: ${it.message}"
        }
        assertTrue(
            result.wasSuccessful(),
            "integration suite must pass before wire-coverage can be asserted; " +
                "failures:\n$failureSummary"
        )

        val state = WireCoverageReport.snapshot()
        val reportDir = locateBuildDir().resolve("reports")
        val reportFile = reportDir.resolve("wire-coverage.md")
        WireCoverageReport.writeTo(reportFile, state)
        println(
            "Wire-coverage report written to ${reportFile.absolutePath} " +
                "(server ${state.serverCalled.size}/${state.serverSurface.size}, " +
                "client ${state.clientCalled.size}/${state.clientSurface.size})"
        )

        val baseline = WireCoverageBaseline.parse(loadBaselineText())

        // Bonus: the introspected surface must match the baseline totals so a
        // NEW method added to LanguageServer / LanguageClient without coverage
        // in the suite ALSO fails the build, not just regressions in existing
        // coverage.
        assertTrue(
            state.serverSurface.size == baseline.serverTotal,
            "Server surface size changed: introspected=${state.serverSurface.size}, " +
                "baseline=${baseline.serverTotal}. If you added a method to " +
                "LanguageServer, drive it from the conformance fixture and bump " +
                "wire-coverage-baseline.txt's `server-total:` line accordingly."
        )
        assertTrue(
            state.clientSurface.size == baseline.clientTotal,
            "Client surface size changed: introspected=${state.clientSurface.size}, " +
                "baseline=${baseline.clientTotal}. If you added a method to " +
                "LanguageClient, drive it from the conformance fixture and bump " +
                "wire-coverage-baseline.txt's `client-total:` line accordingly."
        )

        val regressedServer = baseline.server - state.serverCalled
        val regressedClient = baseline.client - state.clientCalled

        if (regressedServer.isNotEmpty() || regressedClient.isNotEmpty()) {
            fail(
                buildString {
                    appendLine("Wire-coverage regression detected.")
                    if (regressedServer.isNotEmpty()) {
                        appendLine(
                            "Server methods no longer exercised (${regressedServer.size}):"
                        )
                        regressedServer.sorted().forEach { appendLine("  - $it") }
                    }
                    if (regressedClient.isNotEmpty()) {
                        appendLine(
                            "Client methods no longer exercised (${regressedClient.size}):"
                        )
                        regressedClient.sorted().forEach { appendLine("  - $it") }
                    }
                    appendLine(
                        "Restore wire coverage in the conformance fixture or the " +
                            "transport-matrix / lsp4j-conformance driver, OR if removal " +
                            "is intentional, update wire-coverage-baseline.txt."
                    )
                    appendLine(
                        "Full report: ${reportFile.absolutePath}"
                    )
                }
            )
        }
    }

    private fun loadBaselineText(): String {
        val resource = javaClass.classLoader.getResourceAsStream("wire-coverage-baseline.txt")
            ?: error(
                "wire-coverage-baseline.txt missing from jvmTest resources " +
                    "(expected at lsp-ksrpc/src/jvmTest/resources/wire-coverage-baseline.txt)"
            )
        return resource.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    /**
     * Locate the Gradle module's `build/` directory. The JUnit test runs with
     * the module dir as `user.dir`, but `gradle.kotlin.dsl` workers may shift
     * that — fall back to the classpath if needed.
     */
    private fun locateBuildDir(): File {
        val cwd = File(System.getProperty("user.dir") ?: ".")
        val candidate = cwd.resolve("build")
        if (cwd.resolve("build.gradle.kts").isFile && cwd.name == "lsp-ksrpc") return candidate
        // If user.dir is the repo root, descend.
        val moduleDir = cwd.resolve("lsp-ksrpc")
        if (moduleDir.resolve("build.gradle.kts").isFile) return moduleDir.resolve("build")
        // Fall back: just use ./build to avoid hard-failing report generation.
        return candidate
    }

    private companion object {
        /**
         * Every JVM integration test class that drives the LSP conformance
         * fixture over a real wire. Run as a single self-contained suite so
         * the report is deterministic regardless of Gradle's class ordering.
         *
         * Real-server tests (`RealServerClientRoleTest`, `RealServerIntegrationTest`,
         * `RawClientServerTest`) are intentionally NOT in this list — they
         * require external servers and would skip under
         * `lsp.requireIntegrationTests=true` without driving any conformance
         * fixture method anyway.
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
