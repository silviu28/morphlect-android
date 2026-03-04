package com.sil.morphlect.view

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sil.morphlect.data.Preset
import com.sil.morphlect.repository.PresetsRepository
import com.sil.morphlect.viewmodel.EditorViewModel
import com.sil.morphlect.enums.Filter
import com.sil.morphlect.view.custom.CircleOutlineButton
import com.sil.morphlect.view.custom.LedDotSlider
import com.sil.morphlect.view.dialog.AddPresetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

// TODO
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

@Composable
fun FilteringSection(vm: EditorViewModel, presetsRepository: PresetsRepository) {
    var presets         by remember { mutableStateOf<List<Preset>>(listOf()) }
    var showAddDialog      by remember { mutableStateOf(false) }
    var showRemoveDialog   by remember { mutableStateOf(false) }
    var selectedPresetName by remember { mutableStateOf<String?>(null) }
    var isBlurring2d       by remember { mutableStateOf(false) }
    var selectedPreset     by remember { mutableStateOf<Preset?>(null) }

    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    val applyPreset = { preset: Preset ->
        preset.params.forEach { (effect, value) ->
            vm.adjustEffect(effect, value)
        }
    }

    LaunchedEffect(Unit) {
        presets = presetsRepository.load()
    }

    if (showAddDialog) {
        AddPresetDialog(
            onDismissRequest = { showAddDialog = false },
            onAddPreset = { preset ->
                scope.launch {
                    presetsRepository.addPreset(preset)
                    presets = presetsRepository.load()
                }
            },
            onAddPresetFromEditor = { name ->
                scope.launch {
                    presetsRepository.addPreset(name, vm.filterValues)
                    presets = presetsRepository.load()
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
                        presets = presetsRepository.load()
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
            text = "${(vm.filterValues[vm.selectedFilter]!! * 100).roundToInt()}",
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
                AnimatedContent(
                    targetState = vm.filterValues[vm.selectedFilter] != 0.0,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut() using SizeTransform(clip = false)
                    }
                ) { filterUsed ->
                    if (filterUsed) {
                        ElevatedButton(
                            modifier = Modifier.height(30.dp),
                            onClick = {
                                vm.adjustEffect(value = 0.0)
                            }) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "undo effect"
                            )

                        }
                    }
                }
                AnimatedContent(
                    targetState = vm.selectedFilter == Filter.Blur,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                ) { isBlurring ->
                    if (isBlurring) {
                        ElevatedButton(
                            modifier = Modifier.height(30.dp),
                            onClick = { isBlurring2d = !isBlurring2d }) {
                            if (isBlurring2d) Text("..") else Text(".")
                        }
                    }
                }
            }

            Column {
                AnimatedContent(
                    targetState = vm.selectedFilter.name,
                    transitionSpec = {
                        slideInVertically { it } togetherWith slideOutVertically { it }
                    }
                ) { filterName ->
                    Text(text = filterName.lowercase(), fontSize = 30.sp)
                }
            }
        }

        // the usual slider
        LedDotSlider(
            value = vm.filterValues[vm.selectedFilter]!!.toFloat(),
            onValueChange = { value ->
                vm.adjustEffect(value = value.toDouble())
                // merge both blurring axes
                if (!isBlurring2d) {
                    vm.adjustEffect(filter = Filter.BlurSecondAxis, value = value.toDouble())
                }
            },
            valueRange = -1f..1f,
        )

        // the slider that appears when enabling 2d blur
        AnimatedContent(
            targetState = isBlurring2d,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            }
        ) { isBlurring2d ->
            if (isBlurring2d) {
                LedDotSlider(
                    value = vm.filterValues[Filter.BlurSecondAxis]!!.toFloat(),
                    onValueChange = { value ->
                        vm.adjustEffect(filter = Filter.BlurSecondAxis, value = value.toDouble())
                    },
                    valueRange = -1f..1f
                )
            }
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.Center
    ) {
        CircleOutlineButton(onClick = {
            vm.changeSelectedEffect(Filter.Contrast)
        }) {
            Icon(Icons.Filled.Contrast, contentDescription = "contrast")
        }
        CircleOutlineButton(onClick = {
            vm.changeSelectedEffect(Filter.Blur)
        }) {
            Icon(Icons.Filled.LensBlur, contentDescription = "blur")
        }
        CircleOutlineButton(onClick = {
            vm.changeSelectedEffect(Filter.Sharpness)
        }) {
            Icon(Icons.Filled.Deblur, contentDescription = "sharpen")
        }
        CircleOutlineButton(onClick = {
            vm.changeSelectedEffect(Filter.Brightness)
        }) {
            Icon(Icons.Filled.Brightness4, contentDescription = "brightness")
        }
        CircleOutlineButton(onClick = {
            vm.changeSelectedEffect(Filter.LightBalance)
        }) {
            Icon(Icons.Filled.Lightbulb, contentDescription = "light balance")
        }
        CircleOutlineButton(onClick = {
            vm.changeSelectedEffect(Filter.Hue)
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
            presets.forEach { preset ->
                PresetPreview(
                    preset = preset,
                    originalMat = vm.originalMat,
                    onPress = { applyPreset(preset) },
                    onLongPress = {
                        selectedPresetName = preset.name
                        selectedPreset = preset
                    })
            }
            ElevatedButton(
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(16.dp),
                onClick = { showAddDialog = true }
            ) {
                Text("+")
            }
        }
    }
}


