package com.sil.morphlect.view.dialog

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sil.morphlect.imgproc.FormatConverters
import com.sil.morphlect.layerwork.LayerPosition

@Composable
fun AddImageDialog(
    onDismissRequest: () -> Unit,
    onAddImage: (Bitmap, LayerPosition) -> Unit,
) {
    val context = LocalContext.current
    var position by remember { mutableStateOf(LayerPosition.CENTER)}
    val imagePickLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bmp = FormatConverters.uriToBitmap(context, it)
            onAddImage(bmp, position)
        }
    }

    DialogScaffold(
        title = "add image",
        onDismissRequest,
        icon = Icons.Default.AddCircle,
    ) {
        FlowRow {
            LayerPosition.entries.forEach { pos ->
                Column(modifier = Modifier.padding(10.dp)) {
                    RadioButton(
                        selected = (position == pos),
                        onClick = { position = pos },
                    )
                    Text(text = pos.name, style = MaterialTheme.typography.bodySmall,)
                }
            }
        }

        Button(onClick = { imagePickLauncher.launch("image/*") }) {
            Icon(Icons.Default.Save, contentDescription = "load")
            Text("load from device")
        }
    }
}