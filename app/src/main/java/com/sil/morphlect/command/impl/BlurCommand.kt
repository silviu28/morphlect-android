package com.sil.morphlect.command.impl

import com.sil.morphlect.command.EditorCommand
import com.sil.morphlect.logic.Filtering
import org.opencv.core.Mat

class BlurCommand(var xFactor: Double, var yFactor: Double) : EditorCommand {
    override val actionName: String
        get() = "Blur ${
            if (xFactor == yFactor) "%.2f".format(xFactor)
            else "[${"%.2f".format(xFactor)}, ${"%.2f".format(yFactor)}]"
        }"

    override fun execute(src: Mat): Mat {
        return Filtering.blur(src, xFactor, yFactor)
    }
}