package com.sil.morphlect.view

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.google.mlkit.vision.common.InputImage
import com.sil.morphlect.logic.FormatConverters
import com.sil.morphlect.logic.objectDetector
import com.sil.morphlect.view.custom.DecoratedContainer
import com.sil.morphlect.view.custom.FlickeringLedDotProgressIndicator
import com.sil.morphlect.view.dialog.DialogScaffold
import com.sil.morphlect.viewmodel.CameraModeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.collections.listOf

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

// TODO the camera mode should also receive the state of filter values in order to apply them to the camera feed.
@Composable
fun CameraMode(
    navController: NavController,
    vm: CameraModeViewModel,
    analyzerFeedFlow: MutableSharedFlow<String>,
    onCaptureConfirm: (Uri) -> Unit,
) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var cameraPermissionGranted by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
    ) }
    var cameraPermissionPrompted by remember { mutableStateOf(!cameraPermissionGranted) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permGranted -> cameraPermissionGranted = permGranted }

    capturedImageUri?.let {
        val img = FormatConverters.uriToBitmap(ctx, it)
        DialogScaffold(
            title = "capture",
            onDismissRequest = { capturedImageUri = null },
        ) {
            Image(
                bitmap = img.asImageBitmap(),
                contentDescription = "captured image",
                modifier = Modifier.size(300.dp),
            )
            Row {
                TextButton(onClick = { capturedImageUri = null }) {
                    Text("discard")
                }
                TextButton(onClick = {
                    coroutineScope.launch {
                        saveImage(ctx, it)
                    }
                }) {
                    Text("save")
                }
                TextButton(onClick = { onCaptureConfirm(it) } ) {
                    Text("go to editor")
                }
            }
        }
    }

    if (!cameraPermissionGranted) {
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
        if (cameraPermissionPrompted) {
            DialogScaffold(
                title = "camera mode",
                onDismissRequest = { cameraPermissionPrompted = false },
                icon = Icons.Default.QuestionMark
            ) {
                Text("welcome to camera mode! please provide morphlect the permission to access your camera")
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("give access to camera")
                }
            }
        }
    } else {
        CameraFeed(
            context = ctx,
            lifecycleOwner = lifecycleOwner,
            onImageCaptured = { imageUri -> capturedImageUri = imageUri },
            onGoBack = { navController.popBackStack() },
            analyzerFeedFlow,
        )
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraFeed(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    onImageCaptured: (Uri) -> Unit,
    onGoBack: () -> Unit,
    analyzerFeedFlow: MutableSharedFlow<String>
) {
    val mainExecutor = ContextCompat.getMainExecutor(context)
    val classificationExecutor = remember { Executors.newSingleThreadExecutor() }

    var lastFeedMessage    by remember { mutableStateOf("") }

    var isCapturing        by remember { mutableStateOf(false) }
    var showGrid           by remember { mutableStateOf(false) }
    var showFeed           by remember { mutableStateOf(true) }
    var showSliders        by remember { mutableStateOf(true) }
    var showClassification by remember { mutableStateOf(true) }

    var imageWidth         by remember { mutableStateOf(1) }
    var imageHeight        by remember { mutableStateOf(1) }

    val shutterAlpha       by animateFloatAsState(
        targetValue = if (isCapturing) 0f else 1f,
        animationSpec = tween(50),
        finishedListener = { if (it == 1f) isCapturing = false }
    )
    var boundingBoxes   by remember { mutableStateOf<List<android.graphics.Rect>>(emptyList()) }

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            bindToLifecycle(lifecycleOwner)
            setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS)
            setImageAnalysisAnalyzer(classificationExecutor) { imageProxy ->
                val mediaImage = imageProxy.image ?: return@setImageAnalysisAnalyzer
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                objectDetector
                    .process(image)
                    .addOnSuccessListener { detectedObjects ->
                        imageWidth = imageProxy.width
                        imageHeight = imageProxy.height
                        Log.i("CAMERA", "Detected ${detectedObjects.size} objects.")
                        // take only the objects with high confidence
                        boundingBoxes = detectedObjects
                            .filter { it.labels.any { label -> label.confidence >= 0.5f } }
                            .map { it.boundingBox }
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }
        }
    }

    LaunchedEffect(analyzerFeedFlow) {
        analyzerFeedFlow.collect { lastFeedMessage = it }
    }

    Box {
        // the feed itself
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = shutterAlpha }
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        controller = cameraController
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )

            // rule of thirds grid
            AnimatedVisibility(
                visible = showGrid,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                RuleOfThirdsGrid(modifier = Modifier.fillMaxSize())
            }
        }

        // bounding-box canvas
        // TODO - disabling 'showClassification' should also disable the analyzer for performance
        if (showClassification)
            BoundingBoxCanvas(boundingBoxes, imageWidth, imageHeight)

        // top right sliders
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp)
        ) {
            AnimatedVisibility(visible = showSliders) {
                Column {
                    SettingsRow(Icons.Default.GridOn) {
                        Switch(checked = showGrid, onCheckedChange = { showGrid = it })
                    }
                    SettingsRow(Icons.Default.TextFormat) {
                        Switch(checked = showFeed, onCheckedChange = { showFeed = it })
                    }
                    SettingsRow(Icons.Default.CropSquare) {
                        Switch(showClassification, onCheckedChange = { showClassification = it })
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
                IconButton(onClick = { showSliders = !showSliders }) {
                    Icon(
                        imageVector = if (showSliders) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "toggle slider visibility",
                        tint = Color.White,
                    )
                }
            }
        }

        // bottom controls - go back, shutter, reverse camera
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            IconButton(onClick = onGoBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "back")
            }
            IconButton(
                onClick = {
                    if (!isCapturing) {
                        isCapturing = true
                        val file = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                        val outputFileOptions = ImageCapture
                            .OutputFileOptions
                            .Builder(file)
                            .build()

                        cameraController.takePicture(
                            outputFileOptions,
                            mainExecutor,
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    isCapturing = false
                                    onImageCaptured(Uri.fromFile(file))
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    isCapturing = false
                                    Log.e("CAMERA", exception.stackTraceToString())
                                }
                            }
                        )
                    }
                },
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.White, CircleShape)
                    .border(3.dp, Color.White, CircleShape)
            ) {
                if (isCapturing)
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
                    cameraController.cameraSelector =
                        if (cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        else
                            CameraSelector.DEFAULT_BACK_CAMERA
                }
            ) {
                Icon(Icons.Default.Cameraswitch, contentDescription = "switch to opposite facing camera")
            }
        }

        // analyzer feed
        AnimatedVisibility(
            visible = showFeed,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = lastFeedMessage,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(20.dp),
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun RuleOfThirdsGrid(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val gridStroke = Stroke(width = 1.dp.toPx()).width
        drawLine(Color.White, Offset(size.width / 3, 0f), Offset(size.width / 3, size.height), gridStroke)
        drawLine(Color.White, Offset(size.width * 2 / 3, 0f), Offset(size.width * 2 / 3, size.height), gridStroke)
        drawLine(Color.White, Offset(0f, size.height / 3), Offset(size.width, size.height / 3), gridStroke)
        drawLine(Color.White, Offset(0f, size.height * 2 / 3), Offset(size.width, size.height * 2 / 3), gridStroke)
    }
}

@Preview
@Composable
fun CameraFeedPreview() {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewFeedFlow = MutableSharedFlow<String>()

    LaunchedEffect(Unit) {
        while (true) {
            previewFeedFlow.emit("feed output here.")
            delay(1000L)
            previewFeedFlow.emit("feed output here..")
            delay(1000L)
            previewFeedFlow.emit("feed output here...")
            delay(1000L)
        }
    }

    CameraFeed(
        context = ctx,
        lifecycleOwner,
        onImageCaptured = { _ -> },
        onGoBack = { },
        analyzerFeedFlow = previewFeedFlow
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    content: @Composable (() -> Unit)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        content()
    }
}

@Composable
private fun BoundingBoxCanvas(
    boxes: List<android.graphics.Rect>,
    imageWidth: Int,
    imageHeight: Int,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val scaleX = size.width / imageWidth
        val scaleY = size.height / imageHeight

        boxes.forEach { box ->
            drawRect(
                color = Color.White.copy(alpha = .5f),
                topLeft = Offset(box.left * scaleX, box.top * scaleY),
                size = Size(box.width() * scaleX, box.height() * scaleY),
                style = Stroke(width = .5.dp.toPx())
            )
        }
    }
}