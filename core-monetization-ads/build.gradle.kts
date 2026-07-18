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
    api(project(":core-monetization-billing"))

    api(libs.facebook)

    implementation(libs.google.play.services.ads)
    implementation(libs.appodeal)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    api(libs.ads.networks.amazon)
    api(libs.ads.networks.mintegral)
    api(libs.ads.networks.my.target)
    api(libs.ads.networks.pangle)
    api(libs.ads.networks.vungle)
    api(libs.amazon.adapter)
    api(libs.applovin.adapter)
    api(libs.bidmachine.adapter)
    api(libs.bigoads.adapter)
    api(libs.chartboost.adapter)
    api(libs.dtexchange.adapter)
    api(libs.inmobi.adapter)
    api(libs.ironsource.adapter)
    api(libs.meta.adapter)
    api(libs.mintegral.adapter)
    api(libs.mobilefuse.adapter)
    api(libs.moloco.adapter)
    api(libs.startio.adapter)
    api(libs.taurusx.adapter)
    api(libs.unityads.adapter)
    api(libs.vkads.adapter)
    api(libs.vungle.adapter)
    api(libs.yandex.adapter)
    api(libs.amazon.tam.adapter)
    api(libs.mediation.bidmachine.adapter)
    api(libs.mediation.bigoads.adapter)
    api(libs.bytedance.adapter)
    api(libs.mediation.chartboost.adapter)
    api(libs.facebook.adapter)
    api(libs.mediation.fyber.adapter)
    api(libs.mediation.google.ad.manager.adapter)
    api(libs.google.adapter)
    api(libs.mediation.inmobi.adapter)
    api(libs.mediation.ironsource.adapter)
    api(libs.mediation.mintegral.adapter)
    api(libs.mediation.mobilefuse.adapter)
    api(libs.mediation.moloco.adapter)
    api(libs.mytarget.adapter)
    api(libs.ogury.presage.adapter)
    api(libs.pubmatic.adapter)
    api(libs.smaato.adapter)
    api(libs.mediation.unityads.adapter)
    api(libs.verve.adapter)
    api(libs.mediation.vungle.adapter)
    api(libs.mediation.yandex.adapter)
    api(libs.adjust)
    api(libs.admob)
    api(libs.amazon)
    api(libs.applovin)
    api(libs.applovin.max)
    api(libs.bidmachine)
    api(libs.bidon)
    api(libs.bigo.ads)
    api(libs.chartboost)
    api(libs.dt.exchange)
    api(libs.facebook.analytics)
    api(libs.firebase)
    api(libs.iab)
    api(libs.inmobi)
    api(libs.ironsource)
    api(libs.adapters.level.play)
    api(libs.meta)
    api(libs.mintegral)
    api(libs.mobilefuse)
    api(libs.moloco)
    api(libs.my.target)
    api(libs.ogury)
    api(libs.pangle)
    api(libs.pubmatic)
    api(libs.sentry.analytics)
    api(libs.smaato)
    api(libs.startio)
    api(libs.taurusx)
    api(libs.unity.ads)
    api(libs.verve)
    api(libs.vungle)
    api(libs.yandex)
    api(libs.admob.adapter)
    api(libs.ads.mediation.applovin.adapter)
    api(libs.ads.mediation.bidmachine.adapter)
    api(libs.ads.mediation.bigo.adapter)
    api(libs.ads.mediation.facebook.adapter)
    api(libs.ads.mediation.fyber.adapter)
    api(libs.ads.mediation.inmobi.adapter)
    api(libs.unity3d.mintegral.adapter)
    api(libs.unity3d.mobilefuse.adapter)
    api(libs.ads.mediation.moloco.adapter)
    api(libs.unity3d.mytarget.adapter)
    api(libs.ads.mediation.ogury.adapter)
    api(libs.ads.mediation.pangle.adapter)
    api(libs.unity3d.smaato.adapter)
    api(libs.unity3d.unityads.adapter)
    api(libs.unity3d.verve.adapter)
    api(libs.unity3d.vungle.adapter)
}

configureMavenPublishing("core-monetization-ads")
