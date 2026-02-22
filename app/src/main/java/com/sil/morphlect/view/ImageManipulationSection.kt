package com.sil.morphlect.view

import android.graphics.Point
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sil.morphlect.viewmodel.EditorViewModel

// TODO this structure can definitely be optimized...
@Composable
fun ImageManipulationSection(
    vm: EditorViewModel,
    croppingMode: Boolean,
    onCropToggle: () -> Unit,
    onCropApply: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        if (croppingMode) {
            Button(onClick = { onCropApply();onCropToggle() }) {
                Icon(Icons.Default.Check, contentDescription = "apply crop")
            }
        } else {
            TextButton(onClick = onCropToggle) {
                Text("crop")
            }
        }
//        TextButton(onClick = onTextWrite) {
//            Text("text")
//        }
//        TextButton(onClick = onImageAdd) {
//            Text("overlay image")
//        }
    }
}