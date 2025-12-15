package com.sil.morphlect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FilteringSection(vm: EditorViewModel) {
    Column {
        Slider(
            value = vm.effectValues[vm.selectedEffect]!!,
            onValueChange = { value ->
                vm.adjustEffect(value = value)
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