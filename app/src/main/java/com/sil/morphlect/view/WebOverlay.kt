package com.sil.morphlect.view

import android.app.Dialog
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import com.sil.morphlect.BuildConfig
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.overscroll
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale

val http by lazy { OkHttpClient() }

suspend fun fetchImages(query: String? = null): List<String> = withContext(Dispatchers.IO) {
    val url = if (query.isNullOrBlank()) {
        "https://api.unsplash.com/photos/random?count=8"
    } else {
        "https://api.unsplash.com/search/photos?page=1&query=$query"
    }
    val request = Request.Builder()
        .url(url)
        .header("Authorization", "Client-ID ${BuildConfig.UNSPLASH_ACCESS_KEY}")
        .build()
    val response = http.newCall(request).execute()

    if (!response.isSuccessful)
        return@withContext emptyList()

    val body = response.body?.string()

    if (query.isNullOrBlank()) {
        val images = JSONArray(body)
        return@withContext List(images.length()) {
            images
                .getJSONObject(it)
                .getJSONObject("urls")
                .getString("small")
        }
    } else {
        val parsedBody = JSONObject(body)
        val images = parsedBody.getJSONArray("results")
        return@withContext List(images.length()) {
            images
                .getJSONObject(it)
                .getJSONObject("urls")
                .getString("small")
        }
    }
}

@Composable
fun WebOverlay(
    onDismissRequest: () -> Unit,
    onImageSelected: (String) -> Unit
) {
    var searchTerm by remember { mutableStateOf("") }
    var images by remember { mutableStateOf<List<String>>(listOf()) }
    var expandedImageUrl by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // initial image fetch
    LaunchedEffect(Unit) {
        images = fetchImages()
    }

    if (expandedImageUrl != null) {
        Dialog(onDismissRequest = { expandedImageUrl = null }) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .heightIn(max = 800.dp)
                    .widthIn(max = 800.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                AsyncImage(
                    model = expandedImageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                TextButton(onClick = { expandedImageUrl = null }) {
                    Text("close")
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "online images",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = searchTerm,
                    onValueChange = { searchTerm = it },
                    label = { Text("search") },
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = {
                        scope.launch {
                            images = fetchImages()
                        }
                    }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "random")
                    }

                    Button(onClick = {
                        scope.launch {
                            images = fetchImages(searchTerm)
                        }
                    }) {
                        Icon(Icons.Rounded.Search, contentDescription = "search")
                    }
                }

                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(3),
                    modifier = Modifier.height(400.dp),
                ) {
                    items(images) { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentScale = ContentScale.Crop,
                            contentDescription = null,
                            modifier = Modifier
                                .size(150.dp)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = { onImageSelected(imageUrl) },
                                        onLongPress = { expandedImageUrl = imageUrl }
                                    )
                                }
                        )
                    }
                }
                TextButton(onClick = onDismissRequest) {
                    Text("cancel")
                }
            }
        }
    }
}