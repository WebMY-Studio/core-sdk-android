package us.webmy.core.internal.theme

import androidx.compose.runtime.staticCompositionLocalOf
import us.webmy.core.theme.ColorsPalette
import us.webmy.core.theme.WebmySpacings
import us.webmy.core.theme.WebmyTypography

internal val LocalColorsPalette = staticCompositionLocalOf<ColorsPalette> {
    noLocalProvidedFor("ColorsPalette")
}

internal val LocalWebmyTypography = staticCompositionLocalOf<WebmyTypography> {
    noLocalProvidedFor("WebmyTypography")
}

internal val LocalWebmySpacings = staticCompositionLocalOf<WebmySpacings> {
    noLocalProvidedFor("WebmySpacings")
}
