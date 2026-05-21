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
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
    signing
}

group = "com.monkopedia.lsp"

ktlint {
    version.set("1.8.0")
}

// Signing uses the maintainer's GPG key. It's off by default so local builds
// and dry-runs (publishToMavenLocal) don't try to sign; release CI enables it
// via -PRELEASE_SIGNING_ENABLED=true.
val signingEnabled = (findProperty("RELEASE_SIGNING_ENABLED") as String?)?.toBoolean() == true

mavenPublishing {
    pom {
        name.set(project.name)
        description.set("Kotlin Multiplatform LSP 3.17 types and transport library")
        url.set("https://github.com/Monkopedia/lsp-types-kotlin")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("monkopedia")
                name.set("Jason Monk")
                email.set("monkopedia@gmail.com")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/Monkopedia/lsp-types-kotlin.git")
            developerConnection.set("scm:git:ssh://github.com/Monkopedia/lsp-types-kotlin.git")
            url.set("https://github.com/Monkopedia/lsp-types-kotlin/")
        }
    }
    publishToMavenCentral(automaticRelease = true)
}

if (signingEnabled) {
    signing {
        useGpgCmd()
        sign(extensions.getByType<PublishingExtension>().publications)
    }
}
