package lab.smartbanner.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a complete poster template configuration.
 * Templates are JSON-driven and designed to be rendered dynamically on a canvas.
 *
 * @property id Unique identifier for the template.
 * @property name Display name of the template.
 * @property category Template category (e.g., Jewellery, Grocery, Festival).
 * @property previewUrl URL or path to the preview image of this template.
 * @property width Reference width of the poster used for scaling calculations.
 * @property height Reference height of the poster.
 * @property background Configuration for the poster's background layer.
 * @property elements List of dynamic elements (text, images, banners) to be rendered.
 */
@Serializable
data class PosterTemplate(
    val id: String,
    val name: String,
    val category: String,
    val previewUrl: String? = null,
    val width: Float = 1080f,
    val height: Float = 1350f, // Standard 4:5 Portrait
    val background: BackgroundConfig = BackgroundConfig(),
    val elements: List<ElementConfig> = emptyList()
)

/**
 * Configuration for the poster background.
 */
@Serializable
data class BackgroundConfig(
    val color: String = "#FFFFFF",
    val imageUrl: String? = null
)

/**
 * Base configuration for all renderable elements on the poster canvas.
 * All positions and dimensions are relative to the [PosterTemplate.width] and [PosterTemplate.height].
 */
@Serializable
sealed class ElementConfig {
    abstract val id: String
    abstract val x: Float
    abstract val y: Float
    abstract val width: Float
    abstract val height: Float
    abstract val zIndex: Int
}

/**
 * Configuration for rendering text elements.
 */
@Serializable
@SerialName("text")
data class TextElement(
    override val id: String,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val zIndex: Int = 0,
    val text: String,
    val fontSize: Float = 24f,
    val color: String = "#000000",
    val fontWeight: String = "NORMAL", // NORMAL, BOLD
    val textAlign: String = "CENTER"   // START, CENTER, END
) : ElementConfig()

/**
 * Configuration for rendering image elements.
 */
@Serializable
@SerialName("image")
data class ImageElement(
    override val id: String,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val zIndex: Int = 0,
    val imageUrl: String,
    val cornerRadius: Float = 0f
) : ElementConfig()

/**
 * Configuration for rendering solid color banners or shapes.
 */
@Serializable
@SerialName("banner")
data class BannerElement(
    override val id: String,
    override val x: Float,
    override val y: Float,
    override val width: Float,
    override val height: Float,
    override val zIndex: Int = 0,
    val color: String = "#000000",
    val cornerRadius: Float = 0f,
    val alpha: Float = 1.0f
) : ElementConfig()
