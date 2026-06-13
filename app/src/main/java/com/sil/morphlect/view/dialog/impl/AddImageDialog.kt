package com.sil.morphlect.view.dialog.impl

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.sil.morphlect.imgproc.FormatConverters
import com.sil.morphlect.view.dialog.DialogScaffold

@Composable
fun AddImageDialog(
    onDismissRequest: () -> Unit,
    onAddImage: (Bitmap) -> Unit,
) {
    val context = LocalContext.current
    val imagePickLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bmp = FormatConverters.uriToBitmap(context, it)
            onAddImage(bmp)
        }
    }

    DialogScaffold(
        title = "add image",
        onDismissRequest,
        icon = Icons.Default.AddCircle,
    ) {
        Button(onClick = { imagePickLauncher.launch("image/*") }) {
            Icon(Icons.Default.Save, contentDescription = "load")
            Text("load from device")
        }
    }
}


