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
import us.webmy.core.ui.presentation.base.fragment.BaseComposeFragment
import us.webmy.core.ui.presentation.base.navigator.Navigation
import us.webmy.core.ui.presentation.base.navigator.Navigator
import us.webmy.core.ui.presentation.base.navigator.screen

class HomeFragment : BaseComposeFragment() {

    @Composable
    override fun ScreenContent() {
        val navigator: Navigator = koinInject()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("WebMY Demo — Home (Compose)")
            WebmyButton(
                text = "Open Settings (XML) with args",
                onClick = {
                    navigator.go(
                        screen<SettingsFragment>(
                            SettingsArgs(userId = "42", title = "Hello from Home"),
                        )
                    )
                },
            )
            WebmyButton(
                text = "Open Browser",
                onClick = { navigator.go(Navigation.Browser("https://example.com")) },
            )
        }
    }
}
