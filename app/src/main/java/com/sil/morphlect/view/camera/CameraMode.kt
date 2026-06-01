package com.sil.morphlect.view.camera

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.LifecycleCameraController
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.sil.morphlect.imgproc.FormatConverters
import com.sil.morphlect.logic.ClusteringType
import com.sil.morphlect.repository.ExtensionsRepository
import com.sil.morphlect.repository.PresetsRepository
import com.sil.morphlect.view.custom.DecoratedContainer
import com.sil.morphlect.view.custom.FlickeringLedDotProgressIndicator
import com.sil.morphlect.view.dialog.DialogScaffold
import com.sil.morphlect.viewmodel.CameraModeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private suspend fun saveImage(context: Context, uri: Uri) {
    withContext(Dispatchers.IO) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "morphlect_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        imageUri?.let {
            resolver.openOutputStream(it)?.use { output ->
                context.contentResolver.openInputStream(uri)?.use { input ->
                    input.copyTo(output)
                }
            }
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(it, contentValues, null, null)
        }
    }

    Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_LONG).show()
}

class CameraControllerState(
    var lifecycleCameraController: LifecycleCameraController? = null,
    var takePicture: (() -> Unit)? = null,
    var selectCamera: (() -> Unit)? = null,
)

@Stable
class CameraModeUiState(context: Context) {
    var cameraPermissionGranted by mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
    )
    var cameraPermissionPrompted by mutableStateOf(!cameraPermissionGranted)
    var showSliders by mutableStateOf(true)
    var showModelsDialog by mutableStateOf(false)
    var showGrid by mutableStateOf(false)
    var showFeed by mutableStateOf(false)
    var showClassification by mutableStateOf(false)
    var applyFiltering by mutableStateOf(false)
    var lastFeedMessage by mutableStateOf("")
    var isCapturing by mutableStateOf(false)
    var isExpanded by mutableStateOf(false)
    var adheresToRuleOfThirds by mutableStateOf(true)
}

