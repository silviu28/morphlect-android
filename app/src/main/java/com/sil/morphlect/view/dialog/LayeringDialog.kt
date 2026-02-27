package com.sil.morphlect.view.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.sil.morphlect.data.EditorLayer

@Composable
fun LayeringDialog(
    layers: List<EditorLayer>,
    onRemoveLayer: (EditorLayer) -> Unit,
    onMergeLayerWithBelow: (Int) -> Unit,
    onDismissRequest: () -> Unit,
    onInterchangeLayers: (Int, Int) -> Unit,
    onVisibilityToggle: (Int) -> Unit,
) {
    DialogScaffold("layers", onDismissRequest) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        ) {
            layers.forEachIndexed { i, layer ->
                LayerInfo(
                    key = i,
                    layer,
                    onRemoveLayer,
                    onMergeLayerWithBelow,
                    onInterchangeLayers,
                    onVisibilityToggle
                )
            }
        }
    }
}

@Composable
private fun LayerInfo(
    key: Int,
    layer: EditorLayer,
    onRemoveLayer: (EditorLayer) -> Unit,
    onMergeLayerWithAbove: (Int) -> Unit,
    onInterchangeLayers: (Int, Int) -> Unit,
    onVisibilityToggle: (Int) -> Unit
) {
    val baseColor = MaterialTheme.colorScheme.background
    val insetColor = baseColor.copy(
        red = (baseColor.red + 0.04f).coerceAtMost(1f),
        green = (baseColor.green + 0.04f).coerceAtMost(1f),
        blue = (baseColor.blue + 0.04f).coerceAtMost(1f),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(insetColor)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(52.dp)) {
            Image(
                bitmap = layer.visual,
                contentDescription = "layer preview",
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = { onVisibilityToggle(key) }),
            )
            if (!layer.visible)
                Icon(Icons.Default.VisibilityOff, contentDescription = "layer hidden")
        }

        Text(
            text = layer.name,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )

        IconButton(onClick = { onInterchangeLayers(key, key - 1) }) {
            Icon(Icons.Default.ArrowDropUp, contentDescription = "move layer up")
        }
        IconButton(onClick = { onInterchangeLayers(key, key + 1) }) {
            Icon(Icons.Default.ArrowDropDown, contentDescription = "move layer down")
        }
        IconButton(onClick = { onMergeLayerWithAbove(key) }) {
            Icon(Icons.Default.Merge, contentDescription = "merge with layer above")
        }
        IconButton(onClick = { onRemoveLayer(layer) }) {
            Icon(Icons.Default.Delete, contentDescription = "remove layer")
        }
    }
}