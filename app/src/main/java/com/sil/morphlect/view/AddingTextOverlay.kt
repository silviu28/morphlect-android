package com.sil.morphlect.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.sil.morphlect.layerwork.LayerPosition
import com.sil.morphlect.ui.theme.MorphlectTheme

@Composable
fun AddingTextOverlay(
    onDismissRequest: () -> Unit,
    onConfirm: (String, Int, LayerPosition) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    var textSize by remember { mutableIntStateOf(24) }
    var position by remember { mutableStateOf(LayerPosition.CENTER)}

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .zIndex(10f)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismissRequest() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
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
                    placeholder = { Text("Enter text...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = Color.White,
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                    ),
                )

                Row {
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
                }

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

                IconButton(onClick = { onConfirm(text, textSize, position) }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Confirm",
                        tint = Color.White
                    )
                }
            }
        }
    }
}