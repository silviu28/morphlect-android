package com.sil.morphlect.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.sil.morphlect.viewmodel.EditorViewModel

@Composable
fun SmartFeaturesSection(navController: NavController, vm: EditorViewModel) {
    var showVibeDialog by remember { mutableStateOf(false) }
    var showEvalDialog by remember { mutableStateOf(false) }
    var showStyleDialog by remember { mutableStateOf(false) }

    Column {
        if (showStyleDialog) {
            AlertDialog(
                onDismissRequest = { showStyleDialog = false },
                title = { Text("style transfer") },
                text = { Text("this option requires you to insert an image and apply modifications to your image to match the latter's style. continue?") },
                confirmButton = {
                    TextButton(onClick = {
                        showStyleDialog = false
                        navController.navigate("styletransfer")
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
        }
        if (showEvalDialog) {
            AlertDialog(
                onDismissRequest = { showEvalDialog = false },
                title = { Text("image evaluation") },
                text = { Text("this option will process your image and give you a style rating. continue?") },
                confirmButton = {
                    TextButton(onClick = {
                        showEvalDialog = false
                        navController.navigate("imageeval")
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
        }
        if (showVibeDialog) {
            AlertDialog(
                onDismissRequest = { showVibeDialog = false },
                title = { Text("vibe matcher") },
                text = { Text("this option allows you to infer the vibe of an image, make changes to your liking, and modify your image accordingly. continue?") },
                confirmButton = {
                    TextButton(onClick = {
                        showVibeDialog = false
                        navController.navigate("vibematch")
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
        Row {
            TextButton(onClick = {
                showVibeDialog = true
            }) {
                Text("vibe matcher")
            }
            TextButton(onClick = {
                showEvalDialog = true
            }) {
                Text("image evaluation")
            }
            TextButton(onClick = {
                showStyleDialog = true
            }) {
                Text("style transfer")
            }
        }
    }
}