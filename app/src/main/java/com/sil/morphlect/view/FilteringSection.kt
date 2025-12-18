package com.sil.morphlect.view

import android.graphics.Bitmap
import android.graphics.Paint.Align
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sil.morphlect.viewmodel.EditorViewModel
import com.sil.morphlect.enums.Effect
import com.sil.morphlect.logic.Filtering
import com.sil.morphlect.logic.FormatConverters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.core.Mat
import kotlin.math.roundToInt

@Composable
fun FilteringSection(vm: EditorViewModel) {
    val presetsMap = remember {
            mapOf(
            "eyesore" to mapOf(
                Effect.Hue to 20.0
            ),
            "black and white" to mapOf(
                Effect.Hue to 0.0
            ),
            "brighty bright" to mapOf(
                Effect.Brightness to .5
            )
        )
    }
    var showAddDialog by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "${(vm.effectValues[vm.selectedEffect]!! * 100).roundToInt()}",
            fontSize = 30.sp,
            modifier = Modifier.offset(x = (-70).dp, y = (-40).dp).align(Alignment.End)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                if (vm.effectValues[vm.selectedEffect] != 0.0) {
                    ElevatedButton(
                        modifier = Modifier.height(30.dp),
                        onClick = {
                        vm.adjustEffect(value = 0.0)
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "undo effect")
                    }
                }
            }
            Column {
                Text(text = vm.selectedEffect.name, fontSize = 30.sp)
            }
        }
        Slider(
            value = vm.effectValues[vm.selectedEffect]!!.toFloat(),
            onValueChange = { value ->
                vm.adjustEffect(value = value.toDouble())
            },
            valueRange = -1f..1f
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            ElevatedButton(onClick = {
                vm.changeSelectedEffect(Effect.Contrast)
            }) {
                Text("con")
            }
            ElevatedButton(onClick = {
                vm.changeSelectedEffect(Effect.Blur)
            }) {
                Text("blr")
            }
            ElevatedButton(onClick = {
                vm.changeSelectedEffect(Effect.Brightness)
            }) {
                Text("bri")
            }
            ElevatedButton(onClick = {
                vm.changeSelectedEffect(Effect.LightBalance)
            }) {
                Text("lb")
            }
            ElevatedButton(onClick = {
                vm.changeSelectedEffect(Effect.Hue)
            }) {
                Text("hue")
            }
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.Gray.copy(alpha = .2f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                presetsMap.forEach { (name, preset) ->
                    PresetPreview(name, preset, vm.getOriginalMat())
                }
                ElevatedButton (
                    modifier = Modifier.size(60.dp), onClick = {}
                ) {
                    Text("+")
                }
            }
        }
    }
}

@Composable
fun PresetPreview(name: String, preset: Map<Effect, Double>, originalMat: Mat?) {
    var processedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    if (originalMat == null) {
        Box(modifier = Modifier.size(60.dp)) {
            CircularProgressIndicator()
        }
        return
    }

    LaunchedEffect(originalMat, preset) {
        withContext(Dispatchers.Default) {
            try {
                var previewMat = originalMat.clone()

                val blurValue = preset[Effect.Blur] ?: 0.0
                if (blurValue != 0.0) {
                    previewMat = Filtering.blur(previewMat, blurValue / 10, blurValue / 10)
                }

                val hueValue = preset[Effect.Hue] ?: 0.0
                if (hueValue != 0.0) {
                    previewMat = Filtering.hueShift(previewMat, hueValue)
                }

                val resultBitmap = FormatConverters.matToBitmap(previewMat)

                previewMat.release()

                processedBitmap = resultBitmap
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(8.dp)),
    ) {
        if (processedBitmap == null) {
            CircularProgressIndicator()
        } else {
            Image(
                bitmap = processedBitmap!!.asImageBitmap(),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}