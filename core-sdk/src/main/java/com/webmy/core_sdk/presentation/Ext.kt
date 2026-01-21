package com.webmy.core_sdk.presentation

import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan

fun SpannableString.addImage(drawable: Drawable, verticalAlignment: Int = ImageSpan.ALIGN_CENTER){
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    this.setSpan(
        ImageSpan(drawable, ImageSpan.ALIGN_CENTER),
        this.length - 1,
        this.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}