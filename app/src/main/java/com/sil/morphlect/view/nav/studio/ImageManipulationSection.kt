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
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sil.morphlect.view.dialog.impl.AddImageDialog

@Composable
fun ImageManipulationSection(
    croppingMode: Boolean,
    onCropToggle: () -> Unit,
    onCropApply: (cropAllLayers: Boolean, outerCrop: Boolean) -> Unit,
    addingImage: Boolean,
    onImageAddToggle: () -> Unit,
    onAddImage: (Bitmap) -> Unit,
    addingText: Boolean,
    onAddText: () -> Unit,
    onCancel: () -> Unit,
) {
    var applyCropOnAllLayers by remember { mutableStateOf(false) }
    var outerCrop by remember { mutableStateOf(false) }
    when {
        addingImage -> AddImageDialog(
            onDismissRequest = { onImageAddToggle() },
            onAddImage = { bmp -> onAddImage(bmp) }
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
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Button(onClick = {
                            when {
                                croppingMode -> {
                                    onCropApply(applyCropOnAllLayers, outerCrop)
                                    onCropToggle()
                                }

                                addingImage -> {
                                    onImageAddToggle()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "apply")
                        }
                        Button(onClick = onCancel) {
                            Icon(Icons.Default.Close, contentDescription = "cancel")
                        }
                        if (croppingMode) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("cropped only the top layer")
                                Checkbox(
                                    checked = !applyCropOnAllLayers,
                                    onCheckedChange = {
                                        applyCropOnAllLayers = !applyCropOnAllLayers
                                    },
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("outer cropped (cutout by selection)")
                                Checkbox(
                                    checked = outerCrop,
                                    onCheckedChange = {
                                        outerCrop = !outerCrop
                                    },
                                )
                            }
                        }
                    }
                } else {
                    TextButton(onClick = onCropToggle) {
                        Text("cropped")
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