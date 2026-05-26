package com.sil.morphlect.ml.impl

import android.content.Context
import android.util.Log
import com.sil.morphlect.data.Preset
import com.sil.morphlect.enums.Filter
import com.sil.morphlect.ml.BertTokenizer
import com.sil.morphlect.ml.ModelLoader
import com.sil.morphlect.ml.cosineSimilarity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

// https://github.com/hissain/AndroidSemanticSearch

class MiniLMEmbeddingLoader: ModelLoader<Any, Any> {
    companion object {
        const val MAX_SEQ_LEN = 128
        const val EMBEDDING_DIM = 384
        const val ANCHORS_FILE = "anchors.json"
    }

    override val name: String
        get() = "all-MiniLM-L6-v2-quant.tflite"

    private var interpreter: Interpreter? = null
    private var tokenizer: BertTokenizer? = null
    private var anchorPresets: List<Preset> = listOf()
    private var anchorEmbeddings: List<FloatArray> = listOf()

    override fun initialize(context: Context): Boolean {
        return try {
            interpreter = Interpreter(FileUtil.loadMappedFile(context, name))
            tokenizer = BertTokenizer(context)
            anchorPresets = try {
                val jsonString = context.assets.open(ANCHORS_FILE)
                    .bufferedReader()
                    .use { it.readText() }

                val jsonArray = JSONArray(jsonString)

                (0 until jsonArray.length()).map { index ->
                    val jsonObject = jsonArray.getJSONObject(index)
                    val name = jsonObject.getString("name")

                    val paramsMap = mutableMapOf<Filter, Double>()
                    val paramsObject = jsonObject.getJSONObject("params")

                    paramsObject.keys().forEach { key ->
                        try {
                            val filter = Filter.valueOf(key)
                            val value = paramsObject.getDouble(key)
                            paramsMap[filter] = value
                        } catch (e: IllegalArgumentException) {
                            Log.w("PresetLoader", "Unknown filter: $key")
                        }
                    }
                    Preset(name, paramsMap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
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

    suspend fun computeMostSimilarAnchors(limit: Int = 5, word: String): List<Pair<String, Float>>
        = withContext(Dispatchers.Default) {
        if (anchorEmbeddings.isEmpty())
            anchorEmbeddings = anchorPresets.map { anchor ->
                generateEmbedding(anchor.name)
            }
        val wordEmbed = generateEmbedding(word)

        // return
        anchorEmbeddings
            .mapIndexed { idx, it ->
                anchorPresets[idx].name to cosineSimilarity(it, wordEmbed)
            }
            .sortedBy { (_, score) -> score }
            .take(limit)
    }

    suspend fun batchComputeSimilarAnchors(limit: Int = 5, words: List<String>): List<List<Pair<String, Float>>>
        = withContext(Dispatchers.Default) { words.map { computeMostSimilarAnchors(limit, it) } }

    fun combineMultipleTags(
        tagsSimilarities: List<List<Pair<String, Float>>>
    ): Preset {
        if (tagsSimilarities.isEmpty()) return Preset("combined", emptyMap())

        // sum similarities for each anchor across all tags
        val aggregatedScores = mutableMapOf<String, Float>()

        tagsSimilarities.forEach { tagScores ->
            tagScores.forEach { (anchorName, score) ->
                aggregatedScores[anchorName] = (aggregatedScores[anchorName] ?: 0f) + score
            }
        }

        // normalize and create interpolated preset
        return interpolatePreset("combined", aggregatedScores.toList())
    }

    private fun interpolatePreset(
        name: String,
        similarityScores: List<Pair<String, Float>>
    ): Preset {
        val validScores = similarityScores.filter { it.second > 0 }
        if (validScores.isEmpty()) return Preset(name, emptyMap())

        val totalWeight = validScores.fold(0f) { acc, it -> acc + it.second }
        val normalized = validScores.associate { (anchor, score) ->
            anchor to (score / totalWeight).toDouble()
        }

        val combinedParams = mutableMapOf<Filter, Double>()

        anchorPresets
            .filter { normalized.containsKey(it.name) }
            .forEach { preset ->
                val weight = normalized[preset.name]!!
                preset.params.forEach { (filter, value) ->
                    combinedParams[filter] = (combinedParams[filter] ?: 0.0) + (value * weight)
                }
            }

        return Preset(name, combinedParams)
    }

    fun computeNewPreset(name: String, similarityScores: List<Pair<String, Float>>): Preset {
        val validScores = similarityScores.filter { it.second > 0 }
        if (validScores.isEmpty()) return Preset(name, emptyMap())

        val totalWeight = validScores.fold(0f) { acc, it -> acc + it.second }

        val normalizedWeights = validScores.associate { (anchorName, score) ->
            anchorName to (score / totalWeight).toDouble()
        }

        val combinedParams = anchorPresets
            .filter { normalizedWeights.containsKey(it.name) }
            .flatMap { preset ->
                preset.params.map { (filter, value) ->
                    filter to value * normalizedWeights[preset.name]!!
                }
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, values) -> values.sum() }

        return Preset(name, combinedParams)
    }
}