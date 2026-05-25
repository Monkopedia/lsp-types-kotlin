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
}

group = "com.monkopedia.lsp"

apiValidation {
    @OptIn(kotlinx.validation.ExperimentalBCVApi::class)
    klib {
        enabled = false
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
}
