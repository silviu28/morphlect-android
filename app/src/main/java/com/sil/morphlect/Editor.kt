package com.sil.morphlect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Editor(navController: NavController, imageViewModel: PickImageViewModel) {
    val vm: EditorViewModel = viewModel()
    val imageUri = imageViewModel.imageUri

    Scaffold { _ ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row {
                    TextButton(onClick = {
                        vm.changeSection(Section.Filtering)
                    }) {
                        Text("filtering")
                    }
                    TextButton(onClick = {
                        vm.changeSection(Section.SmartFeatures)
                    }) {
                        Text("smart features")
                    }
                    TextButton(onClick = {
                        vm.changeSection(Section.ImageManipulation)
                    }) {
                        Text("image manipulation")
                    }
                }
                Row {
                    Spacer(Modifier.weight(1f))
                    ElevatedButton(onClick = {
                    }) {
                        Icon(Icons.Default.Menu, contentDescription = "options")
                    }
                }
                Spacer(modifier = Modifier.size(10.dp))
                // thumbnail
                if (imageUri == null) {
                    CircularProgressIndicator()
                } else {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier.size(300.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                Text(vm.selectedEffect.name)
                Text("${(vm.effectValues[vm.selectedEffect]!! * 100).roundToInt()}")
                when (vm.section) {
                    Section.Filtering -> Column {
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
                    Section.SmartFeatures -> Box {}
                    Section.ImageManipulation -> Box {}
                }
            }
        }
    }
}