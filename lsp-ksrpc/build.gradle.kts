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

import java.time.Duration
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("lsp-types-kotlin.library")
    alias(libs.plugins.ksrpc.plugin)
}

kotlin {
    // Interface tier (commonMain → ksrpc-core/api) covers the full ksrpc-core
    // target set. The jsonrpc connection helpers (jsonrpcMain → ksrpc-jsonrpc)
    // are confined below to the subset ksrpc-jsonrpc supports — everything but
    // mingwX64. mingwX64 still gets the @KsService interfaces (usable over a
    // ksrpc relay channel), just not the jsonrpc transport helpers.
    jvm()

    js(IR) {
        browser {
            testTask { enabled = false }
        }
        nodejs()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            testTask { enabled = false }
        }
        nodejs()
    }

    macosArm64()
    macosX64()
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    linuxX64()
    linuxArm64()
    mingwX64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            api(project(":lsp"))
            api(libs.ksrpc.api)
            api(libs.ksrpc.core)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        jvmTest.dependencies {
            implementation(libs.lsp4j)
            // ktor-network gives the transport-matrix TCP test true non-blocking
            // loopback sockets yielding ktor ByteReadChannel/ByteWriteChannel, the
            // same channel type the LSP jsonrpc connection consumes. Test-only.
            implementation(libs.ktor.network)
        }
        // jsonrpc wiring — only on targets ksrpc-jsonrpc supports, i.e. every
        // target except mingwX64. mingwX64Main is intentionally left attached to
        // commonMain only (via the default nativeMain hierarchy), so it gets the
        // interfaces but not the jsonrpc helpers.
        val jsonrpcMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                api(libs.ksrpc.jsonrpc)
            }
        }
        val jsMain by getting { dependsOn(jsonrpcMain) }
        val wasmJsMain by getting { dependsOn(jsonrpcMain) }

        // Process-spawn tier. The unified cross-platform `spawnLspServer` /
        // `LspServerProcess` contract (the `expect suspend fun` + the
        // `LspServerProcess` interface) lives here so JVM and the posix-fd native
        // targets share a single, compiler-enforced surface — no API drift. It sits
        // between jsonrpcMain (for the connection types + lspKsrpcEnvironment) and
        // the two platforms that can actually fork/exec a child process: jvm and
        // posix. js/wasm/mingw do NOT depend on it — they can't spawn processes, so
        // they get no `spawnLspServer` declaration (and thus no missing-actual error).
        // processMain itself needs no ksrpc-sockets; that's a posix-only (native
        // posix-fd ByteChannel) dependency kept on posixMain below.
        val processMain by creating {
            dependsOn(jsonrpcMain)
        }
        jvmMain.get().dependsOn(processMain)

        // posix-fd targets (linux + apple). These are the native targets that
        // (a) ksrpc-jsonrpc supports and (b) have a real posix process-spawn
        // surface (fork/exec/pipe). The native `spawnLspServer` actual and its
        // posix-fd ByteChannel plumbing (ksrpc-sockets) live here. mingwX64 is
        // intentionally excluded: no posix process model, and ksrpc-jsonrpc
        // doesn't target it anyway (it stays interface-only via commonMain).
        val posixMain by creating {
            dependsOn(processMain)
            dependencies {
                api(libs.ksrpc.sockets)
            }
        }
        val appleMain by getting { dependsOn(posixMain) }
        val linuxMain by getting { dependsOn(posixMain) }

        // Shared native (linux + apple) test source set for the jsonrpc-transport
        // smoke. It depends only on commonTest (for the fixtures); the jsonrpc
        // connection helpers (`asLspConnection`, in jsonrpcMain) are already
        // visible because every leaf test compilation that consumes this set
        // (linuxX64Test, macos*Test) associates with a *Main that depends on
        // jsonrpcMain (linuxMain / appleMain). Do NOT add an explicit
        // dependsOn(jsonrpcMain) here: a test set depending on a main set links
        // it twice and the native linker fails with "symbol already bound".
        val nativeJsonrpcTest by creating {
            dependsOn(commonTest.get())
        }
        val linuxX64Test by getting { dependsOn(nativeJsonrpcTest) }
        val macosArm64Test by getting { dependsOn(nativeJsonrpcTest) }
        val macosX64Test by getting { dependsOn(nativeJsonrpcTest) }
    }
}

