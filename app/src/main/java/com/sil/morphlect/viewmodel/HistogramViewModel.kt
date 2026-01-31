package com.sil.morphlect.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.graphics.blue
import androidx.core.graphics.get
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
/**
 describes the business logic for the histogram.
*/
class HistogramViewModel : ViewModel() {
    var redValues = mutableStateOf(IntArray(256))
    var greenValues = mutableStateOf(IntArray(256))
    var blueValues = mutableStateOf(IntArray(256))

    fun getMergedReds(): IntArray {
        var res = IntArray(256)
        val barCount = 256 / histogramMergeFactor.intValue
        for (i in 0 until barCount) {
            var highestMergeValue = 0
            val startIndex = i * histogramMergeFactor.intValue

            for (seg in 0 until histogramMergeFactor.intValue) {
                highestMergeValue = max(highestMergeValue, redValues.value[startIndex + seg])
            }

            for (seg in 0 until histogramMergeFactor.intValue) {
                res[startIndex + seg] = highestMergeValue
            }
        }
        return res
    }

    fun getMergedGreens(): IntArray {
        var res = IntArray(256)
        val barCount = 256 / histogramMergeFactor.intValue
        for (i in 0 until barCount) {
            var highestMergeValue = 0
            val startIndex = i * histogramMergeFactor.intValue

            for (seg in 0 until histogramMergeFactor.intValue) {
                highestMergeValue = max(highestMergeValue, greenValues.value[startIndex + seg])
            }

            for (seg in 0 until histogramMergeFactor.intValue) {
                res[startIndex + seg] = highestMergeValue
            }
        }
        return res
    }

    fun getMergedBlues(): IntArray {
        var res = IntArray(256)
        val barCount = 256 / histogramMergeFactor.intValue
        for (i in 0 until barCount) {
            var highestMergeValue = 0
            val startIndex = i * histogramMergeFactor.intValue

            for (seg in 0 until histogramMergeFactor.intValue) {
                highestMergeValue = max(highestMergeValue, blueValues.value[startIndex + seg])
            }

            for (seg in 0 until histogramMergeFactor.intValue) {
                res[startIndex + seg] = highestMergeValue
            }
        }
        return res
    }

    var redAvg = .0
    var greenAvg = .0
    var blueAvg = .0

    var histogramMergeFactor = mutableIntStateOf(1)

    fun increaseHistogramMergeFactor() {
        histogramMergeFactor.intValue = (histogramMergeFactor.intValue * 2).coerceIn(1, 16)
    }
    fun decreaseHistogramMergeFactor() {
        histogramMergeFactor.intValue = (histogramMergeFactor.intValue / 2).coerceIn(1, 16)
    }

    var histogramYUpperLimit = -1

    fun computeHistogram(colorReference: Bitmap) {
        viewModelScope.launch(Dispatchers.Default) {
            val reds = IntArray(256)
            val greens = IntArray(256)
            val blues = IntArray(256)

            val size = colorReference.height * colorReference.width
            var redSum = 0
            var greenSum = 0
            var blueSum = 0

            for (y in 0 until colorReference.height) {
                for (x in 0 until colorReference.width) {
                    val red = colorReference[x, y].red.toInt() and 0xff
                    val green = colorReference[x, y].green.toInt() and 0xff
                    val blue = colorReference[x, y].blue.toInt() and 0xff

                    reds[red] += 1
                    greens[green] += 1
                    blues[blue] += 1

                    redSum += red
                    greenSum += green
                    blueSum += blue
                }
            }

            withContext(Dispatchers.Main) {
                redValues.value = reds
                greenValues.value = greens
                blueValues.value = blues

                histogramYUpperLimit = maxOf(
                    reds.maxOrNull() ?: 0,
                    greens.maxOrNull() ?: 0,
                    blues.maxOrNull() ?: 0
                )

                redAvg = (redSum / size).toDouble()
                greenAvg = (greenSum / size).toDouble()
                blueAvg = (blueSum / size).toDouble()
            }
        }
    }
}