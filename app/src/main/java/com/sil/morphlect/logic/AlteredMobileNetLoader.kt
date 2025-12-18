package com.sil.morphlect.logic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import androidx.core.graphics.scale
import java.nio.ByteOrder
import androidx.core.graphics.get
import com.sil.morphlect.enums.Output
import org.tensorflow.lite.support.common.FileUtil

class AlteredMobileNetLoader(private val context: Context) {
    companion object {
        private const val IMAGE_SIZE = 224
        private const val CHANNELS = 3
        private const val OUTPUT_SIZE = 6
        private const val MODEL_NAME = "altered_mobilenet.tflite"
    }

    private var interpreter: Interpreter? = null

    init { loadModel() }

    private fun loadModel() {
        val options = Interpreter.Options()
        val model = FileUtil.loadMappedFile(context, MODEL_NAME)
        interpreter = Interpreter(model, options)
    }

    fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val resized = bitmap.scale(IMAGE_SIZE, IMAGE_SIZE, false)
        val buffer = ByteBuffer.allocateDirect(1 * IMAGE_SIZE * IMAGE_SIZE * CHANNELS)
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

    fun infer(bitmap: Bitmap): Map<Output, Double> {
        val input = bitmapToByteBuffer(bitmap)
        val output = Array(1) { DoubleArray(OUTPUT_SIZE) }
        interpreter?.run(input, output)

        val actualOutput = output[0]
        return mapOf(
            Output.Sharpness to actualOutput[0],
            Output.Brightness to actualOutput[1],
            Output.Contrast to actualOutput[2],
            Output.Hue to actualOutput[3],
            Output.Bitrate to actualOutput[4],
            Output.QualityRating to actualOutput[5]
        )
    }

    fun close() {
        this.run {
            interpreter?.close()
            interpreter = null
        }
    }
}