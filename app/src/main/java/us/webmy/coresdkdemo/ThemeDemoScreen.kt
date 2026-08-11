package us.webmy.coresdkdemo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import us.webmy.core.WebMY
import us.webmy.core.components.WebmyButton
import us.webmy.core.components.WebmySurface
import us.webmy.core.components.WebmySwitch
import us.webmy.core.theme.WebmyTheme
import us.webmy.core.theme.WebmyThemeController

@Composable
fun ThemeDemoScreen() {
    WebmySurface {
        ThemeDemoContent()
    }
}

@Composable
private fun ThemeDemoContent() {
    val colors = WebmyTheme.colors
    val controller = WebMY.theme
    val themeId by controller.theme.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundPrimary)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SectionTitle("Theme")
        controller.themes.forEach { theme ->
            val titleRes = themeTitleRes(theme.id)
            if (titleRes != null) {
                ThemeRow(
                    label = stringResource(titleRes),
                    selected = themeId == theme.id,
                    onClick = { controller.select(theme.id) },
                )
            }
        }

        SectionTitle("Text & icons")
        Text("textAndIconsPrimary", color = colors.textAndIconsPrimary)
        Text("textAndIconsSecondary", color = colors.textAndIconsSecondary)
        Text("textAndIconsTertiary", color = colors.textAndIconsTertiary)
        Text("textAndIconsDisabled", color = colors.textAndIconsDisabled)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(colors.backgroundInverse)
                .padding(8.dp),
        ) {
            Text("textAndIconsInversePrimary", color = colors.textAndIconsInversePrimary)
        }

        SectionTitle("Backgrounds")
        Swatch("backgroundSystem", colors.backgroundSystem)
        Swatch("backgroundPrimary", colors.backgroundPrimary)
        Swatch("backgroundSecondary", colors.backgroundSecondary)
        Swatch("backgroundTertiary", colors.backgroundTertiary)
        Swatch("backgroundInverse", colors.backgroundInverse)

        SectionTitle("Applied")
        Swatch("appliedOverlay", colors.appliedOverlay)
        Swatch("appliedHover", colors.appliedHover)
        Swatch("appliedButtonText", colors.appliedButtonText)
        Swatch("appliedStroke", colors.appliedStroke)
        Swatch("appliedSeparator", colors.appliedSeparator)
        Swatch("indicatorDisabled", colors.indicatorDisabled)

        SectionTitle("Fills")
        Swatch("fill2", colors.fill2)
        Swatch("fill6", colors.fill6)
        Swatch("fill8", colors.fill8)
        Swatch("fill12", colors.fill12)
        Swatch("fill18", colors.fill18)
        Swatch("fill24", colors.fill24)
        Swatch("fill30", colors.fill30)
        Swatch("fill48", colors.fill48)
        Swatch("fill70", colors.fill70)
        Swatch("fill100", colors.fill100)
        Swatch("fillDark30", colors.fillDark30)
        Swatch("fillDark45", colors.fillDark45)
        Swatch("fillDark66", colors.fillDark66)
        Swatch("fillDark100", colors.fillDark100)

        SectionTitle("Status")
        Swatch("success", colors.success)
        Swatch("error", colors.error)
        Swatch("warning", colors.warning)

        SectionTitle("Components")
        WebmyButton(text = "WebmyButton", onClick = {})
        var switchOn by remember { mutableStateOf(true) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            WebmySwitch(checked = switchOn, onCheckedChange = { switchOn = it })
            Text("WebmySwitch", color = colors.textAndIconsPrimary)
        }
    }
}

@Composable
private fun ThemeRow(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = WebmyTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) colors.fill8 else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = colors.textAndIconsPrimary,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Text("✓", color = colors.textAndIconsPrimary)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = WebmyTheme.colors.textAndIconsSecondary,
        modifier = Modifier.padding(top = 16.dp),
    )
}

@Composable
private fun Swatch(name: String, color: Color) {
    val colors = WebmyTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, colors.appliedStroke, RoundedCornerShape(8.dp))
                .background(color),
        )
        Text(name, color = colors.textAndIconsPrimary)
    }
}
