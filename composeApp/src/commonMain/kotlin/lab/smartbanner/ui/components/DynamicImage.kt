package lab.smartbanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import lab.smartbanner.model.ImageElement
import lab.smartbanner.utils.toColor

/**
 * Renders an image element dynamically on the poster canvas.
 * Supports scaling, rounded corners, and Coil-based loading with placeholders.
 */
@Composable
fun DynamicImage(
    element: ImageElement,
    scale: Float,
    modifier: Modifier = Modifier
) {
    val width = (element.width * scale).dp
    val height = (element.height * scale).dp
    val shape = RoundedCornerShape((element.cornerRadius * scale).dp)

    Box(
        modifier = modifier
            .offset(
                x = (element.x * scale).dp,
                y = (element.y * scale).dp
            )
            .size(width, height)
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
    ) {
        SubcomposeAsyncImage(
            model = element.imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            loading = {
                ImagePlaceholder(color = Color.LightGray.copy(alpha = 0.3f))
            },
            error = {
                // Better error state - uses a theme-friendly color or just a subtle placeholder
                ImagePlaceholder(color = Color.Gray.copy(alpha = 0.1f))
            }
        )
    }
}

@Composable
private fun ImagePlaceholder(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = "Placeholder",
            tint = Color.Gray.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxSize(0.4f)
        )
    }
}
