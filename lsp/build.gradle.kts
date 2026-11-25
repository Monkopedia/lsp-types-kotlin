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
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("lsp-types-kotlin.library")
}

kotlin {
    // Pure types — target everything kotlinx-serialization supports.
    //
    // Published JVM bytecode is Java 11 (class-file major 55), declared rather than
    // inherited: with nothing set, KGP takes `jvmTarget` from whichever JDK runs Gradle
    // (21 on CI), so consumers on JDK 11/17 hit `UnsupportedClassVersionError`.
    // `-Xjdk-release=11` is needed as well: `jvmTarget` only picks the bytecode version,
    // while the JDK API surface the compiler resolves against still comes from the
    // compiling JDK, so class-55 code could still call a JDK 21 method and fail with
    // `NoSuchMethodError` instead. Same approach as kodemirror#305 (see kodemirror#308).
    //
    // Scoped to the `main` compilation only: tests stay on the build JDK's default target
    // because they use JDK 16+ APIs (`Stream.toList`, `Thread.threadId`, ...).
    // `verifyJvmBytecodeVersion` (convention plugin, wired into `check`) fails the build
    // if `jvmJar` ever contains a class that is not major 55.
    jvm {
        compilations.named("main") {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                    freeCompilerArgs.add("-Xjdk-release=11")
                }
            }
        }
    }

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

    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs()
    }

    // Native — Apple
    macosArm64()
    macosX64()
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()
    watchosX64()
    tvosArm64()
    tvosSimulatorArm64()
    tvosX64()

    // Native — Linux
    linuxX64()
    linuxArm64()

    // Native — Windows
    mingwX64()

    // Native — Android
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX64()
    androidNativeX86()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
