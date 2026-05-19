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
        
        // 1. Fetch user specific template mappings (key and id)
        val userMappings = try {
            configRepository.getTemplateMappingsForUser(supportId)
        } catch (e: Exception) {
            emptyList()
        }
        val userTemplateIds = userMappings.map { it.id }.toSet()

        // 2. Fetch global template mappings
        val globalMappings = try {
            configRepository.getGlobalTemplateMappings()
        } catch (e: Exception) {
            emptyList()
        }

        // 3. Remove global mappings if the template ID is already present in user mappings
        // This ensures the user's specific version (marked as free) takes precedence.
        val filteredGlobalMappings = globalMappings.filter { it.id !in userTemplateIds }
        
        // Combine mappings: User specific ones first
        val allMappings = userMappings + filteredGlobalMappings
        
        val templates = mutableListOf<PosterTemplate>()

        allMappings.forEach { mapping ->
            try {
                // Fetch JSON using the 'key'
                val templateJson = configRepository.getTemplateJson(mapping.key)
                if (!templateJson.isNullOrBlank()) {
                    val template = json.decodeFromString<PosterTemplate>(templateJson)
                    
                    // If it's a user-owned template (by ID), it's always free for them
                    val finalTemplate = if (mapping.id in userTemplateIds) {
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
        // Note: For fetching a single template, we still go through the logic to ensure correct free/premium status
        return getTemplates().find { it.id == id }
    }

    override suspend fun refresh(): Boolean {
        return configRepository.refresh()
    }
}
