package com.sil.morphlect.logic

import android.content.Context
import com.sil.mxtengine.data.MXTManifest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import net.mamoe.yamlkt.Yaml
import java.io.File

data class MXTManifestDTO(
    val manifest: MXTManifest,
    val path: String
)

suspend fun loadExtension(context: Context, extensionName: String): MXTManifestDTO
    = withContext(Dispatchers.IO) {
    val cd = File(context.filesDir, "models/$extensionName")
    val manifest = Yaml.decodeFromString<MXTManifest>(
        File(cd, "extension_manifest.yml").readText()
    )
    MXTManifestDTO(manifest, "$cd/${manifest.name}")
}