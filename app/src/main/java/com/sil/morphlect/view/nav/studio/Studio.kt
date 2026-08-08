package com.sil.morphlect.view.nav.studio

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sil.morphlect.repository.PresetsRepository
import com.sil.morphlect.viewmodel.StudioViewModel
import com.sil.morphlect.enums.Section
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import com.sil.morphlect.data.EvaluationResult
import com.sil.morphlect.data.FingerprintData
import com.sil.morphlect.layerwork.StudioLayer
import com.sil.morphlect.imgproc.FormatConverters
import com.sil.morphlect.repository.AppConfigRepository
import com.sil.morphlect.repository.ExtensionsRepository
import com.sil.morphlect.repository.FingerprintRepository
import com.sil.morphlect.ui.theme.Typography
import com.sil.morphlect.view.AddingTextOverlay
import com.sil.morphlect.view.analysis.HistogramBottomSheet
import com.sil.morphlect.view.history.History
import com.sil.morphlect.view.analysis.InteractiveThumbnail
import com.sil.morphlect.view.OptionsBottomSheet
import com.sil.morphlect.view.animated.AnimatedSectionButton
import com.sil.morphlect.view.dialog.impl.LayeringDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Stable
class StudioUiState() {
    var showExitDialog   by mutableStateOf(false)
    var showHistoryStack by  mutableStateOf(false)
    var showHistogram    by mutableStateOf(false)
    var showOptionsSheet by mutableStateOf(false)
    var showLayersView   by mutableStateOf(false)
    var showLayering     by mutableStateOf(false)
    var croppingMode     by mutableStateOf(false)
    var cropUpCorner     by mutableStateOf<Offset?>(null)
    var cropDownCorner   by mutableStateOf<Offset?>(null)
    var addingImage      by mutableStateOf(false)
    var addingText       by mutableStateOf(false)
    var fingerprint      by mutableStateOf<FingerprintData?>(null)
}

