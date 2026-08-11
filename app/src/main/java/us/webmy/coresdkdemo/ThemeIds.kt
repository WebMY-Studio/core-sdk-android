package us.webmy.coresdkdemo

import androidx.annotation.StringRes
import us.webmy.core.theme.BuildInThemeIds
import us.webmy.core.theme.ThemeId
import us.webmy.core.R as CoreR

object ThemeIds {
    const val ACCENT: ThemeId = "accent"
}

@StringRes
fun themeTitleRes(id: ThemeId): Int? = when (id) {
    BuildInThemeIds.DARK -> CoreR.string.webmy_theme_dark
    BuildInThemeIds.LIGHT -> CoreR.string.webmy_theme_light
    ThemeIds.ACCENT -> R.string.theme_accent
    else -> null
}
