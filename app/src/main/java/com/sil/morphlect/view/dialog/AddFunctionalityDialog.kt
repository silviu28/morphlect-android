package com.sil.morphlect.view.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sil.morphlect.repository.ModelsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFunctionalityDialog(
    onDismissRequest: () -> Unit,
) {
    val ctx = LocalContext.current
    val modelsRepository = ModelsRepository(ctx)

    var dropdownExpanded by remember { mutableStateOf(false) }
    var modelNames by remember { mutableStateOf<List<String>>(listOf()) }
    var selectedModel by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        modelNames = modelsRepository.readContents()
    }

    DialogScaffold(
        title = "add functionality",
        onDismissRequest
    ) {
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedModel ?: "select a model",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                modelNames.forEach { modelOption ->
                    DropdownMenuItem(
                        text = { Text(modelOption) },
                        onClick = {
                            selectedModel = modelOption
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismissRequest) {
                Text("cancel")
            }
            TextButton(
                onClick = { }
            ) {
                Text("add")
            }
        }
    }

}