@Composable
fun CameraMode(
    navController: NavController,
    vm: CameraModeViewModel,
    analyzerFeedFlow: MutableSharedFlow<String>,
    onCaptureConfirm: (Uri) -> Unit,
    presetsRepository: PresetsRepository,
    extensionsRepository: ExtensionsRepository,
) {
    val cameraControllerState = remember { CameraControllerState() }
    val context = LocalContext.current
    val state = remember { CameraModeUiState(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        vm.loadRepositories(presetsRepository, extensionsRepository)
        vm.loadEligibleModels(context)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permGranted -> state.cameraPermissionGranted = permGranted }

    vm.capturedImageUri?.let {
        val img = FormatConverters.uriToBitmap(context, it)
        DialogScaffold(
            title = "capture",
            onDismissRequest = { vm.capturedImageUri = null },
        ) {
            Image(
                bitmap = img.asImageBitmap(),
                contentDescription = "captured image",
                modifier = Modifier.size(300.dp),
            )
            Row {
                TextButton(onClick = { vm.capturedImageUri = null }) {
                    Text("discard")
                }
                TextButton(onClick = {
                    coroutineScope.launch {
                        saveImage(context, it)
                    }
                }) {
                    Text("save")
                }
                TextButton(onClick = { onCaptureConfirm(it) } ) {
                    Text("go to studio")
                }
            }
        }
    }

    if (state.showModelsDialog) {
        DialogScaffold(
            title = "downloaded extensions",
            onDismissRequest = { state.showModelsDialog = false },
            icon = Icons.Default.Camera,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                vm.imageOnlyLoadedModels.forEach { (model, enabled) ->
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(model.name)
                        Spacer(Modifier.weight(1f))
                        Switch(enabled, { vm.imageOnlyLoadedModels += (model to !enabled) })
                    }
                }

                Spacer(Modifier.weight(1f))

                Column {
                    Text("clustering type")
                    HorizontalDivider()
                    ClusteringType.entries.forEach {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("$it")
                            RadioButton(
                                selected = vm.clusteringType == it,
                                onClick = { vm.clusteringType = it },
                            )
                        }
                    }
                }

                Column {
                    Text("inference refresh timeout")
                    HorizontalDivider()
                    vm.inferenceRefreshTimes.forEach {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("$it")
                            RadioButton(
                                selected = vm.inferenceRefreshInterval == it,
                                onClick = { vm.inferenceRefreshInterval = it },
                            )
                        }
                    }
                }
            }
        }
    }

    if (!state.cameraPermissionGranted) {
        DecoratedContainer(icon = Icons.Default.Error) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(50.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text("cannot continue as camera permissions have not been given.")
                Button(onClick = { navController.popBackStack() }) { Text("back") }
            }
        }
        if (state.cameraPermissionPrompted) {
            DialogScaffold(
                title = "camera mode",
                onDismissRequest = { state.cameraPermissionPrompted = false },
                icon = Icons.Default.QuestionMark
            ) {
                Text("welcome to camera mode! please provide morphlect the permission to access your camera")
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("give access to camera")
                }
            }
        }
    } else {
        Box {
            CameraFeed(
                context = context,
                lifecycleOwner = lifecycleOwner,
                onImageCaptured = { imageUri -> vm.capturedImageUri = imageUri },
                analyzerFeedFlow,
                vm.imageOnlyLoadedModels,
                cameraControllerState = cameraControllerState,
                uiState = state,
                clusteringType = vm.clusteringType,
                onCompositionCheck = { adheres -> state.adheresToRuleOfThirds = adheres }
            )

            // tip-giving snackbar
            if (state.adheresToRuleOfThirds) {
                Box(
                    modifier = Modifier
                        .offset(y = 67.dp)
                        .align(Alignment.TopCenter)
                ) {
                    Card(
                        modifier = Modifier
                            .width(200.dp)
                            .height(80.dp)
                    ) {
                        Text(
                            text = "you may want to move objects in the scene or your phone more to the left/right.",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // top right sliders
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp)
            ) {
                AnimatedVisibility(visible = state.showSliders) {
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        SettingsRow(Icons.Default.GridOn) {
                            Switch(checked = state.showGrid, onCheckedChange = { state.showGrid = it })
                        }
                        SettingsRow(Icons.Default.TextFormat) {
                            Switch(checked = state.showFeed, onCheckedChange = { state.showFeed = it })
                        }
                        SettingsRow(Icons.Default.CropSquare) {
                            Switch(
                                state.showClassification,
                                onCheckedChange = { state.showClassification = it })
                        }
                        SettingsRow(Icons.Default.Filter) {
                            Switch(
                                checked = state.applyFiltering,
                                onCheckedChange = { state.applyFiltering = it })
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = { state.showModelsDialog = true }) {
                                Icon(Icons.Default.AddBox, contentDescription = "use a model")
                            }
                        }
                    }
                }

                // slider panel chevron
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { state.showSliders = !state.showSliders }) {
                        Icon(
                            imageVector =
                                if (state.showSliders) Icons.Default.KeyboardArrowUp
                                else Icons.Default.KeyboardArrowDown,
                            contentDescription = "toggle slider visibility",
                            tint = Color.White,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(top = 12.dp, bottom = 12.dp)
            ) {
                // bottom controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "back")
                    }
                    IconButton(
                        onClick = {
                            if (!state.isCapturing) {
                                state.isCapturing = true
                                cameraControllerState.takePicture?.invoke()
                                state.isCapturing = false
                            }
                        },
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color.White, CircleShape)
                            .border(3.dp, Color.White, CircleShape)
                    ) {
                        if (state.isCapturing)
                            FlickeringLedDotProgressIndicator()
                        else
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White, CircleShape)
                            )
                    }
                    IconButton(
                        onClick = {
                            cameraControllerState.selectCamera?.invoke()
                        }
                    ) {
                        Icon(Icons.Default.Cameraswitch, contentDescription = "switch camera")
                    }
                }
            }
            // analyzer feed
            AnimatedVisibility(
                visible = state.showFeed,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .width(200.dp)
                        .padding(20.dp)
                        .padding(top = 30.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = state.lastFeedMessage,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    HorizontalDivider()
                    Text("running ${vm.imageOnlyLoadedModels.size} analyzers.")
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    content: @Composable (() -> Unit)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        content()
    }
}