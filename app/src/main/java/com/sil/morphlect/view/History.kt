package com.sil.morphlect.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sil.morphlect.command.EditorCommand
import com.sil.morphlect.view.dialog.DialogScaffold

private enum class HistoryEntryAction { Undo, Redo }

@Composable
fun History(
    onDismissRequest: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    undoStack: List<EditorCommand>,
    redoStack: List<EditorCommand>,
) {
    DialogScaffold(
        "history",
        onDismissRequest,
        icon = Icons.Default.History,
    ) {
        if (undoStack.isEmpty() && redoStack.isEmpty()) {
            Text("no recent history")
        } else {
            if (undoStack.isNotEmpty()) {
                Text("undo")
                undoStack.forEach { HistoryEntry(it, onUndo, HistoryEntryAction.Undo) }
            }

            if (redoStack.isNotEmpty()) {
                Text("redo")
                redoStack.forEach { HistoryEntry(it, onRedo, HistoryEntryAction.Redo) }
            }
        }
    }
}

@Composable
private fun HistoryEntry(
    command: EditorCommand,
    onClick: () -> Unit,
    action: HistoryEntryAction
) {
    val baseColor = MaterialTheme.colorScheme.background
    val insetColor = baseColor.copy(
        red = (baseColor.red + 0.04f).coerceAtMost(1f),
        green = (baseColor.green + 0.04f).coerceAtMost(1f),
        blue = (baseColor.blue + 0.04f).coerceAtMost(1f),
    )

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(insetColor)
            .padding(8.dp)
    ) {
        Text(command.actionName)
        Button(onClick) {
            if (action == HistoryEntryAction.Undo) {
                Icon(
                    Icons.AutoMirrored.Default.Undo,
                    contentDescription = "undo",
                )
            } else {
                Icon(
                    Icons.AutoMirrored.Default.Redo,
                    contentDescription = "redo",
                )
            }
        }
    }
}