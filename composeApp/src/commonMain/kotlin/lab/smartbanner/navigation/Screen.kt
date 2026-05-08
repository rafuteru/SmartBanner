package lab.smartbanner.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Home : Screen()
    
    @Serializable
    data class TemplatePreview(val templateId: String) : Screen()
}
