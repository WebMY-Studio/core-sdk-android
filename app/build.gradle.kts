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
}