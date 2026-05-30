import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
}

android {
    namespace = "us.webmy.core.ui"

    compileSdk = CompileSdkVersion

    defaultConfig {
        minSdk = MinSdkVersion
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
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
    api(project(":core"))

    api(libs.appcompat)
    api(libs.material)

    api(libs.koin.compose)

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
    api(libs.androidx.fragment.ktx)

    debugApi(libs.androidx.compose.ui.tooling)
    debugApi(libs.androidx.compose.ui.tooling.preview)
}

configureMavenPublishing("core-ui")
