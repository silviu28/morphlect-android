package com.sil.morphlect.view

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.sil.morphlect.enums.Effect
import com.sil.morphlect.logic.Filtering
import com.sil.morphlect.logic.FormatConverters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.core.Mat

@Composable
fun PresetPreview(
    name: String,
    preset: Map<Effect, Double>,
    originalMat: Mat?,
    onPress: () -> Unit,
    onLongPress: () -> Unit
) {
    var processedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var scope = rememberCoroutineScope()

    if (originalMat == null) {
        Box(modifier = Modifier.size(60.dp)) {
            CircularProgressIndicator()
        }
        return
    }

    LaunchedEffect(originalMat, preset) {
        try {
            var previewMat = originalMat.clone()

            val blurValue = preset[Effect.Blur] ?: 0.0
            if (blurValue != 0.0) {
                previewMat = Filtering.blur(previewMat, blurValue / 10, blurValue / 10)
            }

            val hueValue = preset[Effect.Hue] ?: 0.0
            if (hueValue != 0.0) {
                previewMat = Filtering.hueShift(previewMat, hueValue)
            }

            val resultBitmap = FormatConverters.matToBitmap(previewMat)

            previewMat.release()

            processedBitmap = resultBitmap
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(8.dp)),
    ) {
        if (processedBitmap == null) {
            CircularProgressIndicator()
        } else {
            Image(
                bitmap = processedBitmap!!.asImageBitmap(),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onPress() },
                            onLongPress = { onLongPress() }
                        )
                    }
            )
        }
    }
}