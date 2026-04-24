package com.sil.mxtengine.data

import kotlinx.serialization.Serializable

typealias Shape = List<Int>

@Serializable
data class ModelInteractor(
    val name: String,
    val type: InteractorType,
    val shape: Shape
)