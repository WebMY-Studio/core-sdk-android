package com.webmy.core_sdk.presentation.base.viewpager

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

abstract class BaseViewPager2Adapter(activity: AppCompatActivity) :
    FragmentStateAdapter(activity) {

    abstract val fragments: List<Fragment>

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}