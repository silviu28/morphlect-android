package com.sil.morphlect.view.nav.studio

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Deblur
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.LensBlur
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sil.morphlect.data.FingerprintData
import com.sil.morphlect.data.Preset
import com.sil.morphlect.repository.PresetsRepository
import com.sil.morphlect.viewmodel.StudioViewModel
import com.sil.morphlect.enums.Filter
import com.sil.morphlect.view.preset.PresetBar
import com.sil.morphlect.view.preset.PresetPreview
import com.sil.morphlect.view.custom.CircleOutlineButton
import com.sil.morphlect.view.custom.LedDotSlider
import com.sil.morphlect.view.dialog.DialogScaffold
import com.sil.morphlect.view.dialog.impl.AddPresetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

suspend fun savePreset(context: Context, preset: Preset) {
    val uri = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, "${preset.name}.preset")
            put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Morphlect")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        // Use Downloads instead of Files
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return@withContext null

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

        // redirect the user to the directory in which the preset is saved
        context.run {
            val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
            startActivity(intent)
        }
        return@withContext uri
    }
    uri?.let {
        Toast.makeText(
            context,
            "preset saved at ${it.path}",
            Toast.LENGTH_LONG
        ).show()
    }
}

@Composable
fun FilteringSection(
    vm: StudioViewModel,
    presetsRepository: PresetsRepository,
    fingerprint: FingerprintData?,
) {
    var presets            by remember { mutableStateOf<List<Preset>>(listOf()) }
    var showAddDialog      by remember { mutableStateOf(false) }
    var showRemoveDialog   by remember { mutableStateOf(false) }
    var selectedPreset     by remember { mutableStateOf<Preset?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val applyPreset = { preset: Preset ->
        preset.params.forEach { (effect, value) ->
            vm.changeSelectedEffect(effect)
            vm.adjustEffect(effect, value)
        }
    }

    LaunchedEffect(Unit) {
        presets = presetsRepository.load()
    }

    when {
        showAddDialog -> AddPresetDialog(
            onDismissRequest = { showAddDialog = false },
            onAddPreset = { preset ->
                coroutineScope.launch {
                    presetsRepository.addPreset(preset)
                    presets += preset
                }
            },
            onAddPresetFromStudio = { name ->
                coroutineScope.launch {
                    val preset = Preset(name, vm.getCumulativeFilterMap()).apply {
                        presetsRepository.addPreset(this)
                    }
                    presets += preset
                }
            }
        )

        showRemoveDialog -> AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("remove preset") },
            text = { Text("do you want to remove ${selectedPreset?.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    selectedPreset?.let {
                        coroutineScope.launch {
                            presetsRepository.removePreset(it)
                        }
                        presets = presets.filter { preset -> !preset.name.equals( it.name) }
                        showRemoveDialog = false
                        selectedPreset = null
                    }
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
        DialogScaffold(
            title = it.name,
            onDismissRequest = { selectedPreset = null },
            icon = Icons.Default.Image,
        ) {
            PresetPreview(
                preset = it,
                originalMat = vm.originalMat!!,
                onPress = { /* nothing */ },
                onLongPress = { /* nothing */ },
                expanded = true,
            )
            TextButton(onClick = {
                coroutineScope.launch { savePreset(context, it) }
            }) {
                Icon(Icons.Default.Save, contentDescription = "save")
                Text("save preset")
            }
            TextButton(onClick = {
                showRemoveDialog = true
            }) {
                Icon(Icons.Default.Delete, contentDescription = "remove")
                Text("remove preset")
            }
        }
    }

    Column {
        Text(
            text = "${(vm.filterValues[vm.selectedFilter]!! * 100).roundToInt()}",
            fontSize = 30.sp,
            modifier = Modifier
                .offset(x = (-20).dp, y = (-40).dp)
                .align(Alignment.End)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val filterUsed = vm.filterValues[vm.selectedFilter] != 0.0

                AnimatedContent(
                    targetState = filterUsed,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut() using SizeTransform(clip = false)
                    }
                ) { used ->
                    if (used) {
                        ElevatedButton(
                            modifier = Modifier.height(30.dp),
                            onClick = { vm.adjustEffect(value = 0.0) }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "undo effect"
                            )
                        }
                    } else {
                        fingerprint?.let {
                            ElevatedButton(
                                modifier = Modifier.height(30.dp),
                                onClick = {
                                    it.minorAdjustments.forEach { (filter, value) ->
                                        vm.changeSelectedEffect(filter)
                                        vm.adjustEffect(value = value)
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Fingerprint,
                                    contentDescription = "apply fingerprint adjustment"
                                )
                            }
                        }
                    }
                }
            }

            Column {
                AnimatedContent(
                    targetState = vm.selectedFilter.name,
                    transitionSpec = {
                        (fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 }) togetherWith
                                (fadeOut(tween(300)) + slideOutVertically(tween(300)) { -it / 2 })
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
            },
            valueRange = -1f..1f,
        )
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

    PresetBar(
        presets = presets,
        originalMat = vm.originalMat,
        onApply = { applyPreset(it) },
        onLongPress = { preset ->
            selectedPreset = preset
        },
        onAddNew = { showAddDialog = true }
    )
}