package us.webmy.coresdkdemo

import android.app.Application
import us.webmy.core.WebMY
import us.webmy.core.WebMYConfig
import us.webmy.core.installUi
import us.webmy.core.theme.ThemePalette

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        WebMY.init(WebMYConfig(application = this))
        WebMY.installUi(
            extraPalettes = listOf(
                ThemePalette(
                    id = ThemeIds.ACCENT,
                    isDark = true,
                    palette = AccentColorsPalette(),
                ),
            ),
        )
    }
}
