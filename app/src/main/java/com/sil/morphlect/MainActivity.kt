package com.sil.morphlect

import android.os.Bundle
import android.os.Debug
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.sil.morphlect.repository.AppConfigRepository
import com.sil.morphlect.ui.theme.MorphlectTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.opencv.android.OpenCVLoader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val configRepository = AppConfigRepository(this)
        val developerMode = runBlocking { configRepository.developerMode.first() }
        if (developerMode)
            Debug.startMethodTracing("trace")

        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        setContent {
            MorphlectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    AppNavHost()
                }
            }
        }

        try {
            val opencvLoaded = OpenCVLoader.initLocal()
            Log.d("OpenCV", "=============> OPENCV STATUS: $opencvLoaded")
        } catch (e: Exception) {
            Log.e("Error", e.toString())
        }
    }
}