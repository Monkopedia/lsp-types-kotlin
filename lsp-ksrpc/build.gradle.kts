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
        jvmMain.get().dependsOn(jsonrpcMain)
        val jsMain by getting { dependsOn(jsonrpcMain) }
        val wasmJsMain by getting { dependsOn(jsonrpcMain) }
        val appleMain by getting { dependsOn(jsonrpcMain) }
        val linuxMain by getting { dependsOn(jsonrpcMain) }

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
}
