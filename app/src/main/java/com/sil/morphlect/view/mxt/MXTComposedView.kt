package com.sil.morphlect.view.mxt

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import kotlin.collections.forEach

// TODO
@Composable
fun MXTComposedView(
    extensionName: String,
    onRun: () -> Unit
) {
    val ctx = LocalContext.current
    // TODO bindings...
//    val bindings: Map<ModelInteractor, Any?> = remember {
//        manifest.uiComposerElements
//            .filter { it.parameterBinding != null }
//            .associate { it.parameterBinding!! to null }
//    }
    var manifest by remember { mutableStateOf<MXTManifest?>(null) }

    LaunchedEffect(Unit) {
        manifest = loadExtension(ctx, extensionName).manifest
    }

    // crazy inference kotlin....
    manifest?.let { manifest ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(manifest.name)
            Text(manifest.description)
            manifest.ui.forEach { component ->
                when (component.type) {
                    ComposerElementType.RunButton -> TextButton(onClick = onRun) {
                        Text("run inference")
                    }

                    ComposerElementType.ImageUpload -> Button(onClick = { }) {
                        Text(component.label)
                    }

                    ComposerElementType.TextInput -> OutlinedTextField(
                        value = "",
                        onValueChange = { it },
                        label = { Text(component.label) },
                    )

                    ComposerElementType.AudioUpload -> TextButton(onClick = { }) {
                        Text("add audio")
                    }

                    ComposerElementType.FloatGauge -> OutlinedTextField(
                        value = "",
                        onValueChange = { it },
                        label = { Text(component.label, fontSize = 10.sp) }
                    )
                }
            }
        }
    }
}