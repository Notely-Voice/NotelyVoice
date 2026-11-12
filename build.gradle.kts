plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.app.icon) apply false
}
buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.sqldelight.gradle.plugin)
        classpath(libs.hilt.android.gradle.plugin)
        classpath(libs.atomicfu.gradle.plugin)
    }
}
allprojects {
    apply(plugin = "kotlinx-atomicfu")
}
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
