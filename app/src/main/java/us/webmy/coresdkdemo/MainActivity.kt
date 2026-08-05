package us.webmy.coresdkdemo

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import us.webmy.core.ui.single.WebmyActivity

class MainActivity : WebmyActivity() {

    override fun startScreen(): NavKey = HomeKey

    override fun EntryProviderScope<NavKey>.screens() {
        entry<HomeKey> { HomeScreen() }
        entry<ThemeDemoKey> { ThemeDemoScreen() }
    }
}
