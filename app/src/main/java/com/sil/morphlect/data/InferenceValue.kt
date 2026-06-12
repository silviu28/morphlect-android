package com.sil.morphlect.data

import android.graphics.Bitmap

sealed class InferenceValue {
    data class StringValue(val value: String?): InferenceValue()
    data class MapValue<TKey, TValue>(val value: Map<TKey, TValue>?): InferenceValue()
    data class Tensor4DValue(val value: Tensor4D): InferenceValue()
    data class FloatValue(val value: Float?): InferenceValue()
    class Empty(): InferenceValue()
    data class BitmapValue(val bitmap: Bitmap): InferenceValue()
}