package com.sil.morphlect.command

import androidx.compose.runtime.snapshots.SnapshotStateList

/** represents a set of actions required to work with StudioCommands */
interface StudioCommandManager {
    var undoStack: SnapshotStateList<StudioCommand>
    var redoStack: SnapshotStateList<StudioCommand>

    fun runCommand(command: StudioCommand)
    fun undoLastCommand()
    fun redoLastCommand()
}