package us.webmy.core.ui.compose.components.spacer

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import us.webmy.core.ui.compose.configs.WebmySpacings
import us.webmy.core.ui.compose.theme.WebmyTheme

@Composable
inline fun VerticalSpacer(space: WebmySpacings.() -> Dp) {
    WebmyTheme.spacings.apply {
        Spacer(Modifier.height(space()))
    }
}

@Composable
inline fun HorizontalSpacer(space: WebmySpacings.() -> Dp) {
    WebmyTheme.spacings.apply {
        Spacer(Modifier.width(space()))
    }
}