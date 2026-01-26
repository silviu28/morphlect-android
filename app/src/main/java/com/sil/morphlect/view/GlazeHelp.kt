package com.sil.morphlect.view

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sil.morphlect.R
import com.sil.morphlect.constant.WebConstants
import androidx.core.net.toUri

@Composable
fun GlazeHelp(onDismissRequest: () -> Unit) {
    val ctx = LocalContext.current

    Dialog(onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(Icons.Default.History, contentDescription = "history")
                Text("glaze")
                Image(
                    painter = painterResource(id = R.drawable.placeholder),
                    contentDescription = "glaze infographic",
                    modifier = Modifier
                        .size(280.dp)
                        .padding(bottom = 48.dp)
                )
                Text("protect your images from AI scrapers. applies a special type of noise invisible to the naked eye that significantly reduces the accuracy of algorithms used in machine learning.")
                Row {
                    TextButton(onClick = {
                        ctx.startActivity(
                            Intent(Intent.ACTION_VIEW, WebConstants.GLAZE_URL.toUri())
                        )
                    }) {
                        Text("learn more")
                    }
                    TextButton(onClick = onDismissRequest) {
                        Text("got it")
                    }
                }
            }
        }
    }
}