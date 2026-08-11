package us.webmy.core.monetization.ads

import android.widget.FrameLayout
import us.webmy.core.util.ActivityProvider

/**
 * Wraps Appodeal SDK with analytics + logging on all callbacks.
 *
 * Activity is pulled from [ActivityProvider]; callers (composables, ViewModels,
 * [DisplayAdUseCase][us.webmy.core.monetization.ads.DisplayAdUseCase]) do not pass it.
 */
interface AdsManager {

    fun init()

    fun showBanner(container: FrameLayout): Boolean

    fun hideBanner(container: FrameLayout)

    fun showReward(
        source: String? = null,
        placement: String? = null,
        rewardCallback: (Boolean) -> Unit,
    )

    fun showInter(source: String? = null)

    fun destroy()
}
