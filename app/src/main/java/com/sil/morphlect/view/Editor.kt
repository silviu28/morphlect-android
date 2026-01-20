package com.sil.morphlect.view

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentCompositionContext
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.sil.morphlect.PresetsRepository
import com.sil.morphlect.viewmodel.EditorViewModel
import com.sil.morphlect.viewmodel.PickImageViewModel
import com.sil.morphlect.enums.Section
import kotlin.math.roundToInt
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer


@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun Editor(
    navController: NavController,
    imageViewModel: PickImageViewModel,
    editorViewModel: EditorViewModel,
    presetsRepository: PresetsRepository
) {
    val vm = editorViewModel
    val imageUri = imageViewModel.imageUri
    val ctx = LocalContext.current

    var showExitDialog by remember { mutableStateOf(false) }

    var thumbnailZoomScale by remember { mutableStateOf(1f) }
    var thumbnailOffset by remember { mutableStateOf(Offset.Zero) }
    val thumbnailTransformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        thumbnailZoomScale = (thumbnailZoomScale * zoomChange).coerceIn(.1f, 5f)
        thumbnailOffset += offsetChange
    }

    BackHandler {
        showExitDialog = true
    }

    var showOptionsSheet by remember { mutableStateOf(false) }
    LaunchedEffect(imageUri) {
        if (imageUri != null) {
            vm.loadImage(ctx, imageUri)
        }
    }

    Scaffold { _ ->
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
                    TextButton(onClick = {
                        showExitDialog = false
                    }) {
                        Text("no")
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row {
                    TextButton(onClick = {
                        vm.changeSection(Section.Filtering)
                    }) {
                        Text("filtering")
                    }
                    TextButton(onClick = {
                        vm.changeSection(Section.SmartFeatures)
                    }) {
                        Text("smart features")
                    }
                    TextButton(onClick = {
                        vm.changeSection(Section.ImageManipulation)
                    }) {
                        Text("image manipulation")
                    }
                }
                Row {
                    Spacer(Modifier.weight(1f))
                    ElevatedButton(onClick = {
                        showOptionsSheet = true
                    }) {
                        Icon(Icons.Default.Menu, contentDescription = "options")
                    }
                }
                Spacer(modifier = Modifier.size(10.dp))
                // thumbnail
                if (vm.previewBitmap == null) {
                    CircularProgressIndicator()
                } else {
                    vm.previewBitmap?.asImageBitmap()?.let {
                        Image(
                            bitmap = it,
                            contentDescription = "preview",
                            modifier = Modifier
                                .size(300.dp)
                                .clip(RectangleShape)
                                .transformable(state = thumbnailTransformState)
                                .graphicsLayer(
                                    scaleX = thumbnailZoomScale,
                                    scaleY = thumbnailZoomScale,
                                    translationX = thumbnailOffset.x,
                                    translationY = thumbnailOffset.y
                                ),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                when (vm.section) {
                    Section.Filtering -> FilteringSection(vm, presetsRepository)
                    Section.SmartFeatures -> SmartFeaturesSection(navController, vm)
                    Section.ImageManipulation -> ImageManipulationSection(vm)
                }
            }
        }
    }
}