package com.sil.morphlect.ml.impl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.core.graphics.get
import com.sil.morphlect.data.BindingMap
import com.sil.morphlect.data.InferenceValue
import com.sil.morphlect.data.Tensor4D
import com.sil.morphlect.enums.Output
import com.sil.morphlect.exception.ModelLoaderException
import com.sil.morphlect.ml.ModelLoader
import com.sil.mxtengine.data.InteractorType
import com.sil.mxtengine.data.ModelInteractor
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.to

class ExtensionModelLoader(
    override val name: String,
    val inputs: List<ModelInteractor>,
    val outputs: List<ModelInteractor>,
    val threadCount: Int = 4,
) : ModelLoader<BindingMap, InferenceValue> {
    class Builder {
        private var name: String = "none"
        private var inputs: List<ModelInteractor> = listOf()
        private var outputs: List<ModelInteractor> = listOf()
        private var threadCount: Int = 4

        fun named(name: String): Builder {
            this.name = name
            return this
        }

        fun withInputs(inputs: List<ModelInteractor>): Builder {
            this.inputs = inputs
            return this
        }

        fun withOutputs(outputs: List<ModelInteractor>): Builder {
            this.outputs = outputs
            return this
        }

        fun withThreads(threadCount: Int): Builder {
            this.threadCount = threadCount
            return this
        }

        fun build(): ExtensionModelLoader {
            return ExtensionModelLoader(name, inputs, outputs, threadCount)
        }
    }

    override fun initialize(context: Context): Boolean {
        return try {
            val options = Interpreter.Options().apply {
                addDelegate(NnApiDelegate())
                numThreads = threadCount
            }

            val model = File(context.filesDir, "/models/$name/model.tflite")
            interpreter = Interpreter(model, options)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private var interpreter: Interpreter? = null

    fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(1 * bitmap.width * bitmap.height * 3 * Float.SIZE_BYTES)
        buffer.order(ByteOrder.nativeOrder())

        for (y in 0 until bitmap.width) {
            for (x in 0 until bitmap.height) {
                val px = bitmap[x, y]
                buffer.putFloat(Color.red(px) / 255f)
                buffer.putFloat(Color.green(px) / 255f)
                buffer.putFloat(Color.blue(px) / 255f)
            }
        }

        buffer.rewind()
        return buffer
    }

    @Throws(ModelLoaderException::class, Exception::class)
    override fun infer(inputVals: BindingMap): InferenceValue {
        if (interpreter == null) {
            throw ModelLoaderException("Unable to load the model with given properties.")
        }
        val interp = interpreter ?: throw ModelLoaderException("Unable to load the model with given properties.")

        // order inputs by index to match model's expected input order
        val fmtInputs = inputs.mapIndexed { index, inputSpec ->
            when (inputSpec.type) {
                InteractorType.Image ->
                    bitmapToByteBuffer(inputVals[inputSpec.name] as? Bitmap ?: throw Exception())

                InteractorType.Text ->
                    (inputVals[inputSpec.name] as? String ?: throw Exception()).toByteArray()

                InteractorType.TextArray -> TODO()

                InteractorType.Float ->
                    listOf(inputVals[inputSpec.name] as? Float ?: throw Exception()).toFloatArray()

                InteractorType.FloatArray ->
                    inputVals[inputSpec.name] as? FloatArray ?: throw Exception()

                else -> throw Exception()
            }
        }.toTypedArray()

        // prepare output buffers keyed by output index
        val fmtOutputs = mutableMapOf<Int, Any>()
        outputs.forEachIndexed { index, outputSpec ->
            fmtOutputs[index] = when (outputSpec.type) {
                InteractorType.FilterParams -> Array(1) { FloatArray(Output.entries.size) }
                InteractorType.Text -> Array(1) { FloatArray(outputSpec.shape[0]) }
                else -> {
                    // might not be the best... but why would you need a tensor of a bigger shape for an output?
                    when (outputSpec.shape.size) {
                        1 -> FloatArray(outputSpec.shape[0])

                        2 -> Array(outputSpec.shape[0]) { FloatArray(outputSpec.shape[1]) }

                        3 -> Array(outputSpec.shape[0]) {
                            Array(outputSpec.shape[1]) {
                                FloatArray(outputSpec.shape[2])
                            }
                        }

                        4 -> Array(outputSpec.shape[0]) {
                            Array(outputSpec.shape[1]) {
                                Array(outputSpec.shape[2]) {
                                    FloatArray(outputSpec.shape[3])
                                }
                            }
                        }

                        // oh my god bruh
                        else -> Array(1) { FloatArray(1) }
                    }
                }
            }
        }

        val inferenceResult = runCatching {
            interp.runForMultipleInputsOutputs(fmtInputs, fmtOutputs)
        }
        inferenceResult.onFailure { throwable ->
            Log.e("ML", throwable.stackTraceToString())
        }

        // map results back to Output enum
        return when (outputs[0].type){
            InteractorType.FilterParams -> {
                val resultBuffer = (fmtOutputs[0] as Array<FloatArray>)[0]
                InferenceValue.MapValue(
                    Output.entries.associate { it to resultBuffer[it.ordinal] }
                )
            }

            InteractorType.Image -> TODO()
            InteractorType.Text -> TODO()
            InteractorType.TextArray -> TODO()
            InteractorType.Float -> TODO()
            InteractorType.FloatArray -> {
                when (outputs[0].shape.size) {
                    4 -> InferenceValue.Tensor4DValue(
                        Tensor4D(fmtOutputs[0] as Array<Array<Array<FloatArray>>>)
                    )
                    else -> TODO()
                }
            }
            InteractorType.DepthMap -> TODO()
        }
    }

    override fun close() {
        interpreter?.close()
        interpreter = null
    }
}