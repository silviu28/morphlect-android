package com.sil.morphlect.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.sil.morphlect.data.Preset
import com.sil.morphlect.logic.ClusteringType
import com.sil.morphlect.ml.impl.ExtensionModelLoader
import com.sil.morphlect.repository.ExtensionsRepository
import com.sil.morphlect.repository.PresetsRepository
import com.sil.morphlect.logic.loadExtension
import com.sil.mxtengine.data.InteractorType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

class CameraModeViewModel() : ViewModel() {
    var capturedImageUri by mutableStateOf<Uri?>(null)
    var presets by mutableStateOf<List<Preset>>(emptyList())
    var models by mutableStateOf<Map<String, Boolean>>(emptyMap())
    var imageOnlyLoadedModels by mutableStateOf<Map<ExtensionModelLoader, Boolean>>(emptyMap())
    val inferenceRefreshTimes = arrayOf(1.seconds, 2.seconds, 4.seconds, 5.seconds)
    var inferenceRefreshInterval by mutableStateOf(inferenceRefreshTimes[0])
    var clusteringType by mutableStateOf(ClusteringType.DBSCAN)

    suspend fun loadRepositories(presetsRepository: PresetsRepository, extensionsRepository: ExtensionsRepository)
        = withContext(Dispatchers.IO) {
        presets = presetsRepository.load()
        models = extensionsRepository.readExtensionNames().associate { it to true }
    }

    suspend fun loadEligibleModels(context: Context) = withContext(Dispatchers.IO) {
        imageOnlyLoadedModels = models
            .map { loadExtension(context, it.key).manifest }
            .filter { manifest -> // here we want only the extensions that take in an image
                manifest.inputs.firstOrNull { it.type != InteractorType.Image } == null
            }
            .associate { // then construct them
                ExtensionModelLoader.Builder()
                    .named(it.name)
                    .withInputs(it.inputs)
                    .withOutputs(it.outputs)
                    .build()
                    .apply { initialize(context) } to true
            }
    }
}