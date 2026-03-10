package us.webmy.core_sdk_extended.presentation.paywall

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.lifecycle.viewModelScope
import us.webmy.core_sdk.R
import us.webmy.core_sdk.presentation.base.navigator.BaseNavigator
import us.webmy.core_sdk.tools.analytics.AnalyticsManager
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import us.webmy.core_sdk_extended.domain.interactor.PremiumInteractor
import us.webmy.core_sdk_extended.presentation.paywall.base.BasePaywallViewModel
import us.webmy.core_sdk_extended.presentation.paywall.model.OfferPaywallConfig
import us.webmy.core_sdk_extended.presentation.paywall.model.OfferUiState

abstract class BaseOfferPaywallViewModel(
    private val config: OfferPaywallConfig,
    private val navigator: BaseNavigator,
    premiumInteractor: PremiumInteractor,
    analyticsManager: AnalyticsManager
) : BasePaywallViewModel(
    navigator, premiumInteractor, analyticsManager
) {

    val offerUiStateFlow = subscriptionsFlow
        .map {
            val annual = it.find { it.id == config.basePlanId }
                ?.phases
                ?.firstOrNull()

            val annualPriceFormatted = annual?.formattedPrice

            val discount = it.find { it.id == config.offerPlanId }
                ?.phases
                ?.firstOrNull()

            val discountPriceFormatted = discount?.formattedPrice

            val context = navigator.activity

            val annualStyled = SpannableString(annualPriceFormatted).apply {
                setSpan(
                    StrikethroughSpan(),
                    0,
                    length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                val colorSpan = ForegroundColorSpan(
                    ContextCompat.getColor(context, R.color.textAndIconsSecondary)
                )
                setSpan(colorSpan, 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            val formattedPrice = buildSpannedString {
                append(context.getString(R.string.offer_just_for))
                append(" ")
                append(annualStyled)
                append(" ")
                append(discountPriceFormatted)
                append("/")
                append(context.getString(R.string.year))
            }

            val discountPercent =
                (100 - (discount?.priceMicros ?: 1L) * 100 / (annual?.priceMicros ?: 1L)).toInt()

            val discountFormatted = context
                .getString(R.string.offer_title, discountPercent.toString())

            OfferUiState(
                formattedPrice = formattedPrice,
                discountFormatted = discountFormatted
            )
        }

    fun onContinueClick() {
        viewModelScope.launch {
            purchase(config.offerPlanId)
        }
    }
}