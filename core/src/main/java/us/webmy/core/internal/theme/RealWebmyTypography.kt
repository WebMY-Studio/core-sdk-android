package us.webmy.core.internal.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.sp
import us.webmy.core.theme.WebmyFontFamilies
import us.webmy.core.theme.WebmyTypography

@Immutable
internal class RealWebmyTypography : WebmyTypography() {
    private val lineHeightStyle = LineHeightStyle(
        LineHeightStyle.Alignment.Center,
        LineHeightStyle.Trim.None
    )
    private val platformTextStyle = PlatformTextStyle(includeFontPadding = false)

    override val titleXXXXL = TextStyle(
        fontFamily = WebmyFontFamilies.poppins,
        fontSize = 80.sp,
        lineHeight = 88.sp,
        lineHeightStyle = lineHeightStyle,
        fontWeight = FontWeight.SemiBold,
        platformStyle = platformTextStyle
    )

    override val titleXXXL = TextStyle(
        fontFamily = WebmyFontFamilies.poppins,
        fontSize = 56.sp,
        lineHeight = 64.sp,
        lineHeightStyle = lineHeightStyle,
        fontWeight = FontWeight.SemiBold,
        platformStyle = platformTextStyle
    )

    override val titleXXL = TextStyle(
        fontFamily = WebmyFontFamilies.poppins,
        fontSize = 48.sp,
        lineHeight = 58.sp,
        lineHeightStyle = lineHeightStyle,
        fontWeight = FontWeight.SemiBold,
        platformStyle = platformTextStyle
    )

    override val titleXL = TextStyle(
        fontFamily = WebmyFontFamilies.poppins,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        lineHeightStyle = lineHeightStyle,
        fontWeight = FontWeight.SemiBold,
        platformStyle = platformTextStyle
    )

    override val titleL = TextStyle(
        fontFamily = WebmyFontFamilies.poppins,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        lineHeightStyle = lineHeightStyle,
        fontWeight = FontWeight.SemiBold,
        platformStyle = platformTextStyle
    )

    override val titleM = TextStyle(
        fontFamily = WebmyFontFamilies.poppins,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        lineHeightStyle = lineHeightStyle,
        fontWeight = FontWeight.SemiBold,
        platformStyle = platformTextStyle
    )

    override val titleSMedium = TextStyle(
        fontFamily = WebmyFontFamilies.poppins,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        lineHeightStyle = lineHeightStyle,
        fontWeight = FontWeight.Medium,
        platformStyle = platformTextStyle
    )

    override val titleSSemiBold = TextStyle(
        fontFamily = WebmyFontFamilies.poppins,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        lineHeightStyle = lineHeightStyle,
        fontWeight = FontWeight.SemiBold,
        platformStyle = platformTextStyle
    )

    override val headline = TextStyle(
        fontFamily = WebmyFontFamilies.poppins,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        lineHeightStyle = lineHeightStyle,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = TextUnit(0.02f, TextUnitType.Sp),
        platformStyle = platformTextStyle
    )

    override val bodyM = TextStyle(
        fontFamily = WebmyFontFamilies.poppins,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        lineHeightStyle = lineHeightStyle,
        fontWeight = FontWeight.Normal,
        platformStyle = platformTextStyle
    )

    override val bodyS = TextStyle(
        fontFamily = WebmyFontFamilies.poppins,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        lineHeightStyle = lineHeightStyle,
        fontWeight = FontWeight.Normal,
        platformStyle = platformTextStyle
    )

    override val bodySSemiBold = TextStyle(
        fontFamily = WebmyFontFamilies.poppins,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        lineHeightStyle = lineHeightStyle,
        fontWeight = FontWeight.SemiBold,
        platformStyle = platformTextStyle
    )

    override val caption1 = TextStyle(
        fontFamily = WebmyFontFamilies.poppins,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        lineHeightStyle = lineHeightStyle,
        fontWeight = FontWeight.Normal,
        platformStyle = platformTextStyle
    )

    override val caption1SemiBold = TextStyle(
        fontFamily = WebmyFontFamilies.poppins,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        lineHeightStyle = lineHeightStyle,
        fontWeight = FontWeight.SemiBold,
        platformStyle = platformTextStyle
    )

    override val caption2 = TextStyle(
        fontFamily = WebmyFontFamilies.poppins,
        fontSize = 10.sp,
        lineHeight = 16.sp,
        lineHeightStyle = lineHeightStyle,
        fontWeight = FontWeight.Normal,
        platformStyle = platformTextStyle
    )
}
