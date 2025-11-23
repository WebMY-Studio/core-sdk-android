import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
}

android {
    namespace = "com.webmy.core_sdk"

    compileSdkVersion(36)

    defaultConfig {
        minSdk = 27

        manifestPlaceholders["ADMOB_APPLICATION_ID"] = "ca-app-pub-3940256099942544~3347511713"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        buildConfig = true
    }

    publishing {
        singleVariant("release")
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_21.toString()
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
    jvmToolchain(21)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}


dependencies {
    api(libs.coroutines)
    api(libs.androidx.core.ktx)

    api(platform(libs.koin.bom))
    api(libs.koin.android)

    implementation(libs.amplitude)
    implementation(libs.appodeal)

    implementation(libs.google.review.ktx)
    implementation(libs.google.play.services.ads)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.config)

    implementation(libs.billingclient)
    api(libs.squareup.okhttp3.core)
    api(libs.squareup.okhttp3.logging)
    api(libs.squareup.retrofit2.core)
    api(libs.squareup.retrofit2.converters.gson)

}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "com.github.WebMY-Studio"
                artifactId = "core-sdk-android"
                version = rootProject.computeVersionName()
            }
        }
    }
}