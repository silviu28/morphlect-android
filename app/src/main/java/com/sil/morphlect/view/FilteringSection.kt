package com.sil.morphlect.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sil.morphlect.viewmodel.EditorViewModel
import com.sil.morphlect.enums.Effect
import kotlin.math.roundToInt

@Composable
fun FilteringSection(vm: EditorViewModel) {
    Column {
        Text(vm.selectedEffect.name)
        Text("${(vm.effectValues[vm.selectedEffect]!! * 100).roundToInt()}")
        if (vm.effectValues[vm.selectedEffect] != 0.0) {
            ElevatedButton(onClick = {
                vm.adjustEffect(value = 0.0)
            }) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "undo effect")
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
    }
}