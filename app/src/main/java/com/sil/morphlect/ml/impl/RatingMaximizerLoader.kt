package com.sil.morphlect.ml.impl

import android.content.Context
import com.sil.morphlect.enums.Output
import com.sil.morphlect.exception.ModelLoaderException
import com.sil.morphlect.ml.ModelLoader
import okio.IOException
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil

class RatingMaximizerLoader : ModelLoader<Map<Output, Float>, Float> {
    companion object {
        private const val IMAGE_SIZE = 224
        private const val CHANNELS = 3
        private const val OUTPUT_SIZE = 6
        private const val MODEL_NAME = "ratingmaximizer.tflite"
    }

    override val name = "ratingmaximizer.tflite"

    private var interpreter: Interpreter? = null

    override fun initialize(context: Context): Boolean {
        return try {
            val options = Interpreter.Options()
            val model = FileUtil.loadMappedFile(context, name)
            interpreter = Interpreter(model, options)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    override fun infer(input: Map<Output, Float>): Float {
        if (interpreter == null) {
            throw ModelLoaderException("Unable to load the model with given properties.")
        }

        val output = arrayOf(floatArrayOf(0f))
        val fmtInput = input.map { (_, value) -> value }.toFloatArray()
        interpreter!!.run(fmtInput, output)

        return output[0][0]
    }

    override fun close() {
        interpreter?.close()
        interpreter = null
    }
}