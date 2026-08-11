import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

android {
    namespace = "us.webmy.core.monetization"

    compileSdk = CompileSdkVersion

    defaultConfig {
        minSdk = MinSdkVersion
    }

    buildFeatures {
        buildConfig = true
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
    api(project(":core-monetization-billing"))

    implementation(libs.google.play.services.ads)
    implementation(libs.appodeal)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    implementation(libs.ads.networks.amazon)
    implementation(libs.ads.networks.mintegral)
    implementation(libs.ads.networks.my.target)
    implementation(libs.ads.networks.pangle)
    implementation(libs.ads.networks.vungle)
    implementation(libs.amazon.adapter)
    implementation(libs.applovin.adapter)
    implementation(libs.bidmachine.adapter)
    implementation(libs.bigoads.adapter)
    implementation(libs.chartboost.adapter)
    implementation(libs.dtexchange.adapter)
    implementation(libs.inmobi.adapter)
    implementation(libs.ironsource.adapter)
    implementation(libs.meta.adapter)
    implementation(libs.mintegral.adapter)
    implementation(libs.mobilefuse.adapter)
    implementation(libs.moloco.adapter)
    implementation(libs.startio.adapter)
    implementation(libs.taurusx.adapter)
    implementation(libs.unityads.adapter)
    implementation(libs.vkads.adapter)
    implementation(libs.vungle.adapter)
    implementation(libs.yandex.adapter)
    implementation(libs.amazon.tam.adapter)
    implementation(libs.mediation.bidmachine.adapter)
    implementation(libs.mediation.bigoads.adapter)
    implementation(libs.bytedance.adapter)
    implementation(libs.mediation.chartboost.adapter)
    implementation(libs.facebook.adapter)
    implementation(libs.mediation.fyber.adapter)
    implementation(libs.mediation.google.ad.manager.adapter)
    implementation(libs.google.adapter)
    implementation(libs.mediation.inmobi.adapter)
    implementation(libs.mediation.ironsource.adapter)
    implementation(libs.mediation.mintegral.adapter)
    implementation(libs.mediation.mobilefuse.adapter)
    implementation(libs.mediation.moloco.adapter)
    implementation(libs.mytarget.adapter)
    implementation(libs.ogury.presage.adapter)
    implementation(libs.pubmatic.adapter)
    implementation(libs.smaato.adapter)
    implementation(libs.mediation.unityads.adapter)
    implementation(libs.verve.adapter)
    implementation(libs.mediation.vungle.adapter)
    implementation(libs.mediation.yandex.adapter)
    implementation(libs.adjust)
    implementation(libs.admob)
    implementation(libs.amazon)
    implementation(libs.applovin)
    implementation(libs.applovin.max)
    implementation(libs.bidmachine)
    implementation(libs.bidon)
    implementation(libs.bigo.ads)
    implementation(libs.chartboost)
    implementation(libs.dt.exchange)
    implementation(libs.facebook.analytics)
    implementation(libs.firebase)
    implementation(libs.iab)
    implementation(libs.inmobi)
    implementation(libs.ironsource)
    implementation(libs.adapters.level.play)
    implementation(libs.meta)
    implementation(libs.mintegral)
    implementation(libs.mobilefuse)
    implementation(libs.moloco)
    implementation(libs.my.target)
    implementation(libs.ogury)
    implementation(libs.pangle)
    implementation(libs.pubmatic)
    implementation(libs.sentry.analytics)
    implementation(libs.smaato)
    implementation(libs.startio)
    implementation(libs.taurusx)
    implementation(libs.unity.ads)
    implementation(libs.verve)
    implementation(libs.vungle)
    implementation(libs.yandex)
    implementation(libs.admob.adapter)
    implementation(libs.ads.mediation.applovin.adapter)
    implementation(libs.ads.mediation.bidmachine.adapter)
    implementation(libs.ads.mediation.bigo.adapter)
    implementation(libs.ads.mediation.facebook.adapter)
    implementation(libs.ads.mediation.fyber.adapter)
    implementation(libs.ads.mediation.inmobi.adapter)
    implementation(libs.unity3d.mintegral.adapter)
    implementation(libs.unity3d.mobilefuse.adapter)
    implementation(libs.ads.mediation.moloco.adapter)
    implementation(libs.unity3d.mytarget.adapter)
    implementation(libs.ads.mediation.ogury.adapter)
    implementation(libs.ads.mediation.pangle.adapter)
    implementation(libs.unity3d.smaato.adapter)
    implementation(libs.unity3d.unityads.adapter)
    implementation(libs.unity3d.verve.adapter)
    implementation(libs.unity3d.vungle.adapter)
}

configureMavenPublishing("core-monetization-ads")
