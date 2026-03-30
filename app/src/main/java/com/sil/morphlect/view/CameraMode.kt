package com.sil.morphlect.view

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore.Images
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.sil.morphlect.view.custom.DecoratedContainer
import com.sil.morphlect.view.dialog.DialogScaffold
import com.sil.morphlect.viewmodel.CameraModeViewModel
import java.io.File

@Composable
fun CameraMode(
    navController: NavController,
    vm: CameraModeViewModel
) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var cameraPermissionGranted by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
    ) }
    var cameraPermissionPrompted by remember { mutableStateOf(!cameraPermissionGranted) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permGranted -> cameraPermissionGranted = permGranted }

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
            onImageCaptured = { uri ->
                val contentValues = ContentValues().apply {
                    put(Images.Media.DISPLAY_NAME, "photo_${System.currentTimeMillis()}.jpg")
                    put(Images.Media.MIME_TYPE, "image/jpeg")
                    put(Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    put(Images.Media.IS_PENDING, 1)
                }

                val resolver = ctx.contentResolver
                val imageUri = resolver.insert(Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                imageUri?.let {
                    resolver.openOutputStream(it)?.use { output ->
                        ctx.contentResolver.openInputStream(uri)?.use { input ->
                            input.copyTo(output)
                        }
                    }
                    contentValues.clear()
                    contentValues.put(Images.Media.IS_PENDING, 0)
                    resolver.update(it, contentValues, null, null)
                }

                Toast.makeText(ctx, "Image saved to gallery", Toast.LENGTH_LONG).show()
            }
        )
    }
}

@Composable
private fun CameraFeed(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    onImageCaptured: (Uri) -> Unit
) {
    var isCapturing by remember { mutableStateOf(false) }
    val shutterAlpha by animateFloatAsState(
        targetValue = if (isCapturing) 0f else 1f,
        animationSpec = tween(50),
        finishedListener = { if (it == 1f) isCapturing = false }
    )

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            bindToLifecycle(lifecycleOwner)
        }
    }

    Box {
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
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            isCapturing = false
                            onImageCaptured(Uri.fromFile(file))
                            file.delete()
                        }

                        override fun onError(exception: ImageCaptureException) {
                            isCapturing = false
                        }
                    }
                )
            }
        },
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(32.dp)
            .size(72.dp)
            .background(Color.White, CircleShape)
            .border(3.dp, Color.White, CircleShape)
        ) {
            if (isCapturing)
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            else
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White, CircleShape)
                )
        }
    }
}