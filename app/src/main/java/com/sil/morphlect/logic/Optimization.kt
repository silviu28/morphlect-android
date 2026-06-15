package com.sil.morphlect.logic

//import com.sil.morphlect.enums.Output
import com.sil.morphlect.ml.impl.RatingMaximizerLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//suspend fun RatingMaximizerLoader.optimizeComposition(
//    current: Map<Output, Float>,
//    iterationCount: Int
//): Map<Output, Float> = withContext(Dispatchers.Default) {
//    return@withContext emptyMap()
//    var stepSize = .05
//    val decayPerStep = .995
////    var bestSolution = current.toMutableMap().apply { remove(Output.QualityRating) }
//    var bestSolution = current
////    var bestScore = current[Output.QualityRating]!!
//    var bestScore: Float = 0f
//    for (i in 0..iterationCount) {
//        val next = bestSolution.entries
//            .associate { entry ->
//                val disposition = ((Math.random() * stepSize) * if (Math.random() > 0.5) 1 else -1).toFloat()
//                entry.key to (entry.value + disposition).coerceIn(-0.8f, 0.8f)
//            }
//
//        val newScore = infer(next).coerceAtMost(1f)
//
//        if (newScore > bestScore) {
//            bestSolution = next.toMutableMap()
//            bestScore = newScore
//        }
//
//        stepSize *= decayPerStep
//    }
//
////    bestSolution[Output.QualityRating] = bestScore
//    return@withContext bestSolution.toMap()
//}