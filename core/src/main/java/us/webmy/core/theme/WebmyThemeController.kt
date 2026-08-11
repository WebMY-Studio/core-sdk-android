package us.webmy.core.theme

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import us.webmy.core.internal.theme.ThemeRepository
import us.webmy.core.theme.BuildInThemeIds
import us.webmy.core.theme.ThemeId
import us.webmy.core.theme.ColorsPalette

class WebmyThemeController internal constructor(
    private val repository: ThemeRepository,
    palettes: List<ThemePalette>,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {

    private val byId: Map<ThemeId, ThemePalette> = palettes.associateBy { it.id }

    private val fallback: ThemePalette = byId[BuildInThemeIds.DEFAULT] ?: palettes.first()

    val themes: List<ThemePalette> = palettes

    val theme: StateFlow<ThemeId> =
        repository.observeSelected()
            .stateIn(scope, SharingStarted.Eagerly, fallback.id)

    fun get(id: ThemeId): ThemePalette = byId[id] ?: fallback

    fun palette(id: ThemeId): ColorsPalette = get(id).palette

    fun isDark(id: ThemeId): Boolean = get(id).isDark

    fun select(id: ThemeId) = repository.select(id)
}
