package com.sil.morphlect.dto

import kotlinx.serialization.Serializable

/**
 * contains a brief of a model retrieved from the server.
 */
@Serializable
data class ModelInfoDTO(
    val name: String,
    val description: String,
    val size: Long,
)