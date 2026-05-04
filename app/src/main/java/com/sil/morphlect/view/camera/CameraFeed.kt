package com.sil.morphlect.view.camera

import com.sil.morphlect.view.PresetPreview
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import android.view.MotionEvent
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.FocusMeteringAction
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.sil.morphlect.R
import com.sil.morphlect.data.EditorLayer
import com.sil.morphlect.data.Preset
import com.sil.morphlect.extension.yuvToRgba
import com.sil.morphlect.logic.FormatConverters
import com.sil.morphlect.logic.objectDetector
import com.sil.morphlect.ml.impl.ExtensionModelLoader
import com.sil.morphlect.view.custom.FlickeringLedDotProgressIndicator
import com.sil.morphlect.view.dialog.DialogScaffold
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.seconds

// TODO the camera mode should also receive the state of filter values in order to apply them to the camera feed.
@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraFeed(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    onImageCaptured: (Uri) -> Unit,
    onGoBack: () -> Unit,
    analyzerFeedFlow: MutableSharedFlow<String>,
    presets: List<Preset>,
    imageOnlyLoadedModels: List<ExtensionModelLoader>,
) {
    val presetDefaultImage = remember {
        FormatConverters.bitmapToMat(
            BitmapFactory.decodeResource(context.resources, R.drawable.preset_default)
        )
    }
    val mainExecutor = ContextCompat.getMainExecutor(context)
    val classificationExecutor = remember { Executors.newSingleThreadExecutor() }

    var lastFeedMessage    by remember { mutableStateOf("") }

    var isCapturing        by remember { mutableStateOf(false) }
    var showGrid           by remember { mutableStateOf(false) }
    var showFeed           by remember { mutableStateOf(true) }
    var showSliders        by remember { mutableStateOf(true) }
    var showClassification by remember { mutableStateOf(true) }
    var showModelsDialog   by remember { mutableStateOf(false) }
    var applyFiltering     by remember { mutableStateOf(false) }

    var imageWidth         by remember { mutableStateOf(1) }
    var imageHeight        by remember { mutableStateOf(1) }
    var focusIndicatorPoint by remember { mutableStateOf<Offset?>(null) }

    val shutterAlpha       by animateFloatAsState(
        targetValue = if (isCapturing) 0f else 1f,
        animationSpec = tween(50),
        finishedListener = { if (it == 1f) isCapturing = false }
    )
    var boundingBoxes   by remember { mutableStateOf<List<Rect>>(emptyList()) }
    var currentFrame by remember { mutableStateOf<EditorLayer?>(null) }
    var reanalyzeTriggerKey by remember { mutableStateOf(false) }
    var inferenceRefreshInterval by remember { mutableStateOf(2.seconds) }

    val inferenceRefreshTimes = remember { listOf(1.seconds, 2.seconds, 4.seconds, 5.seconds) }

    // this signals models to reanalyze in a given interval
    LaunchedEffect(Unit) {
        analyzerFeedFlow.emit("awaiting output...")
        while (true) {
            reanalyzeTriggerKey = !reanalyzeTriggerKey
            delay(inferenceRefreshInterval)
        }
    }

    // this calls inference on all models and aggregates their output
    LaunchedEffect(reanalyzeTriggerKey) {
            try {
                // analyze the current frame through all models and join their output
                analyzerFeedFlow.emit(
                    currentFrame?.let { frame ->
                        imageOnlyLoadedModels.map { model ->
                            model.infer(
                                mapOf(
                                    model.inputs[0].name to frame.visual
                                        .asAndroidBitmap()
                                        .scale(224, 224)
                                )
                            )
                            .map { (k, v) -> "$k: ${v*100}" }
                        }.joinToString("\n")
                    } ?: return@LaunchedEffect
                )
            } catch (_: Exception) {
                analyzerFeedFlow.emit(":(")
            }
    }

    // this controls the camera and attaches the necessary analyzers (not the extensions!!)
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            bindToLifecycle(lifecycleOwner)
            setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS)
            setImageAnalysisAnalyzer(classificationExecutor) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage == null) {
                    imageProxy.close()
                    return@setImageAnalysisAnalyzer
                }
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                // section 1 - performs conversion to Cv::Mat
                if (mediaImage.planes.size == 3) {
                    val mat = mediaImage.yuvToRgba()
//                    currentFrame?.close()
                    currentFrame = EditorLayer(mat)
                }

                // section 2 - performs object detection through ObjectDetector instance
                objectDetector
                    .process(image)
                    .addOnSuccessListener { detectedObjects ->
                        imageWidth = imageProxy.width
                        imageHeight = imageProxy.height
//                        Log.i("CAMERA", "Detected ${detectedObjects.size} objects.")
                        // take only the objects with high confidence
                        boundingBoxes = detectedObjects
                            .filter { it.labels.any { label -> label.confidence >= 0.5f } }
                            .map { it.boundingBox }
                    }
                    .addOnFailureListener { error ->
                        Log.e("CAMERA", "Object detection failed", error)
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }
        }
    }

    // this collects the messages to be viewed on the screen
    LaunchedEffect(analyzerFeedFlow) {
        analyzerFeedFlow.collect { lastFeedMessage = it }
    }

    // this is for the indicator that appears when tapping the screen
    LaunchedEffect(focusIndicatorPoint) {
        if (focusIndicatorPoint != null) {
            delay(900)
            focusIndicatorPoint = null
        }
    }

    if (showModelsDialog) {
        DialogScaffold(
            title = "downloaded models",
            onDismissRequest = { showModelsDialog = false },
            icon = Icons.Default.Camera,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                imageOnlyLoadedModels.forEach {
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(it.name)
                        Spacer(Modifier.weight(1f))
                        Switch(false, { })
                    }
                }
                Spacer(Modifier.weight(1f))
                Column {
                    Text("inference refresh timeout")
                    HorizontalDivider()
                    inferenceRefreshTimes.forEach {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("$it")
                            RadioButton(
                                selected = inferenceRefreshInterval == it,
                                onClick = { inferenceRefreshInterval = it },
                            )
                        }
                    }
                }
            }
        }
    }

    Box {
        // the feed itself
        Box(
            modifier = Modifier
                .graphicsLayer { alpha = shutterAlpha }
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        controller = cameraController
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        setOnTouchListener { _, event ->
                            performClick()
                            if (event.action != MotionEvent.ACTION_UP) {
                                return@setOnTouchListener true
                            }

                            val meteringPoint = meteringPointFactory.createPoint(event.x, event.y)
                            val focusMeteringAction = FocusMeteringAction.Builder(
                                meteringPoint,
                                FocusMeteringAction.FLAG_AF or
                                        FocusMeteringAction.FLAG_AE or
                                        FocusMeteringAction.FLAG_AWB
                            )
                                .setAutoCancelDuration(3, TimeUnit.SECONDS)
                                .build()

                            cameraController.cameraControl?.startFocusAndMetering(focusMeteringAction)
                            focusIndicatorPoint = Offset(event.x, event.y)
                            true
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
            if (applyFiltering)
                currentFrame?.let {
                    Image(
                        bitmap = it.visual,
                        contentDescription = "frame",
                        modifier = Modifier.fillMaxSize(),
                    )
                }

            // rule of thirds grid
            AnimatedVisibility(
                visible = showGrid,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                RuleOfThirdsGrid(modifier = Modifier.fillMaxSize())
            }

            AnimatedVisibility(
                visible = focusIndicatorPoint != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    focusIndicatorPoint?.let { tapPoint ->
                        drawCircle(
                            color = Color.White,
                            radius = 32.dp.toPx(),
                            center = tapPoint,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
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
                    SettingsRow(Icons.Default.Filter) {
                        Switch(checked = applyFiltering, onCheckedChange = { applyFiltering = it })
                    }

                    IconButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showModelsDialog = true }
                    ) {
                        Icon(Icons.Default.AddBox, contentDescription = "use a model")
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
                        imageVector =
                            if (showSliders) Icons.Default.KeyboardArrowUp
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
                .background(Color.Black.copy(alpha = .7f))
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(top = 12.dp, bottom = 12.dp)
        ) {
            // presets bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                presets.forEach { preset ->
                    PresetPreview(
                        preset = preset,
                        originalMat = presetDefaultImage,
                        onPress = { },
                        onLongPress = { },
                    )
                }
            }

            // bottom controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
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
                            val file =
                                File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
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
                    Icon(Icons.Default.Cameraswitch, contentDescription = "switch camera")
                }
            }
        }

        // analyzer feed
        AnimatedVisibility(
            visible = showFeed,
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
                    text = lastFeedMessage,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                HorizontalDivider()
                Text("running ${imageOnlyLoadedModels.size} analyzers.")
            }
        }
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
        analyzerFeedFlow = previewFeedFlow,
        presets = emptyList(),
        imageOnlyLoadedModels = emptyList(),
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
    boxes: List<Rect>,
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