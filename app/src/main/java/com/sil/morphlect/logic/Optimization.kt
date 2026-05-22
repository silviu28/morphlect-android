package com.sil.morphlect.logic

import com.sil.morphlect.enums.Output
import com.sil.morphlect.ml.impl.RatingMaximizerLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

private typealias Solution = Map<Output, Float>

suspend fun RatingMaximizerLoader.optimizeComposition(
    current: Map<Output, Float>,
    iterationCount: Int
): Map<Output, Float> = withContext(Dispatchers.Default) {
    var stepSize = .05
    var decayPerStep = .995
    var bestSolution = current.toMutableMap().apply { remove(Output.QualityRating) }
    var bestScore = current[Output.QualityRating]!!
    var history = mutableListOf(bestScore)

    for (i in 0..iterationCount) {
        // shift the solutions a bit
        var next = bestSolution.entries
            .associate { entry ->
                val disposition
                    = ((Math.random() * stepSize) * Random.nextInt(-1, 1)).toFloat()
                entry.key to (entry.value + disposition).coerceIn(-1f, 1f)
            }
        // compute the solution's score
        val newScore = infer(next)
        // if the score is better, set the solution as the best
        if (newScore > bestScore) {
            bestSolution = next.toMutableMap()
            bestScore = newScore
        }
        // add to history and decay step
        history.add(newScore)
        stepSize *= decayPerStep
    }

    bestSolution[Output.QualityRating] = bestScore
    return@withContext bestSolution.toMap()
}