@Composable
fun Studio(
    navController:     NavController,
    vm:                StudioViewModel,
    presetsRepository: PresetsRepository,
    configRepository:  AppConfigRepository,
    extensionsRepository: ExtensionsRepository,
    fingerprintRepository: FingerprintRepository,
) {
    val ctx     = LocalContext.current
    val density = LocalDensity.current

    val thumbnailSizePx = with(density) {
        Size(330.dp.toPx(), 330.dp.toPx())
    }

    val state = remember { StudioUiState() }

    LaunchedEffect(Unit) {
        state.fingerprint = fingerprintRepository.load()
    }

    val advancedMode by configRepository.advancedMode.collectAsState(initial = false)

    // listen for back gesture - in case if it's accidental
    BackHandler { state.showExitDialog = true }

    suspend fun applyEvaluationResultWithKeyframes(result: EvaluationResult)
        = withContext(Dispatchers.IO) {
        vm.changeSection(Section.Filtering)
        delay(100L)

        result.outputs.entries.forEachIndexed { index, (key, value) ->
            val targetValue = value * 10
            delay(index * 400L)
            key.run {
                vm.changeSelectedEffect(this)
                delay(150L)

                val animatable = Animatable(vm.filterValues[this]!!.toFloat())

                launch {
                    animatable.animateTo(
                        targetValue = targetValue.toFloat(),
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }

                launch {
                    snapshotFlow { animatable.value }
                        .collect { vm.adjustEffect(value = it.toDouble()) }
                }
            }
        }
    }

    // consume pending results emitted while Studio was not collecting,
    // then keep collecting live results while Studio is visible.
    LaunchedEffect(Unit) {
        vm.consumePendingEvaluation()?.let { pending ->
            applyEvaluationResultWithKeyframes(pending)
        }

        vm.evaluationResult.collect { result ->
            applyEvaluationResultWithKeyframes(result)
        }
    }

    Scaffold { _ ->
        with(state) {
            when {
                showHistoryStack -> History(
                    onDismissRequest = { showHistoryStack = false },
                    undoStack = vm.undoStack,
                    redoStack = vm.redoStack,
                    onUndo = { index -> vm.undoCommandAtIndex(index) },
                    onRedo = { index -> vm.redoCommandAtIndex(index) },
                )

                showLayering -> LayeringDialog(
                    layers = vm.previewLayers,
                    onRemoveLayer = { _ -> vm.removeLayer(vm.layers.size - 1) },
                    onMergeLayerWithBelow = { i -> vm.mergeLayerWithAbove(i) },
                    onDismissRequest = { showLayering = false },
                    onInterchangeLayers = { l1, l2 -> vm.interchangeLayers(l1, l2) },
                    onVisibilityToggle = { vm.toggleVisibilityOfLayer(it) }
                )

                showOptionsSheet -> OptionsBottomSheet(
                    onNavigate = { route -> navController.navigate(route) },
                    onDismiss = { showOptionsSheet = false }
                )

                showExitDialog -> AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    title = { Text("quit app?") },
                    text = { Text("all unsaved changes will be lost.") },
                    confirmButton = {  },
                    dismissButton = { // you can stick everything in one 'button'
                        TextButton(onClick = {
                            (ctx as? ComponentActivity)?.finish()
                        }) { Text("quit") }

                        TextButton(onClick = {
                            showExitDialog = false
                            navController.navigate("pick")
                            vm.clearComposition()
                        }) {
                            Text("choose something else")
                        }

                        TextButton(onClick = { showExitDialog = false }) {
                            Text("no")
                        }
                    }
                )

                showHistogram -> HistogramBottomSheet(
                    onDismissRequest = { showHistogram = false },
                    colorReference = vm.previewBitmap!!
                )

                addingText -> AddingTextOverlay(
                    onDismissRequest = { addingText = false },
                    onConfirm = { text, size, pos, color ->
                        vm.addTextLayer(text, size, pos, color)
                        addingText = false
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                AnimatedVisibility(
                    visible = state.showLayersView,
                    enter = slideInHorizontally { it },
                    exit = slideOutHorizontally { it }
                ) {
                    FloatingActionButton(
                        containerColor = MaterialTheme.colorScheme.primary,
                        onClick = { state.showLayering = true },
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Sort, "layering")
                    }
                }

                Spacer(Modifier.size(2.dp))

                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.primary,
                    onClick = { state.showLayersView = !state.showLayersView }
                ) {
                    if (state.showLayersView)
                        Icon(Icons.Default.LayersClear, "layers")
                    else
                        Icon(Icons.Default.Layers, "layers")
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Row {
                    AnimatedSectionButton(
                        onClick = { vm.changeSection(Section.Filtering) },
                        isSelected = vm.section == Section.Filtering,
                    ) {
                        Text("filtering", style = Typography.bodyLarge, fontSize = 14.sp)
                    }
                    AnimatedSectionButton(
                        onClick = { vm.changeSection(Section.SmartFeatures) },
                        isSelected = vm.section == Section.SmartFeatures
                    ) {
                        Text("smart features", style = Typography.bodyLarge, fontSize = 14.sp)
                    }
                    AnimatedSectionButton(
                        onClick = { vm.changeSection(Section.ImageManipulation) },
                        isSelected = vm.section == Section.ImageManipulation
                    ) {
                        Text("manipulation", style = Typography.bodyLarge, fontSize = 14.sp)
                    }
                }

                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    ElevatedButton(
                        enabled = vm.canUndo,
                        onClick = { vm.undoLastCommand() }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "undo")
                    }

                    ElevatedButton(
                        enabled = vm.canRedo,
                        onClick = { vm.redoLastCommand() }) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "redo")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    ElevatedButton(onClick = { state.showHistoryStack = true }) {
                        Icon(Icons.Default.History, contentDescription = "history")
                    }

                    ElevatedButton(onClick = { state.showOptionsSheet = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "options")
                    }
                }

                Spacer(modifier = Modifier.size(10.dp))

                // thumbnail
                InteractiveThumbnail(
                    layers = vm.previewLayers,
                    expandLayers = state.showLayersView,
                    croppingMode = state.croppingMode,
                    cropUpCorner = state.cropUpCorner,
                    cropDownCorner = state.cropDownCorner,
                    onDragStart = { state.cropUpCorner = it },
                    onDrag = { state.cropDownCorner = it; state.showLayersView = false },
                )

                // animate section switching using AnimatedContent
                AnimatedContent(
                    targetState = vm.section,
                    transitionSpec = {
                        (fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 }) togetherWith
                                (fadeOut(tween(300)) + slideOutVertically(tween(300)) { -it / 2 })
                    }
                ) { targetState ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        when (targetState) {
                            Section.Filtering -> FilteringSection(vm, presetsRepository, state.fingerprint)
                            Section.SmartFeatures -> SmartFeaturesSection(
                                navController,
                                { vm.emitEvaluationResult(it) },
                                configRepository,
                                extensionsRepository
                            )
                            Section.ImageManipulation -> ImageManipulationSection(
                                croppingMode = state.croppingMode,
                                onCropToggle = {
                                    state.croppingMode = !state.croppingMode
                               },
                                onCropApply = { cropAll, outerCrop ->
                                    if (state.cropUpCorner != null && state.cropDownCorner != null)
                                        if (cropAll)
                                            vm.cropLayers(
                                                state.cropUpCorner!!,
                                                state.cropDownCorner!!,
                                                thumbnailSizePx,
                                                outerCrop
                                            )
                                        else vm.cropLayer(vm.layers.size - 1, state.cropUpCorner!!, state.cropDownCorner!!, thumbnailSizePx, outerCrop)
                                },
                                addingImage = state.addingImage,
                                onImageAddToggle = { state.addingImage = true },
                                addingText = state.addingText,
                                onAddText = { state.addingText = true },
                                onAddImage = { bmp ->
                                    val mat = FormatConverters.bitmapToMat(bmp)
                                    vm.addLayer(StudioLayer(mat))
                                    state.addingImage = false
                                },
                                onCancel = { state.croppingMode = false; state.addingImage = false; state.addingText = false }
                            )
                        }

                        if (advancedMode)
                            TextButton(onClick = { state.showHistogram = true }) {
                                Text("histogram")
                            }
                    }
                }
            }
        }
    }
}