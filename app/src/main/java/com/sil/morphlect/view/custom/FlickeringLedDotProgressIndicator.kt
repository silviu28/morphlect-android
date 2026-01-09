package com.sil.morphlect.view.custom

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

// A custom loading indicator, featuring flickering LEDs that randomly light up.
@Composable
fun FlickeringLedDotProgressIndicator(
    modifier: Modifier = Modifier,
    dotRadius: Float = 6f,
    onColor: Color = MaterialTheme.colorScheme.primary,
    offColor: Color = Color(0xFF2C2C2C),
    spacing: Float = 8f,
    stayOnTime: Int = 300,
) {
    val dotToggles = remember { mutableStateListOf(*Array(9) { false }) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        // for each dot run probabilities if they get lit
        for (i in 0 until 9) {
            scope.launch {
                delay(Random.nextLong(0, 2000))
                while (true) {
                    delay(Random.nextLong(800, 2500))

                    if (Random.nextFloat() > .4f) {
                        // turn on
                        dotToggles[i] = true

                        // stay on
                        delay(stayOnTime.milliseconds)

                        // power off
                        dotToggles[i] = false
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val dotDiameter = dotRadius * 2
            val gridSize = (dotDiameter * 3) + (spacing * 2)
            val startX = (size.width - gridSize) / 2 + dotRadius
            val startY = (size.height - gridSize) / 2 + dotRadius

            for (row in 0 until 3) {
                for (col in 0 until 3) {
                    val index = row * 3 + col
                    val x = startX + (col * (dotDiameter + spacing))
                    val y = startY + (row * (dotDiameter + spacing))

                    val currentColor = if (dotToggles[index]) onColor else offColor

                    drawCircle(
                        color = currentColor,
                        radius = dotRadius,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}