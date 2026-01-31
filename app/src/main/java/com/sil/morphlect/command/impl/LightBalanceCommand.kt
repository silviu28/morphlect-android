package com.sil.morphlect.command.impl

import com.sil.morphlect.command.EditorCommand
import com.sil.morphlect.logic.Filtering
import org.opencv.core.Mat

class LightBalanceCommand(val factor: Double) : EditorCommand {
    override val actionName: String
        get() = "LB ${".2f".format(factor)}"

    override fun execute(src: Mat): Mat = Filtering.lightBalance(src, factor)
}