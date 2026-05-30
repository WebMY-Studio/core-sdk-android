package us.webmy.core.monetization.ads.navigator

import us.webmy.core.monetization.ads.tools.ads.AdsManager
import us.webmy.core.ui.presentation.base.navigator.AdNavigationHandler
import us.webmy.core.ui.presentation.base.navigator.Navigation

class AdsHandler(
    private val adsManager: AdsManager,
) : AdNavigationHandler {
    override fun handle(ad: Navigation.Ad): Result<Unit> = runCatching {
        when (ad) {
            is Navigation.Ad.Interstitial -> adsManager.showInter(ad.source)
            is Navigation.Ad.Reward -> adsManager.showReward(
                source = ad.source,
                placement = ad.placement,
                rewardCallback = ad.onResult,
            )
            is Navigation.Ad.Banner -> { adsManager.showBanner(ad.container); Unit }
        }
    }
}
