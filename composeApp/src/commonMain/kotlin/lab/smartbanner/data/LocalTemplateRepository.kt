package lab.smartbanner.data

import kotlinx.serialization.json.Json
import lab.smartbanner.domain.AccessCodeRepository
import lab.smartbanner.domain.ConfigRepository
import lab.smartbanner.domain.TemplateRepository
import lab.smartbanner.model.PosterTemplate

class LocalTemplateRepository(
    private val authRepository: AccessCodeRepository,
    private val configRepository: ConfigRepository
) : TemplateRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override suspend fun getTemplates(): List<PosterTemplate> {
        val supportId = authRepository.getAccessCode()
        
        // 1. Fetch user specific template IDs
        val userTemplateIds = try {
            configRepository.getTemplateIdsForUser(supportId).toSet()
        } catch (e: Exception) {
            emptySet()
        }

        // 2. Fetch global template IDs from "premium_banner"
        val globalTemplateIds = try {
            configRepository.getGlobalTemplateIds()
        } catch (e: Exception) {
            emptyList()
        }

        // Combine all IDs (prioritize user specific ones in the raw list for processing)
        val allIds = (userTemplateIds + globalTemplateIds).distinct()
        
        val templates = mutableListOf<PosterTemplate>()

        allIds.forEach { templateId ->
            try {
                val templateJson = configRepository.getTemplateJson(templateId)
                if (!templateJson.isNullOrBlank()) {
                    val template = json.decodeFromString<PosterTemplate>(templateJson)
                    
                    // If it's a user-owned template, it's always free for them
                    val finalTemplate = if (templateId in userTemplateIds) {
                        template.copy(config = template.config.copy(isFree = true))
                    } else {
                        template
                    }
                    
                    templates.add(finalTemplate)
                }
            } catch (e: Exception) {
                // Skip failed ones
            }
        }

        // Prioritize: User templates first (determines category order), then by category
        return templates.sortedWith(
            compareByDescending<PosterTemplate> { it.id in userTemplateIds }
                .thenBy { it.category }
                .thenBy { it.name }
        )
    }

    override suspend fun getTemplateById(id: String): PosterTemplate? {
        return getTemplates().find { it.id == id }
    }

    override suspend fun refresh(): Boolean {
        return configRepository.refresh()
    }
}
