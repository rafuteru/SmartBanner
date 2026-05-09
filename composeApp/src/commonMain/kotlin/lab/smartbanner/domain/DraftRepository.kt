package lab.smartbanner.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import lab.smartbanner.model.PosterContent

@Serializable
data class PosterDraft(
    val templateId: String,
    val content: PosterContent
)

interface DraftRepository {
    fun getLatestDraft(): Flow<PosterDraft?>
    fun getSavedContent(templateId: String): Flow<PosterContent?>
    suspend fun saveDraft(draft: PosterDraft)
    suspend fun clearActiveDraft()
    suspend fun saveTemplateContent(templateId: String, content: PosterContent)
}
