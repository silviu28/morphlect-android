package com.sil.morphlect.command.impl

import com.sil.morphlect.command.StudioCommand
import com.sil.morphlect.data.StudioLayer
import com.sil.morphlect.logic.Filtering

class BlurCommand(var xFactor: Double, var yFactor: Double) : StudioCommand {
    override val actionName: String
        get() = "Blur ${
            if (xFactor == yFactor) "%.2f".format(xFactor)
            else "[${"%.2f".format(xFactor)}, ${"%.2f".format(yFactor)}]"
        }"

    override fun execute(src: StudioLayer): StudioLayer {
        return StudioLayer(Filtering.blur(src.mat, xFactor, yFactor))
    }
}