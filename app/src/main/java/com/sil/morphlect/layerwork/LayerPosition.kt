package com.sil.morphlect.layerwork

import androidx.compose.ui.unit.IntOffset

enum class LayerPosition {
    TopLeft,
    TopCenter,
    TopRight,
    CenterLeft,
    Center,
    CenterRight,
    BottomLeft,
    BottomCenter,
    BottomRight;

    fun toOffset(canvasWidth: Int, canvasHeight: Int, layerWidth: Int, layerHeight: Int): IntOffset {
        val x = when (this) {
            TopLeft, CenterLeft, BottomLeft -> 0
            TopCenter, Center, BottomCenter -> (canvasWidth - layerWidth) / 2
            TopRight, CenterRight, BottomRight -> canvasWidth - layerWidth
        }
        val y = when (this) {
            TopLeft, TopCenter, TopRight -> 0
            CenterLeft, Center, CenterRight -> (canvasHeight - layerHeight) / 2
            BottomLeft, BottomCenter, BottomRight -> canvasHeight - layerHeight
        }
        return IntOffset(x, y)
    }
}