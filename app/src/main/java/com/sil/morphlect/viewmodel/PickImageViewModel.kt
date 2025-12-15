package com.sil.morphlect.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class PickImageViewModel : ViewModel() {
    var imageUri by mutableStateOf<Uri?>(null)
        private set

    fun setImage(uri: Uri) {
        imageUri = uri
    }
}