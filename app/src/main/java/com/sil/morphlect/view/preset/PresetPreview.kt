package com.sil.morphlect.view.preset

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.sil.morphlect.data.Preset
import com.sil.morphlect.enums.Filter
import com.sil.morphlect.imgproc.Filtering
import com.sil.morphlect.imgproc.FormatConverters
import com.sil.morphlect.layerwork.applyFilterMap
import com.sil.morphlect.view.custom.FlickeringLedDotProgressIndicator
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

@Composable
fun PresetPreview(
    preset: Preset,
    originalMat: Mat?,
    onPress: () -> Unit,
    onLongPress: () -> Unit,
    expanded: Boolean = false,
) {
    val processedBitmap = remember(preset, originalMat) {
        originalMat?.let {
            val smallMat = Mat()
            val ratio = maxOf(it.width(), it.height()) / 256.0
            Imgproc.resize(
                it, smallMat,
                Size(it.width() / ratio, it.height() / ratio)
            )
            val previewMat = smallMat.applyFilterMap(preset.params)
            val bitmap = FormatConverters.matToBitmap(previewMat)
            smallMat.release()
            previewMat.release()
            bitmap
        }
    }

    if (originalMat == null) {
        Box(modifier = Modifier.size(60.dp)) {
            FlickeringLedDotProgressIndicator()
        }
        return
    }

    Box(
        modifier = Modifier
            .size(if (expanded) 200.dp else 60.dp)
            .clip(RoundedCornerShape(8.dp)),
    ) {
        if (processedBitmap == null) {
            FlickeringLedDotProgressIndicator()
        } else processedBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = preset.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
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