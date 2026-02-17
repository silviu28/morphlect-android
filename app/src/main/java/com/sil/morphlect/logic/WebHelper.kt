package com.sil.morphlect.logic

import android.content.Context
import android.util.Log
import com.sil.morphlect.BuildConfig
import com.sil.morphlect.constant.WebConstants
import com.sil.morphlect.dto.ModelInfoDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * contains helper methods for working with server IO.
*/
object WebHelper {
    private val http by lazy { OkHttpClient() }

/**
 * retrieve a page of model information from the server.
 */
    suspend fun fetchModelData(
        query: String? = null,
        limit: Int = 10,
        page: Int = 0,
    ): List<ModelInfoDTO> = withContext(Dispatchers.IO) {
        val url = StringBuilder()
            .append("${WebConstants.SERVER_BASE}/models?")
            .append(if (!query.isNullOrEmpty()) "query=$query&" else "")
            .append("limit=$limit&page=$page")
            .toString()

        val request = Request.Builder()
            .url(url)
            .build()
        try {
            val response = http.newCall(request).execute()

            if (!response.isSuccessful)
                return@withContext emptyList()

            val body = response.body?.string()
            val models = JSONArray(body)
            return@withContext List(models.length()) {
                val data = models.getJSONObject(it)
                ModelInfoDTO(
                    id = data.getInt("id"),
                    name = data.getString("name"),
                    description = data.getString("description"),
                    size = data.getLong("size"),
                )
            }
        } catch (e: Exception) {
            Log.e("Model data", "Unable to retrieve data from server - $e")
            return@withContext emptyList()
        }
    }

    // TODO might be used in the future?
    suspend fun fetchOneModelData(id: Int): ModelInfoDTO? = withContext(Dispatchers.IO) {
        val url = "${WebConstants.SERVER_BASE}/models/$id"
        val request = Request.Builder()
            .url(url)
            .build()
        try {
            val response = http.newCall(request).execute()

            if (!response.isSuccessful)
                return@withContext null

            val body = response.body?.string() ?: return@withContext null
            val data = JSONObject(body)
            return@withContext ModelInfoDTO(
                id = data.getInt("id"),
                name = data.getString("name"),
                description = data.getString("description"),
                size = data.getLong("size"),
            )
        } catch (e: Exception) {
            Log.e("Model data", "Unable to retrieve data from server - $e")
            return@withContext null
        }
    }

    suspend fun downloadModel(id: Int, context: Context, name: String): File? = withContext(Dispatchers.IO) {
        val url = "${WebConstants.SERVER_BASE}/models/$id/download"
        val request = Request.Builder()
            .url(url)
            .build()
        try {
            val response = http.newCall(request).execute()
            if (!response.isSuccessful)
                return@withContext null

            val file = File(context.filesDir.toString() + "/models", "$name.tflite")
            file.parentFile?.mkdirs()

            response.body?.run {
                byteStream().use { input ->
                    file.outputStream().use {
                        input.copyTo(it)
                    }
                }
            } ?: return@withContext null

            return@withContext file
        } catch (e: Exception) {
            Log.e("Model download", "Unable to download model - $e")
            return@withContext null
        }
    }

    suspend fun fetchImages(query: String? = null): List<String> = withContext(Dispatchers.IO) {
        val url = if (query.isNullOrBlank()) {
            WebConstants.UNSPLASH_API_BASE + "/photos/random?count=8"
        } else {
            WebConstants.UNSPLASH_API_BASE + "/photos?page=1&query=$query"
        }
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Client-ID ${BuildConfig.UNSPLASH_ACCESS_KEY}")
            .build()
        val response = http.newCall(request).execute()

        if (!response.isSuccessful)
            return@withContext emptyList()

        val body = response.body?.string()

        if (query.isNullOrBlank()) {
            val images = JSONArray(body)
            return@withContext List(images.length()) {
                images
                    .getJSONObject(it)
                    .getJSONObject("urls")
                    .getString("small")
            }
        } else {
            val parsedBody = JSONObject(body)
            val images = parsedBody.getJSONArray("results")
            return@withContext List(images.length()) {
                images
                    .getJSONObject(it)
                    .getJSONObject("urls")
                    .getString("small")
            }
        }
    }
}