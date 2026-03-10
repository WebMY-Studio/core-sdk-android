package us.webmy.coresdkdemo

import android.app.Activity
import android.os.Bundle
import us.webmy.core_sdk.presentation.views.AppButton

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<AppButton>(R.id.button).setOnClickListener {

        }
    }
}
