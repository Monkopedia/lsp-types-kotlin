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

plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.serialization) apply false
    // Loaded (not applied) at the root so the per-module convention plugin shares
    // a single MavenCentralBuildService classloader — without this, publishing
    // both :lsp and :lsp-ksrpc in one invocation fails with a build-service scope
    // clash. See convention-plugins/.../lsp-types-kotlin.library.gradle.kts.
    alias(libs.plugins.vanniktech.publish) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.bcv)
    alias(libs.plugins.ktlint)
    // Kover is applied (not just loaded) at the root so it can register the
    // unified `koverHtmlReport` / `koverXmlReport` / `koverVerify` tasks here
    // and aggregate JVM coverage across :lsp + :lsp-ksrpc — generated
    // serializers live in :lsp but their code paths only execute under the
    // :lsp-ksrpc integration suite, so both legs have to feed the same report.
    alias(libs.plugins.kover)
}

group = "com.monkopedia.lsp"

// Kotlin 2.4.10 defaults its managed Node.js to 25.0.0 for the JS/Wasm toolchains,
// but that odd (non-LTS) release is rejected by npm deps the Wasm test tooling pulls
// on a fresh resolve (nanoid@6 requires Node `^22 || ^24 || >=26` — 25 is excluded),
// which breaks `:kotlinWasmNpmInstall`. Pin both the JS and Wasm managed Node to the
// 22.x LTS: every current npm dep accepts it and the Kotlin toolchain fully supports it.
plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin> {
    the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec>().version.set("22.11.0")
}
plugins.withType<org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsPlugin> {
    the<org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsEnvSpec>().version.set("22.11.0")
}

apiValidation {
    @OptIn(kotlinx.validation.ExperimentalBCVApi::class)
    klib {
        // Track the target-specific public API of every klib target
        // (native/JS/wasm), not just the JVM `.api`. BCV infers the ABI of
        // targets that can't be built on the current host (e.g. Apple targets
        // on a Linux CI leg) from the targets it CAN build, recording them as
        // "inferred" in the merged `*.klib.api`. That keeps `apiCheck` runnable
        // and the baseline identical on both the ubuntu and macOS CI legs
        // without needing cross-compilation (kotlin.native.enableKlibsCrossCompilation).
        enabled = true
    }
    // echo-server is a sample; lsp-codegen is an internal build-time tool — neither
    // is published, so neither needs binary-compatibility validation.
    ignoredProjects += listOf("echo-server", "lsp-codegen")
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    ktlint {
        version.set("1.8.0")
    }

    // Kover has to be applied to every subproject that participates in the
    // aggregated coverage report (so it can expose its compiled-classes +
    // test-instrument variants). The actual report config lives at the root.
    // We restrict it to the two modules we care about — :lsp (subject of
    // measurement) and :lsp-ksrpc (test driver) — to avoid registering
    // unused coverage tasks on :lsp-codegen and :samples:echo-server.
    if (name == "lsp" || name == "lsp-ksrpc") {
        apply(plugin = "org.jetbrains.kotlinx.kover")
    }
}

// --- Kover: JVM coverage of :lsp serializers, fed by the integration suite ---
//
// The generated LSP types/serializers live in :lsp, but their executable code
// paths (encode/decode, sealed-union dispatch, default-value liberality) are
// driven by the integration tests in :lsp-ksrpc:jvmTest. So we instrument
// :lsp, run :lsp-ksrpc:jvmTest, and aggregate both into a single root report.
//
// FIRST PASS scope is JVM only: KMP-Kover doesn't instrument native targets,
// and the serializer logic is `commonMain` source — exercising it on JVM is
// representative. The instrumented module is :lsp; :lsp-ksrpc is included
// only as the test driver (its production sources are filtered out of the
// report below so the metric stays focused on the generated types).
dependencies {
    kover(project(":lsp"))
    kover(project(":lsp-ksrpc"))
}

