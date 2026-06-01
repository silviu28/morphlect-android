package com.sil.morphlect.view.camera

import android.content.Context
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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.sil.morphlect.layerwork.StudioLayer
import com.sil.morphlect.extension.yuvToRgba
import com.sil.morphlect.logic.CenterWise
import com.sil.morphlect.logic.ClusteringType
import com.sil.morphlect.logic.dbscan
import com.sil.morphlect.logic.depthToMat
import com.sil.morphlect.logic.imageSegmentKmeans
import com.sil.morphlect.logic.objectDetector
import com.sil.morphlect.ml.impl.ExtensionModelLoader
import com.sil.morphlect.ml.impl.Parameters
import com.sil.morphlect.ml.impl.Tensor4D
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.Executors
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.seconds

/**
 * returns the bounding box given by the dominant cluster from clusters given bounding boxes `bbs` using the specified `clustering` algorithm.
 */
fun dominantClusterBoundingBox(
    bbs: List<Rect>,
    imageWidth: Int,
    imageHeight: Int,
    eps: Float,
    minPoints: Int = 2,
    clustering: ClusteringType = ClusteringType.DBSCAN,
): Rect? {
    val clusters = when (clustering) {
        ClusteringType.DBSCAN -> dbscan(bbs, eps, minPoints) { CenterWise(it.centerX(), it.centerY(), it) }
        ClusteringType.Kmeans -> imageSegmentKmeans(bbs, imageWidth, imageHeight) { Pair(it.centerX(), it.centerY()) }
    }
    val dominant = clusters.maxByOrNull { it.value.size }?.value ?: return null

    return Rect(
        dominant.minOf { it.left },
        dominant.minOf { it.top },
        dominant.maxOf { it.right },
        dominant.maxOf { it.bottom }
    )
}

