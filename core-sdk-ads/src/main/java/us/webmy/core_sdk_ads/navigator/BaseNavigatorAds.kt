package us.webmy.core_sdk_ads.navigator

import androidx.appcompat.app.AppCompatActivity
import us.webmy.core_sdk.presentation.base.navigator.Navigation
import us.webmy.core_sdk.tools.biometrics.domain.BiometricsServiceFactory
import us.webmy.core_sdk_ads.tools.ads.AdsPremiumManager
import us.webmy.core_sdk_extended.navigator.BaseNavigatorExtended
import us.webmy.core_sdk_extended.tools.billing.BillingManager

abstract class BaseNavigatorAds(
    biometricsServiceFactory: BiometricsServiceFactory,
    billingManager: BillingManager,
    private val adsPremiumManager: AdsPremiumManager,
) : BaseNavigatorExtended(biometricsServiceFactory, billingManager) {

    override suspend fun navigate(activity: AppCompatActivity, nav: Navigation): Result<Unit> {
        return runCatching {
            when (nav) {
                is Navigation.Ad.Interstitial -> adsPremiumManager.requestInterstitial(
                    activity = activity,
                    source = nav.source
                )

                is Navigation.Ad.Reward -> adsPremiumManager.requestReward(
                    activity = activity,
                    source = nav.source,
                    placement = nav.placement,
                    grantWhenPremium = nav.grantWhenPremium,
                    rewardCallback = nav.rewardCallback
                )

                is Navigation.Ad.Banner -> adsPremiumManager.requestBanner(
                    activity = activity,
                    container = nav.container
                )

                else -> super.navigate(activity, nav)
            }
        }
    }
}