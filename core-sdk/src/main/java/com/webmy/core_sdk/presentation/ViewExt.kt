package com.webmy.core_sdk.presentation

import android.R
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat

/**
 * Converts density-independent pixels (dp) to pixels (px).
 */
fun Int.dpToPx(): Int {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}

/**
 * Applies the ripple effect (selectableItemBackground) to the view as foreground.
 * This provides visual feedback when the view is clicked.
 */
fun View.applyRippleEffect(borderless: Boolean) {
    val attr = if (borderless) {
        R.attr.selectableItemBackgroundBorderless
    } else {
        R.attr.selectableItemBackground
    }
    val typedValue = TypedValue()
    context.theme.resolveAttribute(attr, typedValue, true)
    foreground = ContextCompat.getDrawable(context, typedValue.resourceId)
    clipToOutline = true
}

/**
 * Safely obtains styled attributes and executes the provided block.
 * The TypedArray is automatically recycled after the block execution.
 *
 * @param context The context to obtain styled attributes from
 * @param attrs The attribute set containing the attributes
 * @param styleable The styleable resource array (e.g., R.styleable.AppButton)
 * @param defStyleAttr An attribute in the current theme that contains a reference to a style resource
 * @param defStyleRes A resource identifier of a style resource that supplies default values
 * @param block The block to execute with the TypedArray
 */
inline fun <T> Context.withStyledAttributes(
    attrs: AttributeSet?,
    styleable: IntArray,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
    block: (TypedArray) -> T
): T? {
    if (attrs == null) return null

    val typedArray = obtainStyledAttributes(attrs, styleable, defStyleAttr, defStyleRes)
    return try {
        block(typedArray)
    } finally {
        typedArray.recycle()
    }
}

/**
 * Gets a text attribute from TypedArray.
 *
 * @param attrIndex The index of the attribute to retrieve
 * @param defaultValue The default value to return if the attribute is not set or is empty
 * @return The text value or null/defaultValue if not set
 */
fun TypedArray.getText(attrIndex: Int, defaultValue: String?): String? {
    val text = getString(attrIndex)
    return text?.takeIf { it.isNotEmpty() } ?: defaultValue
}

/**
 * Gets an enum attribute from TypedArray by converting the ordinal value.
 *
 * @param attrIndex The index of the attribute to retrieve
 * @param defaultValue The default enum value to return if the attribute is not set
 * @return The enum value corresponding to the ordinal, or defaultValue if not set
 */
inline fun <reified T : Enum<T>> TypedArray.getEnum(
    attrIndex: Int,
    defaultValue: T
): T {
    val ordinal = getInt(attrIndex, defaultValue.ordinal)
    val enumValues = enumValues<T>()
    return if (ordinal in enumValues.indices) {
        enumValues[ordinal]
    } else {
        defaultValue
    }
}

