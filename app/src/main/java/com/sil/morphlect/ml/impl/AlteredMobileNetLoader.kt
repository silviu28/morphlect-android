package com.sil.morphlect.ml.impl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.get
import androidx.core.graphics.scale
import com.sil.morphlect.enums.Output
import com.sil.morphlect.exception.ModelLoaderException
import com.sil.morphlect.ml.ModelLoader
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AlteredMobileNetLoader(private val context: Context) : ModelLoader<Bitmap, Map<Output, Float>> {
    companion object {
        private const val IMAGE_SIZE = 224
        private const val CHANNELS = 3
        private const val OUTPUT_SIZE = 6
        private const val MODEL_NAME = "altered_mobilenet.tflite"
    }

    override val modelName = "altered_mobilenet.tflite"

    override fun initialize(): Boolean {
        return try {
            val options = Interpreter.Options()
            val model = FileUtil.loadMappedFile(context, modelName)
            interpreter = Interpreter(model, options)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private var interpreter: Interpreter? = null

    fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val resized = bitmap.scale(IMAGE_SIZE, IMAGE_SIZE, false)
        val buffer = ByteBuffer.allocateDirect(1 * IMAGE_SIZE * IMAGE_SIZE * CHANNELS * Float.SIZE_BYTES)
        buffer.order(ByteOrder.nativeOrder())

        for (y in 0 until IMAGE_SIZE) {
            for (x in 0 until IMAGE_SIZE) {
                val px = resized[x, y]
                buffer.putFloat(Color.red(px) / 255f)
                buffer.putFloat(Color.green(px) / 255f)
                buffer.putFloat(Color.blue(px) / 255f)
            }
        }

        buffer.rewind()
        return buffer
    }

    override fun infer(bitmap: Bitmap): Map<Output, Float> {
        if (interpreter == null) {
            if (!initialize())
                throw ModelLoaderException("Unable to load the model with given properties.")
        }

        val input = bitmapToByteBuffer(bitmap)
        val output = Array(1) { FloatArray(OUTPUT_SIZE) }
        interpreter!!.run(input, output)

        val actualOutput = output[0]
        return Output.entries.associate { it to actualOutput[it.ordinal] }
    }

    override fun dispose() {
        interpreter?.close()
        interpreter = null
    }
}