package com.sil.morphlect.logic

import android.util.Log
import org.opencv.core.Mat

object Filtering {
    fun brightness(src: Mat, brightness: Double): Mat {
        Log.i("Filtering", "apply $brightness")
        val dst = Mat()
        src.convertTo(dst, -1, 1.0, brightness * 100)
        return dst
    }
}