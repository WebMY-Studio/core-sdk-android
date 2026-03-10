package us.webmy.coresdkdemo

import android.app.Application
import us.webmy.core_sdk.KoinMode
import us.webmy.core_sdk.WebMY
import us.webmy.core_sdk.WebMYConfig

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        WebMY.init(
            WebMYConfig(
                application = this,
                koinMode = KoinMode.START,
            )
        )
    }
}
