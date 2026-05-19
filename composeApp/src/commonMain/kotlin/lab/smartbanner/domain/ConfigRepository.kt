package lab.smartbanner.domain

import kotlinx.serialization.Serializable

@Serializable
data class TemplateMapping(
    val key: String,
    val id: String
)

interface ConfigRepository {
    fun getTemplateMappingsForUser(userId: String): List<TemplateMapping>
    fun getGlobalTemplateMappings(): List<TemplateMapping>
    suspend fun getTemplateJson(key: String): String?
    suspend fun refresh(): Boolean
}
