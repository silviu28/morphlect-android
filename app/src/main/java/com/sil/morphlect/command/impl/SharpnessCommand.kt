package com.sil.morphlect.command.impl

import com.sil.morphlect.command.EditorCommand
import com.sil.morphlect.logic.Filtering
import org.opencv.core.Mat

class SharpnessCommand(val factor: Double) : EditorCommand {
    override val actionName: String
        get() = "Sharpness ${".2f".format(factor)}"

    override fun execute(mat: Mat) = Filtering.sharpen(mat, factor)
}