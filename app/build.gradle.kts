plugins {
    alias(libs.plugins.android)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "ru.application.homemedkit"
    compileSdk = 36

    defaultConfig {
        applicationId = "ru.application.homemedkit"
        minSdk = 26
        targetSdk = 36
        versionCode = 71
        versionName = "1.9.6"
    }

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
        }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    androidResources {
        generateLocaleConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

dependencies {
    // ==================== Android ====================
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.workmanager)

    // ==================== Room ====================
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    // ==================== Network ====================
    implementation(libs.bundles.ktor)

    // ==================== Navigation ====================
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.json)

    // ==================== Scanner ====================
    implementation(libs.bundles.camera)
    implementation(libs.zxing.android.cpp)

    // ==================== AI and ML ====================
    implementation(libs.gemini)
    implementation(libs.play.services.auth)
    implementation("com.google.guava:guava:31.1-android")
    implementation("com.google.api-client:google-api-client-android:1.26.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev136-1.25.0")
    implementation(libs.mlkit.text.recognition)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // ==================== Settings ====================
    implementation(libs.material.preferences)

    // ==================== Coil ====================
    implementation(libs.coil.compose)
}