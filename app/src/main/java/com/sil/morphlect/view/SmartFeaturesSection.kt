package com.sil.morphlect.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.sil.morphlect.data.EvaluationResult
import com.sil.morphlect.enums.Filter
import com.sil.morphlect.repository.AppConfigRepository
import com.sil.morphlect.repository.ExtensionsRepository
import com.sil.morphlect.viewmodel.EditorViewModel
import kotlin.collections.listOf
import kotlin.random.Random

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SmartFeaturesSection(
    navController: NavController,
    vm: EditorViewModel,
    configRepository: AppConfigRepository,
    extensionsRepository: ExtensionsRepository,
) {
    var showVibeDialog    by remember { mutableStateOf(false) }
    var showEvalDialog    by remember { mutableStateOf(false) }
    var showStyleDialog   by remember { mutableStateOf(false) }
    var extensions        by remember { mutableStateOf<List<String>>(listOf()) }

    LaunchedEffect(Unit) {
        extensions = extensionsRepository.readExtensionNames()
    }

    val developerMode by configRepository.developerMode.collectAsState(initial = false)

    Column {
        when {
            showStyleDialog -> AlertDialog(
                onDismissRequest = { showStyleDialog = false },
                title = { Text("style transfer") },
                text = { Text("this option requires you to insert an image and apply modifications to your image to match the latter's style. continue?") },
                confirmButton = {
                    TextButton(onClick = {
                        showStyleDialog = false
                        navController.navigate("style-transfer")
                    }) {
                        Text("yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showStyleDialog = false
                    }) {
                        Text("no")
                    }
                }
            )

            showEvalDialog -> AlertDialog(
                onDismissRequest = { showEvalDialog = false },
                title = { Text("image evaluation") },
                text = { Text("this option will process your image and give you a style rating. continue?") },
                confirmButton = {
                    TextButton(onClick = {
                        showEvalDialog = false
                        navController.navigate("image-eval")
                    }) {
                        Text("yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showEvalDialog = false
                    }) {
                        Text("no")
                    }
                }
            )

            showVibeDialog -> AlertDialog(
                onDismissRequest = { showVibeDialog = false },
                title = { Text("vibe matcher") },
                text = { Text("this option allows you to infer the vibe of an image, make changes to your liking, and modify your image accordingly. continue?") },
                confirmButton = {
                    TextButton(onClick = {
                        showVibeDialog = false
                        navController.navigate("vibe-match")
                    }) {
                        Text("yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showVibeDialog = false
                    }) {
                        Text("no")
                    }
                }
            )
        }

        FlowRow(
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = { showVibeDialog = true }) {
                Text("vibe matcher")
            }
            TextButton(onClick = { showEvalDialog = true }) {
                Text("image evaluation")
            }
            TextButton(onClick = { showStyleDialog = true }) {
                Text("style transfer")
            }
            if (developerMode)
                TextButton(onClick = { vm.emitEvaluationResult(generateRandomResult()) }) {
                    Text("[DEBUG] verify animation")
                }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            extensions.forEach {
                TextButton(onClick = { navController.navigate("extension-view/$it") }) {
                    Text(it)
                }
            }
        }
    }
}

fun generateRandomResult(): EvaluationResult = EvaluationResult(
    Filter.entries.associate { filter -> filter to Random.nextDouble() }
)