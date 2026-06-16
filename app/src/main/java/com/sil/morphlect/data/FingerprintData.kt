package com.sil.morphlect.data

import com.sil.morphlect.enums.Filter
import kotlinx.serialization.Serializable
import org.json.JSONException
import org.json.JSONObject

@Serializable
data class FingerprintData(
    val id: String,
    val minorAdjustments: Map<Filter, Double>,
    val preferHigherRatings: Boolean,
    val meanPreferenceRating: Double,
    val savedImageCount: UInt,
) {
    companion object {
        @Throws(JSONException::class)
        fun fromJSON(json: JSONObject): FingerprintData {
            val adjustments = with(json) {
                val obj = getJSONObject("minorAdjustments")
                Filter.entries.associate {
                    it to obj.optDouble(it.name).let { v -> if (!v.isNaN()) v else .0 } // why is it nan????
                }
            }

            return FingerprintData(
                json.getString("id"),
                adjustments,
                json.getBoolean("preferHigherRatings"),
                json.getDouble("meanPreferenceRating"),
                json.getLong("savedImageCount").toUInt()
            )
        }
    }

    @Throws(JSONException::class)
    fun toJSON(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("minorAdjustments", JSONObject().apply {
                minorAdjustments.forEach { (filter, value) ->
                    put(filter.name, value)
                }
            })
            put("preferHigherRatings", preferHigherRatings)
            put("meanPreferenceRating", meanPreferenceRating)
            put("savedImageCount", savedImageCount.toLong())
        }
    }
}
