package com.sil.morphlect.command

import androidx.compose.runtime.snapshots.SnapshotStateList

interface EditorCommandManager {
    var undoStack: SnapshotStateList<EditorCommand>
    var redoStack: SnapshotStateList<EditorCommand>

    fun runCommand(command: EditorCommand)
    fun undoLastCommand()
    fun redoLastCommand()
}