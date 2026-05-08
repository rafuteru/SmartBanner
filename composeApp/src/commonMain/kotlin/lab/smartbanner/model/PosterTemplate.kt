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
    val height: Float = 1350f, // Standard 4:5 Portrait
    val background: BackgroundConfig = BackgroundConfig(),
    val elements: List<ElementConfig> = emptyList()
)

@Serializable
data class BackgroundConfig(
    val color: String = "#FFFFFF",
    val imageUrl: String? = null
)

@Serializable
sealed class ElementConfig {
    abstract val id: String
    abstract val x: Float
    abstract val y: Float
    abstract val width: Float
    abstract val height: Float
    abstract val zIndex: Int
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
    val text: String,
    val fontSize: Float = 24f,
    val color: String = "#000000",
    val fontWeight: String = "NORMAL", // NORMAL, BOLD
    val textAlign: String = "CENTER"   // START, CENTER, END
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
    val imageUrl: String,
    val cornerRadius: Float = 0f
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
    val color: String = "#000000",
    val cornerRadius: Float = 0f,
    val alpha: Float = 1.0f
) : ElementConfig()
