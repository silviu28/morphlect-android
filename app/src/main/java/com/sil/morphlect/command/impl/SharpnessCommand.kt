package com.sil.morphlect.command.impl

import com.sil.morphlect.command.EditorCommand
import com.sil.morphlect.data.EditorLayer
import com.sil.morphlect.logic.Filtering
import org.opencv.core.Mat

class SharpnessCommand(val factor: Double) : EditorCommand {
    override val actionName: String
        get() = "Sharpness ${".2f".format(factor)}"

    override fun execute(src: EditorLayer): EditorLayer {
        return EditorLayer(src.name, Filtering.sharpen(src.mat, factor))
    }
}