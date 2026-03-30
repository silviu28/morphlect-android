package com.sil.morphlect.data

import com.sil.morphlect.enums.Filter

data class EvaluationResult(val filters: Map<Filter, Double>)