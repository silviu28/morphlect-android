package com.sil.morphlect.data

import com.sil.morphlect.enums.Output

data class EvaluationResult(val outputs: Map<Output, Double>) {
    fun delta(other: EvaluationResult): EvaluationResult {
        val deltaMap = this.outputs.entries.associate { (output, initialFactor) ->
            val selectedFactor = other.outputs[output] ?: 0.0
            output to (selectedFactor - initialFactor)
        }
        return EvaluationResult(deltaMap)
    }
}