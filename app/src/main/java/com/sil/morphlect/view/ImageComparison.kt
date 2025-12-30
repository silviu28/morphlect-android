package com.sil.morphlect.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sil.morphlect.logic.FormatConverters
import com.sil.morphlect.viewmodel.EditorViewModel

@Composable
fun ImageComparison(vm: EditorViewModel, navController: NavController) {
    var dividerRatio by remember { mutableStateOf(.5) }
    var original by remember { mutableStateOf(FormatConverters.matToBitmap(vm.getOriginalMat()!!)) }
    var edited by remember { mutableStateOf(vm.previewBitmap!!) }

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
                    .size(400.dp)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            val width = size.width
                            dividerRatio = (dividerRatio + dragAmount / width)
                                .coerceIn(0.0, 1.0)
                        }
                    }) {
                val dividerX = constraints.maxWidth * dividerRatio

                Image(
                    bitmap = edited.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
                Image(
                    bitmap = original.asImageBitmap(),
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
                    contentScale = ContentScale.Crop
                )
            }

            TextButton(onClick = { navController.navigate("editor") }) {
                Text("back to editor")
            }
        }
    }
}