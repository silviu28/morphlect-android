package com.sil.morphlect.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import com.sil.morphlect.viewmodel.EditorViewModel
import com.sil.morphlect.viewmodel.PickImageViewModel
import com.sil.morphlect.enums.Section
import kotlin.math.roundToInt

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
                    Section.Filtering -> FilteringSection(vm)
                    Section.SmartFeatures -> SmartFeaturesSection(navController, vm)
                    Section.ImageManipulation -> ImageManipulationSection(vm)
                }
            }
        }
    }
}