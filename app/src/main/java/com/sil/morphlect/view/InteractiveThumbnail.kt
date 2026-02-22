package com.sil.morphlect.view

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.ZoomInMap
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sil.morphlect.data.EditorLayer
import com.sil.morphlect.view.custom.FlickeringLedDotProgressIndicator
import com.sil.morphlect.view.custom.ResizableCropRegion

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
    expandLayers: Boolean = false,
    croppingMode: Boolean,
    cropUpCorner: Offset?,
    cropDownCorner: Offset?,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
) {
    var zoomScale      by remember { mutableStateOf(1f) }
    var positionOffset by remember { mutableStateOf(Offset.Zero) }

    val thumbnailTransformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        zoomScale = (zoomScale * zoomChange).coerceIn(minZoomInRatio, maxZoomOutRatio)
        positionOffset += offsetChange
    }

    val transition = updateTransition(expandLayers, label = "stack")

    // animates the tilting of the layers with an angle of 45 degs
    val tilt by transition.animateFloat(
        label = "tilt",
        transitionSpec = { tween(300, easing = EaseInOutCubic) }
    ) { tilted -> if (tilted) 45f else 0f }

    // animates the spreading out of layers
    val spread by transition.animateFloat(
        label = "spread",
        transitionSpec = { tween(300, easing = EaseInOutCubic) }
    ) { isSpread -> if (isSpread) 1f else 0f }

    Box(
        modifier = Modifier
            .width(width)
            .height(height)
    ) {
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
                layers.forEachIndexed { index, layer ->
                    val offset = index * 60f * spread

                    Image(
                        bitmap = layer.visual,
                        contentDescription = "preview",
                        modifier = Modifier
                            .size(300.dp)
                            .graphicsLayer {
                                cameraDistance = 12 * density
                                rotationX = tilt
                                translationY = -offset
                                alpha = if (expandLayers) 1f - (index * .08f) else 1f
                            }
                    )
                    if (croppingMode) {
                        ResizableCropRegion(cropUpCorner, cropDownCorner, onDragStart, onDrag)
                    }
                }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            if (zoomScale != 1f)
                IconButton(onClick = { zoomScale = 1f }) {
                    Icon(Icons.Default.ZoomInMap, contentDescription = "reset zoom")
                }
            if (positionOffset != Offset.Zero)
                IconButton(onClick = { positionOffset = Offset.Zero }) {
                    Icon(Icons.Default.OpenWith, contentDescription = "reset position")
                }
        }
    }
}