package lab.smartbanner.domain

interface ConfigRepository {
    fun getTemplateIdsForUser(userId: String): List<String>
    fun getPremiumTemplateIds(): List<String>
    suspend fun getTemplateJson(templateId: String): String?
    suspend fun refresh(): Boolean
}
