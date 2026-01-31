package com.sil.morphlect.command.impl

import com.sil.morphlect.command.EditorCommand
import com.sil.morphlect.logic.Filtering
import org.opencv.core.Mat

class BrightnessCommand(var factor: Double) : EditorCommand {
    override val actionName: String
        get() = "Brightness ${"%.2f".format(factor)}"

    override fun execute(src: Mat): Mat {
        return Filtering.brightness(src, factor)
    }
}