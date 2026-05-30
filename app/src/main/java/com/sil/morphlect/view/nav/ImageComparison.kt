package com.sil.morphlect.view.nav

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sil.morphlect.layerwork.StudioLayer
import com.sil.morphlect.imgproc.FormatConverters

@Composable
fun ImageComparison(
    originalImageBitmap: Bitmap?,
    layers: List<StudioLayer>,
    navController: NavController
) {
    var dividerRatio by remember { mutableDoubleStateOf(.5) }
    val editedImageBitmap = layers
        .reduce { allMerge, layer -> allMerge.mergeWith(layer) }
        .toCvMat()
        .let { FormatConverters.matToBitmap(it) }

    if (originalImageBitmap == null)
        Text("no image loaded.")

    else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text("slide to reveal more of each image")
                BoxWithConstraints(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(500.dp)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { _, dragAmount ->
                                val width = size.width
                                dividerRatio = (dividerRatio + dragAmount / width)
                                    .coerceIn(0.0, 1.0)
                            }
                        }) {
                    val dividerX = constraints.maxWidth * dividerRatio

                    Image(
                        bitmap = editedImageBitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit
                    )
                    Image(
                        bitmap = originalImageBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .drawWithContent {
                                clipRect(
                                    left = 0f,
                                    top = 0f,
                                    right = dividerX.toFloat(),
                                    bottom = size.height
                                ) {
                                    this@drawWithContent.drawContent()
                                }
                            },
                        contentScale = ContentScale.Fit
                    )
                }

                TextButton(onClick = { navController.navigate("studio") }) {
                    Text("back to studio")
                }
            }
        }
    }
}