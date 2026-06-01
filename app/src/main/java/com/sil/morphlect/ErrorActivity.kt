package com.sil.morphlect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sil.morphlect.ui.theme.MorphlectTheme
import com.sil.morphlect.view.nav.FatalErrorScreen

class ErrorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val stackTrace = intent.getStringExtra("stackTrace") ?: "Unknown error"
        setContent {
            MorphlectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    Box(modifier = Modifier.padding(15.dp)) {
                        FatalErrorScreen(stackTrace = stackTrace)
                    }
                }
            }
        }
    }
}