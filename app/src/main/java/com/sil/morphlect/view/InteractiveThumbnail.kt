package com.sil.morphlect.view

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sil.morphlect.data.EditorLayer
import com.sil.morphlect.view.custom.FlickeringLedDotProgressIndicator

/**
 * a component that displays a thumbnail for a given image. it can be repositioned and zoomed in or out.
 */
@Composable
fun InteractiveThumbnail(
    layers: List<EditorLayer>,
    width: Dp = 300.dp,
    height: Dp = 300.dp,
    minZoomInRatio: Float = .1f,
    maxZoomOutRatio: Float = 5f,
) {
    var zoomScale by remember { mutableStateOf(1f) }
    var positionOffset by remember { mutableStateOf(Offset.Zero) }
    val thumbnailTransformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        zoomScale = (zoomScale * zoomChange).coerceIn(minZoomInRatio, maxZoomOutRatio)
        positionOffset += offsetChange
    }

    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(RectangleShape)
            .transformable(state = thumbnailTransformState)
            .graphicsLayer(
                scaleX = zoomScale,
                scaleY = zoomScale,
                translationX = positionOffset.x,
                translationY = positionOffset.y
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (layers.isEmpty())
            FlickeringLedDotProgressIndicator()
        else
            layers.forEach { layer ->
            Image(
                bitmap = layer.visual,
                contentDescription = "preview",
                modifier = Modifier.size(300.dp)
            )
        }
    }
}