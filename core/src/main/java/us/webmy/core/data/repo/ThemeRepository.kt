package us.webmy.core.data.repo

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import us.webmy.core.data.prefs.ThemePreferences
import us.webmy.core.domain.model.ThemeId

interface ThemeRepository {
    fun observeSelected(): Flow<ThemeId>
    fun select(id: ThemeId)
}

class ThemeRepositoryImpl(
    private val prefs: ThemePreferences,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) : ThemeRepository {

    override fun observeSelected(): Flow<ThemeId> = prefs.flow().flowOn(io)

    override fun select(id: ThemeId) {
        prefs.setValue(id)
    }
}
