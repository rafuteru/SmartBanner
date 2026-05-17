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

@Composable
fun PosterRenderer(
    template: PosterTemplate,
    modifier: Modifier = Modifier,
    content: PosterContent = PosterContent()
) {
    // Resolve background color
    val backgroundColor = template.background.colorKey?.let { key ->
        content.colorMap[key]
    } ?: template.background.contentKey?.let { key ->
        content.colorMap[key]
    } ?: template.background.color

    PosterCanvas(
        template = template.copy(
            background = template.background.copy(color = backgroundColor)
        ),
        modifier = modifier
    ) { scale ->
        template.elements.sortedBy { it.zIndex }.forEach { element ->
            when (element) {
                is TextElement -> {
                    val displayText = element.contentKey?.let { key ->
                        content.textMap[key]
                    } ?: element.text

                    val displayColor = element.colorKey?.let { key ->
                        content.colorMap[key]
                    } ?: element.color

                    val displayStrokeColor = element.strokeColorKey?.let { key ->
                        content.colorMap[key]
                    } ?: element.strokeColor

                    DynamicText(
                        element = element.copy(
                            text = displayText, 
                            color = displayColor,
                            strokeColor = displayStrokeColor
                        ),
                        scale = scale
                    )
                }
                is ImageElement -> {
                    val displayUrl = element.contentKey?.let { key ->
                        content.imageMap[key]
                    } ?: element.imageUrl

                    val displayBorderColor = element.borderColorKey?.let { key ->
                        content.colorMap[key]
                    } ?: element.borderColor

                    DynamicImage(
                        element = element.copy(
                            imageUrl = displayUrl,
                            borderColor = displayBorderColor
                        ),
                        scale = scale
                    )
                }
                is BannerElement -> {
                    val displayColor = element.colorKey?.let { key ->
                        content.colorMap[key]
                    } ?: element.color

                    val displayBorderColor = element.borderColorKey?.let { key ->
                        content.colorMap[key]
                    } ?: element.borderColor

                    DynamicBanner(
                        element = element.copy(
                            color = displayColor,
                            borderColor = displayBorderColor
                        ),
                        scale = scale
                    )
                }
            }
        }
    }
}
