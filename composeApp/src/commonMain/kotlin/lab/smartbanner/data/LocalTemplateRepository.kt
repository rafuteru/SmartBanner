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

    private val templatePaths = listOf(
        "templates/jewellery_1.json",
        "templates/festival_1.json",
        "templates/clothing_1.json",
        "templates/grocery_1.json",
        "templates/coaching_1.json"
    )

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun getTemplates(): List<PosterTemplate> {
        return templatePaths.mapNotNull { path ->
            try {
                // By default, Res is in lab.smartbanner.generated.resources
                val bytes = Res.readBytes(path)
                val jsonString = bytes.decodeToString()
                json.decodeFromString<PosterTemplate>(jsonString)
            } catch (e: Exception) {
                println("Error loading template $path: ${e.message}")
                null
            }
        }
    }

    override suspend fun getTemplateById(id: String): PosterTemplate? {
        return getTemplates().find { it.id == id }
    }
}
