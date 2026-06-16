package us.webmy.core.ui.compose.components.surface

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import us.webmy.core.ui.compose.theme.WebmyTheme

@Composable
fun WebmySurface(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {

    Box(
        modifier = modifier
            .background(WebmyTheme.colors.backgroundPrimary)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        content = content
    )
}