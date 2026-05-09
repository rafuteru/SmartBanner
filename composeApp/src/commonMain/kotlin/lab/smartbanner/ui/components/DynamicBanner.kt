package lab.smartbanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import lab.smartbanner.model.BannerElement
import lab.smartbanner.utils.toColor

/**
 * Renders a banner or shape element dynamically on the poster canvas.
 * Supports background colors, alpha, rounded corners, and borders.
 */
@Composable
fun DynamicBanner(
    element: BannerElement,
    scale: Float,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape((element.cornerRadius * scale).dp)
    
    Box(
        modifier = modifier
            .offset(
                x = (element.x * scale).dp,
                y = (element.y * scale).dp
            )
            .size(
                width = (element.width * scale).dp,
                height = (element.height * scale).dp
            )
            .alpha(element.alpha)
            .then(
                if (element.borderWidth > 0 && element.borderColor != null) {
                    Modifier.border(
                        width = (element.borderWidth * scale).dp,
                        color = element.borderColor.toColor(),
                        shape = shape
                    )
                } else {
                    Modifier
                }
            )
            .clip(shape)
            .background(element.color.toColor())
    )
}
