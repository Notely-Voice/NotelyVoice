package com.module.notelycompose.dailyexport

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DailyExportRepository(
    private val dataStore: DataStore<Preferences>
) {

    private companion object {
        val KEY_ENABLED = booleanPreferencesKey("daily_export_enabled")
        val KEY_FOLDER_URI = stringPreferencesKey("daily_export_folder_uri")
        val KEY_HOUR = intPreferencesKey("daily_export_hour")
        val KEY_MINUTE = intPreferencesKey("daily_export_minute")
        val KEY_SCOPE = stringPreferencesKey("daily_export_scope")
        val KEY_KEEP_VERSIONS = booleanPreferencesKey("daily_export_keep_versions")
        val KEY_LAST_RUN_AT = longPreferencesKey("daily_export_last_run_at")
        val KEY_LAST_RUN_MESSAGE = stringPreferencesKey("daily_export_last_run_message")
        // "noteId=contentHash" pairs, one per line. Lets us detect changes without a database column.
        val KEY_EXPORTED_HASHES = stringPreferencesKey("daily_export_hashes")
    }

    fun getSettings(): Flow<DailyExportSettings> = dataStore.data.map { prefs ->
        val defaults = DailyExportSettings()
        DailyExportSettings(
            enabled = prefs[KEY_ENABLED] ?: defaults.enabled,
            folderUri = prefs[KEY_FOLDER_URI],
            hour = prefs[KEY_HOUR] ?: defaults.hour,
            minute = prefs[KEY_MINUTE] ?: defaults.minute,
            scope = prefs[KEY_SCOPE]
                ?.let { name -> DailyExportScope.entries.firstOrNull { it.name == name } }
                ?: defaults.scope,
            keepVersions = prefs[KEY_KEEP_VERSIONS] ?: defaults.keepVersions,
            lastRunAtMillis = prefs[KEY_LAST_RUN_AT],
            lastRunMessage = prefs[KEY_LAST_RUN_MESSAGE]
        )
    }

    suspend fun currentSettings(): DailyExportSettings = getSettings().first()

    suspend fun setEnabled(enabled: Boolean) = dataStore.edit { it[KEY_ENABLED] = enabled }

    suspend fun setFolderUri(uri: String) = dataStore.edit { it[KEY_FOLDER_URI] = uri }

    suspend fun setTime(hour: Int, minute: Int) = dataStore.edit {
        it[KEY_HOUR] = hour
        it[KEY_MINUTE] = minute
    }

    suspend fun setScope(scope: DailyExportScope) = dataStore.edit { it[KEY_SCOPE] = scope.name }

    suspend fun setKeepVersions(keep: Boolean) = dataStore.edit { it[KEY_KEEP_VERSIONS] = keep }

    suspend fun setLastRun(atMillis: Long, message: String) = dataStore.edit {
        it[KEY_LAST_RUN_AT] = atMillis
        it[KEY_LAST_RUN_MESSAGE] = message
    }

    suspend fun getExportedHashes(): Map<Long, Int> {
        val raw = dataStore.data.first()[KEY_EXPORTED_HASHES].orEmpty()
        return raw.lineSequence()
            .mapNotNull { line ->
                val parts = line.split('=')
                val id = parts.getOrNull(0)?.toLongOrNull()
                val hash = parts.getOrNull(1)?.toIntOrNull()
                if (id != null && hash != null) id to hash else null
            }
            .toMap()
    }

    suspend fun setExportedHashes(hashes: Map<Long, Int>) = dataStore.edit {
        it[KEY_EXPORTED_HASHES] = hashes.entries.joinToString("\n") { (id, hash) -> "$id=$hash" }
    }
}
