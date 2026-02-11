package com.webmy.core_sdk.presentation.views

import android.content.Context
import android.text.SpannableString
import android.util.AttributeSet
import android.view.Gravity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import com.webmy.core_sdk.R
import com.webmy.core_sdk.presentation.addImageToEnd
import com.webmy.core_sdk.presentation.addImageToStart
import com.webmy.core_sdk.presentation.applyRippleEffect
import com.webmy.core_sdk.presentation.dpToPx
import com.webmy.core_sdk.presentation.getEnum
import com.webmy.core_sdk.presentation.withStyledAttributes

class AppButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    enum class ButtonType {
        PRIMARY,
        SECONDARY,
        PREMIUM
    }

    private var buttonType: ButtonType = ButtonType.PRIMARY

    init {
        setTextAppearance(context, R.style.Webmy_Text_Body1_Medium)
        applyRippleEffect(borderless = false)

        gravity = Gravity.CENTER
        minHeight = 52.dpToPx()

        context.withStyledAttributes(attrs, R.styleable.AppButton) { typedArray ->
            val buttonType = typedArray.getEnum(
                attrIndex = R.styleable.AppButton_buttonType,
                defaultValue = ButtonType.PRIMARY
            )
            setButtonType(buttonType)
        }
    }

    private fun updateButtonStyle() {
        when (buttonType) {
            ButtonType.PRIMARY -> {
                setBackgroundResource(R.drawable.bg_button_primary)
                setTextColor(context.getColor(R.color.textAndIconsPrimaryInverse))
            }

            ButtonType.SECONDARY -> {
                setBackgroundResource(R.drawable.bg_button_secondary)
                setTextColor(context.getColor(R.color.textAndIconsPrimary))
            }

            ButtonType.PREMIUM -> {
                setBackgroundResource(R.drawable.bg_button_premium)
                setTextColor(context.getColor(R.color.textAndIconsPrimaryInverse))
            }
        }
    }

    fun setButtonType(buttonType: ButtonType) {
        this.buttonType = buttonType
        updateButtonStyle()
    }

    fun setupWith(@StringRes textResId: Int, @DrawableRes imageReIds: Int, iconAlign: IconAlign) {
        val splitter = "   "
        val prefix = if (iconAlign == IconAlign.START) splitter else ""
        val postfix = if (iconAlign == IconAlign.END) splitter else ""
        val btnText = prefix
            .plus(context.getString(textResId))
            .plus(postfix)
            .toSpannable() as SpannableString
        val drawable = ContextCompat.getDrawable(context, imageReIds)

        drawable?.let {
            when (iconAlign) {
                IconAlign.START -> btnText.addImageToStart(drawable)
                IconAlign.END -> btnText.addImageToEnd(drawable)
            }
        }
        text = btnText
    }

    enum class IconAlign {
        START, END
    }

}