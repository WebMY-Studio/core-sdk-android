package us.webmy.core.ui.compose.components.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import us.webmy.core.ui.compose.components.text.WebmyText
import us.webmy.core.ui.compose.theme.WebmyTheme

@Composable
fun WebmyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonColor: Color = WebmyTheme.colors.textAndIconsPrimary,
    textColor: Color = WebmyTheme.colors.textAndIconsInversePrimary,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = textColor,
            disabledContainerColor = buttonColor.copy(alpha = 0.5f),
            disabledContentColor = WebmyTheme.colors.textAndIconsInversePrimary
        ),
        contentPadding = PaddingValues(
            horizontal = WebmyTheme.spacings.spacing20,
            vertical = WebmyTheme.spacings.spacing12,
        )
    ) {
        WebmyText(
            text = text,
            style = WebmyTheme.typography.bodyM,
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//private fun WebmyButtonPreview() {
//    WebmyTheme {
//        WebmyButton(
//            text = "Enabled",
//            onClick = {},
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//        )
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//private fun WebmyButtonDisabledPreview() {
//    WebmyTheme {
//        WebmyButton(
//            text = "Disabled",
//            onClick = {},
//            enabled = false,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//        )
//    }
//}