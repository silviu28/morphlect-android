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

@Composable
fun ImageEvaluation(
    previewBitmap: Bitmap?,
    onFinished: (EvaluationResult) -> Unit,
    onReturn: () -> Unit,
) {
    val ctx = LocalContext.current.applicationContext
    val loader = remember { AlteredMobileNetLoader().apply { initialize(ctx) } }
    val optimizer = remember { RatingMaximizerLoader().apply { initialize(ctx) } }
    var values by remember { mutableStateOf<Map<Output, Float>>(mapOf()) }
    var infoText by remember { mutableStateOf("processing...") }
    var keepParamsDialogActive by remember { mutableStateOf(false) }

    var stepsParamVeryInteresting by remember { mutableStateOf(0) }
    var optimizingTrustMeBro by remember { mutableStateOf(false) }
    var optimizedParams by remember { mutableStateOf<Map<Output, Float>?>(null) }

    LaunchedEffect(optimizingTrustMeBro) {
        if (optimizingTrustMeBro) {
            optimizedParams = optimizer.optimizeComposition(values, 10000)
            optimizingTrustMeBro = !optimizingTrustMeBro
            keepParamsDialogActive = !keepParamsDialogActive
            values = optimizedParams!!
            optimizedParams?.let {
                onFinished(
                    EvaluationResult(it.entries.associate { a -> a.key to a.value.toDouble() })
                )
            }
            onReturn()
        }
    }

    LaunchedEffect(previewBitmap) {
        values = withContext(Dispatchers.Default) {
            loader.infer(previewBitmap!!)
        }
        infoText = "done!"
    }

    if (optimizingTrustMeBro) {
        DialogScaffold(
            title = "",
            onDismissRequest = {},
        ) {
            Text("applying optimizer step $stepsParamVeryInteresting out of 200...")
        }
    }

    if (keepParamsDialogActive) {
        KeepParamsDialog(
            onDismissRequest = { keepParamsDialogActive = false },
            onApply = { optimizingTrustMeBro = true })
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
        values.forEach { (effect, value) ->
            Text("${effect.name}: ${"%.2f".format(value)}")
        }
        Row {
            Button(onClick = { onReturn() }) {
                Text("back to studio")
            }
            Button(onClick = { keepParamsDialogActive = true }) {
                Text("improve")
            }
        }
    }
}