// Baseline thresholds are kept in `lsp/kover-baseline.txt` so they can be
// reviewed and bumped in their own commits — see that file for the policy
// ("raise the floor over time, never lower it"). Read here so they can be
// fed into koverVerify rules below.
val koverBaseline: Pair<Int, Int> = run {
    val baselineFile = layout.projectDirectory.file("lsp/kover-baseline.txt").asFile
    if (!baselineFile.exists()) {
        0 to 0
    } else {
        val props = baselineFile.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .mapNotNull { line ->
                val idx = line.indexOf(':')
                if (idx < 0) {
                    null
                } else {
                    line.substring(0, idx).trim() to line.substring(idx + 1).trim()
                }
            }.toMap()
        (props["line-coverage-min"]?.toIntOrNull() ?: 0) to
            (props["branch-coverage-min"]?.toIntOrNull() ?: 0)
    }
}

kover {
    reports {
        filters {
            includes {
                // Only the generated LSP types/serializers — that's the
                // surface this gate is meant to defend.
                classes("com.monkopedia.lsp.*")
            }
            excludes {
                // The :lsp-ksrpc transport/connection layer is the driver,
                // not the subject under measurement. Same for the codegen
                // module (build-time tool, not published).
                classes("com.monkopedia.lsp.ksrpc.*")
                classes("com.monkopedia.lsp.codegen.*")
            }
        }
        total {
            verify {
                onCheck = true
                rule("line coverage >= baseline") {
                    minBound(
                        koverBaseline.first,
                        kotlinx.kover.gradle.plugin.dsl.CoverageUnit.LINE
                    )
                }
                rule("branch coverage >= baseline") {
                    minBound(
                        koverBaseline.second,
                        kotlinx.kover.gradle.plugin.dsl.CoverageUnit.BRANCH
                    )
                }
            }
        }
    }
}

// Mirror the aggregated root reports into :lsp's build dir, which is where
// reviewers expect them per the task spec: `lsp/build/reports/kover/...`.
// We DON'T touch the kover report paths themselves (that path would collide
// with the subproject's own koverXmlReport — both modules have kover applied
// for variant exposure), so we copy after the root reports finish.
val koverPublishLspHtml by tasks.registering(Copy::class) {
    group = "verification"
    description = "Copies the aggregated Kover HTML report into lsp/build/reports/kover/html."
    val rootHtml = tasks.named("koverHtmlReport")
    dependsOn(rootHtml)
    from(layout.buildDirectory.dir("reports/kover/html"))
    into(layout.projectDirectory.dir("lsp/build/reports/kover/html"))
}
val koverPublishLspXml by tasks.registering(Copy::class) {
    group = "verification"
    description = "Copies the aggregated Kover XML report into lsp/build/reports/kover."
    val rootXml = tasks.named("koverXmlReport")
    dependsOn(rootXml)
    from(layout.buildDirectory.file("reports/kover/report.xml"))
    into(layout.projectDirectory.dir("lsp/build/reports/kover"))
}

