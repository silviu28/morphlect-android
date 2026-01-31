package com.sil.morphlect.ml

/**
 defines the base behaviour of an object that loads a machine-learning model.
 */
interface ModelLoader<In, Out> {
    val modelName: String

    fun initialize(): Boolean
    fun infer(input: In): Out
    fun dispose()
}