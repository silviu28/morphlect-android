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
import com.sil.morphlect.layerwork.StudioLayer
import com.sil.morphlect.imgproc.FormatConverters
import com.sil.morphlect.repository.AppConfigRepository
import com.sil.morphlect.repository.ExtensionsRepository
import com.sil.morphlect.ui.theme.Typography
import com.sil.morphlect.view.AddingTextOverlay
import com.sil.morphlect.view.analysis.HistogramBottomSheet
import com.sil.morphlect.view.history.History
import com.sil.morphlect.view.analysis.InteractiveThumbnail
import com.sil.morphlect.view.OptionsBottomSheet
import com.sil.morphlect.view.animated.AnimatedSectionButton
import com.sil.morphlect.view.dialog.impl.LayeringDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
}

@Composable
fun Studio(
    navController:     NavController,
    vm:                StudioViewModel,
    presetsRepository: PresetsRepository,
    configRepository:  AppConfigRepository,
    extensionsRepository: ExtensionsRepository,
) {
    val ctx     = LocalContext.current
    val density = LocalDensity.current

    val thumbnailSizePx = with(density) {
        Size(330.dp.toPx(), 330.dp.toPx())
    }

    val state = remember { StudioUiState() }

    val imagePickLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.run {
            val bitmap = FormatConverters.uriToBitmap(ctx, this)
            val mat = FormatConverters.bitmapToMat(bitmap)
            vm.addLayer(StudioLayer(mat))
            state.addingImage = false
        }
    }

    val advancedMode by configRepository.advancedMode.collectAsState(initial = false)

    // listen for back gesture - in case if it's accidental
    BackHandler { state.showExitDialog = true }

    // listen to emissions of evaluation results.
    // apply filtering with keyframing
    LaunchedEffect(Unit) {
        vm.evaluationResult.collect { result ->
            vm.changeSection(Section.Filtering)
            delay(100L)

            result.filters.entries.forEachIndexed { index, (key, targetValue) ->
                delay(index * 400L)
                vm.changeSelectedEffect(key)
                delay(150L)

                val animatable = Animatable(vm.filterValues[key]!!.toFloat())

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

    Scaffold { _ ->
        when {
            state.showHistoryStack -> History(
                onDismissRequest = { state.showHistoryStack = false },
                undoStack = vm.undoStack,
                redoStack = vm.redoStack,
                onUndo = { index -> vm.undoCommandAtIndex(index) },
                onRedo = { index -> vm.redoCommandAtIndex(index) },
            )

            state.showLayering -> LayeringDialog(
                layers = vm.layers,
                onRemoveLayer = { _ -> vm.removeLayer(vm.layers.size - 1) },
                onMergeLayerWithBelow = { i -> vm.mergeLayerWithAbove(i) },
                onDismissRequest = { state.showLayering = false },
                onInterchangeLayers = { l1, l2 -> vm.interchangeLayers(l1, l2) },
                onVisibilityToggle = { vm.toggleVisibilityOfLayer(it) }
            )

            state.showOptionsSheet -> OptionsBottomSheet(
                navController,
                onDismiss = { state.showOptionsSheet = false }
            )

            state.showExitDialog -> AlertDialog(
                onDismissRequest = { state.showExitDialog = false },
                title = { Text("quit app?") },
                text = { Text("all unsaved changes will be lost.") },
                confirmButton = {
                    TextButton(onClick = {
                        (ctx as? ComponentActivity)?.finish()
                    }) { Text("quit") }
                },
                dismissButton = {
                    TextButton(onClick = { state.showExitDialog = false }) {
                        Text("no")
                    }
                }
            )

            state.showHistogram -> HistogramBottomSheet(
                onDismissRequest = { state.showHistogram = false },
                colorReference = vm.previewBitmap!!
            )

            state.addingText -> AddingTextOverlay(
                onDismissRequest = { state.addingText = false },
                onConfirm = { text -> vm.addTextLayer(text); state.addingText = false }
            )
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
                        enabled = vm.canUndo(),
                        onClick = {
                        vm.undoLastCommand()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "undo")
                    }

                    ElevatedButton(
                        enabled = vm.canRedo(),
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
                    onDrag = { state.cropDownCorner = it },
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
                            Section.Filtering -> FilteringSection(vm, presetsRepository)
                            Section.SmartFeatures -> SmartFeaturesSection(
                                navController,
                                vm,
                                configRepository,
                                extensionsRepository
                            )
                            Section.ImageManipulation -> ImageManipulationSection(
                                vm = vm,
                                croppingMode = state.croppingMode,
                                onCropToggle = { state.croppingMode = !state.croppingMode },
                                onCropApply = {
                                    if (state.cropUpCorner != null && state.cropDownCorner != null)
                                        vm.cropLayers(
                                            state.cropUpCorner!!,
                                            state.cropDownCorner!!,
                                            thumbnailSizePx
                                        )
                                },
                                addingImage = state.addingImage,
                                onImageAddToggle = { imagePickLauncher.launch("image/*") },
                                addingText = state.addingText,
                                onAddText = { state.addingText = true },
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