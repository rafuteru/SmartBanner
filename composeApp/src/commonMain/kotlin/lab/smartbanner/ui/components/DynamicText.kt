package lab.smartbanner.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
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

    Box(
        modifier = modifier
            .offset(
                x = (element.x * scale).dp,
                y = (element.y * scale).dp
            )
            .width((element.width * scale).dp)
            .height((element.height * scale).dp),
        contentAlignment = when (element.verticalAlignment) {
            "TOP" -> when (element.textAlign) {
                "CENTER" -> Alignment.TopCenter
                "END" -> Alignment.TopEnd
                else -> Alignment.TopStart
            }
            "BOTTOM" -> when (element.textAlign) {
                "CENTER" -> Alignment.BottomCenter
                "END" -> Alignment.BottomEnd
                else -> Alignment.BottomStart
            }
            else -> when (element.textAlign) {
                "CENTER" -> Alignment.Center
                "END" -> Alignment.CenterEnd
                else -> Alignment.CenterStart
            }
        }
    ) {
        val baseStyle = TextStyle(
            fontSize = (element.fontSize * scale).sp,
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
        )

        // Draw Stroke if defined (typically for the Shop Name)
        if (element.strokeWidth > 0 && element.strokeColor != null) {
            Text(
                text = textToShow,
                style = baseStyle.copy(
                    color = element.strokeColor.toColor(),
                    drawStyle = Stroke(
                        width = element.strokeWidth * scale,
                        join = StrokeJoin.Round
                    )
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

        // Draw Fill
        Text(
            text = textToShow,
            style = baseStyle.copy(color = textColor),
            maxLines = element.maxLines ?: Int.MAX_VALUE,
            overflow = when (element.overflow) {
                "ELLIPSIS" -> TextOverflow.Ellipsis
                "VISIBLE" -> TextOverflow.Visible
                else -> TextOverflow.Clip
            },
            softWrap = true
        )
    }
}
