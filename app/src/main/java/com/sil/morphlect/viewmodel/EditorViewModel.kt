package com.sil.morphlect.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sil.morphlect.command.impl.BlurCommand
import com.sil.morphlect.command.impl.BrightnessCommand
import com.sil.morphlect.command.impl.ContrastCommand
import com.sil.morphlect.command.EditorCommand
import com.sil.morphlect.command.EditorCommandManager
import com.sil.morphlect.command.impl.HueCommand
import com.sil.morphlect.command.impl.LightBalanceCommand
import com.sil.morphlect.command.impl.SharpnessCommand
import com.sil.morphlect.data.EditorLayer
import com.sil.morphlect.enums.Effect
import com.sil.morphlect.enums.Section
import com.sil.morphlect.logic.FormatConverters
import com.sil.morphlect.logic.LayerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.core.Mat

class EditorViewModel : ViewModel(), EditorCommandManager {
    override var undoStack = mutableStateListOf<EditorCommand>()
    override var redoStack = mutableStateListOf<EditorCommand>()

    var originalMat by mutableStateOf<Mat?>(null)
        private set

    private val layerManager = LayerManager(mutableStateListOf())
    val layers by derivedStateOf {
        layerManager.layers.map { layer ->
            if (!layer.visible) return@map EditorLayer.emptyNamed("")
            // TODO works but a bit verbose
            (undoStack +
                EditorCommand.of(selectedEffect, effectValues[selectedEffect]!!)
            ).fold(layer) { layer, comm -> comm.execute(layer) }
    } }
    var originalLayers = mutableStateListOf<EditorLayer>()

    override fun redoLastCommand() {
        if (redoStack.isEmpty()) return
        val command = redoStack.removeAt(redoStack.lastIndex)
        undoStack.add(command)
        updateLayers()
    }

    override fun undoLastCommand() {
        if (undoStack.isEmpty()) return
        val command = undoStack.removeAt(undoStack.lastIndex)
        redoStack.add(command)
        updateLayers()
    }

    private fun updateLayers() {
//        val src = originalMat ?: return
        viewModelScope.launch(Dispatchers.Default) {
            // compute processed layers
//            var processed = layers.map { layer ->
//                undoStack.fold(layer) { layer, comm -> comm.execute(layer) }
//            }
//
//            // undo stack + current adjustment
//            if (hasActiveAdjustment) {
//                val previewCommand = createCommandForEffect(
//                    selectedEffect,
//                    effectValues[selectedEffect] ?: .0
//                )
//                processed = processed.map { previewCommand.execute(it) }
//            }

//            withContext(Dispatchers.Main) {
//                processedBitmap = processedBmp
//                previewBitmap = previewBmp
//                layerManager.layers = processed.toMutableList()
//            }
        }
    }

    override fun runCommand(command: EditorCommand) {
        redoStack.clear()
        undoStack.add(command)
        updateLayers()
    }

    // states
    var processedBitmap by mutableStateOf<Bitmap?>(null)
        private set

    var previewBitmap by mutableStateOf<Bitmap?>(null)
        private set

    var section by mutableStateOf(Section.Filtering)
        private set

    var effectValues = mutableStateMapOf<Effect, Double>().apply {
        Effect.entries.forEach { effect -> put(effect, .0) }
    }
        private set

    var selectedEffect by mutableStateOf(Effect.Contrast)
        private set

    fun changeSection(section: Section) {
        this.section = section
    }

    private fun createCommandForEffect(effect: Effect, factor: Double): EditorCommand {
        return when (effect) {
            Effect.Contrast -> ContrastCommand(factor)
            Effect.Brightness -> BrightnessCommand(factor)
            Effect.Blur -> BlurCommand(
                xFactor = factor,
                yFactor = effectValues[Effect.BlurSecondAxis] ?: .0
            )
            Effect.Sharpness -> SharpnessCommand(factor)
            Effect.Hue -> HueCommand(factor)
            Effect.LightBalance -> LightBalanceCommand(factor)
            else -> ContrastCommand(.0)
        }
    }

