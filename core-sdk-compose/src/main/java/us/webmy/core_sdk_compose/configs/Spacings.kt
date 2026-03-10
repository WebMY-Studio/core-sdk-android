package us.webmy.core_sdk_compose.configs

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import us.webmy.core_sdk_compose.utils.noLocalProvidedFor

internal val LocalWebmySpacings = staticCompositionLocalOf<WebmySpacings> {
    noLocalProvidedFor("WebmySpacings")
}

@Immutable
abstract class WebmySpacings {
    abstract val spacing1: Dp
    abstract val spacing2: Dp
    abstract val spacing4: Dp
    abstract val spacing6: Dp
    abstract val spacing8: Dp
    abstract val spacing10: Dp
    abstract val spacing12: Dp
    abstract val spacing14: Dp
    abstract val spacing16: Dp
    abstract val spacing20: Dp
    abstract val spacing24: Dp
    abstract val spacing28: Dp
    abstract val spacing32: Dp
    abstract val spacing36: Dp
    abstract val spacing40: Dp
    abstract val spacing44: Dp
    abstract val spacing48: Dp
    abstract val spacing56: Dp
    abstract val spacing62: Dp
    abstract val spacing64: Dp
}

internal class RealWebmySpacings : WebmySpacings() {
    override val spacing1: Dp = 1.dp
    override val spacing2: Dp = 2.dp
    override val spacing4: Dp = 4.dp
    override val spacing6: Dp = 6.dp
    override val spacing8: Dp = 8.dp
    override val spacing10: Dp = 10.dp
    override val spacing12: Dp = 12.dp
    override val spacing14: Dp = 14.dp
    override val spacing16: Dp = 16.dp
    override val spacing20: Dp = 20.dp
    override val spacing24: Dp = 24.dp
    override val spacing28: Dp = 28.dp
    override val spacing32: Dp = 32.dp
    override val spacing36: Dp = 36.dp
    override val spacing40: Dp = 40.dp
    override val spacing44: Dp = 44.dp
    override val spacing48: Dp = 48.dp
    override val spacing56: Dp = 56.dp
    override val spacing64: Dp = 64.dp
    override val spacing62: Dp = 62.dp
}
