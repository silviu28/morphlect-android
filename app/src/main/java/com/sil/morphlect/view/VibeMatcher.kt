package com.sil.morphlect.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sil.morphlect.data.Preset
import com.sil.morphlect.ml.impl.MiniLMEmbeddingLoader
import com.sil.morphlect.view.dialog.DialogScaffold
import com.sil.morphlect.viewmodel.EditorViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VibeMatcher(vm: EditorViewModel, navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var tokens by remember { mutableStateOf(emptySet<String>()) }
    var currentToken by remember { mutableStateOf("") }
    val miniLMEmbeddingLoader = remember {
        MiniLMEmbeddingLoader().apply { initialize(context) }
    }
    var anchorPresets by remember { mutableStateOf<List<Preset>>(listOf()) }
    var isProcessing by remember { mutableStateOf(false) }
    var similarities by remember { mutableStateOf<List<List<Pair<String, Float>>>>(listOf()) }

    when {
        isProcessing -> DialogScaffold(
            title = "Please wait",
            onDismissRequest = { },
        ) { LinearProgressIndicator() }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(anchorPresets.joinToString())
        vm.previewBitmap?.asImageBitmap()?.let {
            Image(
                bitmap = it,
                contentDescription = "preview",
                modifier = Modifier.size(300.dp),
                contentScale = ContentScale.Crop
            )
        }

        OutlinedTextField(
            value = currentToken,
            onValueChange = { currentToken = it },
            label = { Text("add token") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        )
        Button(onClick = {
            if (currentToken.isNotBlank()) {
                tokens += currentToken.trim()
                currentToken = ""
            }
        }) {
            Text("+")
        }

        FlowRow {
            tokens.forEach { token ->
                Button(onClick = { tokens -= token }) {
                    Text(token)
                }
            }
        }

        Row {
            TextButton(onClick = {
                coroutineScope.launch {
                    isProcessing = true
                    similarities = miniLMEmbeddingLoader.batchComputeSimilarAnchors(words = tokens.toList())
                    isProcessing = false
                }
            }) {
                Text("seems good")
            }
        }

        similarities.forEach { l -> Text(l.joinToString { it.first + ":" + it.second.toString() }) }

        TextButton(onClick = { navController.navigate("editor") }) {
            Text("back to editor")
        }
    }
}