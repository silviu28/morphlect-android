package com.sil.morphlect.view.nav.studio

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sil.morphlect.layerwork.LayerPosition
import com.sil.morphlect.view.dialog.AddImageDialog
import com.sil.morphlect.viewmodel.StudioViewModel

// TODO this structure can definitely be optimized...
@Composable
fun ImageManipulationSection(
    croppingMode: Boolean,
    onCropToggle: () -> Unit,
    onCropApply: (cropAllLayers: Boolean) -> Unit,
    addingImage: Boolean,
    onImageAddToggle: () -> Unit,
    onAddImage: (Bitmap, LayerPosition) -> Unit,
    addingText: Boolean,
    onAddText: () -> Unit,
) {
    var applyCropOnAllLayers by remember { mutableStateOf(false) }
    when {
        addingImage -> AddImageDialog(
            onDismissRequest = { onImageAddToggle() },
            onAddImage = { bmp, pos -> onAddImage(bmp, pos) }
        )
    }

    AnimatedContent(
        targetState = croppingMode || addingImage || addingText,
        transitionSpec = {
            fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
        }
    ) { isWorking ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (isWorking) {
                    Column {
                        Button(onClick = {
                            when {
                                croppingMode -> {
                                    onCropApply(applyCropOnAllLayers)
                                    onCropToggle()
                                }

                                addingImage -> {
                                    onImageAddToggle()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "apply")
                        }
                        if (croppingMode)
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("crop only the top layer")
                                Checkbox(
                                    checked = !applyCropOnAllLayers,
                                    onCheckedChange = { applyCropOnAllLayers = !applyCropOnAllLayers },
                                )
                            }
                    }
                } else {
                    TextButton(onClick = onCropToggle) {
                        Text("crop")
                    }
                    TextButton(onClick = onImageAddToggle) {
                        Text("add image")
                    }
                    TextButton(onClick = onAddText) {
                        Text("add text")
                    }
                }
        }
    }
}