package com.sil.morphlect.ml.impl

import android.content.Context
import com.sil.morphlect.enums.Output
import com.sil.morphlect.ml.ModelLoader

class TagEmbeddingLoader: ModelLoader<List<String>, Map<Output, Double>> {
    override val name = "tag_embedding.tflite"

    override fun initialize(context: Context): Boolean {
        TODO("Not yet implemented")
    }

    override fun infer(input: List<String>): Map<Output, Double> {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}