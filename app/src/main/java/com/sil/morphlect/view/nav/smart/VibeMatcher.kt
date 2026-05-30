package com.sil.morphlect.view.nav.smart

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.sil.morphlect.enums.Filter
import com.sil.morphlect.imgproc.Filtering
import com.sil.morphlect.imgproc.FormatConverters
import com.sil.morphlect.ml.impl.MiniLMEmbeddingLoader
import com.sil.morphlect.view.custom.LedDotSlider
import com.sil.morphlect.view.dialog.DialogScaffold
import com.sil.morphlect.viewmodel.StudioViewModel
import kotlinx.coroutines.launch
import org.opencv.core.Mat

fun applyPresetsWithIntensity(mat: Mat, presets: List<Preset>, intensities: Map<String, Float>): Mat {
    var newMat = mat.clone()
    presets.forEachIndexed { idx, preset ->
        preset.params.entries.forEach { (k, v) ->
            newMat = when (k) {
                Filter.Contrast -> Filtering.contrast(mat, v * (intensities[preset.name]?.toDouble() ?: 1.0))
                Filter.Brightness -> Filtering.brightness(mat, v * (intensities[preset.name]?.toDouble() ?: 1.0))
                Filter.Blur -> Filtering.blur(mat, v * (intensities[preset.name]?.toDouble() ?: 1.0), v * (intensities[preset.name]?.toDouble() ?: 1.0))
                Filter.LightBalance -> Filtering.lightBalance(mat, v * (intensities[preset.name]?.toDouble() ?: 1.0))
                Filter.Hue -> Filtering.hueShift(mat, v * (intensities[preset.name]?.toDouble() ?: 1.0))
                Filter.Sharpness -> Filtering.sharpen(mat, v * (intensities[preset.name]?.toDouble() ?: 1.0))
            }
        }
    }
    return newMat
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VibeMatcher(vm: StudioViewModel, navController: NavController) {
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
    var cherryPicking by remember { mutableStateOf(false) }
    var selectedVibeIdx by remember { mutableIntStateOf(0) }
    var appliedIntensities by remember {
        mutableStateOf(
            tokens.associate { it to .5f }
        )
    }
    val tokensList by remember { derivedStateOf { tokens.toList() } }
    var separatePresets by remember { mutableStateOf<List<Preset>>(emptyList()) }
    var initialMat by remember { mutableStateOf(vm.originalMat) }

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
        initialMat?.let { mat ->
            FormatConverters.matToBitmap(
                applyPresetsWithIntensity(mat, separatePresets, appliedIntensities)
            ).asImageBitmap().let {
                Image(
                    bitmap = it,
                    contentDescription = "preview",
                    modifier = Modifier.size(300.dp),
                    contentScale = ContentScale.Crop
                )
            }

            if (cherryPicking) {
                tokensList[selectedVibeIdx].let { selected ->
                    Text("select the intensity of each vibe")
                    Text(selected)
                    LedDotSlider(
                        value = appliedIntensities[selected] ?: 0f,
                        onValueChange = { newValue ->
                            appliedIntensities = appliedIntensities + (selected to newValue)
                        },
                        modifier = Modifier,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        tokensList.forEachIndexed { idx, it ->
                            if (it == tokensList[selectedVibeIdx])
                                OutlinedButton(onClick = { Unit }) {
                                    Text(it)
                                }
                            else
                                Button(onClick = { selectedVibeIdx = idx }) {
                                    Text(it)
                                }
                        }
                    }
                }
                Row {
                    TextButton(onClick = { cherryPicking = false }) {
                        Text("continue")
                    }
                    TextButton(onClick = { }) {
                        Text("auto")
                    }
                }
                Text(miniLMEmbeddingLoader.combineMultipleTags(similarities).toString())
            } else {
                Text(anchorPresets.joinToString())
                OutlinedTextField(
                    value = currentToken,
                    onValueChange = {
                        if (it.contains(" ")) {
                            tokens += currentToken.trim()
                            currentToken = ""
                        } else currentToken = it
                    },
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
                            similarities =
                                miniLMEmbeddingLoader.batchComputeSimilarAnchors(words = tokens.toList())
                            separatePresets = similarities.mapIndexed { idx, it ->
                                miniLMEmbeddingLoader.computeNewPreset(tokensList[idx], it)
                            }
                            isProcessing = false; cherryPicking = true
                        }
                    }) {
                        Text("seems good")
                    }
                }

                similarities.forEach { l -> Text(l.joinToString { it.first + ":" + it.second.toString() }) }

                TextButton(onClick = { navController.navigate("studio") }) {
                    Text("back to studio")
                }
            }
        }
    }
}