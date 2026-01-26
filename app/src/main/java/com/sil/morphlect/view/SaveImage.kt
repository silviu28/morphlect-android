package com.sil.morphlect.view

import android.content.ContentValues
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import com.sil.morphlect.view.dialog.GlazeDialog
import com.sil.morphlect.viewmodel.EditorViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveImage(editorViewModel: EditorViewModel) {
    val vm = editorViewModel
    val ctx = LocalContext.current

    var imageName by remember { mutableStateOf("image name") }
    var format by remember { mutableStateOf("JPG") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var showGlazeInfo by remember { mutableStateOf(false) }
    var showGlazeDialog by remember { mutableStateOf(false) }

    val formats = listOf("JPG", "PNG", "WEBP")

    fun saveImage() {
        try {
            val resolver = ctx.contentResolver
            val mimeType = when (format) {
                "png" -> "image/png"
                "webp" -> "image/webp"
                else -> "image/jpeg"
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$imageName.$format")
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
                vm.previewBitmap!!.compress(compressFmt, 100, out)
            }

            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)

            Toast.makeText(ctx, "image saved.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(ctx, "unable to save image", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(modifier = Modifier.padding(18.dp)) { _ ->
        if (showGlazeInfo) {
            GlazeHelp(onDismissRequest = { showGlazeInfo = false })
        }

        if (showGlazeDialog) {
            GlazeDialog(onDismissRequest = { showGlazeDialog = false })
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("save image")
                if (vm.previewBitmap == null) {
                    CircularProgressIndicator()
                } else {
                    vm.previewBitmap?.asImageBitmap()?.let {
                        Image(
                            bitmap = it,
                            contentDescription = "preview",
                            modifier = Modifier.size(300.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Text("image name")
                OutlinedTextField(
                    value = imageName,
                    onValueChange = { imageName = it },
                    modifier = Modifier.fillMaxWidth()
                )
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

                Row {
                    TextButton(onClick = { showGlazeDialog = true }) {
                        Icon(Icons.Default.WaterDrop, contentDescription = "glaze")
                        Text("glaze...")
                    }
                    IconButton(onClick = { showGlazeInfo = true }) {
                        Icon(Icons.Default.QuestionMark, contentDescription = "what is glaze?")
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                ElevatedButton(onClick = { saveImage() }) {
                    Text("save")
                }
            }
        }
    }
}