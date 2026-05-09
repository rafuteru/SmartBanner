package lab.smartbanner.domain

import kotlinx.coroutines.flow.Flow
import lab.smartbanner.model.PosterContent

data class PosterDraft(
    val templateId: String,
    val content: PosterContent
)

interface DraftRepository {
    fun getLatestDraft(): Flow<PosterDraft?>
    suspend fun saveDraft(draft: PosterDraft)
    suspend fun clearDraft()
}
