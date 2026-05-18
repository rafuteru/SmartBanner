package lab.smartbanner.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PosterTemplate(
    val id: String,
    val name: String,
    val category: String,
    val previewUrl: String? = null,
    val width: Float = 1080f,
    val height: Float = 1350f,
    val background: BackgroundConfig = BackgroundConfig(),
    val elements: List<ElementConfig> = emptyList(),
    val themes: List<PosterTheme> = emptyList(),
    val config: TemplateConfig = TemplateConfig()
    ) {

    /**
     * Elements with their absolute coordinates resolved based on the 'below' property.
     */
    val resolvedElements: List<ElementConfig>
        get() {
            val resolved = mutableMapOf<String, ElementConfig>()
            return elements.map { element ->
                val finalElement = if (element.below == null) {
                    element
                } else {
                    val anchor = resolved[element.below]
                    val absoluteY = (anchor?.y ?: 0f) + (anchor?.height ?: 0f) + element.y
                    element.withY(absoluteY)
                }
                resolved[element.id] = finalElement
                finalElement
            }
        }

    /**
     * Calculates the height required to fit all elements, 
     * ensuring it's at least the base [height].
     */
    val intrinsicHeight: Float
        get() = resolvedElements.maxOfOrNull { it.y + it.height }?.let { maxOf(it, height) } ?: height
}

@Serializable
data class TemplateConfig(
    val isLocked: Boolean = true
)

@Serializable
data class PosterTheme(
    val id: String,
    val name: String,
    val colors: Map<String, String> // Map of colorKey to hex color
)

@Serializable
data class BackgroundConfig(
    val color: String = "#FFFFFF",
    val imageUrl: String? = null,
    val contentKey: String? = null,
    val colorKey: String? = null
)

@Serializable
sealed class ElementConfig {
    abstract val id: String
    abstract val x: Float
    abstract val y: Float
    abstract val width: Float
    abstract val height: Float
    abstract val zIndex: Int
    abstract val below: String?

    fun withY(newY: Float): ElementConfig = when (this) {
        is TextElement -> copy(y = newY)
        is ImageElement -> copy(y = newY)
        is BannerElement -> copy(y = newY)
    }
}

@Serializable
@SerialName("text")
data class TextElement(
    override val id: String,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val zIndex: Int = 0,
    override val below: String? = null,
    val text: String,
    val fontSize: Float = 24f,
    val color: String = "#000000",
    val fontWeight: String = "NORMAL", // THIN, LIGHT, NORMAL, MEDIUM, BOLD, BLACK
    val fontStyle: String = "NORMAL", // NORMAL, ITALIC
    val textAlign: String = "CENTER", // START, CENTER, END, JUSTIFY
    val verticalAlignment: String = "CENTER", // TOP, CENTER, BOTTOM
    val fontFamily: String = "SANS_SERIF", // SANS_SERIF, SERIF, MONOSPACE
    val textDecoration: String = "NONE", // NONE, UNDERLINE, LINE_THROUGH
    val letterSpacing: Float = 0f,
    val lineHeightMultiplier: Float = 1.2f,
    val maxLines: Int? = null,
    val overflow: String = "CLIP", // CLIP, ELLIPSIS, VISIBLE
    val strokeWidth: Float = 0f,
    val strokeColor: String? = null,
    val contentKey: String? = null,
    val colorKey: String? = null,
    val strokeColorKey: String? = null,
    val priority: Int = 0 // Higher priority fields shown first
) : ElementConfig()

@Serializable
@SerialName("image")
data class ImageElement(
    override val id: String,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val zIndex: Int = 0,
    override val below: String? = null,
    val imageUrl: String,
    val cornerRadius: Float = 0f,
    val borderWidth: Float = 0f,
    val borderColor: String? = null,
    val contentKey: String? = null,
    val borderColorKey: String? = null
) : ElementConfig()

@Serializable
@SerialName("banner")
data class BannerElement(
    override val id: String,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val zIndex: Int = 0,
    override val below: String? = null,
    val color: String = "#000000",
    val cornerRadius: Float = 0f,
    val alpha: Float = 1.0f,
    val borderWidth: Float = 0f,
    val borderColor: String? = null,
    val colorKey: String? = null,
    val borderColorKey: String? = null
) : ElementConfig()
