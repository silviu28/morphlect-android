package com.sil.morphlect.ml

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.Locale

// https://github.com/hissain/AndroidSemanticSearch
class BertTokenizer {
    companion object {
        const val VOCAB_FILE = "bert_uncased_vocab.txt"
        const val UNK_TOKEN = "[UNK]"
        const val CLS_TOKEN = "[CLS]"
        const val SEP_TOKEN = "[SEP]"
    }

    constructor(context: Context) {
        loadVocabulary(context)
    }

    private val vocabMap: MutableMap<String?, Int?> = HashMap()

    fun tokenize(text: String, maxLength: Int): IntArray {
        val tokens: MutableList<String?> = ArrayList()
        tokens.add(CLS_TOKEN)

        val words: Array<String?> = text.lowercase(Locale.getDefault()).split("\\s+".toRegex())
            .dropLastWhile { it.isEmpty() }.toTypedArray()
        for (word in words) {
            if (tokens.size >= maxLength - 1) {
                break
            }
            tokens.add(if (vocabMap.containsKey(word)) word else UNK_TOKEN)
        }

        tokens.add(SEP_TOKEN)


        // Convert tokens to IDs
        val ids = IntArray(maxLength)
        for (i in tokens.indices) {
            ids[i] = vocabMap.getOrDefault(tokens[i], vocabMap[UNK_TOKEN])
                ?: 0
        }

        return ids
    }

    @Throws(IOException::class)
    private fun loadVocabulary(context: Context) {
        val reader = BufferedReader(
            InputStreamReader(
                context.assets.open(VOCAB_FILE)
            )
        )
        var line: String?
        var index = 0
        while ((reader.readLine().also { line = it }) != null) {
            vocabMap[line!!.trim()] = index++
        }
        reader.close()
    }
}