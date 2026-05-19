package lab.smartbanner.data

import kotlinx.serialization.json.Json
import lab.smartbanner.Res
import lab.smartbanner.domain.AccessCodeRepository
import lab.smartbanner.domain.ConfigRepository
import lab.smartbanner.domain.TemplateRepository
import lab.smartbanner.model.PosterTemplate
import lab.smartbanner.model.TemplateConfig
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

        // 2. Fetch and merge remote templates (Owned templates)
        val supportId = authRepository.getAccessCode()
        val ownedTemplateIds = try {
            configRepository.getTemplateIdsForUser(supportId).toSet()
        } catch (e: Exception) {
            emptySet()
        }

        ownedTemplateIds.forEach { templateKey ->
            try {
                val templateJson = configRepository.getTemplateJson(templateKey)
                if (!templateJson.isNullOrBlank()) {
                    val remoteTemplate = json.decodeFromString<PosterTemplate>(templateJson)
                    // Mark as free since the user owns it
                    val unlockedTemplate = remoteTemplate.copy(config = remoteTemplate.config.copy(isFree = true))
                    
                    val existingIndex = templates.indexOfFirst { it.id == unlockedTemplate.id }
                    if (existingIndex != -1) {
                        templates[existingIndex] = unlockedTemplate
                    } else {
                        templates.add(unlockedTemplate)
                    }
                }
            } catch (e: Exception) {
                // Skip templates that fail to parse
            }
        }

        // 3. Fetch premium templates (Paid templates)
        try {
            val premiumTemplateIds = configRepository.getPremiumTemplateIds()
            // Avoid duplicates: If already owned, don't show in premium/locked list
            val filteredPremiumIds = premiumTemplateIds.filter { it !in ownedTemplateIds }

            filteredPremiumIds.forEach { templateKey ->
                try {
                    val templateJson = configRepository.getTemplateJson(templateKey)
                    if (!templateJson.isNullOrBlank()) {
                        val remoteTemplate = json.decodeFromString<PosterTemplate>(templateJson)
                        // Mark as not free and ensure category is "Premium"
                        val premiumTemplate = remoteTemplate.copy(
                            category = "Premium",
                            config = remoteTemplate.config.copy(isFree = false)
                        )
                        
                        val existingIndex = templates.indexOfFirst { it.id == premiumTemplate.id }
                        if (existingIndex == -1) {
                            templates.add(premiumTemplate)
                        }
                    }
                } catch (e: Exception) {
                    // Skip
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
                    category = "General",
                    config = TemplateConfig(isFree = true)
                )
            )
        }

        return templates
    }

    override suspend fun getTemplateById(id: String): PosterTemplate? {
        // For individual template fetch, we might want to ensure the lock state is correct
        // However, getTemplates() already handles the merging logic.
        return getTemplates().find { it.id == id }
    }
}
