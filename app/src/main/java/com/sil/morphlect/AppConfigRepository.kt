package com.sil.morphlect

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "app_config")
class AppConfigRepository(private val context: Context) {
    // companion object acts as the "static" initializer block in java
    companion object {
        val ADVANCED_MODE = booleanPreferencesKey("advanced_mode")
        val HIDE_PRIMARY_BAR = booleanPreferencesKey("hide_primary_bar")
        val LOCAL_SMART_FEATURES = booleanPreferencesKey("local_smart")
        val CLOUD_COMPUTE = booleanPreferencesKey("cloud_compute")
    }

    val advancedMode: Flow<Boolean> = context.dataStore.data.map {
        prefs -> prefs[ADVANCED_MODE] ?: false
    }
    val hidePrimaryBar: Flow<Boolean> = context.dataStore.data.map {
            prefs -> prefs[HIDE_PRIMARY_BAR] ?: false
    }
    val localSmartFeatures: Flow<Boolean> = context.dataStore.data.map {
            prefs -> prefs[LOCAL_SMART_FEATURES] ?: false
    }
    val cloudCompute: Flow<Boolean> = context.dataStore.data.map {
            prefs -> prefs[CLOUD_COMPUTE] ?: false
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
    suspend fun setLocalSmartFeatures(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[LOCAL_SMART_FEATURES] = enabled
        }
    }
    suspend fun setCloudCompute(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[CLOUD_COMPUTE] = enabled
        }
    }

}