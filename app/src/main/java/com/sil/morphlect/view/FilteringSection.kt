package com.sil.morphlect.view

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Deblur
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.LensBlur
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sil.morphlect.data.Preset
import com.sil.morphlect.repository.PresetsRepository
import com.sil.morphlect.viewmodel.EditorViewModel
import com.sil.morphlect.enums.Effect
import com.sil.morphlect.view.custom.LedDotSlider
import com.sil.morphlect.view.dialog.AddPresetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import kotlin.math.roundToInt

// TODO
@RequiresApi(Build.VERSION_CODES.Q)
suspend fun savePreset(ctx: Context, preset: Preset) = withContext(Dispatchers.IO) {
    val resolver = ctx.contentResolver

    val contentValues = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, "${preset.name}.preset")
        put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Morphlect")
        put(MediaStore.Downloads.IS_PENDING, 1)
    }

    // Use Downloads instead of Files
    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        ?: return@withContext

    resolver.openOutputStream(uri)?.use { out ->
        out.write(
            preset.toJSON()
                .toString(2)
                .toByteArray()
        )
    }

    contentValues.clear()
    contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
    resolver.update(uri, contentValues, null, null)
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun FilteringSection(vm: EditorViewModel, presetsRepository: PresetsRepository) {
    var presetsMap         by remember { mutableStateOf<Map<String, Map<Effect, Double>>>(emptyMap()) }
    var showAddDialog      by remember { mutableStateOf(false) }
    var showRemoveDialog   by remember { mutableStateOf(false) }
    var selectedPresetName by remember { mutableStateOf<String?>(null) }
    var isBlurring2d       by remember { mutableStateOf(false) }
    var selectedPreset     by remember { mutableStateOf<Preset?>(null) }

    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    val applyPreset = { preset: Map<Effect, Double> ->
        preset.forEach { (effect, value) ->
            vm.adjustEffect(effect, value)
        }
    }

    LaunchedEffect(Unit) {
        presetsMap = presetsRepository.load()
    }

    if (showAddDialog) {
        AddPresetDialog(
            onDismissRequest = { showAddDialog = false },
            onAddPreset = { preset ->
                scope.launch {
                    presetsRepository.addPreset(preset)
                    presetsMap = presetsRepository.load()
                }
            },
            onAddPresetFromEditor = { name ->
                scope.launch {
                    presetsRepository.addPreset(name, vm.effectValues)
                    presetsMap = presetsRepository.load()
                }
            }
        )
    }

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("remove preset") },
            text = { Text("do you want to remove $selectedPresetName?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        presetsRepository.removePreset(selectedPresetName!!)
                        presetsMap = presetsRepository.load()
                    }
                    showRemoveDialog = false
                }) {
                    Text("yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text("no")
                }
            }
        )
    }

    selectedPreset?.let {
        Dialog(onDismissRequest = { selectedPreset = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("options")
                TextButton(onClick = {
                    scope.launch { savePreset(ctx, it) }
                }) {
                    Icon(Icons.Default.Save, contentDescription = "save")
                    Text("save preset")
                }
                TextButton(onClick = {
                    selectedPreset = null
                    showRemoveDialog = true
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "remove")
                    Text("remove preset")
                }
            }
        }
    }

    Column {
        Text(
            text = "${(vm.effectValues[vm.selectedEffect]!! * 100).roundToInt()}",
            fontSize = 30.sp,
            modifier = Modifier
                    .offset(x = (-70).dp, y = (-40).dp)
                    .align(Alignment.End)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                if (vm.effectValues[vm.selectedEffect] != 0.0) {
                    ElevatedButton(
                        modifier = Modifier.height(30.dp),
                        onClick = {
                        vm.adjustEffect(value = 0.0)
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "undo effect")

                    }
                }
                if (vm.selectedEffect == Effect.Blur) {
                    ElevatedButton(
                        modifier = Modifier.height(30.dp),
                        onClick = { isBlurring2d = !isBlurring2d }) {
                        if (isBlurring2d) Text("XY") else Text("X")
                    }
                }
            }
            Column {
                Text(text = vm.selectedEffect.name, fontSize = 30.sp)
            }
        }

        // the usual slider
        LedDotSlider(
            value = vm.effectValues[vm.selectedEffect]!!.toFloat(),
            onValueChange = { value ->
                vm.adjustEffect(value = value.toDouble())
                // merge both blurring axes
                if (!isBlurring2d) {
                    vm.adjustEffect(effect = Effect.BlurSecondAxis, value = value.toDouble())
                }
            },
            valueRange = -1f..1f,
        )

        // the slider that appears when enabling 2d blur
        if (isBlurring2d) {
            LedDotSlider(
                value = vm.effectValues[Effect.BlurSecondAxis]!!.toFloat(),
                onValueChange = { value ->
                    vm.adjustEffect(effect = Effect.BlurSecondAxis, value = value.toDouble())
                },
                valueRange = -1f..1f
            )
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.Center
    ) {
        ElevatedButton(onClick = {
            vm.changeSelectedEffect(Effect.Contrast)
        }) {
            Icon(Icons.Filled.Contrast, contentDescription = "contrast")
        }
        ElevatedButton(onClick = {
            vm.changeSelectedEffect(Effect.Blur)
        }) {
            Icon(Icons.Filled.LensBlur, contentDescription = "blur")
        }
        ElevatedButton(onClick = {
            vm.changeSelectedEffect(Effect.Sharpness)
        }) {
            Icon(Icons.Filled.Deblur, contentDescription = "sharpen")
        }
        ElevatedButton(onClick = {
            vm.changeSelectedEffect(Effect.Brightness)
        }) {
            Icon(Icons.Filled.Brightness4, contentDescription = "brightness")
        }
        ElevatedButton(onClick = {
            vm.changeSelectedEffect(Effect.LightBalance)
        }) {
            Icon(Icons.Filled.Lightbulb, contentDescription = "light balance")
        }
        ElevatedButton(onClick = {
            vm.changeSelectedEffect(Effect.Hue)
        }) {
            Icon(Icons.Filled.InvertColors, contentDescription = "hue")
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
            presetsMap.forEach { (name, preset) -> // TODO NOT preset!!!!
                PresetPreview(
                    name = name,
                    preset = preset,
                    originalMat = vm.originalMat,
                    onPress = { applyPreset(preset) },
                    onLongPress = {
                        selectedPresetName = name
                        selectedPreset = Preset(name, preset) // TODO CHANGE THIS!!!
                    })
            }
            ElevatedButton(
                modifier = Modifier.size(60.dp),
                onClick = { showAddDialog = true }
            ) {
                Text("+")
            }
        }
    }
}


