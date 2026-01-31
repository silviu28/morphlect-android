package com.sil.morphlect.view

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sil.morphlect.view.custom.DraggablePopup
import com.sil.morphlect.viewmodel.HistogramViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistogramBottomSheet(
    onDismissRequest: () -> Unit,
    colorReference: Bitmap,
    vm: HistogramViewModel = viewModel()
) {
    vm.computeHistogram(colorReference)
    var popUpEnabled by remember { mutableStateOf(false) }

    if (popUpEnabled) {
        DraggablePopup(onDismissRequest = { popUpEnabled = false }) {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(onClick = { popUpEnabled = !popUpEnabled }) {
                    Icon(Icons.Default.Close, contentDescription = "toggle pop-out")
                }
            }
                HistogramGraph(
                    reds = vm.getMergedReds(),
                    greens = vm.getMergedGreens(),
                    blues = vm.getMergedBlues(),
                    maxFrequency = vm.histogramYUpperLimit
                )

        }
    } else {
        ModalBottomSheet(
            onDismissRequest,
            modifier = Modifier.padding(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "histogram",
                        fontSize = 24.sp
                    )
                    Button(onClick = { popUpEnabled = !popUpEnabled }) {
                        Icon(Icons.Default.ArrowOutward, contentDescription = "toggle pop-out")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                HistogramGraph(
                    reds = vm.getMergedReds(),
                    greens = vm.getMergedGreens(),
                    blues = vm.getMergedBlues(),
                    maxFrequency = vm.histogramYUpperLimit
                )

                Text(
                    "red: ${"%.2f".format(vm.redAvg)}\n" +
                            "green: ${"%.2f".format(vm.greenAvg)}\n" +
                            "blue: ${"%.2f".format(vm.blueAvg)}"
                )

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(onClick = { vm.decreaseHistogramMergeFactor() }) {
                        Text("-")
                    }

                    Text(
                        vm.histogramMergeFactor.intValue.toString(),
                        fontSize = 20.sp,
                        modifier = Modifier.padding(10.dp),
                    )

                    Button(onClick = { vm.increaseHistogramMergeFactor() }) {
                        Text("+")
                    }
                }
            }
        }
    }
}

@Composable
fun HistogramGraph(
    reds: IntArray,
    greens: IntArray,
    blues: IntArray,
    maxFrequency: Int,
) {
    val ctx = LocalContext.current
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
        .clipToBounds()
    ) {
        drawRect(color = Color.Black, size = size)

        if (maxFrequency == 0) return@Canvas

        val barWidth = size.width / 256f
        val heightScale = size.height / maxFrequency.toFloat()

        for (i in 0 .. 255) {
            val x = i * barWidth

            if (reds[i] > 0) {
                val barHeight = reds[i] * heightScale
                drawLine(
                    color = Color.Red.copy(alpha = .6f),
                    start = Offset(x, size.height),
                    end = Offset(x, size.height - barHeight),
                    strokeWidth = barWidth.coerceAtLeast(1f)
                )
            }

            if (greens[i] > 0) {
                val barHeight = greens[i] * heightScale
                drawLine(
                    color = Color.Green.copy(alpha = .4f),
                    start = Offset(x, size.height),
                    end = Offset(x, size.height - barHeight),
                    strokeWidth = barWidth.coerceAtLeast(1f)
                )
            }

            if (blues[i] > 0) {
                val barHeight = blues[i] * heightScale
                drawLine(
                    color = Color.Blue.copy(alpha = .8f),
                    start = Offset(x, size.height),
                    end = Offset(x, size.height - barHeight),
                    strokeWidth = barWidth.coerceAtLeast(1f)
                )
            }
        }

    }
}

