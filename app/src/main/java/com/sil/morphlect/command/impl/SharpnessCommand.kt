package com.sil.morphlect.command.impl

import com.sil.morphlect.command.StudioCommand
import com.sil.morphlect.data.StudioLayer
import com.sil.morphlect.logic.Filtering

class SharpnessCommand(val factor: Double) : StudioCommand {
    override val actionName: String
        get() = "Sharpness ${".2f".format(factor)}"

    override fun execute(src: StudioLayer): StudioLayer {
        return StudioLayer(Filtering.sharpen(src.mat, factor))
    }
}