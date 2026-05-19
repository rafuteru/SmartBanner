package lab.smartbanner.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import lab.smartbanner.domain.ConfigRepository
import lab.smartbanner.domain.TemplateMapping
import kotlin.time.Duration.Companion.seconds

class FirebaseConfigRepository(private val isDebug: Boolean) : ConfigRepository {
    private val config = Firebase.remoteConfig
    private val json = Json { ignoreUnknownKeys = true }

    init {
        CoroutineScope(Dispatchers.Main + SupervisorJob()).launch {
            refresh()
        }
    }

    override suspend fun refresh(): Boolean {
        return runCatching {
            config.settings { minimumFetchInterval = if (isDebug) 0.seconds else 3600.seconds }
            config.fetchAndActivate()
        }.isSuccess
    }

    override fun getTemplateMappingsForUser(userId: String): List<TemplateMapping> {
        val raw = config.getValue("templates_$userId").asString()
        return runCatching { json.decodeFromString<List<TemplateMapping>>(raw) }.getOrDefault(emptyList())
    }

    override fun getGlobalTemplateMappings(): List<TemplateMapping> {
        val raw = config.getValue("templates").asString()
        return runCatching { json.decodeFromString<List<TemplateMapping>>(raw) }.getOrDefault(emptyList())
    }

    override suspend fun getTemplateJson(key: String): String? {
        val raw = config.getValue(key).asString().trim()
        if (raw.isEmpty()) return null
        
        // Resolve alias: if 'raw' is a key to another value, use that. Otherwise use 'raw'.
        val resolved = config.getValue(raw).asString().trim()
        return resolved.ifEmpty { raw }
    }
}
