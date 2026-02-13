package com.sil.morphlect.view

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sil.morphlect.constant.WebConstants
import com.sil.morphlect.dto.ModelInfoDTO
import com.sil.morphlect.view.custom.FlickeringLedDotProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.File
import java.net.SocketTimeoutException

val myHttp by lazy { OkHttpClient() }

/**
 * retrieve a page of model information from the server.
 */
suspend fun fetchModelData(
    query: String? = null,
    limit: Int = 10,
    page: Int? = 0,
): List<ModelInfoDTO> = withContext(Dispatchers.IO) {
    delay(1000)
    val url = StringBuilder()
        .append("${WebConstants.SERVER_BASE}/models?")
        .append(if (!query.isNullOrEmpty()) "query=$query&" else "")
        .append("limit=$limit&page=$page")
        .toString()

    val request = Request.Builder()
        .url(url)
        .build()
    try {
        val response = myHttp.newCall(request).execute()

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

// TODO
suspend fun downloadModel(id: Int, context: Context): File? = withContext(Dispatchers.IO) {
    delay(1000)
    val url = "${WebConstants.SERVER_BASE}/models/$id/download"
    val request = Request.Builder()
        .url(url)
        .build()
    try {
        val response = myHttp.newCall(request).execute()
        if (!response.isSuccessful)
            return@withContext null

        val file = File(context.filesDir.toString() + "/models", "model_$id.tflite")
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

@Composable
fun ModelManager() {
    var onDownloads by remember { mutableStateOf(false) }
    var modelInfo by remember { mutableStateOf<List<ModelInfoDTO>>(listOf()) }
    var query by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val installed: List<ModelInfoDTO> = listOf() // TODO

    LaunchedEffect(Unit) {
        modelInfo = fetchModelData()
    }

    Scaffold { _ ->
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("model manager")

            if (onDownloads) {
                // downloads section
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("search") },
                )
                Button(onClick = {
                    scope.launch { modelInfo = fetchModelData(query) }
                }) {
                    Icon(Icons.Default.Search, contentDescription = "search")
                }

                if (modelInfo.isEmpty()) {
                    FlickeringLedDotProgressIndicator()
                    Text("fetching data...")
                } else {
                    modelInfo.map { dto ->
                        ModelInfoView(
                            dto,
                            onDownload = {
                                Toast.makeText(ctx, "installing ${dto.name}...", Toast.LENGTH_SHORT)
                                    .show()
                                scope.launch {
                                    val model = downloadModel(dto.id, ctx)
                                    if (model != null)
                                        Toast.makeText(ctx, "model installed at ${model.absolutePath}",
                                            Toast.LENGTH_SHORT).show()
                                }
                            },
                        )
                    }
                }
            } else {
                // installed section
                if (installed.isEmpty()) {
                    Text("no models yet. try installing some.")
                } else {
                    installed.map { dto ->
                        ModelInfoView(
                            dto,
                            onRemove = {
                                Toast.makeText(ctx, "${dto.name} has been removed.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
            Row {
                TextButton(onClick = { onDownloads = true }) {
                    Text("download")
                }
                TextButton(onClick = { onDownloads = false }) {
                    Text("view installed")
                }
            }
        }
    }
}

@Composable
fun ModelInfoView(
    dto: ModelInfoDTO,
    onDownload: ((String) -> Unit)? = null,
    onRemove: ((String) -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dto.name,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${dto.description}\n(${"%.2f".format(dto.size.toMegabytes())} MB)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
                    .copy(alpha = 0.7f),
            )
        }

        IconButton(onClick = {
            onDownload?.invoke(dto.name) ?: onRemove?.invoke(dto.name) }
        ) {
            Icon(Icons.Default.Download, contentDescription = "download")
        }
    }
}

fun Long.toMegabytes(decimals: Int = 2): Double {
    return this / 1_048_576.0
}