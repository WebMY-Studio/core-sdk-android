package com.webmy.core_sdk.presentation.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import com.webmy.core_sdk.R
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
        SECONDARY
    }

    private var buttonType: ButtonType = ButtonType.PRIMARY

    init {
        setTextAppearance(context, R.style.SpeechToText_Text_Body1_Medium)
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
        }
    }

    fun setButtonType(buttonType: ButtonType) {
        this.buttonType = buttonType
        updateButtonStyle()
    }
}