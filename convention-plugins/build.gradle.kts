plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.plugins.kotlinMultiplatform.get().let {
        "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
    })
    implementation(libs.plugins.serialization.get().let {
        "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
    })
    implementation(libs.plugins.dokka.get().let {
        "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
    })
    implementation(libs.plugins.vanniktech.publish.get().let {
        "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
    })
    implementation(libs.plugins.ktlint.get().let {
        "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
    })
}
