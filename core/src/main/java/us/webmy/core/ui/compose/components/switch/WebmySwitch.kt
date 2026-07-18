package us.webmy.core.ui.compose.components.switch

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import us.webmy.core.ui.compose.theme.WebmyTheme

@Composable
fun WebmySwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = WebmyTheme.colors
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = colors.backgroundPrimary,
            checkedTrackColor = colors.textAndIconsPrimary,
            checkedBorderColor = colors.textAndIconsPrimary,
            uncheckedThumbColor = colors.textAndIconsPrimary,
            uncheckedTrackColor = colors.fill12,
            uncheckedBorderColor = colors.fill12,
            disabledCheckedTrackColor = colors.textAndIconsPrimary.copy(alpha = 0.5f),
            disabledUncheckedTrackColor = colors.fill6,
        ),
    )
}
