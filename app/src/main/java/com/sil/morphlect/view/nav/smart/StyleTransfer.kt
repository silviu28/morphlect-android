package com.sil.morphlect.view.nav.smart

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import com.sil.morphlect.data.EvaluationResult
import com.sil.morphlect.enums.Output
import com.sil.morphlect.imgproc.FormatConverters
import com.sil.morphlect.layerwork.StudioLayer
import com.sil.morphlect.logic.WebHelper
import com.sil.morphlect.ml.impl.AlteredMobileNetLoader
import com.sil.morphlect.view.WebOverlay
import kotlinx.coroutines.launch

fun AlteredMobileNetLoader.computeCompositionDiff(initialImage: Bitmap, selectedImage: Bitmap): EvaluationResult {
    val initialParams = infer(initialImage)
    val selectedParams = infer(selectedImage)

    val delta = initialParams.entries.associate { (output, initialFactor) ->
        val selectedFactor = selectedParams[output] ?: 0f
        output to (selectedFactor - initialFactor).toDouble()
    }

    return EvaluationResult(delta)
}

@Composable
fun StyleTransfer(
    initialImage: Bitmap?,
    onFinished: (EvaluationResult) -> Unit,
    onReturn: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val model = remember { AlteredMobileNetLoader().apply { initialize(context) } }
    var referenceImage by remember { mutableStateOf<Bitmap?>(null) }
    var webOverlayActive by remember { mutableStateOf(false) }
    val imagePickLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let{ referenceImage = FormatConverters.uriToBitmap(context, it) }
    }
    var diff by remember { mutableStateOf<Map<Output, Double>>(emptyMap()) }

    if (initialImage == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("no initial image")
        }
        return
    }

    if (webOverlayActive) {
        WebOverlay(
            onDismissRequest = { webOverlayActive = false },
            onImageSelected = { str ->
                coroutineScope.launch {
                    referenceImage = WebHelper.downloadUnsplashImage(str, context)
                }
                webOverlayActive = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        referenceImage?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "reference image",
                modifier = Modifier
                    .size(300.dp)
                    .weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { imagePickLauncher.launch("image/*") }) {
                Text("search gallery")
            }
            TextButton(onClick = { webOverlayActive = true }) {
                Text("search unsplash")
            }
        }

        referenceImage?.let { ref ->
            Button(onClick = {
                diff = model.computeCompositionDiff(initialImage, ref).outputs
            }) { Text("run") }

            if (diff.isNotEmpty()) {
                Image(
                    bitmap =
                        StudioLayer(FormatConverters.bitmapToMat(initialImage))
                            .applyFilterMap(
                                diff.entries
                                    .mapNotNull { (k, v) -> k.toFilter()?.let { it to v } }.toMap())
                            .mat.let { FormatConverters.matToBitmap(it) }
                            .asImageBitmap(),
                contentDescription = "reference image",
                modifier = Modifier
                    .size(300.dp)
                    .weight(1f)
                )
                Button(onClick = {
                    onFinished(EvaluationResult(diff))
                    onReturn()
                }) {
                    Text("apply")
                }
            }
        }
    }
}