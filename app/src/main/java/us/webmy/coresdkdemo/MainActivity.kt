package us.webmy.coresdkdemo

import androidx.fragment.app.Fragment
import us.webmy.core.ui.single.WebmyActivity

class MainActivity : WebmyActivity() {
    override fun createStartFragment(): Fragment = HomeFragment()
}
