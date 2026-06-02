package com.sil.morphlect.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
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
import com.sil.morphlect.command.StudioCommand
import com.sil.morphlect.command.StudioCommandManager
import com.sil.morphlect.command.impl.HueCommand
import com.sil.morphlect.command.impl.LightBalanceCommand
import com.sil.morphlect.command.impl.SharpnessCommand
import com.sil.morphlect.layerwork.StudioLayer
import com.sil.morphlect.data.EvaluationResult
import com.sil.morphlect.enums.Filter
import com.sil.morphlect.enums.Section
import com.sil.morphlect.imgproc.FormatConverters
import com.sil.morphlect.layerwork.LayerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.core.Mat

class StudioViewModel : ViewModel, StudioCommandManager {
    constructor() : super() {
        viewModelScope.launch {
            _evaluationResult.collect { result ->
                _pendingEvaluation = result
            }
        }
    }

    private var _pendingEvaluation: EvaluationResult? = null
    val pendingEvaluation get() = _pendingEvaluation
    fun consumePendingEvaluation(): EvaluationResult? {
        val res = _pendingEvaluation
        _pendingEvaluation = null
        return res
    }

    private val _evaluationResult = MutableSharedFlow<EvaluationResult>()
    val evaluationResult = _evaluationResult.asSharedFlow()

    override var undoStack = mutableStateListOf<StudioCommand>()
    override var redoStack = mutableStateListOf<StudioCommand>()

    var originalMat by mutableStateOf<Mat?>(null)

    private val layerManager = LayerManager(mutableStateListOf())
    val layers by derivedStateOf {
        layerManager.layers.map { layer ->
            (undoStack +
                StudioCommand.of(selectedFilter, filterValues[selectedFilter]!!)
            ).fold(layer) { layer, comm -> comm.execute(layer)
                .also { it.visible = layer.visible } }
    } }
    val previewLayers by derivedStateOf {
        layerManager.downscaledLayers.value.map { layer ->
            (undoStack +
                StudioCommand.of(selectedFilter, filterValues[selectedFilter]!!)
            ).fold(layer) { layer, comm -> comm.execute(layer)
                .also { it.visible = layer.visible } }
        }
    }
    val originalLayers = mutableStateListOf<StudioLayer>()

    override fun redoLastCommand() {
        if (redoStack.isEmpty()) return
        val command = redoStack.removeAt(redoStack.lastIndex)
        undoStack.add(command)
    }

    override fun undoLastCommand() {
        if (undoStack.isEmpty()) return
        val command = undoStack.removeAt(undoStack.lastIndex)
        redoStack.add(command)
    }

    override fun runCommand(command: StudioCommand) {
        redoStack.clear()
        undoStack.add(command)
    }

    // states
    var processedBitmap by mutableStateOf<Bitmap?>(null)
        private set

    var previewBitmap by mutableStateOf<Bitmap?>(null)
        private set

    var section by mutableStateOf(Section.Filtering)
        private set

    var filterValues = mutableStateMapOf<Filter, Double>().apply {
        Filter.entries.forEach { effect -> put(effect, .0) }
    }
        private set

    var selectedFilter by mutableStateOf(Filter.Contrast)
        private set

    fun changeSection(section: Section) {
        this.section = section
    }

    private fun createCommandForEffect(filter: Filter, factor: Double): StudioCommand {
        return when (filter) {
            Filter.Contrast -> ContrastCommand(factor)
            Filter.Brightness -> BrightnessCommand(factor)
            Filter.Blur -> BlurCommand(
                xFactor = factor,
                yFactor = factor
            )
            Filter.Sharpness -> SharpnessCommand(factor)
            Filter.Hue -> HueCommand(factor)
            Filter.LightBalance -> LightBalanceCommand(factor)
        }
    }

    fun changeSelectedEffect(selectedFilter: Filter) {
        // apply current effect before switching (if it has a non-zero value)
        applyCurrentEffect()
        this.selectedFilter = selectedFilter
    }

    fun adjustEffect(filter: Filter = selectedFilter, value: Double) {
        filterValues[filter] = value
    }

    fun applyCurrentEffect() {
        val effect = selectedFilter
        val value = filterValues[effect] ?: .0

        // only add command if value is non-zero
        if (value != .0) {
            val command = createCommandForEffect(effect, value)
            runCommand(command)
            // reset the effect value after applying
            filterValues[effect] = .0
            // clear redo stack since we made a new change
            redoStack.clear()
        }
    }

    fun loadImage(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.Default) {
            // clear all state
            undoStack.clear()
            redoStack.clear()

            // reset effect values
            filterValues.forEach { (effect, _) -> filterValues[effect] = .0 }

            val bitmap = FormatConverters.uriToBitmap(context, uri)
            originalMat = FormatConverters.bitmapToMat(bitmap)

            layerManager.addLayer(StudioLayer(originalMat!!))

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

    val canUndo get() = undoStack.isNotEmpty()
    val canRedo get() = redoStack.isNotEmpty()

    fun removeLayer(index: Int) {
        if (layerManager.layers.size == 1) return // disallow complete deletion
        layerManager.removeLayer(index)
    }

    fun addLayer(layer: StudioLayer = StudioLayer.empty()) {
        layerManager.addLayer(layer)
        originalLayers.add(layer)
    }

    fun interchangeLayers(firstIndex: Int, secondIndex: Int) {
        if (firstIndex >= 0 && firstIndex < layerManager.layers.size
            && secondIndex >= 0 && secondIndex < layerManager.layers.size) {
            layerManager.interchangeLayers(firstIndex, secondIndex)
        }
    }

    fun cropLayers(upCorner: Offset, downCorner: Offset, size: Size) {
        layerManager.cropLayers(upCorner, downCorner, size)
    }

    fun toggleVisibilityOfLayer(index: Int) {
        layerManager.layers[index].apply { visible = !visible }
    }

    fun mergeLayerWithAbove(index: Int) {
        if (index < layers.size)
            layerManager.mergeLayerWithAbove(index)
    }

    fun addTextLayer(text: String) {
        val textLayer = StudioLayer.withText(text)
        layerManager.addLayer(textLayer)
    }

    fun emitEvaluationResult(result: EvaluationResult) {
        viewModelScope.launch {
            _evaluationResult.emit(result)
        }
    }

    fun undoCommandAtIndex(index: Int) {
        if (index >= 0 && undoStack.size > index) {
            val comm = undoStack.removeAt(index)
            redoStack.add(comm)
        }
    }

    fun redoCommandAtIndex(index: Int) {
        if (index >= 0 && redoStack.size > index) {
            val comm = redoStack.removeAt(index)
            undoStack.add(comm)
        }
    }
}