package com.sil.morphlect.view.nav.smart

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sil.morphlect.data.EvaluationResult
import com.sil.morphlect.enums.Filter
import com.sil.morphlect.imgproc.FormatConverters
import com.sil.morphlect.layerwork.StudioLayer
import com.sil.morphlect.layerwork.applyFilterMap
import com.sil.morphlect.ml.impl.AlteredMobileNetLoader
import com.sil.morphlect.ml.impl.RatingMaximizerLoader
import com.sil.morphlect.view.dialog.DialogScaffold
import com.sil.morphlect.view.dialog.impl.KeepParamsDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun RatingMaximizerLoader.optimizeComposition(image: Bitmap, iterationCount: Int): Map<Filter, Double> = withContext(Dispatchers.Default) {
    val cvmat = FormatConverters.bitmapToMat(image)
    var stepSize = .05
    val decayPerStep = .995
    var bestSolution: Map<Filter, Double> = Filter.entries.associate { it to .0 }
    var bestScore = 0f
    for (i in 0..iterationCount) {
        val next = bestSolution.entries
            .associate { entry ->
                val disposition = ((Math.random() * stepSize) * if (Math.random() > 0.5) 1 else -1).toFloat()
                entry.key to (entry.value + disposition).coerceIn(-0.8, 0.8)
            }

        val filteredMat = cvmat.applyFilterMap(bestSolution)
        val newScore = infer(FormatConverters.matToBitmap(filteredMat))
            .coerceAtMost(1f)

        if (newScore > bestScore) {
            bestSolution = next.toMutableMap()
            bestScore = newScore
        }

        stepSize *= decayPerStep
    }

    return@withContext bestSolution.toMap()
}

@Stable
class ImageEvaluationUiState() {
    var values by mutableStateOf<Map<Filter, Float>>(mapOf())
    var keepParamsDialogActive by mutableStateOf(false)

    var optimizing by mutableStateOf(false)
    var optimizedParams by mutableStateOf<Map<Filter, Double>?>(null)
    var score by mutableFloatStateOf(0f)
}

@Composable
fun ImageEvaluation(
    previewBitmap: Bitmap?,
    onFinished: (EvaluationResult) -> Unit,
    onReturn: () -> Unit,
) {
    val ctx = LocalContext.current
    val loader = remember { AlteredMobileNetLoader().apply { initialize(ctx) } }
    val evaluator = remember { RatingMaximizerLoader().apply { initialize(ctx) } }
    val state = remember { ImageEvaluationUiState() }
    var informativeMessage by remember { mutableStateOf("processing...") }

    LaunchedEffect(state.optimizing) {
        if (state.optimizing) {
            state.optimizedParams = evaluator.optimizeComposition(previewBitmap!!, 100)
            state.optimizing = !state.optimizing
            state.keepParamsDialogActive = !state.keepParamsDialogActive
//            state.values = state.optimizedParams!!
            state.optimizedParams?.let {
                val divided = it.entries.associate { (k, v) -> k to v / 10.0 }
                onFinished(EvaluationResult(divided))
            }
            onReturn()
        }
    }

    LaunchedEffect(previewBitmap) {
        val values = withContext(Dispatchers.Default) {
            loader.infer(previewBitmap!!)
        }
        state.values = values
        state.score = withContext(Dispatchers.Default) {
            evaluator.infer(previewBitmap!!)
        }
        val sb = StringBuilder().apply {
            state.values[Filter.Sharpness]?.let {
                when {
                    it > 0f && it <= .1f -> appendLine("i think your image is a bit unclear.")
                    it > .1f && it <= .2f -> appendLine("your image is clear.")
                    it > .2f -> appendLine("your image is very sharp. make sure to not go too overboard.")
                }
            }
            state.values[Filter.Brightness]?.let {
                when {
                    it > 0f && it <= .3f -> appendLine("i think your image is a bit dark.")
                    it > .3f && it <= .5f -> appendLine("brightness is alright.")
                    it > .5f -> appendLine("you may want to tone brightness a bit lower.")
                }
            }
            state.values[Filter.Contrast]?.let {
                when {
                    it > 0f && it <= .1f -> appendLine("your image doesn't have a great color harmony. maybe raise contrast.")
                    it > .1f && it <= .3f -> appendLine("your image has good contrasts.")
                    it > .3f -> appendLine("your image has strong contrasts. if it's not what you intend consider lowering it.")
                }
            }
            state.values[Filter.Hue]?.let {
                when {
                    it > 0f && it <= .15f -> appendLine("your image has dull colors.")
                    it > .15f && it <= .4f -> append("your image has okay colors.")
                    it > .4f -> append("your image has punchy colors. it might be an eye-strain.")
                }
            }
            appendLine("in my opinion...")
            state.score.let {
                when {
                    it > 0f && it <= .4f -> appendLine("your image isn't too great. consider using improvement tooling or taking a better shot")
                    it > .4f && it <= .6f -> appendLine("your image is perfectly serviceable!")
                    it > .6f && it <= .8f -> appendLine("your image is great!")
                    it > .8f -> appendLine("you are a pro.")
                }
            }
        }
        informativeMessage = sb.toString()
    }

    when {
        state.optimizing ->
            DialogScaffold(
                title = "",
                onDismissRequest = {},
            ) {
                Text("please wait...")
            }

        state.keepParamsDialogActive ->
            KeepParamsDialog(
                onDismissRequest = { state.keepParamsDialogActive = false },
                onApply = { state.optimizing = true })
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        previewBitmap?.asImageBitmap()?.let {
            Image(
                bitmap = it,
                contentDescription = "preview",
                modifier = Modifier.size(300.dp),
                contentScale = ContentScale.Crop
            )
        }
        state.values.forEach { (effect, value) ->
            Text("${effect.name}: ${"%.2f".format(value)}")
        }
        Text("Rating ${state.score}")
        Text(informativeMessage)
        Row {
            Button(onClick = { onReturn() }) {
                Text("back to studio")
            }
            Button(onClick = { state.keepParamsDialogActive = true }) {
                Text("improve")
            }
        }
        Row(horizontalArrangement = Arrangement.End) {
            Text(
                text = "image evaluation uses AI and can make mistakes. use this to orientate yourself.",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}