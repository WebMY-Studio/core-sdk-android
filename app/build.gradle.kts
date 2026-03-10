import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "us.webmy.coresdkdemo"
    compileSdk = CompileSdkVersion

    defaultConfig {
        applicationId = namespace

        minSdk = MinSdkVersion
        targetSdk = TargetSdkVersion

        versionCode = computeVersionCode()
        versionName = computeVersionName()
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
}

kotlin {
    compilerOptions {
        languageVersion = KotlinVersion.KOTLIN_2_2
    }
}

dependencies {
    implementation(project(":core-sdk"))
}