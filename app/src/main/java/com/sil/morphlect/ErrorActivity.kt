package com.sil.morphlect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.sil.morphlect.ui.theme.MorphlectTheme
import com.sil.morphlect.view.FatalErrorScreen

class ErrorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val stackTrace = intent.getStringExtra("stackTrace") ?: "Unknown error"
        setContent {
            MorphlectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    FatalErrorScreen(stackTrace = stackTrace)
                }
            }
        }
    }
}