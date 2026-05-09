package lab.smartbanner.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lab.smartbanner.domain.DraftRepository
import lab.smartbanner.domain.PosterDraft
import lab.smartbanner.model.PosterContent

class DataStoreDraftRepository(
    private val dataStore: DataStore<Preferences>
) : DraftRepository {

    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val LATEST_DRAFT = stringPreferencesKey("latest_draft")
        // Template specific content keys will be generated dynamically: "content_templateId"
    }

    override fun getLatestDraft(): Flow<PosterDraft?> {
        return dataStore.data.map { preferences ->
            val draftJson = preferences[Keys.LATEST_DRAFT]
            if (draftJson != null) {
                try {
                    json.decodeFromString<PosterDraft>(draftJson)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }

    override fun getSavedContent(templateId: String): Flow<PosterContent?> {
        val key = stringPreferencesKey("content_$templateId")
        return dataStore.data.map { preferences ->
            val contentJson = preferences[key]
            if (contentJson != null) {
                try {
                    json.decodeFromString<PosterContent>(contentJson)
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
            preferences[Keys.LATEST_DRAFT] = json.encodeToString(draft)
        }
    }

    override suspend fun clearActiveDraft() {
        dataStore.edit { preferences ->
            preferences.remove(Keys.LATEST_DRAFT)
        }
    }

    override suspend fun saveTemplateContent(templateId: String, content: PosterContent) {
        val key = stringPreferencesKey("content_$templateId")
        dataStore.edit { preferences ->
            preferences[key] = json.encodeToString(content)
        }
    }
}