fun adhereToRuleOfThirds(
    bb: Rect,
    imageWidth: Int,
    imageHeight: Int,
    tolerance: Float = 0.1f
): Boolean {
    val powerPoints = listOf(
        imageWidth / 3f to imageHeight / 3f,
        imageWidth * 2 / 3f to imageHeight / 3f,
        imageWidth / 3f to imageHeight * 2 / 3f,
        imageWidth * 2 / 3f to imageHeight * 2 / 3f
    )

    val tolerancePx = imageWidth * tolerance
    val cx = bb.centerX()
    val cy = bb.centerY()

    return powerPoints.any { (px, py) ->
        val dx = cx - px
        val dy = cy - py
        sqrt((dx * dx + dy * dy).toDouble()) <= tolerancePx
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraFeed(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    onImageCaptured: (Uri) -> Unit,
    analyzerFeedFlow: MutableSharedFlow<String>,
    imageOnlyLoadedModels: Map<ExtensionModelLoader, Boolean>,
    cameraControllerState: CameraControllerState,
    uiState: CameraModeUiState,
    clusteringType: ClusteringType,
    onCompositionCheck: (adheres: Boolean) -> Unit,
) {
    val mainExecutor = ContextCompat.getMainExecutor(context)
    val classificationExecutor = remember { Executors.newSingleThreadExecutor() }
    var focusIndicatorPoint by remember { mutableStateOf<Offset?>(null) }

    var imageWidth by remember { mutableIntStateOf(1) }
    var imageHeight by remember { mutableIntStateOf(1) }

    val shutterAlpha       by animateFloatAsState(
        targetValue = if (uiState.isCapturing) 0f else 1f,
        animationSpec = tween(50),
        finishedListener = { if (it == 1f) uiState.isCapturing = false }
    )

    val size by animateDpAsState(
        targetValue = if (uiState.isExpanded) 0.dp else 120.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "frame size"
    )

    var boundingBoxes by remember { mutableStateOf<List<Rect>>(emptyList()) }
    var currentFrame by remember { mutableStateOf<StudioLayer?>(null) }
    var currentProcessedFrame by remember { mutableStateOf<StudioLayer?>(null) }
    var reanalyzeTriggerKey by remember { mutableStateOf(false) }
    var inferenceRefreshInterval by remember { mutableStateOf(2.seconds) }

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
            currentFrame?.let { frame ->
                imageOnlyLoadedModels
                    .map { (model, enabled) ->
                        if (!enabled) Unit
                        else model.infer(
                            mapOf(
                                model.inputs[0].name to frame.visual
                                    .asAndroidBitmap()
                                    .scale(model.inputs[0].shape[0], model.inputs[0].shape[1])
                            )
                        )
                    }
                    .forEach { output ->
                        when (output) {
                            is Unit -> Unit // do nothing
                            is Parameters ->
                                analyzerFeedFlow.emit(
                                    output.data
                                        .map { (k, v) -> "$k: ${v * 100}" }
                                        .joinToString("\n")
                                )

                            is Tensor4D -> currentProcessedFrame = StudioLayer(depthToMat(output))
                            else -> TODO() // unlikely
                        }
                    }
            }
        } catch (e: Exception) {
            Log.e("CameraFeed", e.stackTraceToString())
            analyzerFeedFlow.emit(":(")
        }
    }

    // this periodically runs the rule of thirds adherence check
    LaunchedEffect(Unit) {
        launch {
            while (true) {
                dominantClusterBoundingBox(
                    boundingBoxes,
                    imageWidth,
                    imageHeight,
                    400f,
                    clustering = clusteringType
                )?.let {
                    val adheres = adhereToRuleOfThirds(
                        bb = it,
                        imageWidth = imageWidth,
                        imageHeight = imageHeight,
                        tolerance = .1f
                    )
                    onCompositionCheck(adheres)
                }
                delay(1.seconds)
            }
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
                val image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                // section 1 - performs conversion to Cv::Mat
                if (mediaImage.planes.size == 3) {
                    val mat = mediaImage.yuvToRgba()
//                    currentFrame?.close()
                    currentFrame = StudioLayer(mat)
                }

                // section 2 - performs object detection through ObjectDetector instance
                objectDetector
                    .process(image)
                    .addOnSuccessListener { detectedObjects ->
                        imageWidth = imageProxy.width
                        imageHeight = imageProxy.height
                        // take only the objects with high confidence
                        boundingBoxes = detectedObjects
//                            .filter { it.labels.any { label -> label.confidence >= 0.5f } }
                            .map { it.boundingBox }
                    }
                    .addOnFailureListener { error ->
                        Log.e("CAMERA", "Object detection failed", error)
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }

            // assign for the wrapper the required callback for taking a picture
            cameraControllerState.lifecycleCameraController = this
            cameraControllerState.takePicture = {
                val file =
                    File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                val outputFileOptions = ImageCapture
                    .OutputFileOptions
                    .Builder(file)
                    .build()

                cameraControllerState.lifecycleCameraController?.takePicture(
                    outputFileOptions,
                    mainExecutor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            uiState.isCapturing = false
                            onImageCaptured(Uri.fromFile(file))
                        }

                        override fun onError(exception: ImageCaptureException) {
                            uiState.isCapturing = false
                            Log.e("CAMERA", exception.stackTraceToString())
                        }
                    }
                )
            }
            cameraControllerState.selectCamera = {
                cameraControllerState.lifecycleCameraController?.cameraSelector =
                    if (cameraControllerState.lifecycleCameraController?.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    else
                        CameraSelector.DEFAULT_BACK_CAMERA
            }
        }
    }

    // this collects the messages to be viewed on the screen
    LaunchedEffect(uiState.showFeed) {
        if (uiState.showFeed)
            analyzerFeedFlow.collect { uiState.lastFeedMessage = it }
    }

    // this is for the indicator that appears when tapping the screen
    LaunchedEffect(focusIndicatorPoint) {
        if (focusIndicatorPoint != null) {
            delay(900)
            focusIndicatorPoint = null
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    controller = cameraController
                    scaleType = PreviewView.ScaleType.FIT_CENTER
                    setOnTouchListener { _, event ->
                        performClick()
                        if (event.action != MotionEvent.ACTION_UP) {
                            return@setOnTouchListener true
                        }

                        val meteringPoint =
                            meteringPointFactory.createPoint(event.x, event.y)
                        val focusMeteringAction = FocusMeteringAction.Builder(
                            meteringPoint,
                            FocusMeteringAction.FLAG_AF or
                                    FocusMeteringAction.FLAG_AE or
                                    FocusMeteringAction.FLAG_AWB
                        )
                            .setAutoCancelDuration(3, TimeUnit.SECONDS)
                            .build()

                        cameraController.cameraControl?.startFocusAndMetering(
                            focusMeteringAction
                        )
                        focusIndicatorPoint = Offset(event.x, event.y)
                        true
                    }
                }
            },
            modifier = Modifier.width(imageWidth.dp).height(imageHeight.dp),
        )

        // rule of thirds grid
        AnimatedVisibility(
            visible = uiState.showGrid,
            enter = fadeIn(),
            exit = fadeOut(),
        ) { RuleOfThirdsGrid(modifier = Modifier.width(imageWidth.dp).height(imageHeight.dp)) }

        // bounding-box canvas
        if (uiState.showClassification)
            BoundingBoxCanvas(boundingBoxes, imageWidth, imageHeight, clusteringType)
    }

    if (uiState.applyFiltering)
        currentProcessedFrame?.let {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = if (uiState.isExpanded) 0.dp else (-200).dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Image(
                    bitmap = it.visual,
                    contentDescription = "frame",
                    modifier =
                        Modifier
                            .then(if (uiState.isExpanded) Modifier.fillMaxSize() else Modifier)
                            .zIndex(10f)
                            .size(size)
                            .clickable { uiState.isExpanded = !uiState.isExpanded }
                )
            }
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
        analyzerFeedFlow = previewFeedFlow,
        imageOnlyLoadedModels = emptyMap(),
        cameraControllerState = CameraControllerState(),
        uiState = CameraModeUiState(ctx),
        clusteringType = ClusteringType.DBSCAN,
        onCompositionCheck = { _ -> Unit },
    )
}

fun DrawScope.drawBoundingBox(box: Rect, scaleX: Float, scaleY: Float, color: Color = Color.White.copy(alpha = .5f)) {
    drawRect(
        color = color,
        topLeft = Offset(box.left * scaleX, box.top * scaleY),
        size = Size(box.width() * scaleX, box.height() * scaleY),
        style = Stroke(width = .5.dp.toPx())
    )
}

@Composable
private fun BoundingBoxCanvas(
    boxes: List<Rect>,
    imageWidth: Int,
    imageHeight: Int,
    clusteringType: ClusteringType,
) {
    Canvas(modifier = Modifier.width(imageWidth.dp).height(imageHeight.dp)) {
        val scaleX = size.width / imageWidth
        val scaleY = size.height / imageHeight

        boxes.forEach { drawBoundingBox(it, scaleX, scaleY) }

        dominantClusterBoundingBox(
            boxes,
            imageWidth,
            imageHeight,
            eps = 400f,
            clustering = clusteringType
        )?.let { drawBoundingBox(it, scaleX, scaleY, color = Color.Red) }
    }
}