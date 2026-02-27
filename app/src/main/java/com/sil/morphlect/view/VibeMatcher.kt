package com.sil.morphlect.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sil.morphlect.viewmodel.EditorViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VibeMatcher(vm: EditorViewModel, navController: NavController) {
    var tokens by remember { mutableStateOf(emptySet<String>()) }
    var currentToken by remember { mutableStateOf("") }

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

        OutlinedTextField(
            value = currentToken,
            onValueChange = { currentToken = it },
            label = { Text("add token") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        )
        Button(onClick = {
            if (currentToken.isNotBlank()) {
                tokens += currentToken.trim()
                currentToken = ""
            }
        }) {
            Text("+")
        }

        FlowRow {
            tokens.forEach { token ->
                Button(onClick = { tokens -= token }) {
                    Text(token)
                }
            }
        }

        Row {
            TextButton(onClick = { /* this should start a token parser... */ }) {
                Text("seems good")
            }
            TextButton(onClick = { /* this should generate tokens based on given image... */ }) {
                Text("auto")
            }
        }

        TextButton(onClick = { navController.navigate("editor") }) {
            Text("back to editor")
        }
    }
}