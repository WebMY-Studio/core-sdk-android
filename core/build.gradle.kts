import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
}

android {
    namespace = "us.webmy.core"

    compileSdk = CompileSdkVersion

    defaultConfig {
        minSdk = MinSdkVersion
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    publishing {
        singleVariant("release")
    }
}

kotlin {
    compilerOptions {
        languageVersion = KotlinVersion.KOTLIN_2_2
    }
}

dependencies {
    api(libs.coroutines)

    api(platform(libs.koin.bom))
    api(libs.koin.android)
    api(libs.koin.compose)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.config)

    implementation(libs.amplitude)
    implementation(libs.androidx.biometric)

    api(libs.squareup.okhttp3.core)
    api(libs.squareup.okhttp3.logging)
    api(libs.squareup.retrofit2.core)
    api(libs.squareup.retrofit2.converters.gson)

    implementation(libs.google.review.ktx)

    api(platform(libs.androidx.compose.bom))

    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.animation.graphics)
    api(libs.androidx.compose.ui.util)
    api(libs.androidx.compose.ui.text.google.fonts)
    api(libs.androidx.compose.material.icons)
    api(libs.androidx.compose.material.icons.extended)
    api(libs.androidx.activity.compose)
    api(libs.androidx.navigation3.runtime)
    api(libs.androidx.navigation3.ui)

    // FragmentActivity only: androidx.biometric's BiometricPrompt requires it. No fragments are used.
    api(libs.androidx.fragment.ktx)

    debugApi(libs.androidx.compose.ui.tooling)
    debugApi(libs.androidx.compose.ui.tooling.preview)
}

configureMavenPublishing("core")
