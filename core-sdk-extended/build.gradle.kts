import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

android {
    namespace = "us.webmy.core_sdk_extended"

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
    api(project(":core-sdk"))
    api(libs.billingclient)

    api(libs.facebook)

    implementation(platform(libs.adapty.bom))
    implementation(libs.adapty)
}

configureMavenPublishing("core-sdk-extended")