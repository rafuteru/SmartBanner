package lab.smartbanner.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import lab.smartbanner.domain.ConfigRepository
import kotlin.time.Duration.Companion.seconds

class FirebaseConfigRepository(private val isDebug: Boolean) : ConfigRepository {
    private val config = Firebase.remoteConfig
    private val json = Json { ignoreUnknownKeys = true }

    init {
        CoroutineScope(Dispatchers.Main + SupervisorJob()).launch {
            runCatching {
                config.settings { minimumFetchInterval = if (isDebug) 0.seconds else 3600.seconds }
                config.fetchAndActivate()
            }
        }
    }

    override fun getTemplateIdsForUser(userId: String): List<String> {
        // Use the Firebase UID as the lookup key. 
        // In Remote Config, you would create a key like: templates_YOUR_USER_ID
        val raw = config.getValue("templates_$userId").asString()
        return runCatching { json.decodeFromString<List<String>>(raw) }.getOrDefault(emptyList())
    }

    override suspend fun getTemplateJson(templateId: String): String? {
        val raw = config.getValue(templateId).asString().trim()
        if (raw.isEmpty()) return null
        
        // Resolve alias: if 'raw' is a key to another value, use that. Otherwise use 'raw'.
        val resolved = config.getValue(raw).asString().trim()
        return if (resolved.isNotEmpty()) resolved else raw
    }
}
