import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    id("dev.sergiobelda.compose.vectorize") version "1.0.2"
    id("com.squareup.sqldelight")
    //alias(libs.plugins.app.icon)
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.compose.ui.preview)
            implementation(libs.androidx.compose.ui.tooling)
            implementation(libs.androidx.compose.ui.util)
            implementation(libs.google.accompanist.systemuicontroller)
            implementation(libs.sqldelight.android.driver)

            implementation(libs.google.accompanist.systemuicontroller)

            implementation(libs.kotlinx.serialization.json)
            implementation(project(":lib"))

            // splash
            implementation(libs.core.splashscreen)
            implementation(libs.androidx.compose.documentfile)
        }

        commonMain.dependencies {
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.kotlinx.datetime)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(libs.material.icons.core)
            implementation(compose.components.resources)

            implementation(compose.components.resources)
            implementation(libs.compose.vectorize.core)
            implementation(libs.kotlinx.serialization.json)

            // image loading
            implementation(libs.coil.compose)

            // koin
            implementation(libs.koin.core)
            implementation(libs.koin.test)
            implementation(libs.koin.compose.viewmodel)


            // navigation
            implementation(libs.navigation.compose)

            // logging
            implementation(libs.napier)

            // Data store
            implementation(libs.datastore.preferences)
            implementation(libs.datastore)

            implementation(project(":core:audio"))
        }

        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
            compileOnly(libs.jetbrains.atomicfu)
            api(libs.jetbrains.atomicfu)
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }

    targets.all {
        compilations.all {
            compilerOptions.configure {
                allWarningsAsErrors = false
                freeCompilerArgs.add("-Xexpected-actual-classes")
                // For deterministic builds
                freeCompilerArgs.add("-Xjsr305=strict")
                freeCompilerArgs.add("-Xno-param-assertions")
                freeCompilerArgs.add("-Xno-receiver-assertions")
                freeCompilerArgs.add("-Xno-call-assertions")
                freeCompilerArgs.add("-Xno-optimize")
                freeCompilerArgs.add("-Xassertions=jvm")
                freeCompilerArgs.add("-Xuse-deterministic-jar-order")
                freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
            }
        }
    }

    val whisperFrameworkPath = file("${projectDir}/../iosApp/whisper.xcframework")
    iosSimulatorArm64 {
        compilations.getByName("main") {
            cinterops.create("whisperSimArm64") {
                defFile(project.file("src/nativeInterop/cinterop/whisper.def"))
                compilerOpts(
                    "-I${whisperFrameworkPath}/ios-arm64_x86_64-simulator/whisper.framework/Headers",
                    "-F${whisperFrameworkPath}"
                )
            }
        }
    }
    iosArm64 {
        compilations.getByName("main") {
            cinterops.create("whisperArm64") {
                defFile(project.file("src/nativeInterop/cinterop/whisper.def"))
                compilerOpts(
                    "-I${whisperFrameworkPath}/ios-arm64/whisper.framework/Headers",
                    "-F${whisperFrameworkPath}"
                )
            }
        }
    }

    iosX64 {
        compilations.getByName("main") {
            cinterops.create("whisperX64") {
                defFile(project.file("src/nativeInterop/cinterop/whisper.def"))
                compilerOpts(
                    "-I${whisperFrameworkPath}/ios-arm64/whisper.framework/Headers",
                    "-F${whisperFrameworkPath}"
                )
            }
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.module.notelycompose.resources"
    generateResClass = always
}

sqldelight {
    database("NoteDatabase") {
        packageName = "com.module.notelycompose.database"
        sourceFolders = listOf("sqldelight")
    }
}
android {
    namespace = "com.module.notelycompose.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    // Removed src/commonMain/resources
    // sourceSets["main"].resources.srcDirs("src/commonMain/resources")
    defaultConfig {
        applicationId = "com.module.notelycompose.android"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 27
        versionName = "1.2.6"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.6"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging {
        // Ensure reproducible packaging
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE*"
            excludes += "META-INF/NOTICE*"
            excludes += "META-INF/*.version"
            excludes += "assets/composeResources/com.module.notelycompose.resources/strings.xml"
        }

        // Force deterministic file ordering
        jniLibs {
            useLegacyPackaging = true
            // 16KB Page Size Support: Use uncompressed native libraries
            pickFirsts += listOf("**/libc++_shared.so", "**/libwhisper.so")
        }

        // Ensure reproducible DEX files
        dex {
            useLegacyPackaging = false
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isDebuggable = false
            // uncomment to run on release for testing
            // signingConfig = signingConfigs.getByName("debug")
        }
    }
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }
    ndkVersion = "27.0.12077973"
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.animation.android)
    implementation(libs.androidx.appcompat)
}
