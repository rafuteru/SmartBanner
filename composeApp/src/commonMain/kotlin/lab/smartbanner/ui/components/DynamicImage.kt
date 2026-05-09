package lab.smartbanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            .background(Color(0xFFEEEEEE))
    ) {
        SubcomposeAsyncImage(
            model = element.imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            loading = {
                ImagePlaceholder(scale)
            },
            error = {
                ImagePlaceholder(scale)
            }
        )
    }
}

@Composable
private fun ImagePlaceholder(scale: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.AddPhotoAlternate,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size((32 * scale).dp)
            )
            if (scale > 0.4f) {
                Text(
                    "IMAGE",
                    fontSize = (10 * scale).sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = (4 * scale).dp)
                )
            }
        }
    }
}
