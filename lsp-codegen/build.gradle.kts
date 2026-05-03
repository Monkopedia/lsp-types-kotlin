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
    kotlin("jvm")
    alias(libs.plugins.serialization)
    application
}

// Standalone JVM module — not published, not multiplatform.
// Reads metaModel.json and generates Kotlin source into :lsp.

application {
    mainClass.set("com.monkopedia.lsp.codegen.MainKt")
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}

// Download a fresh metaModel.json for a given LSP spec version.
// Usage: ./gradlew :lsp-codegen:downloadMetaModel -PlspVersion=3.17
tasks.register<Exec>("downloadMetaModel") {
    val lspVersion = providers.gradleProperty("lspVersion").orElse("3.17")
    val outputFile = file("src/main/resources/metaModel.json")

    doFirst {
        val version = lspVersion.get()
        val url = "https://microsoft.github.io/language-server-protocol/" +
            "specifications/lsp/$version/metaModel/metaModel.json"
        commandLine("curl", "-sL", url, "-o", outputFile.absolutePath)
    }
}

// Generate LSP types into :lsp module, then auto-format with ktlint.
// Usage: ./gradlew :lsp-codegen:generate
tasks.register<JavaExec>("generate") {
    dependsOn("classes")
    mainClass.set("com.monkopedia.lsp.codegen.MainKt")
    classpath = sourceSets["main"].runtimeClasspath
    args = listOf(
        file("src/main/resources/metaModel.json").absolutePath,
        project(":lsp").file("src/commonMain/kotlin").absolutePath
    )
    finalizedBy(":lsp:ktlintFormat")
}
