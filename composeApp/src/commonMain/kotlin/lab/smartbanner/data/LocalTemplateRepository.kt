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

        // Remove duplicates: If a template is in userTemplateIds, we don't need to process it from globalTemplateIds
        // because we want the user-specific "free" version to take precedence.
        val filteredGlobalIds = globalTemplateIds.filter { it !in userTemplateIds }
        
        // Combine IDs, putting user-specific ones first
        val allIds = userTemplateIds.toList() + filteredGlobalIds
        
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

        // Sort: User templates first, then by category and name
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