    fun changeSelectedEffect(selectedEffect: Effect) {
        // apply current effect before switching (if it has a non-zero value)
        applyCurrentEffect()
        this.selectedEffect = selectedEffect
//        updatePreviewBitmapOnly() // update preview to show new effect
    }

    fun adjustEffect(effect: Effect = selectedEffect, value: Double) {
        effectValues[effect] = value

        // update only preview (live adjustment)
        if (effect == selectedEffect) {
//            updatePreviewBitmapOnly()
        }
    }

    fun applyCurrentEffect() {
        val effect = selectedEffect
        val value = effectValues[effect] ?: .0

        // only add command if value is non-zero
        if (value != .0) {
            val command = createCommandForEffect(effect, value)
            runCommand(command)
            // reset the effect value after applying
            effectValues[effect] = .0
            // clear redo stack since we made a new change
            redoStack.clear()
        }
    }

//    private fun updatePreviewBitmapOnly() {
//        val src = originalMat ?: return
//        viewModelScope.launch(Dispatchers.Default) {
//            var processed = src.clone()
//
//            // apply all undo stack commands
//            undoStack.forEach { command ->
//                processed = command.execute(processed)
//            }
//
//            // Apply current adjustment on top
//            val currentValue = effectValues[selectedEffect] ?: .0
//            if (currentValue != .0) {
//                val previewCommand = createCommandForEffect(selectedEffect, currentValue)
//                processed = previewCommand.execute(processed)
//            }
//
//            val bitmap = FormatConverters.matToBitmap(processed)
//            processed.release()
//
//            previewBitmap = bitmap
//        }
//    }

    private val hasActiveAdjustment get() = (effectValues[selectedEffect] ?: .0) != .0

    fun loadImage(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.Default) {
            val bitmap = FormatConverters.uriToBitmap(context, uri)
            val mat = FormatConverters.bitmapToMat(bitmap)

            layerManager.addLayer(EditorLayer("layer 1", mat))

            // release old image if exists
            originalMat?.release()

            // store original
            originalMat = mat.clone()

            // clear all state
            undoStack.clear()
            redoStack.clear()

            // reset effect values
            effectValues.forEach { (effect, _) -> effectValues[effect] = .0 }

            val initialBitmap = FormatConverters.matToBitmap(originalMat!!)

            withContext(Dispatchers.Main) {
                processedBitmap = initialBitmap
                previewBitmap = initialBitmap
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        originalMat?.release()
        layerManager.close()
    }

    fun canUndo(): Boolean = undoStack.isNotEmpty()
    fun canRedo(): Boolean = redoStack.isNotEmpty()

    fun removeLayer(index: Int) {
        layerManager.removeLayer(index)
    }

    fun addLayer(name: String, layer: EditorLayer = EditorLayer.emptyNamed(name)) {
        layerManager.addLayer(layer)
        originalLayers.add(layer)
    }

    fun interchangeLayers(firstIndex: Int, secondIndex: Int) {
        // spaghetti........
        if (firstIndex >= 0 && firstIndex < layerManager.layers.size
            && secondIndex >= 0 && secondIndex < layerManager.layers.size) {
            layerManager.interchangeLayers(firstIndex, secondIndex)
        }
    }

    fun cropLayers(upCorner: Offset, downCorner: Offset, size: Size) {
        layerManager.cropLayers(upCorner, downCorner, size)
    }

    fun toggleVisibilityOfLayer(index: Int) {
//        layerManager.toggleVisibilityOf(index)
        layerManager.layers[index].apply { visible = !visible }
    }

    fun mergeLayerWithAbove(index: Int) {
        if (index < layers.size)
            layerManager.mergeLayerWithAbove(index)
    }
}