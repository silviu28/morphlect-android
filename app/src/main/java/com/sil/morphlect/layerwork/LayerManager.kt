package com.sil.morphlect.layerwork

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import org.opencv.core.Size as CVSize
import androidx.compose.ui.geometry.Size
import com.sil.morphlect.extension.extend
import java.io.Closeable

/**
 * safely manages layers and operations with layers.
 */
class LayerManager() : Closeable {
    var maxWidth: Int = 1
    var maxHeight: Int = 1

    // find the biggest dimensions
    var layers: MutableList<StudioLayer> = mutableStateListOf()
    var downscaledLayers = derivedStateOf { layers.map { it.downscaledUniformly() } }

    fun removeLayer(index: Int = 0) {
        if (layers.isEmpty()) return
        val layer = layers.removeAt(index)
        layer.close()
    }

    private fun StudioLayer.extend(size: CVSize)
        = StudioLayer(mat.extend(size))

    fun addLayer(layer: StudioLayer) {
        // determine target dimensions
        val targetWidth = maxOf(maxWidth, layer.width)
        val targetHeight = maxOf(maxHeight, layer.height)

        val needsResize = targetWidth != maxWidth || targetHeight != maxHeight

        if (needsResize) {
            // resize all existing layers to new dimensions
            val resizedLayers = layers.map { existingLayer ->
                existingLayer.extend(CVSize(targetWidth.toDouble(), targetHeight.toDouble()))
            }
            layers.clear()
            layers.addAll(resizedLayers)

            // Update max dimensions
            maxWidth = targetWidth
            maxHeight = targetHeight
        }

        // add the new layer (resized if needed)
        val finalLayer = if (layer.width != targetWidth || layer.height != targetHeight) {
            layer.extend(CVSize(targetWidth.toDouble(), targetHeight.toDouble()))
        } else {
            layer
        }
        layers.add(finalLayer)
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

    fun cropLayers(upCorner: Offset, downCorner: Offset, size: Size, outerCrop: Boolean) {
        val cropped = layers.map { it.cropped(upCorner, downCorner, size, outerCrop) }
        layers.clear()
        layers.addAll(cropped)
    }

    fun cropLayer(index: Int, upCorner: Offset, downCorner: Offset, size: Size, outerCrop: Boolean) {
        if (index >= 0 && index < layers.size) {
            val cropped = layers[index].cropped(upCorner, downCorner, size, outerCrop)
            val copy = layers.mapIndexed { idx, layer -> if (idx == index) cropped else layer }
            layers.clear()
            layers.addAll(copy)
        }
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