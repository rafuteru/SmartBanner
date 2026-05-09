package lab.smartbanner.ui.components

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lab.smartbanner.model.TextElement
import lab.smartbanner.utils.toColor

@Composable
fun DynamicText(
    element: TextElement,
    scale: Float,
    modifier: Modifier = Modifier
) {
    Text(
        text = element.text,
        modifier = modifier
            .offset(
                x = (element.x * scale).dp,
                y = (element.y * scale).dp
            )
            .width((element.width * scale).dp),
        fontSize = (element.fontSize * scale).sp,
        color = element.color.toColor(),
        fontWeight = when (element.fontWeight) {
            "BOLD" -> FontWeight.Bold
            else -> FontWeight.Normal
        },
        textAlign = when (element.textAlign) {
            "CENTER" -> TextAlign.Center
            "END" -> TextAlign.End
            else -> TextAlign.Start
        },
        lineHeight = (element.fontSize * scale * 1.2f).sp,
        softWrap = true
    )
}
