package lab.smartbanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import lab.smartbanner.model.PosterTemplate
import lab.smartbanner.utils.toColor

/**
 * A responsive container for rendering posters with a fixed aspect ratio.
 * 
 * It calculates a scaling factor based on the [PosterTemplate.width] relative to the 
 * actual available width on the screen. This [scale] should be used by all child 
 * elements to ensure the layout remains identical across different screen sizes.
 *
 * @param template The poster configuration containing dimensions and background data.
 * @param modifier Modifier for the canvas container.
 * @param content The content of the poster, providing the calculated [scale].
 */
@Composable
fun PosterCanvas(
    template: PosterTemplate,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(scale: Float) -> Unit
) {
    // Calculate fixed aspect ratio (width / height)
    val aspectRatio = template.width / template.height

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .background(template.background.color.toColor())
            .clipToBounds()
    ) {
        // Calculate the scale: Actual Width in Dp / Reference Width in Template
        // This scale is passed down to children to multiply with their local dimensions.
        val scale = maxWidth.value / template.width
        
        content(scale)
    }
}
