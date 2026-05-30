package us.webmy.core.monetization.ads.tools.ads

import android.widget.FrameLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import us.webmy.core.monetization.billing.tools.billing.BillingManager
import us.webmy.core.monetization.billing.tools.billing.containsPurchased
import java.util.concurrent.atomic.AtomicLong

interface AdsPremiumManager {

    interface Factory {
        fun create(configCreator: suspend () -> AdsPremiumConfig): AdsPremiumManager
    }

    val isPremiumFlow: Flow<Boolean>

    fun requestBanner(container: FrameLayout)

    fun requestReward(
        placement: String? = null,
        grantWhenPremium: Boolean = true,
        source: String? = null,
        rewardCallback: (Boolean) -> Unit,
    )

    fun requestInterstitial(source: String? = null)

    fun dispose()
}

internal class RealAdsPremiumManager(
    premiumProductIds: List<String>,
    billingManager: BillingManager,
    private val adsManager: AdsManager,
    val configCreator: suspend () -> AdsPremiumConfig,
) : AdsPremiumManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val currentTriggerInterCount = AtomicLong(0L)

    private var config: AdsPremiumConfig? = null

    override val isPremiumFlow = billingManager.subscribeProducts().map { products ->
        premiumProductIds.any { id -> products.containsPurchased(id) }
    }

    override fun requestBanner(container: FrameLayout) {
        scope.launch {
            val isPremium = isPremiumFlow.first()
            withContext(Dispatchers.Main) {
                if (isPremium) {
                    adsManager.hideBanner(container)
                } else {
                    adsManager.showBanner(container)
                }
            }
        }
    }

    override fun requestInterstitial(source: String?) {
        scope.launch {
            val isPremium = isPremiumFlow.first()
            if (isPremium) return@launch

            val cfg = getConfig()
            val firstSkipAdsAmount = cfg.firstSkipAdsAmount
            val skipAdsAmount = cfg.skipAdsAmount

            val count = currentTriggerInterCount.incrementAndGet()
            if (count < firstSkipAdsAmount) return@launch

            val countSinceInitial = count - firstSkipAdsAmount

            if (countSinceInitial % skipAdsAmount == 0L) {
                withContext(Dispatchers.Main) {
                    adsManager.showInter(source = source)
                }
            }
        }
    }

    override fun requestReward(
        placement: String?,
        grantWhenPremium: Boolean,
        source: String?,
        rewardCallback: (Boolean) -> Unit,
    ) {
        scope.launch {
            if (isPremiumFlow.first()) {
                if (grantWhenPremium) rewardCallback(true)
            } else {
                withContext(Dispatchers.Main) {
                    adsManager.showReward(
                        source = source,
                        placement = placement,
                        rewardCallback = rewardCallback,
                    )
                }
            }
        }
    }

    override fun dispose() {
        scope.cancel()
    }

    private suspend fun getConfig(): AdsPremiumConfig {
        val cached = config
        if (cached != null) return cached
        val loaded = configCreator()
        config = loaded
        return loaded
    }
}

internal class AdsPremiumManagerFactory(
    private val premiumProductIds: List<String>,
    private val billingManager: BillingManager,
    private val adsManager: AdsManager,
) : AdsPremiumManager.Factory {

    override fun create(configCreator: suspend () -> AdsPremiumConfig): AdsPremiumManager {
        return RealAdsPremiumManager(
            premiumProductIds,
            billingManager,
            adsManager,
            configCreator,
        )
    }
}
