import com.android.build.gradle.internal.utils.createPublishingInfoForApp

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.webmy.core_sdk"

    compileSdkVersion(36)

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        buildConfig = true
    }


}

dependencies {
    implementation(libs.androidx.core.ktx)

    api(platform(libs.koin.bom))
    api(libs.koin.android)

    implementation(libs.amplitude)
    implementation(libs.appodeal)

    implementation(libs.google.review)
    implementation(libs.google.review.ktx)
}

