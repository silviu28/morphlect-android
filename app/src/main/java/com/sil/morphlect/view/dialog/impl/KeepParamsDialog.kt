package com.sil.morphlect.view.dialog.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.sil.morphlect.enums.Filter
import com.sil.morphlect.view.dialog.DialogScaffold

@Composable
fun KeepParamsDialog(
    onDismissRequest: () -> Unit,
    onApply: (keptParams: List<Filter>) -> Unit,
) {
    var keptParams by remember { mutableStateOf(listOf<Filter>()) }

    DialogScaffold(title = "select which parameters to keep", onDismissRequest) {
        Filter.entries.forEach { filter ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(filter.name.lowercase())
                Checkbox(
                    checked = filter in keptParams,
                    onCheckedChange = {
                        if (filter in keptParams)
                            keptParams -= filter
                        else
                            keptParams += filter
                    }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismissRequest) {
                Text("cancel")
            }
            TextButton(onClick = { onApply(keptParams) }) {
                Text("continue")
            }
        }
    }
}