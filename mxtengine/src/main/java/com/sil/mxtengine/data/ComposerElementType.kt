package com.sil.mxtengine.data

import kotlinx.serialization.Serializable

@Serializable
enum class ComposerElementType {
    RunButton,
    ImageUpload,
    TextInput,
    AudioUpload,
    FloatGauge,
}