package lab.smartbanner.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/**
 * Creates a DataStore instance.
 * @param context The platform-specific context (e.g., Android Context).
 */
expect fun createDataStore(context: Any?): DataStore<Preferences>

internal const val DATASTORE_FILE_NAME = "posterwala.preferences_pb"
