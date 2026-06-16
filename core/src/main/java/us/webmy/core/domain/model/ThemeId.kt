package us.webmy.core.domain.model

import androidx.annotation.StringRes

typealias ThemeId = String

data class ThemeSpec(
    val id: ThemeId,
    val isDark: Boolean,
    @param:StringRes val nameRes: Int,
)