package com.sil.mxtengine.data

import kotlinx.serialization.Serializable

@Serializable
enum class InteractorType {
    FilterParams,
    Image,
    Text,
    Float,
    FloatArray,
    DepthMap,
}