package com.sil.morphlect.logic

import com.sil.morphlect.data.EditorLayer
import org.opencv.core.Mat

/**
 * safely manages layers and operations with layers.
 */
class LayerManager {
    var layers: MutableList<EditorLayer>
        private set

    constructor(layers: MutableList<EditorLayer>) {
        this.layers = layers
    }

    fun removeLayer(index: Int = 0) {
        var layer = layers.removeAt(index)
        layer.close()
    }

    fun addLayer(name: String = "layer ${layers.size - 1}", mat: Mat? = null) {
        mat?.let {
            layers.add(EditorLayer(name, it))
        } ?: layers.add(EditorLayer.emptyNamed(name))
    }

    fun mergeLayerWithBelow(index: Int) {
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
}
