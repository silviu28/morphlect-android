package com.sil.morphlect.view.nav.smart

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.collectAsState
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
import com.sil.morphlect.data.EvaluationResult
import com.sil.morphlect.data.Preset
import com.sil.morphlect.enums.Filter
import com.sil.morphlect.enums.Output
import com.sil.morphlect.imgproc.Filtering
import com.sil.morphlect.imgproc.FormatConverters
import com.sil.morphlect.ml.impl.MiniLMEmbeddingLoader
import com.sil.morphlect.repository.AppConfigRepository
import com.sil.morphlect.view.custom.LedDotSlider
import com.sil.morphlect.view.dialog.DialogScaffold
import kotlinx.coroutines.launch
import org.opencv.core.Mat

fun applyPresetsWithIntensity(mat: Mat, presets: List<Preset>, intensities: Map<String, Float>): Mat {
    var newMat = mat.clone()
    presets.forEachIndexed { idx, preset ->
        val intensity = intensities[idx.toString()]?.toDouble() ?: 1.0
        preset.params.entries.forEach { (k, v) ->
            newMat = when (k) {
                Filter.Contrast -> Filtering.contrast(newMat, v * intensity)
                Filter.Brightness -> Filtering.brightness(newMat, v * intensity)
                Filter.Blur -> Filtering.blur(newMat, v * intensity, v * intensity)
                Filter.LightBalance -> Filtering.lightBalance(newMat, v * intensity)
                Filter.Hue -> Filtering.hueShift(newMat, v * intensity)
                Filter.Sharpness -> Filtering.sharpen(newMat, v * intensity)
            }
        }
    }
    return newMat
}

private fun Filter.toOutput(): Output? = when (this) {
    Filter.Sharpness -> Output.Sharpness
    Filter.Brightness -> Output.Brightness
    Filter.Contrast -> Output.Contrast
    Filter.Hue -> Output.Hue
    Filter.Blur -> Output.Bitrate
    Filter.LightBalance -> null
}

private fun mergedEvaluationResult(
    presets: List<Preset>,
    intensities: Map<String, Float>,
): EvaluationResult {
    val merged = mutableMapOf<Output, Double>()
    presets.forEachIndexed { index, preset ->
        val intensity = intensities[index.toString()]?.toDouble() ?: 1.0
        preset.params.forEach { (filter, value) ->
            val outputKey = filter.toOutput() ?: return@forEach
            merged[outputKey] = (merged[outputKey] ?: .0) + (value * intensity/10)
        }
    }
    return EvaluationResult(merged)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VibeMatcher(
    originalMat: Mat?,
    onFinished: (EvaluationResult) -> Unit,
    onReturn: () -> Unit,
    configRepository: AppConfigRepository,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var tokens by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentToken by remember { mutableStateOf("") }
    val miniLMEmbeddingLoader = remember {
        MiniLMEmbeddingLoader().apply { initialize(context) }
    }
    var anchorPresets by remember { mutableStateOf<List<Preset>>(listOf()) }
    var isProcessing by remember { mutableStateOf(false) }
    var similarities by remember { mutableStateOf<List<List<Pair<String, Float>>>>(listOf()) }
    var cherryPicking by remember { mutableStateOf(false) }
    var selectedVibeIdx by remember { mutableIntStateOf(0) }
    var appliedIntensities by remember { mutableStateOf<Map<String, Float>>(emptyMap()) }
    val tokensList by remember { derivedStateOf { tokens } }
    var separatePresets by remember { mutableStateOf<List<Preset>>(emptyList()) }
    var initialMat by remember { mutableStateOf(originalMat) }
    val developerMode by configRepository.developerMode.collectAsState(initial = false)

    when {
        isProcessing -> DialogScaffold(
            title = "Please wait",
            onDismissRequest = { },
        ) { LinearProgressIndicator() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                tokensList.getOrNull(selectedVibeIdx)?.let { selected ->
                    Text("select the intensity of each vibe")
                    Text(selected)
                    LedDotSlider(
                        value = appliedIntensities[selectedVibeIdx.toString()] ?: 0.5f,
                        onValueChange = { newValue ->
                            appliedIntensities = appliedIntensities + (selectedVibeIdx.toString() to newValue)
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
                    TextButton(onClick = {
                        cherryPicking = false
                        onFinished(
                            mergedEvaluationResult(
                                presets = separatePresets,
                                intensities = appliedIntensities,
                            )
                        )
                        onReturn()
                    }) {
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
                            val token = it.trim()
                            if (token.isNotBlank() && token !in tokens) {
                                tokens = tokens + token
                            }
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
                        val token = currentToken.trim()
                        if (token !in tokens) {
                            tokens = tokens + token
                        }
                        currentToken = ""
                    }
                }) {
                    Text("+")
                }

                Row(
                    modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    tokens.forEach { token ->
                        Button(onClick = {
                            val index = tokens.indexOf(token)
                            tokens = tokens.filterNot { it == token }
                            appliedIntensities = appliedIntensities.toMutableMap().apply {
                                remove(index.toString())
                            }
                            if (selectedVibeIdx >= tokens.size) {
                                selectedVibeIdx = (tokens.size - 1).coerceAtLeast(0)
                            }
                        }) {
                            Text(token)
                        }
                    }
                }

                Row {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            isProcessing = true
                            similarities =
                                miniLMEmbeddingLoader.batchComputeSimilarAnchors(words = tokensList)
                            separatePresets = similarities.mapIndexed { idx, it ->
                                miniLMEmbeddingLoader.computeNewPreset(tokensList[idx], it)
                            }
                            appliedIntensities = tokensList
                                .indices
                                .associate { index -> index.toString() to 0.5f }
                            selectedVibeIdx = 0
                            isProcessing = false; cherryPicking = true
                        }
                    }) {
                        Text("seems good")
                    }
                }

                TextButton(onClick = { onReturn() }) {
                    Text("back to studio")
                }

                if (developerMode)
                    similarities.forEach { l ->
                        Text(l.joinToString { it.first + ":" + it.second.toString() })
                    }
            }
        }
    }
}