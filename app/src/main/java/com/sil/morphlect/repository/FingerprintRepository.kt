package com.sil.morphlect.repository

import android.content.Context
import com.sil.morphlect.data.FingerprintData
import com.sil.morphlect.enums.Filter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.util.UUID

class FingerprintRepository(context: Context) {
    val path = context.filesDir.toString() + "/fingerprint"

    suspend fun load(): FingerprintData? = withContext(Dispatchers.IO) {
        val file = File(path + "fingerprint.json")
        if (!file.exists()) return@withContext null
        val fp = file.bufferedReader().use { reader ->
            val json = JSONObject(reader.readText())
            FingerprintData.fromJSON(json)
        }
        fp
    }

    fun generateNew(): FingerprintData {
        val id = UUID.randomUUID().toString()
        return FingerprintData(
            id,
            minorAdjustments = Filter.entries.associate { it to .0 },
            preferHigherRatings = false,
            meanPreferenceRating = 50.0,
        )
    }

    suspend fun save(fingerprintData: FingerprintData) = withContext(Dispatchers.IO) {
        val dump = fingerprintData.toJSON().toString()
        val file = File(path + "fingerprint.json")
        file.bufferedWriter().use { out ->
            out.write(dump)
        }
    }
}