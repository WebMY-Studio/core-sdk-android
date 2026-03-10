package us.webmy.core_sdk_compose.configs

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import us.webmy.core_sdk_compose.R

object WebmyFontFamilies {

    val poppins = FontFamily(
        Font(
            resId = R.font.poppins_regular,
            weight = FontWeight.Normal
        ),
        Font(
            resId = R.font.poppins_medium,
            weight = FontWeight.Medium
        ),
        Font(
            resId = R.font.poppins_semibold,
            weight = FontWeight.SemiBold
        ),
        Font(
            resId = R.font.poppins_bold,
            weight = FontWeight.Bold
        )
    )
}
