package us.webmy.core.monetization.ads.domain

import android.widget.FrameLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import us.webmy.core.monetization.ads.tools.ads.AdsManager
import us.webmy.core.monetization.billing.domain.interactor.PremiumUseCase
import us.webmy.core.monetization.billing.domain.interactor.isPremium
import java.util.concurrent.atomic.AtomicLong

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

internal class RealDisplayAdUseCase(
    private val adsManager: AdsManager,
    private val premiumUseCase: PremiumUseCase,
    private val throttleConfigProvider: suspend () -> InterstitialThrottleConfig,
) : DisplayAdUseCase {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val interCount = AtomicLong(0L)

    @Volatile
    private var throttleConfig: InterstitialThrottleConfig? = null

    override fun showBanner(container: FrameLayout) {
        scope.launch {
            if (premiumUseCase.isPremium()) {
                adsManager.hideBanner(container)
            } else {
                adsManager.showBanner(container)
            }
        }
    }

    override fun hideBanner(container: FrameLayout) {
        adsManager.hideBanner(container)
    }

    override fun showInterstitial(source: String?) {
        scope.launch {
            if (premiumUseCase.isPremium()) return@launch
            val cfg = throttleConfig ?: throttleConfigProvider().also { throttleConfig = it }
            val count = interCount.incrementAndGet()
            if (count < cfg.firstSkipAdsAmount) return@launch
            val offset = count - cfg.firstSkipAdsAmount
            if (offset % cfg.skipAdsAmount == 0L) {
                adsManager.showInter(source = source)
            }
        }
    }

    override fun showReward(
        placement: String?,
        source: String?,
        grantWhenPremium: Boolean,
        onResult: (Boolean) -> Unit,
    ) {
        scope.launch {
            if (premiumUseCase.isPremium()) {
                if (grantWhenPremium) onResult(true)
                return@launch
            }
            adsManager.showReward(
                source = source,
                placement = placement,
                rewardCallback = onResult,
            )
        }
    }
}
