package com.sil.morphlect.command.impl

import com.sil.morphlect.command.StudioCommand
import com.sil.morphlect.layerwork.StudioLayer
import com.sil.morphlect.imgproc.Filtering

class HueCommand(val factor: Double) : StudioCommand {
    override val actionName: String
        get() = "Hue ${".2f".format(factor)}"

    override fun execute(src: StudioLayer): StudioLayer {
        return StudioLayer(Filtering.hueShift(src.mat, factor))
    }
}