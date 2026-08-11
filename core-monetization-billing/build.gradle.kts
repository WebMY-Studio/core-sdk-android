import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

android {
    namespace = "us.webmy.core.monetization.billing"

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
        freeCompilerArgs.add("-opt-in=us.webmy.core.internal.InternalWebmyApi")
    }
}

dependencies {
    api(project(":core"))

    implementation(libs.billingclient)
    implementation(libs.facebook)
    implementation(libs.apphud)
}

configureMavenPublishing("core-monetization-billing")
