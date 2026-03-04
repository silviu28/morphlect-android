package com.sil.morphlect.view.dialog.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.runtime.Composable
import com.sil.morphlect.view.dialog.DialogScaffold

@Composable
fun GlazeDialog(onDismissRequest: () -> Unit) {
    DialogScaffold(
        title = "coming soon...",
        onDismissRequest,
        icon = Icons.Default.WaterDrop,
    ) { }
}