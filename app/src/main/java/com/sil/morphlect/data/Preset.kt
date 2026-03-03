package com.sil.morphlect.data

import com.sil.morphlect.enums.Filter
import kotlinx.serialization.Serializable
import org.json.JSONException
import org.json.JSONObject

@Serializable
data class Preset(val name: String, val params: Map<Filter, Double>) {
    companion object {
        fun fromJSON(json: JSONObject): Preset {
            val name = json.getString("name")
            val paramsData = json.getJSONObject("values")

            val params = Filter.entries.associate { it to paramsData.optDouble(it.name) }
            return Preset(name, params)
        }
    }

    fun toJSON(): JSONObject {
        try {
            return JSONObject().apply {
                put("name", name)
                put("values", JSONObject().apply {
                    params.forEach { (effect, value) ->
                        put(effect.name, value)
                    }
                })
            }
        } catch (_: JSONException) {
            throw JSONException("Preset file is of invalid format.")
        }
    }
}