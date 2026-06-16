package com.sil.morphlect.logic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import java.util.zip.ZipInputStream

/**
 * contains helper methods for working with server IO.
*/
object WebHelper {
    private val http by lazy { OkHttpClient() }
    var providerUrl = WebConstants.SERVER_BASE

/**
 * retrieve a page of model information from the server.
 */
    suspend fun fetchModelData(
        query: String? = null,
        limit: Int = 10,
        page: Int = 0,
    ): List<ModelInfoDTO> = withContext(Dispatchers.IO) {
        val url = StringBuilder()
            .append("$providerUrl/models?")
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
        val url = "$providerUrl/models/$id"
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
        val url = "$providerUrl/models/$id/download"
        val request = Request.Builder()
            .url(url)
            .build()
        try {
            // receive
            val response = http.newCall(request).execute()
            if (!response.isSuccessful)
                return@withContext null

            // download
            val mxtBundle = File(context.cacheDir, "$name.mxt")
            response.body?.byteStream()?.use { input ->
                mxtBundle.outputStream().use { input.copyTo(it) }
            } ?: return@withContext null

            // unzip
            val destination = File(context.filesDir, "models/$name")
            destination.mkdirs()

            ZipInputStream(mxtBundle.inputStream()).use { mxt ->
                var entry = mxt.nextEntry
                while (entry != null) {
                    File(destination, entry.name).outputStream()
                        .use { mxt.copyTo(it) }
                    entry = mxt.nextEntry
                }
            }

            // dispose and return
            mxtBundle.delete()
            return@withContext destination
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

        return@withContext try {
            if (query.isNullOrBlank()) {
                val images = JSONArray(body)
                List(images.length()) {
                    images
                        .getJSONObject(it)
                        .getJSONObject("urls")
                        .getString("small")
                } // return
            } else {
                val parsedBody = JSONArray(body)
                List(parsedBody.length()) {
                    parsedBody
                        .getJSONObject(it)
                        .getJSONObject("urls")
                        .getString("small")
                } // return
            }
        } catch (e: Exception) {
            Log.e("Unsplash", e.toString())
            emptyList()
        }
    }

    suspend fun downloadUnsplashImage(query: String): Bitmap? = withContext(Dispatchers.IO) {
        // first get a random image URL for the query
        val searchUrl = query

        val request = Request.Builder()
            .url(searchUrl)
            .build()

        return@withContext try {
            val response = http.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val bytes = response.body?.bytes() ?: return@withContext null
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            Log.e("Unsplash", e.toString())
            null
        }
    }
}