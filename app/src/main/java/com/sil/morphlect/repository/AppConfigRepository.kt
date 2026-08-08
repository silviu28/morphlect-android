package com.sil.morphlect.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore(name = "app_config")
class AppConfigRepository {
    private val context: Context
    private val migrationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val NO_USE_MSG = "This feature is scrapped. Mentions remain to migrate existing DataStore sessions."
        val ADVANCED_MODE = booleanPreferencesKey("advanced_mode")
        val HIDE_PRIMARY_BAR = booleanPreferencesKey("hide_primary_bar")
        @Deprecated(NO_USE_MSG)
        val LOCAL_SMART_FEATURES = booleanPreferencesKey("local_smart")

        @Deprecated(NO_USE_MSG)
        val CLOUD_COMPUTE = booleanPreferencesKey("cloud_compute")

        val DEVELOPER_MODE = booleanPreferencesKey("developer_mode")
    }

    constructor(context: Context) {
        this.context = context
        // remove dangling keys
        migrationScope.launch { executeDataStoreMigration() }
    }

    private suspend fun executeDataStoreMigration() {
        context.dataStore.edit {
            it.remove(LOCAL_SMART_FEATURES)
            it.remove(CLOUD_COMPUTE)
        }
    }

    val advancedMode: Flow<Boolean> get() = context.dataStore.data.map {
        prefs -> prefs[ADVANCED_MODE] ?: false
    }
    val hidePrimaryBar: Flow<Boolean> get() = context.dataStore.data.map {
            prefs -> prefs[HIDE_PRIMARY_BAR] ?: false
    }
    val developerMode: Flow<Boolean> get() = context.dataStore.data.map {
        prefs -> prefs[DEVELOPER_MODE] ?: false
    }

    suspend fun setAdvancedMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ADVANCED_MODE] = enabled
        }
    }

    suspend fun setHidePrimaryBar(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[HIDE_PRIMARY_BAR] = enabled
        }
    }

    suspend fun setDeveloperMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DEVELOPER_MODE] = enabled
        }
    }
}