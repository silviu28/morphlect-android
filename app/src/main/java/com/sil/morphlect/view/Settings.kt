package com.sil.morphlect.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sil.morphlect.AppConfigRepository
import kotlinx.coroutines.launch

/**
 the settings page, where the user can adjust any setting.
*/
@Composable
fun Settings(
    configRepository: AppConfigRepository,
    navController: NavController,
) {
    val advancedMode by configRepository.advancedMode.collectAsState(initial = false)
    val hidePrimaryBar by configRepository.hidePrimaryBar.collectAsState(initial = false)
    val localSmartFeatures by configRepository.localSmartFeatures.collectAsState(initial = false)
    val cloudCompute by configRepository.cloudCompute.collectAsState(initial = false)

    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.padding(18.dp),
        bottomBar = { Text("morphlect 0.0.0") },
    ) { _ ->
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            SettingsEntry(
                title = "advanced mode",
                description = "activate professional grade tooling, such as dynamic adjustable color levels using a dynamic histogram",
                checked = advancedMode,
                onCheckedChange = {
                    scope.launch { configRepository.setAdvancedMode(it) }
                }
            )

            SettingsEntry(
                title = "hide primary bar",
                description = "hide primary bar for sections that may have it",
                checked = hidePrimaryBar,
                onCheckedChange = {
                    scope.launch { configRepository.setHidePrimaryBar(it) }
                }
            )

            SettingsEntry(
                title = "local smart features",
                description = "use smart features locally instead of offloading to server",
                checked = localSmartFeatures,
                onCheckedChange = {
                    scope.launch { configRepository.setLocalSmartFeatures(it) }
                }
            )

            SettingsEntry(
                title = "cloud compute",
                description = null,
                checked = cloudCompute,
                onCheckedChange = {
                    scope.launch { configRepository.setCloudCompute(it) }
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "manage your models",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                }

                IconButton(onClick = { navController.navigate("modeldownload") }) {
                    Icon(Icons.Default.Settings, contentDescription = "manage models")
                }
            }
        }
    }
}

/**
 a compact brief for a setting, alongside a slider to adjust it.
*/
@Composable
fun SettingsEntry(
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )

            description?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}