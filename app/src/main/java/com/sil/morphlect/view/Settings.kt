package com.sil.morphlect.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sil.morphlect.AppConfigRepository
import kotlinx.coroutines.launch

@Composable
fun Settings(configRepository: AppConfigRepository) {
    val advancedMode by configRepository.advancedMode.collectAsState(initial = false)
    val hidePrimaryBar by configRepository.hidePrimaryBar.collectAsState(initial = false)
    val localSmartFeatures by configRepository.localSmartFeatures.collectAsState(initial = false)
    val cloudCompute by configRepository.cloudCompute.collectAsState(initial = false)

    val scope = rememberCoroutineScope()

    Scaffold { _ ->
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer(Modifier.height(200.dp))
                Text("advanced mode")
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = advancedMode,
                    onCheckedChange = { enabled ->
                        scope.launch { configRepository.setAdvancedMode(enabled) }
                    }
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("hide primary bar")
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = hidePrimaryBar,
                    onCheckedChange = { enabled ->
                        scope.launch { configRepository.setHidePrimaryBar(enabled) }
                    }
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("local smart features")
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = localSmartFeatures,
                    onCheckedChange = { enabled ->
                        scope.launch { configRepository.setLocalSmartFeatures(enabled) }
                    }
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("cloud compute")
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = cloudCompute,
                    onCheckedChange = { enabled ->
                        scope.launch { configRepository.setCloudCompute(enabled) }
                    }
                )
            }
        }
    }
}