package com.sil.morphlect.view

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
import androidx.compose.material.icons.filled.Delete
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
import com.sil.morphlect.dto.ModelInfoDTO
import com.sil.morphlect.logic.WebHelper
import com.sil.morphlect.repository.ModelsRepository
import com.sil.morphlect.view.custom.FlickeringLedDotProgressIndicator
import kotlinx.coroutines.launch

@Composable
fun ModelManager() {
    var onDownloads by remember { mutableStateOf(false) }
    var modelInfo by remember { mutableStateOf<List<ModelInfoDTO>>(listOf()) }
    var query by remember { mutableStateOf("") }
    var installed by remember { mutableStateOf<List<ModelInfoDTO>>(listOf()) }

    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val modelsRepository = ModelsRepository(ctx)

    LaunchedEffect(Unit) {
        modelInfo = WebHelper.fetchModelData()
        installed = modelsRepository.readContents().map {
            ModelInfoDTO(0, it, "", 0)
        }
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
                    scope.launch { modelInfo = WebHelper.fetchModelData(query) }
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
                                    val model = WebHelper.downloadModel(dto.id, ctx, dto.name)
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
                                scope.launch {
                                    modelsRepository.delete(dto.name)
                                        .also {
                                            installed = modelsRepository.readContents().map {
                                                ModelInfoDTO(0, it, "", 0)
                                            }
                                        }
                                }
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

        onDownload?.let {
            IconButton(onClick = { it(dto.name) }) {
                Icon(Icons.Default.Download, contentDescription = "download")
            }
        }
        onRemove?.let {
            IconButton(onClick = { it(dto.name) }) {
                Icon(Icons.Default.Delete, contentDescription = "remove")
            }
        }
    }
}

fun Long.toMegabytes(decimals: Int = 2): Double {
    return this / 1_048_576.0
}