package us.webmy.core.internal.theme

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import us.webmy.core.internal.theme.ThemePreferences
import us.webmy.core.theme.ThemeId

internal interface ThemeRepository {
    fun observeSelected(): Flow<ThemeId>
    fun select(id: ThemeId)
}

internal class ThemeRepositoryImpl(
    private val prefs: ThemePreferences,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) : ThemeRepository {

    override fun observeSelected(): Flow<ThemeId> = prefs.flow().flowOn(io)

    override fun select(id: ThemeId) {
        prefs.setValue(id)
    }
}
