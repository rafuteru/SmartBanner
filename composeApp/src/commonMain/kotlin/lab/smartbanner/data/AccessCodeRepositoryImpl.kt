package lab.smartbanner.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import lab.smartbanner.domain.AccessCodeRepository
import lab.smartbanner.domain.AuthState

class AccessCodeRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : AccessCodeRepository {

    private object Keys {
        val IS_AUTHENTICATED = booleanPreferencesKey("is_authenticated")
        val ACCESS_CODE = stringPreferencesKey("access_code")
        val HAS_SEEN_INITIAL_DIALOG = booleanPreferencesKey("has_seen_initial_dialog")
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val authenticated = dataStore.data.map { it[Keys.IS_AUTHENTICATED] ?: false }.first()
            if (authenticated) {
                _authState.value = AuthState.Authenticated
            }
        }
    }

    override suspend fun isAuthenticated(): Boolean {
        return dataStore.data.map { it[Keys.IS_AUTHENTICATED] ?: false }.first()
    }

    override suspend fun signInWithCode(code: String) {
        _authState.value = AuthState.Loading
        
        try {
            dataStore.edit { preferences ->
                preferences[Keys.IS_AUTHENTICATED] = true
                preferences[Keys.ACCESS_CODE] = code
            }
            _authState.value = AuthState.Authenticated
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Failed to save access code: ${e.message}")
        }
    }

    override suspend fun signOut() {
        dataStore.edit { preferences ->
            preferences[Keys.IS_AUTHENTICATED] = false
            preferences.remove(Keys.ACCESS_CODE)
            preferences.remove(Keys.HAS_SEEN_INITIAL_DIALOG)
        }
        _authState.value = AuthState.Idle
    }

    override suspend fun getAccessCode(): String {
        return dataStore.data.map { it[Keys.ACCESS_CODE] ?: "" }.first()
    }

    override suspend fun hasSeenInitialDialog(): Boolean {
        return dataStore.data.map { it[Keys.HAS_SEEN_INITIAL_DIALOG] ?: false }.first()
    }

    override suspend fun setHasSeenInitialDialog(seen: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.HAS_SEEN_INITIAL_DIALOG] = seen
        }
    }
}
