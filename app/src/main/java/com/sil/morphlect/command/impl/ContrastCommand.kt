package com.sil.morphlect.command.impl

import com.sil.morphlect.command.EditorCommand
import com.sil.morphlect.logic.Filtering
import org.opencv.core.Mat

class ContrastCommand(var factor: Double) : EditorCommand {
    override val actionName
        get() = "Contrast ${"%.2f".format(factor)}"

    override fun execute(src: Mat): Mat {
        return Filtering.contrast(src, factor)
    }
}
