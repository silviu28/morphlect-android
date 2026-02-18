package com.sil.morphlect.command

import com.sil.morphlect.command.impl.BlurCommand
import com.sil.morphlect.command.impl.BrightnessCommand
import com.sil.morphlect.command.impl.ContrastCommand
import com.sil.morphlect.command.impl.HueCommand
import com.sil.morphlect.command.impl.LightBalanceCommand
import com.sil.morphlect.command.impl.SharpnessCommand
import com.sil.morphlect.enums.Effect
import com.sil.morphlect.exception.CommandException
import org.opencv.core.Mat

/** represents an undoable action that is done from the editor. */
interface EditorCommand {
    val actionName: String
    fun execute(src: Mat): Mat

    companion object {
        /**
         * creates an EditorCommand that serves the given functionality.
         * @throws CommandException
         */
        fun of(effect: Effect, vararg factors: Double): EditorCommand? {
            if (factors.isEmpty())
                throw CommandException("No factors provided for command.")

            return when (effect) {
                Effect.Contrast -> ContrastCommand(factors[0])
                Effect.Brightness -> BrightnessCommand(factors[0])
                Effect.Blur ->
                    if (factors.size > 2) {
                    BlurCommand(
                        xFactor = factors[0],
                        yFactor = factors[1],
                    )
                } else throw CommandException("Wrong factor count for this command.")
                Effect.Sharpness -> SharpnessCommand(factors[0])
                Effect.Hue -> HueCommand(factors[0])
                Effect.LightBalance -> LightBalanceCommand(factors[0])
                else -> null
            }
        }
    }
}