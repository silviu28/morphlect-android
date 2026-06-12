package com.sil.morphlect.layerwork

import androidx.compose.ui.unit.IntOffset

enum class LayerPosition {
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    CENTER_LEFT,
    CENTER,
    CENTER_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    BOTTOM_RIGHT;

    fun toOffset(canvasWidth: Int, canvasHeight: Int, layerWidth: Int, layerHeight: Int): IntOffset {
        val x = when (this) {
            TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> 0
            TOP_CENTER, CENTER, BOTTOM_CENTER -> (canvasWidth - layerWidth) / 2
            TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> canvasWidth - layerWidth
        }
        val y = when (this) {
            TOP_LEFT, TOP_CENTER, TOP_RIGHT -> 0
            CENTER_LEFT, CENTER, CENTER_RIGHT -> (canvasHeight - layerHeight) / 2
            BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> canvasHeight - layerHeight
        }
        return IntOffset(x, y)
    }
}