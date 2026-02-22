package com.sil.morphlect.view.custom

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * a rectangle that gets resized by touch gestures.
*/
@Composable
fun ResizableCropRegion(
    cropUpCorner: Offset?,
    cropDownCorner: Offset?,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset -> onDragStart(offset) },
                    onDrag = { change, _ -> onDrag(change.position) }
                )
            }
    ) {
        val start = cropUpCorner
        val end = cropDownCorner

        if (start != null && end != null) {
            val topLeft = Offset(minOf(start.x, end.x), minOf(start.y, end.y))
            val bottomRight = Offset(maxOf(start.x, end.x), maxOf(start.y, end.y))
            val cropSize = Size(bottomRight.x - topLeft.x, bottomRight.y - topLeft.y)

            // area
            drawRect(
                color = Color.Transparent,
                topLeft = topLeft,
                size = cropSize,
            )

            // crop border
            drawRect(
                color = Color.White,
                topLeft = topLeft,
                size = cropSize,
                style = Stroke(width = 2.dp.toPx()),
            )
        }
    }
}