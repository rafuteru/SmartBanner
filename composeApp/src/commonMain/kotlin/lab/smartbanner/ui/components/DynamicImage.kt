package lab.smartbanner.ui.components

import androidx.compose.foundation.background
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

    SubcomposeAsyncImage(
        model = element.imageUrl,
        contentDescription = null,
        modifier = modifier
            .offset(
                x = (element.x * scale).dp,
                y = (element.y * scale).dp
            )
            .size(width, height)
            .clip(RoundedCornerShape((element.cornerRadius * scale).dp)),
        contentScale = ContentScale.Crop,
        loading = {
            // Placeholder shown while loading
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Loading",
                    tint = Color.Gray.copy(alpha = 0.5f)
                )
            }
        },
        error = {
            // Placeholder shown on error
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Error",
                    tint = Color.Red.copy(alpha = 0.2f)
                )
            }
        }
    )
}
