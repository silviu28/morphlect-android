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
import com.sil.morphlect.view.FatalErrorScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.opencv.android.OpenCVLoader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val configRepository = AppConfigRepository(this)
        val developerMode = runBlocking { configRepository.developerMode.first() }
        // makes the app SUPER SLOW
//        if (developerMode)
//            Debug.startMethodTracing("trace")

        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false

        val moduleLoadResult = runCatching {
            val opencvLoaded = OpenCVLoader.initLocal()
            Log.d("OpenCVLoader", "OPENCV STATUS: $opencvLoaded")
        }
        moduleLoadResult.onSuccess {
            setContent {
                MorphlectTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                        AppNavHost()
                    }
                }
            }
        }
        moduleLoadResult.onFailure { cause ->
            setContent {
                MorphlectTheme { FatalErrorScreen(cause) }
            }
        }
    }
}