package com.sil.morphlect.ml

import android.content.Context
import com.sil.morphlect.enums.Output
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil

class RatingMiximizerLoader(private val context: Context) {
    companion object {
        private const val IMAGE_SIZE = 224
        private const val CHANNELS = 3
        private const val OUTPUT_SIZE = 6
        private const val MODEL_NAME = "ratingmaximizer.tflite"
    }

    private var interpreter: Interpreter? = null

    init { loadModel() }

    private fun loadModel() {
        val options = Interpreter.Options()
        val model = FileUtil.loadMappedFile(context, MODEL_NAME)
        interpreter = Interpreter(model, options)
    }

    fun infer(input: Map<Output, Double>): Double {
        val output = arrayOf(doubleArrayOf(0.0))
        interpreter?.run(input, output)

        return output[0][0]
    }

    fun close() {
        this.run {
            interpreter?.close()
            interpreter = null
        }
    }
}