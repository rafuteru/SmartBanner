package lab.smartbanner.renderer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import lab.smartbanner.model.BannerElement
import lab.smartbanner.model.ImageElement
import lab.smartbanner.model.PosterContent
import lab.smartbanner.model.PosterTemplate
import lab.smartbanner.model.TextElement
import lab.smartbanner.ui.components.DynamicBanner
import lab.smartbanner.ui.components.DynamicImage
import lab.smartbanner.ui.components.DynamicText
import lab.smartbanner.ui.components.PosterCanvas

/**
 * The core engine that renders a complete [PosterTemplate].
 * It handles the coordinate system, scaling, and layering of all elements.
 * Supports dynamic content mapping via [PosterContent].
 */
@Composable
fun PosterRenderer(
    template: PosterTemplate,
    modifier: Modifier = Modifier,
    content: PosterContent = PosterContent()
) {
    PosterCanvas(
        template = template,
        modifier = modifier
    ) { scale ->
        // Layering: Elements with higher zIndex are drawn on top
        template.elements.sortedBy { it.zIndex }.forEach { element ->
            when (element) {
                is TextElement -> {
                    // Map content if contentKey is present, otherwise fallback to template default
                    val displayText = element.contentKey?.let { key ->
                        content.textMap[key]
                    } ?: element.text

                    DynamicText(
                        element = element.copy(text = displayText),
                        scale = scale
                    )
                }
                is ImageElement -> {
                    // Map image URL if contentKey is present, otherwise fallback to template default
                    val displayUrl = element.contentKey?.let { key ->
                        content.imageMap[key]
                    } ?: element.imageUrl

                    DynamicImage(
                        element = element.copy(imageUrl = displayUrl),
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
