package lab.smartbanner.renderer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import lab.smartbanner.model.BannerElement
import lab.smartbanner.model.ImageElement
import lab.smartbanner.model.PosterTemplate
import lab.smartbanner.model.TextElement
import lab.smartbanner.ui.components.DynamicBanner
import lab.smartbanner.ui.components.DynamicImage
import lab.smartbanner.ui.components.DynamicText
import lab.smartbanner.ui.components.PosterCanvas

/**
 * The core engine that renders a complete [PosterTemplate].
 * It handles the coordinate system, scaling, and layering of all elements.
 */
@Composable
fun PosterRenderer(
    template: PosterTemplate,
    modifier: Modifier = Modifier
) {
    PosterCanvas(
        template = template,
        modifier = modifier
    ) { scale ->
        // Layering: Elements with higher zIndex are drawn on top
        template.elements.sortedBy { it.zIndex }.forEach { element ->
            when (element) {
                is TextElement -> {
                    DynamicText(
                        element = element,
                        scale = scale
                    )
                }
                is ImageElement -> {
                    DynamicImage(
                        element = element,
                        scale = scale
                    )
                }
                is BannerElement -> {
                    DynamicBanner(
                        element = element,
                        scale = scale
                    )
                }
            }
        }
    }
}
