import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.webmy.coresdkdemo"
    compileSdkVersion(36)

    defaultConfig {
        applicationId = "com.webmy.coresdkdemo"

        minSdk = 27
        targetSdk = 36

        versionCode = computeVersionCode()
        versionName = computeVersionName()

        manifestPlaceholders["ADMOB_APPLICATION_ID"] = ""
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
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
    implementation(libs.androidx.core.ktx)

    implementation(project(":core-sdk"))
}