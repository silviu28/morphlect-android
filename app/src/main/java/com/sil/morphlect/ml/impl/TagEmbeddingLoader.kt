package com.sil.morphlect.ml.impl

import android.content.Context
import com.sil.morphlect.enums.Output
import com.sil.morphlect.ml.ModelLoader

class TagEmbeddingLoader(private val context: Context) : ModelLoader<List<String>, Map<Output, Double>> {
    override val modelName = "tag_embedding.tflite"

    override fun initialize(): Boolean {
        TODO("Not yet implemented")
    }

    override fun infer(input: List<String>): Map<Output, Double> {
        TODO("Not yet implemented")
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }
}