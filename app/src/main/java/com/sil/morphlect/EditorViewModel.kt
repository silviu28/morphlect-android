package com.sil.morphlect

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class EditorViewModel : ViewModel() {
    var section by mutableStateOf(Section.Filtering)
        private set

    var effectValues = mutableStateMapOf(
        Effect.Contrast to 0f,
        Effect.Hue to 0f,
        Effect.Blur to 0f,
        Effect.Brightness to 0f,
        Effect.LightBalance to 0f
    )
        private set

    var selectedEffect by mutableStateOf(Effect.Contrast)
        private set

    fun changeSection(section: Section) {
        this.section = section
    }

    fun changeSelectedEffect(selectedEffect: Effect) {
        this.selectedEffect = selectedEffect
    }

    fun adjustEffect(effect: Effect = selectedEffect, value: Float) {
        effectValues[effect] = value
    }
}