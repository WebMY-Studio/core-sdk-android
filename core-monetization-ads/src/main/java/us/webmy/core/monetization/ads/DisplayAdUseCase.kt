package us.webmy.core.monetization.ads

import android.widget.FrameLayout

class InterstitialThrottleConfig(
    val firstSkipAdsAmount: Long,
    val skipAdsAmount: Long,
) {
    companion object {
        val Default = InterstitialThrottleConfig(firstSkipAdsAmount = 1, skipAdsAmount = 1)
    }
}

interface DisplayAdUseCase {

    fun showBanner(container: FrameLayout)

    fun hideBanner(container: FrameLayout)

    fun showInterstitial(source: String? = null)

    fun showReward(
        placement: String? = null,
        source: String? = null,
        grantWhenPremium: Boolean = true,
        onResult: (Boolean) -> Unit,
    )
}
