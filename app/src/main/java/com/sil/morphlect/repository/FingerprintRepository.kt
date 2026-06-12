package com.sil.morphlect.repository

import android.content.Context
import android.util.Log
import com.sil.morphlect.data.FingerprintData
import com.sil.morphlect.enums.Filter
import com.sil.morphlect.enums.Output
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.UUID

class FingerprintRepository(context: Context) {
    val path = context.filesDir.toString() + "/fingerprint"

    companion object {
        const val INFLUENCE_WEIGHT = .3
    }

    suspend fun load(): FingerprintData = withContext(Dispatchers.IO) {
        val file = File(path + "fingerprint.json")
        if (!file.exists())
            return@withContext generateNew()

        try {
            val fp = file.bufferedReader().use { reader ->
                val json = JSONObject(reader.readText())
                FingerprintData.fromJSON(json)
            }
            fp
        } catch (e: JSONException) {
            Log.e("Fingerprint", e.stackTraceToString())
            generateNew()
        }
    }

    fun generateNew(): FingerprintData {
        val id = UUID.randomUUID().toString()
        return FingerprintData(
            id,
            minorAdjustments = Filter.entries.associate { it to .0 },
            preferHigherRatings = false,
            meanPreferenceRating = 50.0,
            savedImageCount = 0u
        )
    }

    suspend fun save(fingerprintData: FingerprintData) = withContext(Dispatchers.IO) {
        val dump = fingerprintData.toJSON().toString()
        val file = File(path + "fingerprint.json")
        file.bufferedWriter().use { out ->
            out.write(dump)
        }
    }

    fun computeNew(current: FingerprintData, params: Map<Output, Float>): FingerprintData {
        // compute how much new params contribute
        val newImageCnt = current.savedImageCount + 1u
        val ratio = 1.0 / newImageCnt.toInt()
        val rating = params[Output.QualityRating] ?: 0f
        val newRating = current.meanPreferenceRating * (1 - ratio) + rating * ratio
        val filters = params
            .filter { it.key != Output.QualityRating && it.key.toFilter() != null }
            .entries.associate { (key, value) -> key.toFilter()!! to value }

        // compute new preferable filter weights
        val newWeights = filters.entries.associate { (filter, value) ->
            filter to ((value * (1 - ratio) + filters[filter]!!) * INFLUENCE_WEIGHT)
        }

        return FingerprintData(
            id = current.id,
            minorAdjustments = newWeights,
            preferHigherRatings = true,
            meanPreferenceRating = newRating,
            savedImageCount = newImageCnt
        )
    }
}