package com.sil.morphlect.data

import com.sil.morphlect.enums.Filter
import kotlinx.serialization.Serializable
import org.json.JSONObject

@Serializable
data class FingerprintData(
    val id: String,
    val minorAdjustments: Map<Filter, Double>,
    val preferHigherRatings: Boolean,
    val meanPreferenceRating: Double,
) {
    companion object {
        fun fromJSON(json: JSONObject): FingerprintData {
            val adjustments = with(json) {
                val obj = getJSONObject("minorAdjustments")
                Filter.values().associate { it to obj.getDouble(it.name) }
            }
            return FingerprintData(
                json.getString("id"),
                adjustments,
                json.getBoolean("preferHigherRatings"),
                json.getDouble("meanPreferenceRating")
            )
        }
    }

    fun toJSON(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("minorAdjustments", minorAdjustments)
            put("preferHigherRatings", preferHigherRatings)
            put("meanPreferenceRating", meanPreferenceRating)
        }
    }
}
