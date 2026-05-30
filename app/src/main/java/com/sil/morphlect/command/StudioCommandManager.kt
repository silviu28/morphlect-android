package com.sil.morphlect.command

import androidx.compose.runtime.snapshots.SnapshotStateList

/** represents a set of actions required to work with StudioCommands */
interface StudioCommandManager {
    val undoStack: List<StudioCommand>
    val redoStack: List<StudioCommand>

    fun runCommand(command: StudioCommand)
    fun undoLastCommand()
    fun redoLastCommand()
}