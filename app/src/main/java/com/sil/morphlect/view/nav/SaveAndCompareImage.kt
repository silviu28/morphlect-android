package com.sil.morphlect.view.nav

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sil.morphlect.layerwork.StudioLayer
import com.sil.morphlect.ml.impl.AlteredMobileNetLoader
import com.sil.morphlect.repository.FingerprintRepository
import kotlinx.coroutines.launch

fun saveImage(
    ctx: Context,
    image: Bitmap,
    format: String,
    name: String,
) {
    try {
        val resolver = ctx.contentResolver
        val mimeType = when (format) {
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$name.$format")
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Morphlect")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: return

        resolver.openOutputStream(uri)?.use { out ->
            val compressFmt = when (format) {
                "png" -> Bitmap.CompressFormat.PNG
                "webp" -> Bitmap.CompressFormat.WEBP
                else -> Bitmap.CompressFormat.JPEG
            }
            image.compress(compressFmt, 100, out)
        }

        contentValues.clear()
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)

        Toast.makeText(ctx, "image saved.", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(ctx, "Unable to save image ${e.stackTraceToString()}", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveAndCompareImage(
    originalImageBitmap: Bitmap?,
    layers: List<StudioLayer>,
    onReturn: () -> Unit,
    fingerprintRepository: FingerprintRepository,
) {
    val formats = remember { arrayOf("JPG", "PNG", "WEBP") }
    val ctx = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var imageName by remember { mutableStateOf("morphlect_${System.currentTimeMillis()}") }
    var format by remember { mutableStateOf(formats[0]) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    val evaluator = remember { AlteredMobileNetLoader().apply { initialize(ctx) } }

    var editedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(layers) {
        editedImageBitmap = try {
            layers
                .filter { it.visible }
                .fold(StudioLayer.empty()) { acc, layer -> acc.mergeWith(layer) }
                .visual
                .asAndroidBitmap()
        } catch (_: Exception) {
            originalImageBitmap
        }
    }

    var dividerRatio by remember { mutableDoubleStateOf(.5) }


    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = { onReturn() },
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "layering")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Text("save image")
                if (originalImageBitmap == null) {
                    Text("no image loaded.")
                }
                else {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("slide to reveal more of each image")
                            BoxWithConstraints(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(400.dp)
                                    .pointerInput(Unit) {
                                        detectHorizontalDragGestures { _, dragAmount ->
                                            val width = size.width
                                            dividerRatio = (dividerRatio + dragAmount / width)
                                                .coerceIn(0.0, 1.0)
                                        }
                                    }) {
                                val dividerX = constraints.maxWidth * dividerRatio

                                editedImageBitmap?.let {
                                    Image(
                                        bitmap = it.asImageBitmap(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                Image(
                                    bitmap = originalImageBitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .drawWithContent {
                                            clipRect(
                                                left = 0f,
                                                top = 0f,
                                                right = dividerX.toFloat(),
                                                bottom = size.height
                                            ) {
                                                this@drawWithContent.drawContent()
                                            }
                                        },
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
                Text("image name")
                OutlinedTextField(
                    value = imageName,
                    onValueChange = { imageName = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Text("format")
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = format,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        formats.forEach { formatOption ->
                            DropdownMenuItem(
                                text = { Text(formatOption) },
                                onClick = {
                                    format = formatOption
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                ElevatedButton(onClick = {
                    editedImageBitmap?.let { image ->
                        coroutineScope.launch {
                            saveImage(ctx, image, format, imageName)
                            val params = evaluator.infer(image)
                            fingerprintRepository.load().run {
                                val adjusted = fingerprintRepository.computeNew(this, params)
                                fingerprintRepository.save(adjusted)
                            }
                        }
                    }
                }) {
                    Text("save")
                }
            }
        }
    }
}