import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
}

android {
    namespace = "com.webmy.core_sdk_extended"

    compileSdkVersion(36)

    defaultConfig {
        minSdk = 27
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
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
    api(project(":core-sdk"))
    api(libs.billingclient)

    api(libs.facebook)

    implementation(platform(libs.adapty.bom))
    implementation(libs.adapty)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "com.github.WebMY-Studio"
                artifactId = "core-sdk-extended"
                version = rootProject.computeVersionName()
            }
        }
        repositories {
            mavenLocal()
        }
    }
}