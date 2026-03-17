package com.sil.morphlect.command

import com.sil.morphlect.command.impl.BlurCommand
import com.sil.morphlect.command.impl.BrightnessCommand
import com.sil.morphlect.command.impl.ContrastCommand
import com.sil.morphlect.command.impl.HueCommand
import com.sil.morphlect.command.impl.LightBalanceCommand
import com.sil.morphlect.command.impl.SharpnessCommand
import com.sil.morphlect.data.EditorLayer
import com.sil.morphlect.enums.Filter
import com.sil.morphlect.exception.CommandException

/** represents an undoable action that is done from the editor. */
interface EditorCommand {
    val actionName: String
    fun execute(src: EditorLayer): EditorLayer

    companion object {
        /**
         * creates an EditorCommand that serves the given functionality.
         * @throws CommandException
         */
        fun of(filter: Filter, vararg factors: Double): EditorCommand {
            if (factors.isEmpty())
                throw CommandException("No factors provided for command.")

            return when (filter) {
                Filter.Contrast -> ContrastCommand(factors[0])
                Filter.Brightness -> BrightnessCommand(factors[0])
                Filter.Blur, Filter.BlurSecondAxis ->
                    BlurCommand(
                        xFactor = factors[0],
                        yFactor = if (factors.size == 2) factors[1] else factors[0]
                    )
                Filter.Sharpness -> SharpnessCommand(factors[0])
                Filter.Hue -> HueCommand(factors[0])
                Filter.LightBalance -> LightBalanceCommand(factors[0])
                else -> throw CommandException("Non-existent command type.")
            }
        }
    }
}