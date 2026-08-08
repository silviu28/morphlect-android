package com.sil.morphlect.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sil.morphlect.layerwork.LayerPosition
import com.sil.morphlect.view.dialog.DialogScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddingTextOverlay(
    onDismissRequest: () -> Unit,
    onConfirm: (String, Int, LayerPosition, Color) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    var textSize by remember { mutableIntStateOf(24) }
    var position by remember { mutableStateOf(LayerPosition.Center) }
    var colorDropdownExpanded by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(Color.White) }
    var selectedColorName by remember { mutableStateOf("white") }

    DialogScaffold(
        title = "add text",
        onDismissRequest = { onDismissRequest() },
        icon = Icons.Default.TextFields
    ) {
        Column(
            modifier = Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { }
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("your text here") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = Color.White,
                    focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                ),
            )

                Text("text size")
                OutlinedTextField(
                    value = textSize.toString(),
                    onValueChange = { value -> value.toIntOrNull()?.let { textSize = it } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = Color.White,
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                    ),
                    modifier = Modifier.width(80.dp)
                )

                Text("text color")
                ExposedDropdownMenuBox(
                    expanded = colorDropdownExpanded,
                    onExpandedChange = { colorDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedColorName,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = colorDropdownExpanded,
                        onDismissRequest = { colorDropdownExpanded = false }
                    ) {
                        listOf("red", "green", "blue", "white", "black").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedColorName = option
                                    selectedColor = when (option) {
                                        "red" -> Color.Red
                                        "green" -> Color.Green
                                        "blue" -> Color.Blue
                                        "white" -> Color.White
                                        "black" -> Color.Black
                                        else -> Color.White
                                    }
                                    colorDropdownExpanded = false
                                }
                            )
                        }
                    }
            }

            Text("position")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            ) {
                LayerPosition.entries.forEach { pos ->
                    RadioButton(
                        selected = (position == pos),
                        onClick = { position = pos },
                        modifier = Modifier
                            .align(pos.asAlignment())
                            .padding(8.dp)
                    )
                }
            }

            IconButton(onClick = { onConfirm(text, textSize, position, selectedColor) }) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Confirm",
                    tint = Color.White
                )
            }
        }
    }
}

fun LayerPosition.asAlignment(): Alignment =
    when (this) {
        LayerPosition.TopLeft -> Alignment.TopStart
        LayerPosition.TopCenter -> Alignment.TopCenter
        LayerPosition.TopRight -> Alignment.TopEnd
        LayerPosition.CenterLeft -> Alignment.CenterStart
        LayerPosition.Center -> Alignment.Center
        LayerPosition.CenterRight -> Alignment.CenterEnd
        LayerPosition.BottomLeft -> Alignment.BottomStart
        LayerPosition.BottomCenter -> Alignment.BottomCenter
        LayerPosition.BottomRight -> Alignment.BottomEnd
    }