package lab.smartbanner.utils

import androidx.compose.ui.graphics.Color

/**
 * Extension to convert hex string (e.g., "#FFFFFF" or "#FFFFFFFF") to Compose Color.
 */
fun String.toColor(): Color {
    val hex = this.removePrefix("#")
    return when (hex.length) {
        6 -> Color(
            red = hex.substring(0, 2).toInt(16) / 255f,
            green = hex.substring(2, 4).toInt(16) / 255f,
            blue = hex.substring(4, 6).toInt(16) / 255f,
            alpha = 1f
        )
        8 -> Color(
            red = hex.substring(2, 4).toInt(16) / 255f,
            green = hex.substring(4, 6).toInt(16) / 255f,
            blue = hex.substring(6, 8).toInt(16) / 255f,
            alpha = hex.substring(0, 2).toInt(16) / 255f
        )
        else -> Color.White
    }
}
