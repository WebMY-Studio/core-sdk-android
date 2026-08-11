package us.webmy.core

import us.webmy.core.internal.di.registerUiServices
import us.webmy.core.theme.ThemePalette

/**
 * Installs the SDK UI services (theming, navigation, sheets).
 * Call after [WebMY.init]. [ThemePalette.LIGHT] and [ThemePalette.DARK] are always
 * registered; pass [extraPalettes] to add custom ones.
 */
fun WebMY.installUi(extraPalettes: List<ThemePalette> = emptyList()) {
    registerUiServices(extraPalettes)
}
