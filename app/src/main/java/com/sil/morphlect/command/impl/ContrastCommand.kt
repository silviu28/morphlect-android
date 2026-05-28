package com.sil.morphlect.command.impl

import com.sil.morphlect.command.StudioCommand
import com.sil.morphlect.data.StudioLayer
import com.sil.morphlect.logic.Filtering

class ContrastCommand(var factor: Double) : StudioCommand {
    override val actionName
        get() = "Contrast ${"%.2f".format(factor)}"

    override fun execute(src: StudioLayer): StudioLayer {
        return StudioLayer(Filtering.contrast(src.mat, factor))
    }
}
