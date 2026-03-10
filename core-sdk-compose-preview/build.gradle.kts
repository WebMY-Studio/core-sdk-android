import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

android {
    namespace = "com.webmy.core_sdk_compose_preview"

    compileSdk = CompileSdkVersion

    defaultConfig {
        minSdk = MinSdkVersion
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
    api(project(":core-sdk-compose"))
    api(libs.androidx.compose.ui.tooling)
    api(libs.androidx.compose.ui.tooling.preview)
}

configureMavenPublishing("core-sdk-compose-preview")
