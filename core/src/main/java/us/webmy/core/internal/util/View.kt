package us.webmy.core.internal.util

import us.webmy.core.internal.InternalWebmyApi
import android.content.res.Resources

@InternalWebmyApi
fun Int.dpToPx(): Int {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}