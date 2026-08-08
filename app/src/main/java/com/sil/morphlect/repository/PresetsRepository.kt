package com.sil.morphlect.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sil.morphlect.data.Preset
import com.sil.morphlect.enums.Filter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.ifEmpty

class PresetsRepository(private val context: Context) {
    companion object {
        val PRESETS_KEY = stringPreferencesKey("presets")
    }

    suspend fun load(): List<Preset> {
        return try {
            val prefs = context.dataStore.data.first()
            val json = prefs[PRESETS_KEY]
            val array = JSONArray(json)
            List(array.length()) { index ->
                Preset.fromJSON(array.getJSONObject(index))
            } // return
        } catch (e: Exception) {
            Log.e("Presets error", "Unable to load presets: ${e.stackTraceToString()}")
            emptyList() // return
        }
    }

    suspend fun save(presets: List<Preset>) {
        try {
            val dump = presets
                .map { it.toJSON() }
                .joinToString(prefix = "[", postfix = "]")
            context.dataStore.edit { prefs -> prefs[PRESETS_KEY] = dump }
        } catch (e: Exception) {
            Log.e("Presets error", "Unable to save presets: ${e.stackTraceToString()}")
        }
    }

    private val singleThreadContext = Dispatchers.IO.limitedParallelism(1)

    suspend fun addPreset(preset: Preset) = withContext(singleThreadContext) {
        save(load() + preset)
    }

    suspend fun removePreset(preset: Preset) = withContext(singleThreadContext) {
        save(load().filter { it != preset })
    }
}