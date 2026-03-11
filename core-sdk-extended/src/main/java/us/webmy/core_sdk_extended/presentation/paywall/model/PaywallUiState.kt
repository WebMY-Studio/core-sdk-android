package us.webmy.core_sdk_extended.presentation.paywall.model

import android.content.Context
import us.webmy.core_sdk.R
import us.webmy.core_sdk_extended.presentation.paywall.adapter.SubscriptionsUiModel

class PaywallUiState(
    val plans: List<SubscriptionsUiModel>,
    val selectedPlanId: String
) {
    fun getFormattedButtonText(context: Context): String? {
        val priceMicros = plans.map { it.subscription }
            .find { it.id == selectedPlanId }
            ?.phases
            ?.firstOrNull()
            ?.priceMicros
            ?: return null


        val hasFreeTrial = priceMicros == 0L
        val res = if (hasFreeTrial) {
            R.string.paywall_btn_text_free
        } else {
            R.string.paywall_btn_text_continue
        }

        return context.getString(res)
    }
}
