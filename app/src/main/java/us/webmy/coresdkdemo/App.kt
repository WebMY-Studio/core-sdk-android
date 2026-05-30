package us.webmy.coresdkdemo

import android.app.Application
import us.webmy.core.KoinMode
import us.webmy.core.WebMY
import us.webmy.core.WebMYConfig
import us.webmy.core.ui.di.installUi
import us.webmy.coresdkdemo.di.appModule

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        WebMY.init(
            config = WebMYConfig(
                application = this,
                koinMode = KoinMode.START,
            ),
            extraModules = listOf(appModule),
        )
        WebMY.installUi()
    }
}
