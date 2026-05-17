package lab.smartbanner.domain

interface ConfigRepository {
    fun getTemplateIdsForUser(userId: String): List<String>
    suspend fun getTemplateJson(templateId: String): String?
    suspend fun refresh(): Boolean
}
