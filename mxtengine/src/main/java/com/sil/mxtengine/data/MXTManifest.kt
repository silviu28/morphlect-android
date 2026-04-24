package com.sil.mxtengine.data

import kotlinx.serialization.Serializable

@Serializable
data class MXTManifest(
    val name: String,
    val description: String,
    val inputs: List<ModelInteractor>,
    val outputs: List<ModelInteractor>,
    val ui: List<MXTComposerElement>,
    val after: MXTAfterArgs
)