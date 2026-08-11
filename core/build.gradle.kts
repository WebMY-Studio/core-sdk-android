import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
}

android {
    namespace = "us.webmy.core"

    compileSdk = CompileSdkVersion

    defaultConfig {
        minSdk = MinSdkVersion
    }

    buildFeatures {
        buildConfig = true
        compose = true
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
    // api: types below leak into public SDK signatures
    api(libs.coroutines)                       // Flow/StateFlow in Preferences, BillingManager, etc.
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.material3)
    api(libs.androidx.navigation3.runtime)     // NavKey in Router/WebmyActivity
    api(libs.androidx.navigation3.ui)          // NavDisplay.transitionSpec in consumer screens() overrides
    api(libs.androidx.lifecycle.viewmodel.ktx) // BaseViewModel : ViewModel
    api(libs.squareup.okhttp3.core)            // NetworkConfig.interceptors, WebMY.httpClient
    api(libs.squareup.retrofit2.core)          // NetworkApiCreator.createRetrofit
    // FragmentActivity only: androidx.biometric's BiometricPrompt requires it. No fragments are used.
    api(libs.androidx.fragment.ktx)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.config)
    implementation(libs.amplitude)
    implementation(libs.androidx.biometric)
    implementation(libs.google.review.ktx)
    implementation(libs.squareup.okhttp3.logging)
    implementation(libs.squareup.retrofit2.converters.gson)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.tooling.preview)
}

configureMavenPublishing("core")
