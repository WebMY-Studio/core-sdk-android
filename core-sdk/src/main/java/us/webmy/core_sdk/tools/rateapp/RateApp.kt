package us.webmy.core_sdk.tools.rateapp

import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory
import us.webmy.core_sdk.util.coerceToUnit
import us.webmy.core_sdk.util.executeSuspend

suspend fun Context.showRateApp(): Result<Unit> {
    return ReviewManagerFactory.create(this)
        .requestReviewFlow()
        .executeSuspend()
        .coerceToUnit()
}
