package com.webmy.core_sdk.presentation.base.viewpager

import androidx.viewpager2.widget.ViewPager2

class PageChangedListener2(private val listener: (position: Int) -> Unit) :
    ViewPager2.OnPageChangeCallback() {

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        listener.invoke(position)
    }
}