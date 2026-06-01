package com.sil.morphlect.view.nav

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.sil.morphlect.dto.ModelInfoDTO
import com.sil.morphlect.logic.WebHelper
import com.sil.morphlect.repository.ExtensionsRepository
import com.sil.morphlect.view.custom.DecoratedContainer
import com.sil.morphlect.view.custom.FlickeringLedDotProgressIndicator
import com.sil.morphlect.view.dialog.DialogScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

@Stable
internal class ModelManagerUiState() {
    var onDownloads by mutableStateOf(false)
    var providerUrl by mutableStateOf(WebHelper.providerUrl)
    var showProviderSwitch by mutableStateOf(false)
    var modelInfo by mutableStateOf<List<ModelInfoDTO>>(listOf())
    var query by mutableStateOf("")
    var installed by mutableStateOf<List<ModelInfoDTO>>(listOf())
}

@Composable
fun ModelManager(navController: NavHostController) {
    val state = remember { ModelManagerUiState() }
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val extensionsRepository = ExtensionsRepository(ctx)

    LaunchedEffect(Unit) {
        state.modelInfo = WebHelper.fetchModelData()

        if (state.modelInfo.isEmpty()) withContext(Dispatchers.Main) {
            Toast.makeText(ctx, "unable to retrieve server data", Toast.LENGTH_LONG).show()
        }

        state.installed = extensionsRepository.readExtensionNames().map {
            ModelInfoDTO(0, it, "", 0)
        }
    }

    if (state.showProviderSwitch) {
        DialogScaffold(
            title = "switch extension provider",
            icon = Icons.Default.Settings,
            onDismissRequest = {
                state.showProviderSwitch = false
                state.providerUrl = WebHelper.providerUrl
            }) {
                OutlinedTextField(
                    value = state.providerUrl,
                    onValueChange = { state.providerUrl = it },
                    label = { Text("provider url") },
                )
                IconButton(onClick = {
                    state.showProviderSwitch = false
                    WebHelper.providerUrl = state.providerUrl
                }) {
                    Icon(Icons.Default.Check, contentDescription = "apply provider")
                }
                Text("make sure you trust the provider that you're switching to!")
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = { navController.popBackStack() },
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "layering")
            }
        }
    ) { _ ->
        DecoratedContainer(Icons.Default.Science) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("model manager")

                if (state.onDownloads) {
                    // downloads section
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = { state.query = it },
                        label = { Text("search") },
                    )
                    Button(onClick = {
                        scope.launch { state.modelInfo = WebHelper.fetchModelData(state.query) }
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "search")
                    }

                    if (state.modelInfo.isEmpty()) {
                        FlickeringLedDotProgressIndicator()
                        Text("fetching data...")
                    } else {
                        state.modelInfo.map { dto ->
                            ModelInfoView(
                                dto,
                                onDownload = {
                                    Toast.makeText(
                                        ctx,
                                        "installing ${dto.name}...",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    scope.launch {
                                        val model = WebHelper.downloadModel(dto.id, ctx, dto.name)
                                        if (model != null)
                                            Toast.makeText(
                                                ctx, "model installed at ${model.absolutePath}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                    }
                                },
                            )
                        }
                    }
                } else {
                    // installed section
                    if (state.installed.isEmpty()) {
                        Text("no models yet. try installing some.")
                    } else {
                        state.installed.forEach { dto ->
                            ModelInfoView(
                                dto,
                                onRemove = {
                                    scope.launch {
                                        extensionsRepository.delete(dto.name)
                                            .also {
                                                state.installed = extensionsRepository.readExtensionNames().map {
                                                    ModelInfoDTO(0, it, "", 0)
                                                }
                                            }
                                    }
                                    Toast.makeText(ctx,"${dto.name} has been removed.", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
                Row {
                    TextButton(onClick = { state.onDownloads = true }) {
                        Text("download")
                    }
                    TextButton(onClick = { state.onDownloads = false }) {
                        Text("view installed")
                    }
                }
                TextButton(onClick = { state.showProviderSwitch = true }) {
                    Text("switch provider...")
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
            if (dto.description.isNotEmpty() && dto.size > 0)
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