package com.sil.morphlect.data

import com.sil.morphlect.enums.Filter

class Tensor3D(val data: Array<Array<FloatArray>>)

class Tensor4D(val data: Array<Array<Array<FloatArray>>>)

data class Parameters(val data: Map<Filter, Float>)

typealias BindingMap = Map<String, InferenceValue>
