plugins {
    alias(libs.plugins.android)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "in.rahulja.medicinekit"
    compileSdk = 37

    defaultConfig {
        applicationId = "in.rahulja.medicinekit"
        minSdk = 26
        targetSdk = 37
        versionCode = 16
        versionName = "1.1.1"
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    androidResources {
        generateLocaleConfig = true
    }

    buildFeatures {
        buildConfig = true
    }

    val yandexClientId: String = System.getenv("YANDEX_CLIENT_ID")
        ?: (project.findProperty("YANDEX_CLIENT_ID") as? String)
        ?: ""
    val yandexClientSecret: String = System.getenv("YANDEX_CLIENT_SECRET")
        ?: (project.findProperty("YANDEX_CLIENT_SECRET") as? String)
        ?: ""

    defaultConfig {
        buildConfigField("String", "CLIENT_ID_YANDEX", "\"$yandexClientId\"")
        buildConfigField("String", "CLIENT_SECRET_YANDEX", "\"$yandexClientSecret\"")
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "proguard-debug.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = true
        checkReleaseBuilds = true
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.all {
            it.useJUnitPlatform()
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,INDEX.LIST,DEPENDENCIES,LICENSE,LICENSE.txt,NOTICE,NOTICE.txt}"
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

dependencies {
    // ==================== Android ====================
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.workmanager)

    // ==================== Koin ====================
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.workmanager)

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

    // ==================== Settings ====================
    implementation(libs.material.preferences)

    // ==================== Coil ====================
    implementation(libs.coil.compose)

    // ==================== AI & ML ====================
    implementation(libs.google.mlkit.text.recognition)
    implementation(libs.google.ai.client.generativeai)

    // ==================== Google Drive Sync ====================
    implementation(libs.play.services.auth)
    implementation(libs.google.api.services.drive) {

        exclude(group = "org.apache.httpcomponents")
    }
    implementation(libs.google.api.client.android)
    implementation(libs.guava)
    implementation(libs.kotlinx.coroutines.play.services)

    // ==================== Test ====================
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    }