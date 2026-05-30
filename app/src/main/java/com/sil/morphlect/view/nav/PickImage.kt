package com.sil.morphlect.view.nav

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sil.morphlect.viewmodel.StudioViewModel

@Composable
fun PickImage(navController: NavController, studioViewModel: StudioViewModel) {
    val ctx = LocalContext.current

    val imagePickLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.run {
            studioViewModel.loadImage(ctx, uri)
            navController.navigate("studio")
        }
    }

    Scaffold { _ ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp)
                )
                Text(
                    text = "no image",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                TextButton(onClick = { imagePickLauncher.launch("image/*") }) {
                    Text("pick image", fontSize = 18.sp)
                }
                TextButton(onClick = { navController.navigate("camera") }) {
                    Text("camera mode", fontSize = 18.sp)
                }
                TextButton(onClick = { navController.navigate("model-download") }) {
                    Text("manage extensions", fontSize = 18.sp)
                }
            }
        }
    }
}