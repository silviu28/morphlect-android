package com.sil.morphlect.view

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sil.morphlect.data.Preset
import org.opencv.core.Mat
import kotlin.collections.forEach

@Composable fun PresetBar(
    presets: List<Preset>,
    originalMat: Mat?,
    onApply: (Preset) -> Unit,
    onLongPress: (Preset) -> Unit,
    onAddNew: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.Gray.copy(alpha = .2f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            presets.forEach { preset ->
                PresetPreview(
                    preset = preset,
                    originalMat = originalMat,
                    onPress = { onApply(preset) },
                    onLongPress = { onLongPress(preset) },
                )
            }
            ElevatedButton(
                modifier = Modifier
                    .size(60.dp)
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                onClick = { onAddNew() }
            ) {
                Text("+")
            }
        }
    }
}