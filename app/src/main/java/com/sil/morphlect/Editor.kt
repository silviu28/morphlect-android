package com.sil.morphlect

import android.net.Uri
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun Editor(navController: NavController, imageUri: Uri?) {
    Scaffold { paddingValues ->
        if (imageUri == null) {
            Text("no image")
        }
        Text("yes image")
    }
}