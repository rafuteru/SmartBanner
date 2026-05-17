package lab.smartbanner.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Login : Screen()

    @Serializable
    data object Home : Screen()
    
    @Serializable
    data class TemplatePreview(val templateId: String) : Screen()

    @Serializable
    data class EditFields(val templateId: String) : Screen()

    @Serializable
    data class CreateTheme(val templateId: String, val themeId: String? = null) : Screen()
}
