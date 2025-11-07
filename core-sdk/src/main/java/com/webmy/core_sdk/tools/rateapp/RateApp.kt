package com.webmy.core_sdk.tools.rateapp

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory

fun Activity.showRateApp(onResult: (Boolean) -> Unit = {}) {
    val manager = ReviewManagerFactory.create(this)
    val request = manager.requestReviewFlow()
    request.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            manager.launchReviewFlow(this, task.result)
                .addOnCompleteListener { onResult.invoke(true) }
                .addOnCanceledListener { onResult.invoke(false) }
                .addOnFailureListener { onResult.invoke(false) }
        } else {
            onResult.invoke(false)
        }
    }
}
