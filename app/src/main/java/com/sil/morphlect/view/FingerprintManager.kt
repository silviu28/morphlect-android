package com.sil.morphlect.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sil.morphlect.repository.FingerprintRepository
import com.sil.morphlect.view.custom.DecoratedContainer

@Composable
fun FingerprintManager(
    fingerprintRepository: FingerprintRepository
) {
    DecoratedContainer(Icons.Default.Fingerprint) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("fingerprint manager")
            Text(
                """
                morphlect checks some of your preferences,
                alongside usage metrics in order to locally
                personalize your experience. your fingerprint
                determines a slight shift in the way your images
                are processed so they are more like you.
                """.trimIndent()
            )
            Text("your fingerprint determines these features:\n TBA")
            Switch(true, { })
            Text("disable fingerprint")
            TextButton(onClick = { }) {
                Row {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Text("download")
                }
            }
            TextButton(onClick = { }) {
                Row {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Text("load another fingerprint")
                }
            }
        }
    }
}