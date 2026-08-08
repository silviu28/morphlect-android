package com.sil.morphlect.data

import android.graphics.Bitmap
import java.nio.ByteBuffer

sealed class InferenceValue {
    data class StringValue(val value: String): InferenceValue()
    data class MapValue<TKey, TValue>(val value: Map<TKey, TValue>): InferenceValue()
    data class Tensor4DValue(val value: Tensor4D): InferenceValue()
    data class FloatValue(val value: Float): InferenceValue()
    data class FloatArrayValue(val value: FloatArray): InferenceValue()
    data class BitmapValue(val value: Bitmap): InferenceValue()
    data class ByteBufferValue(val value: ByteBuffer): InferenceValue()
}