package com.sil.mxtengine.data

import kotlinx.serialization.Serializable

@Serializable
data class MXTComposerElement(
    val type: ComposerElementType,
    val label: String,
    val parameterBindingRef: String? = null
) {
    companion object {
        val RunButton = MXTComposerElement(ComposerElementType.RunButton, "run inference")
    }
}