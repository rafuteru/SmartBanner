package lab.smartbanner.data

import kotlinx.serialization.json.Json
import lab.smartbanner.Res
import lab.smartbanner.domain.AccessCodeRepository
import lab.smartbanner.domain.ConfigRepository
import lab.smartbanner.domain.TemplateRepository
import lab.smartbanner.model.PosterTemplate
import org.jetbrains.compose.resources.ExperimentalResourceApi

class LocalTemplateRepository(
    private val authRepository: AccessCodeRepository,
    private val configRepository: ConfigRepository
) : TemplateRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val templatePaths = listOf(
        "files/templates/jewellery_1.json",
        "files/templates/festival_1.json",
        "files/templates/clothing_1.json",
        "files/templates/grocery_1.json",
        "files/templates/coaching_1.json",
//        "files/templates/testing.json",
//        "files/templates/premium_testing.json",
    )

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun getTemplates(): List<PosterTemplate> {
        // 1. Load local templates
        val templates = templatePaths.mapNotNull { path ->
            try {
                val bytes = Res.readBytes(path)
                val jsonString = bytes.decodeToString()
                json.decodeFromString<PosterTemplate>(jsonString)
            } catch (e: Exception) {
                null
            }
        }.toMutableList()

        // 2. Fetch and merge remote templates
        try {
            // Use persistent Support ID instead of transient User ID
            val supportId = authRepository.getAccessCode()
            
            val remoteLookupKeys = configRepository.getTemplateIdsForUser(supportId)
            remoteLookupKeys.forEach { templateKey ->
                try {
                    val templateJson = configRepository.getTemplateJson(templateKey)
                    if (!templateJson.isNullOrBlank()) {
                        val remoteTemplate = json.decodeFromString<PosterTemplate>(templateJson)
                        val existingIndex = templates.indexOfFirst { it.id == remoteTemplate.id }
                        if (existingIndex != -1) {
                            templates[existingIndex] = remoteTemplate
                        } else {
                            templates.add(remoteTemplate)
                        }
                    }
                } catch (e: Exception) {
                    // Skip templates that fail to parse
                }
            }
        } catch (e: Exception) {
            // Gracefully handle remote fetch failures
        }

        if (templates.isEmpty()) {
            templates.add(
                PosterTemplate(
                    id = "default_fallback",
                    name = "Default Template",
                    category = "General"
                )
            )
        }

        return templates
    }

    override suspend fun getTemplateById(id: String): PosterTemplate? {
        return getTemplates().find { it.id == id }
    }
}
