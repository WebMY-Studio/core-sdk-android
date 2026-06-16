package us.webmy.core.ui.compose.theme

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import us.webmy.core.data.repo.ThemeRepository
import us.webmy.core.domain.model.ThemeId
import us.webmy.core.domain.model.ThemeSpec
import us.webmy.core.ui.compose.configs.colors.palettes.ColorsPalette

class WebmyThemeController(
    private val repository: ThemeRepository,
    themes: List<ThemePalette>,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {

    private val byId: Map<ThemeId, ThemePalette> = themes.associateBy { it.spec.id }

    private val fallback: ThemePalette = themes.first()

    val specs: List<ThemeSpec> = themes.map { it.spec }

    val theme: StateFlow<ThemeId> =
        repository.observeSelected()
            .stateIn(scope, SharingStarted.Eagerly, fallback.spec.id)

    fun palette(id: ThemeId): ColorsPalette = (byId[id] ?: fallback).palette

    fun spec(id: ThemeId): ThemeSpec = (byId[id] ?: fallback).spec

    fun select(id: ThemeId) = repository.select(id)
}
