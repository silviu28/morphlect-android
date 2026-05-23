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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sil.morphlect.ml.cosineSimilarity
import com.sil.morphlect.ml.impl.MiniLMEmbeddingLoader
import com.sil.morphlect.viewmodel.EditorViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VibeMatcher(vm: EditorViewModel, navController: NavController) {
    val context = LocalContext.current
    var tokens by remember { mutableStateOf(emptySet<String>()) }
    var currentToken by remember { mutableStateOf("") }
    val miniLMEmbeddingLoader = remember {
        MiniLMEmbeddingLoader().apply { initialize(context) }
    }
    var some by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val sb = StringBuilder()
        val tea = miniLMEmbeddingLoader.generateEmbedding("tea")
        val coffee = miniLMEmbeddingLoader.generateEmbedding("coffee")
        val autopsy = miniLMEmbeddingLoader.generateEmbedding("autopsy")
        sb.append("tea: [${tea.joinToString()}]")
        sb.append("coffee: [${coffee.joinToString()}]")
        sb.append("the similarity between coffee and tea is ${cosineSimilarity(tea, coffee)}")
        sb.append("the similarity between coffee and autopsy is ${cosineSimilarity(coffee, autopsy)}")
        sb.append("the similarity between tea and autopsy is ${cosineSimilarity(tea, autopsy)}")
        some = sb.toString()
    }

    Column(
        modifier = Modifier.fillMaxWidth().fillMaxHeight().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(some)
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