// Hard ceiling on every Test task (the KMP `jvmTest` task is an
// `org.gradle.api.tasks.testing.Test`, so `withType<Test>` catches it). The JVM
// integration suite occasionally wedges on teardown — a leaked non-daemon thread
// keeps the test-worker JVM alive after the suite passes — and with no timeout
// the task stalled for 45–118 min in CI before manual cancellation (issue #79).
//
// Real jvmTest runtime is ~2 min, so a 5-minute cap fails a genuine wedge fast
// (Gradle's task `timeout` fails the task on expiry) without tripping on healthy
// runs. The in-process JvmTestWatchdog fires its thread dump at 4 min — under
// this cap — so the diagnostic stack trace lands before Gradle kills the worker.
tasks.withType<Test>().configureEach {
    timeout.set(Duration.ofMinutes(5))
}

// Native test executables (KotlinNativeTest) are NOT `org.gradle.api.tasks.testing.Test`,
// so the cap above doesn't cover them and the #79 in-process JVM watchdog can't see them.
// The native real-server test (LspProcessRealServerTest) drives an external clangd over a
// posix-stdio pipe and tears it down with a process kill; if that teardown ever wedged it
// would stall with no ceiling. Give every native test task its own hard timeout so a wedge
// fails the task fast instead of hanging the build. (Each test also bounds its own wire
// calls with withTimeout and kills the child in a finally, so a hang should fail bounded;
// this is the belt-and-suspenders ceiling.)
//
// Native test processes don't receive Gradle `-P` properties as JVM system properties the
// way `jvmTest` does, so the real-server gate is forwarded as an ENVIRONMENT VARIABLE
// (LSP_REQUIRE_REAL_SERVERS), which the test reads via getenv at runtime.
tasks.withType<org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest>()
    .configureEach {
        timeout.set(Duration.ofMinutes(5))
        environment(
            "LSP_REQUIRE_REAL_SERVERS",
            project.findProperty("lsp.requireRealServers")?.toString() ?: "false"
        )
    }

// RawClientServerTest spawns samples/echo-server as a child process and drives it
// with raw JSON-RPC bytes to validate wire compatibility. Make sure the echo-server
// install is built before JVM tests run.
tasks.named<org.gradle.api.tasks.testing.Test>("jvmTest") {
    dependsOn(":samples:echo-server:installDist")
    // When set (CI does), the wire-compat integration tests hard-fail instead of
    // skipping if their preconditions (clangd on PATH, echo-server built) are
    // missing — a skip must not pass as green where the inputs are guaranteed.
    systemProperty(
        "lsp.requireIntegrationTests",
        project.findProperty("lsp.requireIntegrationTests")?.toString() ?: "false"
    )
    // Independent gate for the real-server client-role matrix (clangd, pyright,
    // gopls, ...). These external servers aren't guaranteed installed everywhere,
    // so they're held out of `requireIntegrationTests`: with this flag off they
    // skip cleanly, and only the dedicated real-server job sets it to `true` to
    // turn a missing server into a hard failure.
    systemProperty(
        "lsp.requireRealServers",
        project.findProperty("lsp.requireRealServers")?.toString() ?: "false"
    )
    // When set (the nightly real-server job does this), RealServerClientRoleTest
    // tees every observed LSP wire frame to
    // `src/jvmTest/resources/captured/<server>/<direction>/<method>__<seq>.json`
    // so the portable replay test (CapturedCorpusReplayTest) has REAL third-party
    // payloads to assert round-trip stability against. Default OFF — capture is
    // opt-in and never the default for per-PR or local runs.
    systemProperty(
        "lsp.captureCorpus",
        project.findProperty("lsp.captureCorpus")?.toString() ?: "false"
    )
}
