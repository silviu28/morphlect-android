package com.sil.morphlect.ml

/**
 defines the base behaviour of an object that loads a machine-learning model.
 */
interface ModelLoader<TIn, TOut> {
    val modelName: String

    fun initialize(): Boolean
    fun infer(input: TIn): TOut
    fun dispose()
}