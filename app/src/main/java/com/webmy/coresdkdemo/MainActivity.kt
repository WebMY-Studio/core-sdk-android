package com.webmy.coresdkdemo

import android.app.Activity
import android.os.Bundle
import com.webmy.core_sdk.tools.ads.AdsManager
import com.webmy.core_sdk.tools.analytics.AnalyticsManager
import org.koin.android.ext.android.inject

class MainActivity : Activity() {

    private val analyticsManager: AnalyticsManager by inject()
    private val adsManager: AdsManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }
}
