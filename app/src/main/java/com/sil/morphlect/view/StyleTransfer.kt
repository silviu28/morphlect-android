package com.sil.morphlect.view

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.sil.morphlect.logic.FormatConverters
import com.sil.morphlect.logic.WebHelper
import kotlinx.coroutines.launch

@Composable
fun StyleTransfer() {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var referenceImage by remember { mutableStateOf<Bitmap?>(null) }
    var webOverlayActive by remember { mutableStateOf(false) }
    val imagePickLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let{referenceImage = FormatConverters.uriToBitmap(context, uri)}
    }

    if (webOverlayActive) {
        WebOverlay(
            onDismissRequest = { webOverlayActive = false },
            onImageSelected = { str ->
                coroutineScope.launch {
                    referenceImage = WebHelper.downloadUnsplashImage(str, context)
                }
                webOverlayActive = false
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            referenceImage?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "reference image",
                    modifier = Modifier.size(300.dp)
                )
            }
            Row {
                TextButton(onClick = { imagePickLauncher.launch("image/*") }) {
                    Text("search gallery")
                }
                TextButton(onClick = { webOverlayActive = true }) {
                    Text("search unsplash")
                }
            }
        }
    }
}