package com.sil.morphlect.ml.impl

import android.content.Context
import com.sil.morphlect.ml.BertTokenizer
import com.sil.morphlect.ml.ModelLoader
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

// https://github.com/hissain/AndroidSemanticSearch

class MiniLMEmbeddingLoader: ModelLoader<Any, Any> {
    companion object {
        const val MAX_SEQ_LEN = 128
        const val EMBEDDING_DIM = 384
    }

    override val name: String
        get() = "all-MiniLM-L6-v2-quant.tflite"

    private var interpreter: Interpreter? = null
    private var tokenizer: BertTokenizer? = null

    override fun initialize(context: Context): Boolean {
        return try {
            interpreter = Interpreter(FileUtil.loadMappedFile(context, name))
            tokenizer = BertTokenizer(context)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun generateEmbedding(text: String): FloatArray {
        if (tokenizer == null || interpreter == null) return FloatArray(0)

        val inputIds = tokenizer!!.tokenize(text, MAX_SEQ_LEN)
        val attentionMask = IntArray(MAX_SEQ_LEN) { i -> if (inputIds[i] != 0) 1 else 0 }

        // wrap in batch dimension [1, 128]
        val inputIdsBatch = Array(1) { inputIds }
        val attentionMaskBatch = Array(1) { attentionMask }

        // output [1, 384]
        val outputBatch = Array(1) { FloatArray(EMBEDDING_DIM) }

        val outputs = mapOf<Int, Any>(0 to outputBatch)

        interpreter!!.runForMultipleInputsOutputs(
            arrayOf(inputIdsBatch, attentionMaskBatch),
            outputs
        )

        return outputBatch[0]
    }

    override fun infer(input: Any): Any {
        TODO("Not yet implemented")
    }

    override fun close() {
        interpreter?.close()
    }

}