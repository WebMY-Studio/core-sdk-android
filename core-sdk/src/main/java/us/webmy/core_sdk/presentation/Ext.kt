package us.webmy.core_sdk.presentation

import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan

fun SpannableString.addImageToEnd(drawable: Drawable) {
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    this.setSpan(
        ImageSpan(drawable, ImageSpan.ALIGN_CENTER),
        this.length - 1,
        this.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}

fun SpannableString.addImageToStart(drawable: Drawable) {
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    this.setSpan(
        ImageSpan(drawable, ImageSpan.ALIGN_CENTER),
        0,
        1,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}