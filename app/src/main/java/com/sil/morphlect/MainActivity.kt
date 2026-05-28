package com.sil.morphlect

import android.content.Intent
import android.os.Bundle
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
import org.opencv.android.OpenCVLoader
import kotlin.jvm.java

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false

        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            val intent = Intent(this, ErrorActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra("stackTrace", throwable.stackTraceToString())
            }
            startActivity(intent)
            Runtime.getRuntime().exit(0)
        }

        val moduleLoadResult = runCatching {
            val opencvLoaded = OpenCVLoader.initLocal()
            Log.d("OpenCVLoader", "OPENCV STATUS: $opencvLoaded")
        }
        moduleLoadResult.onSuccess {
            setContent {
                MorphlectTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { _ -> MorphlectNavHost() }
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