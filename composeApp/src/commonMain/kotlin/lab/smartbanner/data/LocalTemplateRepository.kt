package lab.smartbanner.data

import kotlinx.serialization.json.Json
import lab.smartbanner.Res
import lab.smartbanner.domain.TemplateRepository
import lab.smartbanner.model.PosterTemplate
import org.jetbrains.compose.resources.ExperimentalResourceApi

class LocalTemplateRepository : TemplateRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    // jewellery_3 is now a theme of jewellery_2
    private val templatePaths = listOf(
        "files/templates/jewellery_1.json",
        "files/templates/jewellery_2.json",
        "files/templates/festival_1.json",
        "files/templates/clothing_1.json",
        "files/templates/grocery_1.json",
        "files/templates/coaching_1.json"
    )

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun getTemplates(): List<PosterTemplate> {
        val loadedTemplates = templatePaths.mapNotNull { path ->
            try {
                val bytes = Res.readBytes(path)
                val jsonString = bytes.decodeToString()
                json.decodeFromString<PosterTemplate>(jsonString)
            } catch (e: Exception) {
                println("Error loading template $path: ${e.message}")
                null
            }
        }.toMutableList()

        if (loadedTemplates.isEmpty()) {
            loadedTemplates.add(
                PosterTemplate(
                    id = "default_fallback",
                    name = "Default Template",
                    category = "General"
                )
            )
        }

        return loadedTemplates
    }

    override suspend fun getTemplateById(id: String): PosterTemplate? {
        return getTemplates().find { it.id == id }
    }
}
