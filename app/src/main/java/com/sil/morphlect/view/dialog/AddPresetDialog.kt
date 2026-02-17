package com.sil.morphlect.view.dialog

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sil.morphlect.data.Preset
import com.sil.morphlect.enums.Effect
import com.sil.morphlect.repository.PresetsRepository
import org.json.JSONObject
import java.io.File

@Composable
fun AddPresetDialog(
    onDismissRequest: () -> Unit,
    onAddPreset: (Preset) -> Unit,
    onAddPresetFromEditor: (String) -> Unit,
) {
    val ctx = LocalContext.current
    val presetPickLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val file = File(uri.toString())
            if (!file.name.endsWith(".preset")) {
                Toast.makeText(ctx, "Please select a .preset file.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val json = JSONObject(file.readText())
                val preset = Preset.fromJSON(json)
                onAddPreset(preset)
            }
        }
    }
    var presetName by remember { mutableStateOf("") }

    Dialog(onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "add preset",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    label = { Text("preset name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("cancel")
                    }
                    TextButton(
                        onClick = {
                            if (presetName.isNotBlank()) {
                                onAddPresetFromEditor(presetName)
                                onDismissRequest()
                            }
                        }
                    ) {
                        Text("add")
                    }
                }
                
                Button(onClick = { presetPickLauncher.launch("*/*") }) {
                    Icon(Icons.Default.Save, contentDescription = "load")
                    Text("load from device")
                }
            }
        }
    }
}