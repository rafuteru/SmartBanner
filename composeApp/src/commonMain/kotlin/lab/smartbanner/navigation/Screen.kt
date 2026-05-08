package lab.smartbanner.navigation

import kotlinx.serialization.Serializable

sealed class Screen {
    @Serializable
    data object Home : Screen()
    
    @Serializable
    data object TemplateEditor : Screen() // Placeholder for future
}
