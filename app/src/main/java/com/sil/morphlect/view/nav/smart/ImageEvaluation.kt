package com.sil.morphlect.view.nav.smart

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
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
import com.sil.morphlect.enums.Output
import com.sil.morphlect.logic.optimizeComposition
import com.sil.morphlect.ml.impl.AlteredMobileNetLoader
import com.sil.morphlect.ml.impl.RatingMaximizerLoader
import com.sil.morphlect.view.dialog.DialogScaffold
import com.sil.morphlect.view.dialog.impl.KeepParamsDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Stable
class ImageEvaluationUiState() {
    var values by mutableStateOf<Map<Output, Float>>(mapOf())
    var keepParamsDialogActive by mutableStateOf(false)

    var optimizing by mutableStateOf(false)
    var optimizedParams by mutableStateOf<Map<Output, Float>?>(null)
}

@Composable
fun ImageEvaluation(
    previewBitmap: Bitmap?,
    onFinished: (EvaluationResult) -> Unit,
    onReturn: () -> Unit,
) {
    val ctx = LocalContext.current
    val loader = remember { AlteredMobileNetLoader().apply { initialize(ctx) } }
    val optimizer = remember { RatingMaximizerLoader().apply { initialize(ctx) } }
    val state = remember { ImageEvaluationUiState() }

    LaunchedEffect(state.optimizing) {
        if (state.optimizing) {
            state.optimizedParams = optimizer.optimizeComposition(state.values, 10000)
            state.optimizing = !state.optimizing
            state.keepParamsDialogActive = !state.keepParamsDialogActive
//            state.values = state.optimizedParams!!
            state.optimizedParams?.let {
                // oh my god bruh...
                val asEvalRes = EvaluationResult(it.valuesToDouble())
                val ogAsEvalRes = EvaluationResult(state.values.entries.associate { a -> a.key to a.value.toDouble() })
                onFinished(EvaluationResult(
                    asEvalRes.delta(ogAsEvalRes)
                    .outputs
                    .entries
                    .associate { a -> a.key to a.value / 10 }
                ))
            }
            onReturn()
        }
    }

    LaunchedEffect(previewBitmap) {
        state.values = withContext(Dispatchers.Default) {
            loader.infer(previewBitmap!!)
        }
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
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
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
        Row {
            Button(onClick = { onReturn() }) {
                Text("back to studio")
            }
            Button(onClick = { state.keepParamsDialogActive = true }) {
                Text("improve")
            }
        }
    }
}