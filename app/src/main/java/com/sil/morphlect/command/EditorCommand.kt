package com.sil.morphlect.command

import org.opencv.core.Mat

/** represents an undoable action that is done from the editor. */
interface EditorCommand {
    val actionName: String
    fun execute(src: Mat): Mat
}