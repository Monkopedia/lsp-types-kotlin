pluginManagement {
    includeBuild("convention-plugins")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "lsp-types-kotlin"

include(":lsp")
include(":lsp-ksrpc")
include(":lsp-codegen")
include(":samples:echo-server")
