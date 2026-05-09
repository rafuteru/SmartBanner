package lab.smartbanner.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import lab.smartbanner.domain.DraftRepository
import lab.smartbanner.domain.PosterDraft
import lab.smartbanner.model.PosterContent

class DataStoreDraftRepository(
    private val dataStore: DataStore<Preferences>
) : DraftRepository {

    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val LATEST_DRAFT_TEMPLATE_ID = stringPreferencesKey("latest_draft_template_id")
        val LATEST_DRAFT_CONTENT = stringPreferencesKey("latest_draft_content")
    }

    override fun getLatestDraft(): Flow<PosterDraft?> {
        return dataStore.data.map { preferences ->
            val templateId = preferences[Keys.LATEST_DRAFT_TEMPLATE_ID]
            val contentJson = preferences[Keys.LATEST_DRAFT_CONTENT]
            
            if (templateId != null && contentJson != null) {
                try {
                    val content = json.decodeFromString<PosterContent>(contentJson)
                    PosterDraft(templateId, content)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }

    override suspend fun saveDraft(draft: PosterDraft) {
        dataStore.edit { preferences ->
            preferences[Keys.LATEST_DRAFT_TEMPLATE_ID] = draft.templateId
            preferences[Keys.LATEST_DRAFT_CONTENT] = json.encodeToString(PosterContent.serializer(), draft.content)
        }
    }

    override suspend fun clearDraft() {
        dataStore.edit { preferences ->
            preferences.remove(Keys.LATEST_DRAFT_TEMPLATE_ID)
            preferences.remove(Keys.LATEST_DRAFT_CONTENT)
        }
    }
}
