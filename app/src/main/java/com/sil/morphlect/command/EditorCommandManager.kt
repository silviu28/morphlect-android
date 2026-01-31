package com.sil.morphlect.command

import androidx.compose.runtime.snapshots.SnapshotStateList

/** represents a set of actions required to work with EditorCommands */
interface EditorCommandManager {
    var undoStack: SnapshotStateList<EditorCommand>
    var redoStack: SnapshotStateList<EditorCommand>

    fun runCommand(command: EditorCommand)
    fun undoLastCommand()
    fun redoLastCommand()
}