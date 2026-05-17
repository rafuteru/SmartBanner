package lab.smartbanner.model

import kotlinx.serialization.Serializable

/**
 * Holds user-provided content for a poster, mapped by semantic keys.
 * This separates the template "blueprint" from the actual "data".
 */
@Serializable
data class PosterContent(
    val textMap: Map<String, String> = emptyMap(),
    val imageMap: Map<String, String> = emptyMap(),
    val colorMap: Map<String, String> = emptyMap(),
    val userThemes: List<PosterTheme> = emptyList(),
    val usageCount: Map<String, Int> = emptyMap() // Track how many times each content key was edited
)
