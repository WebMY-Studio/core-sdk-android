package us.webmy.core_sdk_ads.navigator

import androidx.appcompat.app.AppCompatActivity
import us.webmy.core_sdk.presentation.base.navigator.Navigation
import us.webmy.core_sdk_ads.tools.ads.AdsPremiumManager
import us.webmy.core_sdk_extended.navigator.BaseNavigatorExtended
import us.webmy.core_sdk_extended.tools.billing.BillingManager

abstract class BaseNavigatorAds(
    billingManager: BillingManager,
    private val adsPremiumManager: AdsPremiumManager,
) : BaseNavigatorExtended(billingManager) {

    override fun navigate(activity: AppCompatActivity, nav: Navigation) {
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

            else -> super.navigate(activity, nav)
        }
    }
}