// Markdown summary task — parses the Kover XML report and emits a small
// reviewer-friendly digest at `lsp/build/reports/kover/summary.md` with:
//   - overall line + branch coverage %
//   - top 10 lowest-covered classes (source filename only, not FQN, so the
//     hot list reads as `Position.kt`, `WorkspaceEdit.kt`, ...)
// This summary is the artifact reviewers actually read; the HTML is for
// drill-down. The task depends on the XML report so a single `koverSummary`
// invocation produces both inputs and digest.
val koverSummary by tasks.registering {
    group = "verification"
    description = "Generates a Markdown digest of Kover coverage (top-10 lowest-covered classes)."
    val xmlReport = layout.buildDirectory.file("reports/kover/report.xml")
    val summaryFile = layout.projectDirectory.file("lsp/build/reports/kover/summary.md")
    inputs.file(xmlReport).withPropertyName("xmlReport")
        .withPathSensitivity(org.gradle.api.tasks.PathSensitivity.RELATIVE)
    outputs.file(summaryFile).withPropertyName("summaryFile")
    dependsOn(tasks.named("koverXmlReport"))

    doLast {
        val xml: java.io.File = xmlReport.get().asFile
        if (!xml.exists()) {
            throw GradleException("Kover XML report missing: $xml")
        }
        val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance().apply {
            // JaCoCo report DTDs reference a public DOCTYPE; disable validation
            // so the parser doesn't try to fetch them offline-builds-friendly.
            setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
            isValidating = false
        }
        val doc: org.w3c.dom.Document = factory.newDocumentBuilder().parse(xml)

        data class Counter(val covered: Int, val missed: Int) {
            val total = covered + missed
            val percent: Double get() = if (total == 0) 100.0 else covered * 100.0 / total
        }
        fun counterOf(el: org.w3c.dom.Element, type: String): Counter {
            val children: org.w3c.dom.NodeList = el.childNodes
            for (i in 0 until children.length) {
                val n = children.item(i)
                if (n is org.w3c.dom.Element &&
                    n.tagName == "counter" &&
                    n.getAttribute("type") == type
                ) {
                    return Counter(
                        covered = n.getAttribute("covered").toIntOrNull() ?: 0,
                        missed = n.getAttribute("missed").toIntOrNull() ?: 0
                    )
                }
            }
            return Counter(0, 0)
        }

        val root: org.w3c.dom.Element = doc.documentElement
        val totalLine = counterOf(root, "LINE")
        val totalBranch = counterOf(root, "BRANCH")

        // Walk all `class` elements (regardless of package nesting) and rank
        // by line-coverage % ascending, ignoring classes with no executable
        // lines (counter total == 0) — those are typealias-only / pure marker
        // classes and would otherwise spam the "100% but empty" floor.
        val classNodes: org.w3c.dom.NodeList = doc.getElementsByTagName("class")
        data class ClassRow(
            val name: String,
            val sourceFile: String,
            val line: Counter,
            val branch: Counter
        )
        val classes = buildList {
            for (i in 0 until classNodes.length) {
                val el = classNodes.item(i) as org.w3c.dom.Element
                val line = counterOf(el, "LINE")
                if (line.total == 0) continue
                val branch = counterOf(el, "BRANCH")
                val name = el.getAttribute("name").substringAfterLast('/')
                val src = el.getAttribute("sourcefilename").ifEmpty { "$name.kt" }
                add(ClassRow(name, src, line, branch))
            }
        }
        val worst = classes
            .sortedWith(compareBy({ it.line.percent }, { -it.line.total }))
            .take(10)

        val pct = { c: Counter -> "%.2f%%".format(c.percent) }
        val linePct = pct(totalLine)
        val branchPct = pct(totalBranch)
        val sb = StringBuilder()
        sb.appendLine("# :lsp Kover coverage summary")
        sb.appendLine()
        sb.appendLine("- Line coverage: **$linePct** (${totalLine.covered}/${totalLine.total})")
        sb.appendLine(
            "- Branch coverage: **$branchPct** (${totalBranch.covered}/${totalBranch.total})"
        )
        sb.appendLine()
        sb.appendLine("## Top 10 lowest-covered classes (by line %)")
        sb.appendLine()
        sb.appendLine("| # | Class | Source | Line % | Branch % | Lines (cov/total) |")
        sb.appendLine("|---|---|---|---|---|---|")
        worst.forEachIndexed { idx, row ->
            sb.appendLine(
                "| ${idx + 1} | ${row.name} | ${row.sourceFile} | ${pct(row.line)} | " +
                    "${pct(row.branch)} | ${row.line.covered}/${row.line.total} |"
            )
        }
        val out: java.io.File = summaryFile.asFile
        out.parentFile.mkdirs()
        out.writeText(sb.toString())
        logger.lifecycle("Kover summary written to $out")
        logger.lifecycle(
            "  line=${pct(totalLine)} branch=${pct(totalBranch)} (classes=${classes.size})"
        )
    }
}

// The aggregated `:koverXmlReport` / `:koverHtmlReport` / `:koverVerify`
// already depend transitively on `:lsp:jvmTest` and `:lsp-ksrpc:jvmTest` via
// the `kover(project(...))` dependencies declared at the top of this file —
// no extra wiring needed.
