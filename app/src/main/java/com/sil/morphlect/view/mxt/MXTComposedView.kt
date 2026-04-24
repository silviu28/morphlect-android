package com.sil.morphlect.view.mxt

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sil.mxtengine.data.ComposerElementType
import com.sil.mxtengine.data.MXTManifest
import com.sil.mxtengine.data.Shape
import kotlin.collections.forEach
import androidx.core.graphics.scale
import com.sil.morphlect.ml._ModelLoader

fun Uri.getImage(context: Context, conversionShape: Shape? = null): Bitmap? {
    val res = BitmapFactory.decodeStream(context.contentResolver.openInputStream(this))
    return if (conversionShape != null)
        res.scale(conversionShape[0], conversionShape[1], false)
    else res
}

// we'll see if this might be needed...
fun ByteArray.compress(shape: Shape) = this

fun Uri.getAudio(context: Context, conversionShape: Shape? = null): ByteArray? {
    val res = context.contentResolver
        .openInputStream(this)
        ?.use { it.readBytes() }
    return if (conversionShape != null)
        res?.compress(conversionShape)
    else res
}

@Composable
fun MXTComposedView(
    extensionName: String,
    onRun: () -> Unit
) {
    val ctx = LocalContext.current

    var manifest by remember { mutableStateOf<MXTManifest?>(null) }
    var receivingBindingKey by remember { mutableStateOf<String?>(null) }
    var loader by remember { mutableStateOf<_ModelLoader?>(null) }
    val bindings = remember { mutableStateMapOf<String, Any?>() }

    val imagePickLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.run {
            val shape = manifest
                ?.inputs
                ?.find { it.name == receivingBindingKey }
                ?.shape
            val img = getImage(ctx, shape)
            img?.let { bindings[receivingBindingKey ?: return@let] = it }
        }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.run {
            val shape = manifest
                ?.inputs
                ?.find { it.name == receivingBindingKey }
                ?.shape
            val audio = getAudio(ctx, shape)
            audio?.let { bindings[receivingBindingKey ?: return@let] = it }
        }
    }

    LaunchedEffect(Unit) {
        manifest = loadExtension(ctx, extensionName).manifest
        manifest?.run {
            ui.filter { it.parameterBindingRef != null }
              .forEach { bindings[it.parameterBindingRef!!] = null }

            loader = _ModelLoader.Builder()
                .named(name)
                .withInputs(inputs)
                .withOutputs(outputs)
                .build()
                .apply { initialize(ctx) }
        }
    }

    // crazy inference kotlin....
    manifest?.let { manifest ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(manifest.name)
            Text(manifest.description)
            manifest.ui.forEach { component ->
                when (component.type) {
                    ComposerElementType.RunButton -> TextButton(onClick = {
                        loader?.run { infer(bindings) }
                    }) {
                        Text("run inference")
                    }

                    ComposerElementType.ImageUpload -> Button(onClick = {
                        receivingBindingKey = component.parameterBindingRef
                        imagePickLauncher.launch("image/*")
                    }) {
                        Text(component.label)
                    }

                    ComposerElementType.TextInput -> OutlinedTextField(
                        value = (bindings[component.parameterBindingRef] as? String) ?: "",
                        onValueChange = { bindings[component.parameterBindingRef!!] = it },
                        label = { Text(component.label) },
                    )

                    ComposerElementType.AudioUpload -> TextButton(onClick = {
                        receivingBindingKey = component.parameterBindingRef
                        audioPickerLauncher.launch("audio/*")
                    }) {
                        Text("add audio")
                    }

                    ComposerElementType.FloatGauge -> {
                        val value = (bindings[component.parameterBindingRef] as? Float) ?: 0f
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(component.label, fontSize = 10.sp)
                                Text("%.2f".format(value), fontSize = 10.sp)
                            }
                            Slider(
                                value = value,
                                onValueChange = { bindings[component.parameterBindingRef!!] = it },
                                valueRange = 0f..1f,
                            )
                        }
                    }
                }
            }
        }
    }
}