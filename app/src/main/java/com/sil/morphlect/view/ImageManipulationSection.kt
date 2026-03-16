package com.sil.morphlect.view

import android.graphics.Point
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sil.morphlect.data.EditorLayer
import com.sil.morphlect.view.dialog.DialogScaffold
import com.sil.morphlect.viewmodel.EditorViewModel

// TODO this structure can definitely be optimized...
@Composable
fun ImageManipulationSection(
    vm: EditorViewModel,
    croppingMode: Boolean,
    onCropToggle: () -> Unit,
    onCropApply: () -> Unit,
    addingImage: Boolean,
    onImageAddToggle: () -> Unit,
    addingText: Boolean,
    onAddText: () -> Unit,
) {
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
                    Button(onClick = {
                        when {
                            croppingMode -> {
                                onCropApply()
                                onCropToggle()
                            }
                            addingImage -> {
                                onImageAddToggle()
                            }
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "apply crop")
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