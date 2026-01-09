package com.sil.morphlect.view.custom

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

// A slider with the track shape made out of LED dots.
@Composable
fun LedDotSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    dotRadius: Float = 6f,
    dotSpacing: Float = 3f,
    rows: Int = 3,
    onColor: Color = MaterialTheme.colorScheme.primary,
    offColor: Color = Color(0xFF2C2C2C),
    thumbColor: Color = MaterialTheme.colorScheme.primary,
    thumbWidth: Float = 8f,
    thumbHeight: Float = 32f
) {
    var sliderWidth by remember { mutableStateOf(0f) }
    val thumbWidthDp = thumbWidth.dp
    val thumbHeightDp = thumbHeight.dp

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (sliderWidth > 0) {
                        val newValue = (offset.x / sliderWidth)
                            .coerceIn(0f, 1f)
                            .mapToRange(valueRange)
                        onValueChange(newValue)
                    }
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    if (sliderWidth > 0) {
                        val newValue = (change.position.x / sliderWidth)
                            .coerceIn(0f, 1f)
                            .mapToRange(valueRange)
                        onValueChange(newValue)
                    }
                }
            }
    ) {
        // calculate the slider parameters
        sliderWidth = size.width
        val centerY = size.height / 2
        val normalizedValue = value.normalizeFromRange(valueRange)
        val thumbX = normalizedValue * size.width

        // calculate how many dots fit horizontally
        val dotDiameter = dotRadius * 2
        val totalDotWidth = dotDiameter + dotSpacing
        val dotsPerRow = ((size.width + dotSpacing) / totalDotWidth).toInt()

        // calculate vertical spacing to center the rows
        val totalRowHeight = (rows * dotDiameter) + ((rows - 1) * dotSpacing)
        val startY = centerY - (totalRowHeight / 2) + dotRadius

        // draw the dots, similarly to a grid
        for (row in 0 until rows) {
            val y = startY + (row * (dotDiameter + dotSpacing))

            for (col in 0 until dotsPerRow) {
                val x = col * totalDotWidth + dotRadius

                // depending on the thumb position choose if the dot should be lit or not
                val isLit = x <= thumbX
                val dotColor = if (isLit) onColor else offColor

                drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = Offset(x, y)
                )
            }
        }

        // draw thumb at the center of the track's end
        val thumbLeft = (thumbX - thumbWidth / 2).coerceIn(0f, size.width - thumbWidth)
        val thumbTop = centerY - thumbHeight / 2

        drawRoundRect(
            color = thumbColor,
            topLeft = Offset(thumbLeft, thumbTop),
            size = androidx.compose.ui.geometry.Size(thumbWidth, thumbHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(thumbWidth / 2, thumbWidth / 2)
        )
    }
}

// Helper extension functions
private fun Float.normalizeFromRange(range: ClosedFloatingPointRange<Float>): Float {
    return ((this - range.start) / (range.endInclusive - range.start)).coerceIn(0f, 1f)
}

private fun Float.mapToRange(range: ClosedFloatingPointRange<Float>): Float {
    return range.start + (this * (range.endInclusive - range.start))
}