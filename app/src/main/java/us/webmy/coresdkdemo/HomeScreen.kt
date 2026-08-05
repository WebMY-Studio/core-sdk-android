package us.webmy.coresdkdemo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import us.webmy.core.ui.compose.components.button.WebmyButton
import us.webmy.core.ui.compose.components.surface.WebmySurface
import us.webmy.core.ui.presentation.base.navigator.Navigation
import us.webmy.core.ui.presentation.base.navigator.Router
import us.webmy.core.ui.presentation.base.navigator.screen

@Composable
fun HomeScreen() {
    val router: Router = koinInject()
    WebmySurface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("WebMY Demo — Home (Compose)")
            WebmyButton(
                text = "Open Browser",
                onClick = { router.go(Navigation.Browser("https://example.com")) },
            )
            WebmyButton(
                text = "Theme demo",
                onClick = { router.go(screen(ThemeDemoKey)) },
            )
        }
    }
}
