package com.sil.morphlect.view

import android.app.Activity
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sil.morphlect.view.custom.DecoratedContainer

@Composable
fun FatalErrorScreen(cause: Throwable) {
    val context = LocalContext.current

    Scaffold { _ ->
        DecoratedContainer(
            icon = Icons.Default.Error
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("something went wrong when loading some components")

                Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    Text(
                        text = cause.stackTraceToString(),
                        fontSize = 10.sp,
                    )
                }

                Button(onClick = { (context as Activity).finish() }) { Text("exit") }
            }
        }
    }
}