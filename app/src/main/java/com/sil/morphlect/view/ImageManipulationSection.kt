package com.sil.morphlect.view

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.sil.morphlect.viewmodel.EditorViewModel

@Composable
fun ImageManipulationSection(vm: EditorViewModel) {
    Row {
        TextButton(onClick = {}) {
            Text("crop")
        }
        TextButton(onClick = {}) {
            Text("text")
        }
        TextButton(onClick = {}) {
            Text("scale")
        }
    }
}