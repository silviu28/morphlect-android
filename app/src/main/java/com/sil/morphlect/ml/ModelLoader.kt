package com.sil.morphlect.ml

import android.content.Context

/**
 defines the base behaviour of an object that loads a machine-learning model.
 */
interface ModelLoader<TIn, TOut> : AutoCloseable {
    val name: String

    fun initialize(context: Context): Boolean
    fun infer(input: TIn): TOut
}