package com.webmy.core_sdk.presentation

import android.R
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
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

const val DURATION_DEFAULT_GONE = 150L
const val DURATION_DEFAULT_VISIBLE = 300L


fun View.gone() {
    this.visibility = View.GONE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.setVisibleAlpha(flag: Boolean) {
    if (flag) visibleWithAlpha() else goneWithAlpha()
}


fun View.visibleWithAlpha(duration: Long = DURATION_DEFAULT_VISIBLE) {
    if (this.visibility != View.VISIBLE) {
        this.alpha = 0f
        this.visible()
        this.animate().alpha(1f).setDuration(duration).start()
    }
}

fun View.goneWithAlpha(duration: Long = DURATION_DEFAULT_GONE) {
    if (this.visibility == View.VISIBLE) {
        this.alpha = 1f
        this.animate().alpha(0f).setDuration(duration).start()
        Handler(Looper.getMainLooper()).postDelayed({ this.gone() }, duration)
    }
}

fun TextView.setTextAnimation(
    textRes: Int, duration: Long = 300, completion: (() -> Unit)? = null
) {
    val newText = context.getString(textRes)
    if (this.text == newText) return
    fadeOutAnimation(duration) {
        this.text = newText
        fadeInAnimation(duration) {
            completion?.invoke()
        }
    }
}

fun View.fadeOutAnimation(
    duration: Long = 150,
    visibility: Int = View.INVISIBLE,
    completion: (() -> Unit)? = null
) {
    animate()
        .alpha(0f)
        .setDuration(duration)
        .withEndAction {
            this.visibility = visibility
            completion?.let {
                it()
            }
        }
}

fun View.fadeInAnimation(duration: Long = 150, completion: (() -> Unit)? = null) {
    alpha = 0f
    visibility = View.VISIBLE
    animate()
        .alpha(1f)
        .setDuration(duration)
        .withEndAction {
            completion?.let {
                it()
            }
        }
}

fun View.setHeight(height: Int) {
    val viewParams = this.layoutParams as ConstraintLayout.LayoutParams
    viewParams.height = height
    this.layoutParams = viewParams
}


fun View.setOnClickListenerOutlined(onClick: () -> Unit) {
    setOnClickListener { onClick() }
    clipToOutline = true
}

fun View.showKeyboard() {
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}