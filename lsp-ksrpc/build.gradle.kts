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
    id("lsp-types-kotlin.library")
    alias(libs.plugins.ksrpc.plugin)
}

kotlin {
    // JVM + POSIX Native — matching ksrpc-jsonrpc's supported targets.
    jvm()

    macosArm64()
    linuxX64()
    iosArm64()
    iosSimulatorArm64()
    iosX64()

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
        // jsonrpc wiring — only available on targets ksrpc-jsonrpc supports.
        val jsonrpcMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                api(libs.ksrpc.jsonrpc)
            }
        }
        jvmMain.get().dependsOn(jsonrpcMain)
        nativeMain.get().dependsOn(jsonrpcMain)
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
}
