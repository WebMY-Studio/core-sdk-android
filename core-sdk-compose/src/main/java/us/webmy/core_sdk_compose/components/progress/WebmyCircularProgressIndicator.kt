package us.webmy.core_sdk_compose.components.progress

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import us.webmy.core_sdk_compose.theme.WebmyTheme

@Composable
fun WebmyCircularProgressIndicator(
    modifier: Modifier = Modifier
) {
    CircularProgressIndicator(
        modifier = modifier,
        color = WebmyTheme.colors.textAndIconsPrimary,
        strokeWidth = 4.dp,
        trackColor = WebmyTheme.colors.textAndIconsInversePrimary.copy(alpha = 0.12f),
        strokeCap = StrokeCap.Round
    )
}
