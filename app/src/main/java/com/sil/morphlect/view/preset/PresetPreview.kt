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
    val processedBitmap by remember {
        derivedStateOf {
            originalMat?.let {
                val smallMat = Mat()
                val ratio = maxOf(it.width(), it.height()) / 256.0
                Imgproc.resize(
                    it, smallMat,
                    Size(it.width() / ratio, it.height() / ratio)
                )
                var previewMat = smallMat.clone()
                Filter.entries.forEach { filter ->
                    val factor = preset.params[filter] ?: 0.0
                    previewMat = when (filter) {
                        Filter.Contrast -> Filtering.contrast(previewMat, factor)
                        Filter.Brightness -> Filtering.brightness(previewMat, factor)
                        Filter.Blur -> Filtering.blur(previewMat, factor, factor)
                        Filter.LightBalance -> Filtering.lightBalance(previewMat, factor)
                        Filter.Hue -> Filtering.hueShift(previewMat, factor)
                        Filter.Sharpness -> Filtering.sharpen(previewMat, factor)
                    }
                }
                FormatConverters.matToBitmap(previewMat).also { previewMat.release() }
            }
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