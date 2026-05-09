package lab.smartbanner.ui.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lab.smartbanner.model.TextElement
import lab.smartbanner.utils.toColor

@Composable
fun DynamicText(
    element: TextElement,
    scale: Float,
    modifier: Modifier = Modifier,
    overriddenText: String? = null,
    overriddenColor: Color? = null
) {
    val textToShow = overriddenText ?: element.text
    val textColor = overriddenColor ?: element.color.toColor()

    Text(
        text = textToShow,
        modifier = modifier
            .offset(
                x = (element.x * scale).dp,
                y = (element.y * scale).dp
            )
            .width((element.width * scale).dp)
            .heightIn(max = (element.height * scale).dp),
        style = TextStyle(
            fontSize = (element.fontSize * scale).sp,
            color = textColor,
            fontWeight = when (element.fontWeight) {
                "THIN" -> FontWeight.Thin
                "LIGHT" -> FontWeight.Light
                "MEDIUM" -> FontWeight.Medium
                "BOLD" -> FontWeight.Bold
                "BLACK" -> FontWeight.Black
                else -> FontWeight.Normal
            },
            fontStyle = when (element.fontStyle) {
                "ITALIC" -> FontStyle.Italic
                else -> FontStyle.Normal
            },
            fontFamily = when (element.fontFamily) {
                "SERIF" -> FontFamily.Serif
                "MONOSPACE" -> FontFamily.Monospace
                else -> FontFamily.SansSerif
            },
            textAlign = when (element.textAlign) {
                "CENTER" -> TextAlign.Center
                "END" -> TextAlign.End
                "JUSTIFY" -> TextAlign.Justify
                else -> TextAlign.Start
            },
            textDecoration = when (element.textDecoration) {
                "UNDERLINE" -> TextDecoration.Underline
                "LINE_THROUGH" -> TextDecoration.LineThrough
                else -> TextDecoration.None
            },
            letterSpacing = (element.letterSpacing * scale).sp,
            lineHeight = (element.fontSize * scale * element.lineHeightMultiplier).sp,
        ),
        maxLines = element.maxLines ?: Int.MAX_VALUE,
        overflow = when (element.overflow) {
            "ELLIPSIS" -> TextOverflow.Ellipsis
            "VISIBLE" -> TextOverflow.Visible
            else -> TextOverflow.Clip
        },
        softWrap = true
    )
}
