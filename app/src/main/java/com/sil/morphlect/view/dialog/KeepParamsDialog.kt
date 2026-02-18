package com.sil.morphlect.view.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun KeepParamsDialog(onDismissRequest: () -> Unit, onApply: () -> Unit) {
    var keepSharpness by remember { mutableStateOf(false) }
    var keepBrightness by remember { mutableStateOf(false) }
    var keepContrast by remember { mutableStateOf(false) }
    var keepHue by remember { mutableStateOf(false) }

    DialogScaffold(title = "select which parameters to keep", onDismissRequest) {
        Text(
            text = "select which parameters you want to keep",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("sharpness")
            Checkbox(
                checked = keepSharpness,
                onCheckedChange = { keepSharpness = !keepSharpness }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("brightness")
            Checkbox(
                checked = keepBrightness,
                onCheckedChange = { keepBrightness = !keepBrightness }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("contrast")
            Checkbox(
                checked = keepContrast,
                onCheckedChange = { keepContrast = !keepContrast }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("hue")
            Checkbox(
                checked = keepHue,
                onCheckedChange = { keepHue = !keepHue }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismissRequest) {
                Text("cancel")
            }
            TextButton(onClick = onApply) {
                Text("continue")
            }
        }
    }
}