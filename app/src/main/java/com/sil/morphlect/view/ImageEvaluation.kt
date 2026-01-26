package com.sil.morphlect.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sil.morphlect.enums.Output
import com.sil.morphlect.ml.AlteredMobileNetLoader
import com.sil.morphlect.view.dialog.KeepParamsDialog
import com.sil.morphlect.viewmodel.EditorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ImageEvaluation(vm: EditorViewModel, navController: NavController) {
    val ctx = LocalContext.current.applicationContext
    val loader = remember { AlteredMobileNetLoader(ctx) }
    var values by remember { mutableStateOf<Map<Output, Float>>(mapOf()) }
    var infoText by remember { mutableStateOf("processing...") }
    var keepParamsDialogActive by remember { mutableStateOf(false) }

    LaunchedEffect(vm.previewBitmap) {
        values = withContext(Dispatchers.Default) {
            loader.infer(vm.previewBitmap!!)
        }
        infoText = "done!"
    }

    if (keepParamsDialogActive) {
        KeepParamsDialog(
            onDismissRequest = { keepParamsDialogActive = false },
            onApply = { /* this should start an optimizer... */ })
    }
    Column(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        vm.previewBitmap?.asImageBitmap()?.let {
            Image(
                bitmap = it,
                contentDescription = "preview",
                modifier = Modifier.size(300.dp),
                contentScale = ContentScale.Crop
            )
        }
        values.forEach { (effect, value) ->
            Text("${effect.name}: ${"%.2f".format(value)}")
        }
        Row {
            Button(onClick = { navController.navigate("editor") }) {
                Text("back to editor")
            }
            Button(onClick = { keepParamsDialogActive = true }) {
                Text("improve")
            }
        }
    }
}