package com.sil.morphlect.logic

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.sil.morphlect.data.EditorLayer
import java.io.Closeable

/**
 * safely manages layers and operations with layers.
 */
class LayerManager : Closeable {
    var layers: MutableList<EditorLayer>

    constructor(layers: MutableList<EditorLayer>) {
        this.layers = layers
    }

    fun removeLayer(index: Int = 0) {
        var layer = layers.removeAt(index)
        layer.close()
    }

    fun addLayer(layer: EditorLayer) {
        layers.add(layer)
    }

    fun mergeLayerWithAbove(index: Int) {
        if (index == 0) return
        val mergedLayer = layers[index].mergeWith(layers[index - 1])
        layers.apply {
            set(index, mergedLayer)
            removeAt(index - 1)
        }
    }

    fun interchangeLayers(firstIndex: Int, secondIndex: Int) {
        layers.apply {
            val clone = get(firstIndex).clone()
            set(firstIndex, get(secondIndex))
            set(secondIndex, clone)
        }
    }

    fun cropLayers(upCorner: Offset, downCorner: Offset, size: Size) {
        layers = layers.map {
            it.crop(upCorner, downCorner, size)
        }.toMutableList()
    }

    /**
     frees the memory allocated by all layers (JNI optimization)
     */
    override fun close() {
        layers.forEach { it.close() }
    }

    fun toggleVisibilityOf(index: Int) {
        layers[index].apply { visible = !visible }
    }
}
