package us.webmy.core.monetization.billing.presentation.paywall.model

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import us.webmy.core.R
import us.webmy.core.monetization.billing.tools.billing.Product

class OfferUiState(
    val annual: Product.Subscription.Phase,
    val discount: Product.Subscription.Phase,

    ) {
    fun getFormattedPrice(context: Context): SpannedString {
        val annualStyled = SpannableString(annual.formattedPrice).apply {
            setSpan(
                StrikethroughSpan(),
                0,
                length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            val colorSpan = ForegroundColorSpan(
                ContextCompat.getColor(context, us.webmy.core.R.color.textAndIconsSecondary)
            )
            setSpan(colorSpan, 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return buildSpannedString {
            append(context.getString(R.string.offer_just_for))
            append(" ")
            append(annualStyled)
            append(" ")
            append(discount.formattedPrice)
            append("/")
            append(context.getString(R.string.year))
        }
    }

    fun getFormattedDiscount(context: Context): String {
        val discountPercent = (100 - discount.priceMicros * 100 / annual.priceMicros).toInt()

        return context.getString(R.string.offer_title, discountPercent.toString())
    }
}