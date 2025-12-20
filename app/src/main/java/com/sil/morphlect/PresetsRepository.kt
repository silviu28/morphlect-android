package com.sil.morphlect

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sil.morphlect.enums.Effect
import kotlinx.coroutines.flow.first

class PresetsRepository(private val context: Context) {
    private val gson = Gson()
    private val PRESETS_KEY = stringPreferencesKey("presets")

    private val defaults = mapOf(
        "Vintage" to mapOf(
            Effect.Brightness to .2,
            Effect.Contrast to -.1,
            Effect.Hue to .3
        ),
        "Vibrant" to mapOf(
            Effect.Brightness to .3,
            Effect.Contrast to .2,
            Effect.Hue to .5
        )
    )

    suspend fun load(): Map<String, Map<Effect, Double>> {
        return try {
            val prefs = context.dataStore.data.first()
            val json = prefs[PRESETS_KEY]
            if (json != null) {
                val type = object : TypeToken<Map<String, Map<String, Double>>>() {}.type
                val jsonMap = gson.fromJson<Map<String, Map<String, Double>>>(json, type)
                val presetsMap = jsonMap.mapValues { (_, preset) ->
                    preset.mapKeys { (effectName, _) ->
                        Effect.valueOf(effectName)
                    }
                }
                val finalMap = defaults + presetsMap
                finalMap
            } else {
                defaults
            }
        } catch (e: Exception) {
            defaults
        }
    }

    suspend fun save(presets: Map<String, Map<Effect, Double>>) {
        try {
            val presetsMappedNames = presets.mapValues { (_, preset) ->
                preset.mapKeys { (effect, _) ->
                    effect.name
                }
            }
            val json = gson.toJson(presetsMappedNames)
            context.dataStore.edit { preferences ->
                preferences[PRESETS_KEY] = json
            }
        } catch (e: Exception) {
            Log.e("Presets error", "Unable to save presets: $e")
        }
    }

    suspend fun addPreset(name: String, preset: Map<Effect, Double>) {
        save(load() + (name to preset))
    }

    suspend fun removePreset(name: String) {
        save(load() - name)
    }
}