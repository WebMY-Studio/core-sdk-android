import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

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

        val localProperties = gradleLocalProperties(rootDir, providers)

        defaultConfig {
            buildConfigField(
                "String",
                "APPODEAL_APP_KEY",
                localProperties.getStringProperty("APPODEAL_APP_KEY")
            )

            buildConfigField(
                "String",
                "AMPLITUDE_API_KEY",
                localProperties.getStringProperty("AMPLITUDE_API_KEY")
            )
        }
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(project(":core-sdk"))
}