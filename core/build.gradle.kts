import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.android.library)
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
}

configureMavenPublishing("core")
