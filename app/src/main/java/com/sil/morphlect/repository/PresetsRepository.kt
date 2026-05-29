package com.sil.morphlect.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sil.morphlect.data.Preset
import com.sil.morphlect.enums.Filter
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.ifEmpty

class PresetsRepository(private val context: Context) {
    companion object {
        val PRESETS_KEY = stringPreferencesKey("presets")
    }

    var loadedPresets = emptyList<Preset>()

    private val defaults = listOf(
        Preset("Vintage", mapOf(
            Filter.Brightness to .2,
            Filter.Contrast to -.1,
            Filter.Hue to .3
        )),
        Preset("Vibrant", mapOf(
            Filter.Brightness to .3,
            Filter.Contrast to .2,
            Filter.Hue to .5
        )),
    )

    suspend fun load(): List<Preset> {
        return try {
            val prefs = context.dataStore.data.first()
            val json = prefs[PRESETS_KEY]
            val array = JSONArray(json)
            var idx = 0
            var currentObj = array.opt(0) as JSONObject?
            val list = mutableListOf<Preset>()
            while (currentObj != null) {
                list += Preset.fromJSON(currentObj)
                currentObj = array.opt(++idx) as JSONObject?
            }
            list.also { loadedPresets = it }
        } catch (e: Exception) {
            Log.e("Presets error", "Unable to load presets: ${e.stackTraceToString()}")
            defaults
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

    suspend fun addPreset(name: String, params: Map<Filter, Double>) {
        save(loadedPresets.ifEmpty { load() } + Preset(name, params))
    }

    suspend fun addPreset(preset: Preset) {
        save(loadedPresets.ifEmpty { load() } + preset)
    }

    suspend fun removePreset(name: String) {
        save(loadedPresets.ifEmpty { load() }.filter { it.name != name })
    }

    suspend fun loadAll(): List<Preset> = defaults + loadedPresets.ifEmpty { load() }
}