package com.sil.morphlect.data

import com.sil.morphlect.enums.Output

class Tensor3D(val data: Array<Array<FloatArray>>)

class Tensor4D(val data: Array<Array<Array<FloatArray>>>)

class Parameters(val data: Map<Output, Float>)

typealias BindingMap = Map<String, InferenceValue>
