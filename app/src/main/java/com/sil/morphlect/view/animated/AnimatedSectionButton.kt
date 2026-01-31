package com.sil.morphlect.view.animated

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/// a button that when selected scales using a spring animation - can be used for anything
@Composable
fun AnimatedSectionButton(
    isSelected: Boolean,
    onClick: () -> Unit,
    children: @Composable() () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.4f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    TextButton(
        onClick = onClick,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }) {
        // children are passed as a lambda, execute it to also render them
        children()
    }
}
