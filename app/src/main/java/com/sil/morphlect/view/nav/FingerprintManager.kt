package com.sil.morphlect.view.nav

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.sil.morphlect.data.FingerprintData
import com.sil.morphlect.data.Preset
import com.sil.morphlect.repository.FingerprintRepository
import com.sil.morphlect.view.custom.DecoratedContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

suspend fun saveFingerprint(context: Context, fingerprint: FingerprintData) {
    val uri = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, "fingerprint.json")
            put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Morphlect")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        // Use Downloads instead of Files
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return@withContext null

        resolver.openOutputStream(uri)?.use { out ->
            out.write(
                fingerprint.toJSON()
                    .toString(2)
                    .toByteArray()
            )
        }

        contentValues.clear()
        contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)

        // redirect the user to the directory in which the fingerprint is saved
        context.run {
            val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
            startActivity(intent)
        }
        return@withContext uri
    }
    uri?.let {
        Toast.makeText(
            context,
            "fingerprint saved at ${it.path}.",
            Toast.LENGTH_LONG
        ).show()
    }
}

@Composable
fun FingerprintManager(
    fingerprintRepository: FingerprintRepository
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val fingerprintPickLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val fileName = context.contentResolver.query(uri, null, null, null, null)?.use {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                it.moveToFirst()
                it.getString(nameIndex)
            }

            fileName?.let {
                if (it != "fingerprint.json") {
                    Toast.makeText(context, "Please select a fingerprint.json file.", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    try {
                        val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                            stream.bufferedReader().readText()
                        }
                        val json = JSONObject(content ?: return@let)
                        val fp = FingerprintData.fromJSON(json)
                        coroutineScope.launch { fingerprintRepository.save(fp) }
                        Toast.makeText(context, "fingerprint has been loaded.", Toast.LENGTH_SHORT).show()
                    } catch (e: JSONException) {
                        Log.e("Preset parsing", "An error occurred - $e")
                    }
                }
            }

        }
    }

    DecoratedContainer(Icons.Default.Fingerprint) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("fingerprint manager")
            Text(
                text = """
                    morphlect checks some of your preferences,
                    alongside usage metrics in order to locally
                    personalize your experience. your fingerprint
                    determines a slight shift in the way your images
                    are processed so they are more like you.
                """.trimIndent(),
                style = MaterialTheme.typography.bodySmall,
            )
            TextButton(onClick = {
                coroutineScope.launch {
                    saveFingerprint(context, fingerprintRepository.load())
                }
            }) {
                Row {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Text("download")
                }
            }
            TextButton(onClick = { fingerprintPickLauncher.launch("*/*") }) {
                Row {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Text("load another fingerprint")
                }
            }
            TextButton(onClick = {
                coroutineScope.launch {
                    fingerprintRepository.apply { save(generateNew()) }
                }
                Toast.makeText(context, "fingerprint data removed.", Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Default.Delete, contentDescription = "delete")
                Text("remove fingerprint data")
            }
        }
    }
}