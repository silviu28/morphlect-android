package com.sil.morphlect.data

import com.sil.morphlect.enums.Effect
import kotlinx.serialization.Serializable
import org.json.JSONException
import org.json.JSONObject

@Serializable
data class Preset(val name: String, val params: Map<Effect, Double>) {
    companion object {
        fun fromJSON(json: JSONObject): Preset {
            val name = json.getString("name")
            val paramsData = json.getJSONObject("values")

            val params = Effect.entries.associate { e ->
                try {
                    e to paramsData.getDouble(e.name)
                } catch (_: JSONException) {
                    e to 0.0
                }
            }
            return Preset(name, params)
        }
    }

    fun toJSON(): JSONObject {
        return JSONObject().apply {
            put("name", name)
            put("values", JSONObject().apply {
                params.forEach { (effect, value) ->
                    put(effect.name, value)
                }
            })
        }
    }
}