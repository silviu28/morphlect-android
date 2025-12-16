package com.sil.morphlect.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sil.morphlect.enums.Effect
import com.sil.morphlect.enums.Section
import com.sil.morphlect.logic.FormatConverters
import com.sil.morphlect.logic.Filtering
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.core.Mat

class EditorViewModel : ViewModel() {
    private var originalMat: Mat? = null

    // states
    var previewBitmap by mutableStateOf<Bitmap?>(null)
        private set

    var section by mutableStateOf(Section.Filtering)
        private set

    var effectValues = mutableStateMapOf<Effect, Double>(
        Effect.Contrast to 0.0,
        Effect.Hue to 0.0,
        Effect.Blur to 0.0,
        Effect.Brightness to 0.0,
        Effect.LightBalance to 0.0
    )
        private set

    // state mutations
    var selectedEffect by mutableStateOf(Effect.Contrast)
        private set

    fun changeSection(section: Section) {
        this.section = section
    }

    fun changeSelectedEffect(selectedEffect: Effect) {
        this.selectedEffect = selectedEffect
    }

    fun adjustEffect(effect: Effect = selectedEffect, value: Double) {
        effectValues[effect] = value
        updatePreview()
    }

    // utils
    @RequiresApi(Build.VERSION_CODES.P)
    fun loadImage(context: Context, uri: Uri) {
        // run the conversions on a separate thread using coroutine
        viewModelScope.launch(Dispatchers.Default) {
            val bitmap = FormatConverters.uriToBitmap(context, uri)
            val mat = FormatConverters.bitmapToMat(bitmap)
            originalMat = mat.clone()
            updatePreview()
        }
    }

    private fun updatePreview() {
        val src = originalMat ?: return
        viewModelScope.launch {
            // heavy work off main
            val bitmap = kotlinx.coroutines.withContext(Dispatchers.Default) {
                val copy = src.clone()
                val processed = Filtering.brightness(copy, effectValues[Effect.Brightness]!!)
                val bmp = FormatConverters.matToBitmap(processed)
                copy.release()
                processed.release()
                bmp
            }
            previewBitmap = bitmap
        }
    }

    override fun onCleared() {
        super.onCleared()
        originalMat?.release()
    }
}