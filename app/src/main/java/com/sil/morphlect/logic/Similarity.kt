package com.sil.morphlect.logic

import kotlin.math.sqrt

@Throws(UnsupportedOperationException::class)
fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
    if (a.size != b.size)
        throw UnsupportedOperationException("Cannot compute similarity between vectors of different sizes.")

    var dot = 0f
    var normA = 0f
    var normB = 0f
    for (i in 0..<a.size) {
        dot += a[i] * b[i]
        normA += a[i] * a[i]
        normB += b[i] * b[i]
    }
    return dot / (sqrt(normA) * sqrt(normB))
}