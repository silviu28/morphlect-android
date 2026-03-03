package com.sil.morphlect.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sil.morphlect.data.Preset
import com.sil.morphlect.enums.Filter
import kotlinx.coroutines.flow.first

class PresetsRepository(private val context: Context) {
    private val gson = Gson()
    private val PRESETS_KEY = stringPreferencesKey("presets")

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
            if (json != null) {
                val type = object : TypeToken<Map<String, Map<String, Double>>>() {}.type
                val jsonMap = gson.fromJson<Map<String, Map<String, Double>>>(json, type)
                val presets = jsonMap.map { (name, preset) ->
                    Preset(name, preset.mapKeys { (effectName, _) -> Filter.valueOf(effectName) })
                }
                defaults + presets
            } else {
                defaults
            }
        } catch (e: Exception) {
            defaults
        }
    }

    suspend fun save(presets: List<Preset>) {
        try {
            val mapped = presets.associate { preset ->
                preset.name to preset.params.mapKeys { (effect, _) -> effect.name }
            }
            val json = gson.toJson(mapped)
            context.dataStore.edit { preferences ->
                preferences[PRESETS_KEY] = json
            }
        } catch (e: Exception) {
            Log.e("Presets error", "Unable to save presets: $e")
        }
    }

    suspend fun addPreset(name: String, params: Map<Filter, Double>) {
        save(load() + Preset(name, params))
    }

    suspend fun addPreset(preset: Preset) {
        save(load() + preset)
    }

    suspend fun removePreset(name: String) {
        save(load().filter { it.name != name })
    }
}