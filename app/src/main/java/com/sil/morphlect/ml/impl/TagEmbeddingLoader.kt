package com.sil.morphlect.ml.impl

import android.content.Context
import com.sil.morphlect.enums.Output
import com.sil.morphlect.ml.ModelLoader
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
//import org.tensorflow.lite.task.core.BaseOptions
//import org.tensorflow.lite.task.text.nlclassifier.BertNLClassifier
import java.io.IOException

class TagEmbeddingLoader: ModelLoader<List<String>, Map<Output, Double>> {
    override val name = "all-MiniLM-L6-v2-quant.tflite"
    private val vocabName = "vocab.txt"
//    private val textEmbedder: TextEmbedder? = null

    override fun initialize(context: Context): Boolean {
        return try {
            // son...
//            val options = TextEmbedderOptions.builder()
//                .setBaseOptions(BaseOptions.builder()
//                    .setModelAssetPath("all-MiniLM-L6-v2-quant.tflite")
//                    .build())
//                .build()
//            textEmbedder = TextEmbedder.createFromOptions(context, options)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun embed(text: String): FloatArray {
        return FloatArray(1)
    }

    override fun infer(input: List<String>): Map<Output, Double> {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}