package com.sil.morphlect.view.custom

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun DecoratedContainer(
    icon: ImageVector? = null,
    content: @Composable (() -> Unit),
) {
    Box(Modifier.fillMaxSize()) {
        icon?.let {
            Icon(
                it,
                contentDescription = null,
                modifier = Modifier
                    .size(240.dp)
                    .align(Alignment.BottomEnd)
                    .offset(100.dp, 100.dp)
                    .alpha(.1f),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        content()
    }
}