package com.sil.mxtengine.data

import kotlinx.serialization.Serializable

@Serializable
data class MXTManifestRef(
    val name: String,
    val pipelinedOutputs: Map<String, String> = emptyMap()
)

@Serializable
data class MXTAfterArgs(
    val applyResult: Boolean,
    val next: MXTManifestRef? = null,
)
