package com.sil.morphlect.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.core.graphics.blue
import androidx.core.graphics.get
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HistogramViewModel : ViewModel() {
    private var histogramChanged = mutableStateOf(false)

    var redValues = IntArray(256)
    var greenValues = IntArray(256)
    var blueValues = IntArray(256)

    var redAvg = .0
    var greenAvg = .0
    var blueAvg = .0

    var histogramYUpperLimit = -1

    fun computeHistogram(colorReference: Bitmap) {
        viewModelScope.launch {
            if (histogramChanged.value) return@launch

            var size = colorReference.height * colorReference.width

            for (y in 0 until colorReference.height) {
                for (x in 0 until colorReference.width) {
                    val red = colorReference[x, y].red.toInt() and 0xff
                    val green = colorReference[x, y].green.toInt() and 0xff
                    val blue = colorReference[x, y].blue.toInt() and 0xff

                    redValues[red] += 1
                    greenValues[green] += 1
                    blueValues[blue] += 1

                    histogramYUpperLimit =
                        maxOf(redValues[red], greenValues[green], blueValues[blue])

                    redAvg += red
                    greenAvg += green
                    blueAvg += blue
                }
            }

            redAvg /= size
            greenAvg /= size
            blueAvg /= size
        }
    }
}