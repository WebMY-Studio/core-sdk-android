import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
}

android {
    namespace = "us.webmy.core_sdk"

    compileSdk = CompileSdkVersion

    defaultConfig {
        minSdk = MinSdkVersion
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
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

    api(libs.appcompat)
    api(libs.material)

    api(platform(libs.koin.bom))
    api(libs.koin.android)

    implementation(libs.google.review.ktx)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.config)

    implementation(libs.amplitude)

    api(libs.squareup.okhttp3.core)
    api(libs.squareup.okhttp3.logging)
    api(libs.squareup.retrofit2.core)
    api(libs.squareup.retrofit2.converters.gson)

    implementation(project(":core-sdk-compose"))
}

configureMavenPublishing("core-sdk")