package com.sil.morphlect.view

import android.graphics.Point
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sil.morphlect.repository.PresetsRepository
import com.sil.morphlect.viewmodel.EditorViewModel
import com.sil.morphlect.viewmodel.PickImageViewModel
import com.sil.morphlect.enums.Section
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import com.sil.morphlect.repository.AppConfigRepository
import com.sil.morphlect.view.animated.AnimatedSectionButton
import com.sil.morphlect.view.custom.FlickeringLedDotProgressIndicator
import com.sil.morphlect.view.dialog.LayeringDialog


@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun Editor(
    navController:     NavController,
    imageViewModel:    PickImageViewModel,
    editorViewModel:   EditorViewModel,
    presetsRepository: PresetsRepository,
    configRepository:  AppConfigRepository
) {
    val vm = editorViewModel
    val imageUri = imageViewModel.imageUri
    val ctx = LocalContext.current

    var showExitDialog   by remember { mutableStateOf(false) }
    var showHistoryStack by remember { mutableStateOf(false) }
    var showHistogram    by remember { mutableStateOf(false) }
    var showOptionsSheet by remember { mutableStateOf(false) }
    var showLayersView   by remember { mutableStateOf(false) }
    var showLayering     by remember { mutableStateOf(false) }

    var croppingMode     by remember { mutableStateOf(false) }
    var cropUpCorner     by remember { mutableStateOf<Offset?>(null) }
    var cropDownCorner   by remember { mutableStateOf<Offset?>(null) }

    val advancedMode     by configRepository.advancedMode.collectAsState(initial = false)

    // listen for back gesture - in case if it's accidental
    BackHandler {
        showExitDialog = true
    }

    LaunchedEffect(imageUri) {
        if (imageUri != null) {
            vm.loadImage(ctx, imageUri)
        }
    }

    Scaffold { _ ->
        if (showHistoryStack) {
            History(
                onDismissRequest = { showHistoryStack = false },
                undoStack = vm.undoStack,
                redoStack = vm.redoStack,
                onUndo = { vm.undoLastCommand() },
                onRedo = { vm.redoLastCommand() },
            )
        }

        if (showLayering) {
            LayeringDialog(
                layers = vm.layers,
                onRemoveLayer = { _ -> vm.removeLayer(vm.layers.size - 1) },
                onAddLayer = { vm.addLayer("new") },
                onDismissRequest = { showLayering = false },
                onInterchangeLayers = { l1, l2 -> vm.interchangeLayers(l1, l2) },
            )
        }

        if (showOptionsSheet) {
            OptionsBottomSheet(
                navController,
                onDismiss = { showOptionsSheet = false }
            )
        }

        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("quit app?") },
                text = { Text("all unsaved changes will be lost.") },
                confirmButton = {
                    TextButton(onClick = {
                        (ctx as? ComponentActivity)?.finish()
                    }) { Text("quit") }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text("no")
                    }
                }
            )
        }

        if (showHistogram) {
            HistogramBottomSheet(
                onDismissRequest = { showHistogram = false },
                colorReference = vm.previewBitmap!!
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
            ) {
                if (showLayersView) {
                    FloatingActionButton(onClick = { showLayering = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, "layering")
                    }
                }
                FloatingActionButton(onClick = { showLayersView = !showLayersView }) {
                    if (showLayersView)
                        Icon(Icons.Default.LayersClear, "layers")
                    else
                        Icon(Icons.Default.Layers, "layers")
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,

            ) {
                Row(modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(36.dp)
                        )
                ) {
                    AnimatedSectionButton(
                        onClick = { vm.changeSection(Section.Filtering) },
                        isSelected = vm.section == Section.Filtering,
                    ) {
                        Text("filtering")
                    }
                    AnimatedSectionButton(
                        onClick = { vm.changeSection(Section.SmartFeatures) },
                        isSelected = vm.section == Section.SmartFeatures
                    ) {
                        Text("smart features")
                    }
                    AnimatedSectionButton(
                        onClick = { vm.changeSection(Section.ImageManipulation) },
                        isSelected = vm.section == Section.ImageManipulation
                    ) {
                        Text("manipulation")
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

                    ElevatedButton(onClick = { showHistoryStack = true }) {
                        Icon(Icons.Default.History, contentDescription = "history")
                    }

                    ElevatedButton(onClick = { showOptionsSheet = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "options")
                    }
                }

                Spacer(modifier = Modifier.size(10.dp))

                // thumbnail
                InteractiveThumbnail(
                    layers = vm.layers,
                    expandLayers = showLayersView,
                    croppingMode = croppingMode,
                    cropUpCorner = cropUpCorner,
                    cropDownCorner = cropDownCorner,
                    onDragStart = { cropUpCorner = it },
                    onDrag = { cropDownCorner = it },
                )

                // animate section switching using AnimatedContent
                AnimatedContent(
                    targetState = vm.section,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
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
                            Section.SmartFeatures -> SmartFeaturesSection(navController, vm)
                            Section.ImageManipulation -> ImageManipulationSection(
                                vm = vm,
                                croppingMode,
                                onCropToggle = { croppingMode = !croppingMode },
                                onCropApply = { vm.cropLayers(cropUpCorner!!, cropDownCorner!!) },
                            )
                        }

                        if (advancedMode) {
                            TextButton(onClick = { showHistogram = true }) {
                                Text("histogram")
                            }
                        }
                    }
                }
            }
        }
    }
}