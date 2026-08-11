package us.webmy.core.internal.util

import android.app.Activity
import android.app.Application
import android.os.Bundle
import us.webmy.core.SdkError
import us.webmy.core.presentation.WebmyActivity
import us.webmy.core.util.ActivityProvider
import java.util.concurrent.atomic.AtomicReference

internal class RealActivityProvider(application: Application) : ActivityProvider {

    private val ref = AtomicReference<Activity?>(null)

    private var host: WebmyActivity? = null


    override val current: Activity? get() = ref.get()

    override fun bindHost(activity: WebmyActivity) {
        this.host = activity
    }

    override fun requireCurrent(): Activity = ref.get() ?: throw SdkError.NoForegroundActivity()

    override fun requireHost(): WebmyActivity = host ?: throw SdkError.NoForegroundActivity()

    init {
        application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                ref.set(activity)
            }

            override fun onActivityPaused(activity: Activity) {
                ref.compareAndSet(activity, null)
            }

            override fun onActivityDestroyed(activity: Activity) {
                ref.compareAndSet(activity, null)
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        })
    }
}
