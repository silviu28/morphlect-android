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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.sil.morphlect.data.Preset
import com.sil.morphlect.logic.FormatConverters
import com.sil.morphlect.repository.ExtensionsRepository
import com.sil.morphlect.repository.PresetsRepository
import com.sil.morphlect.view.custom.DecoratedContainer
import com.sil.morphlect.view.dialog.DialogScaffold
import com.sil.morphlect.viewmodel.CameraModeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

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

@Composable
fun CameraMode(
    navController: NavController,
    vm: CameraModeViewModel,
    analyzerFeedFlow: MutableSharedFlow<String>,
    onCaptureConfirm: (Uri) -> Unit,
    presetsRepository: PresetsRepository,
    extensionsRepository: ExtensionsRepository,
) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    var cameraPermissionGranted  by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
    ) }
    var cameraPermissionPrompted by remember { mutableStateOf(!cameraPermissionGranted) }
    var capturedImageUri         by remember { mutableStateOf<Uri?>(null) }
    var presets                  by remember { mutableStateOf<List<Preset>>(emptyList()) }
    var models                   by remember { mutableStateOf<List<String>>(emptyList()) }


    LaunchedEffect(Unit) {
        presets = presetsRepository.load()
        models = extensionsRepository.readExtensionNames()
        while (true) {
            analyzerFeedFlow.emit(".")
            delay(1.seconds)
            analyzerFeedFlow.emit("..")
            delay(1.seconds)
            analyzerFeedFlow.emit("...")
        }
    }

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
            presets,
            models
        )
    }
}