package com.sil.morphlect.view.camera

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RuleOfThirdsGrid(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val gridStroke = 1.dp.toPx()
        drawLine(Color.White, Offset(size.width / 3, 0f), Offset(size.width / 3, size.height), gridStroke)
        drawLine(Color.White, Offset(size.width * 2 / 3, 0f), Offset(size.width * 2 / 3, size.height), gridStroke)
        drawLine(Color.White, Offset(0f, size.height / 3), Offset(size.width, size.height / 3), gridStroke)
        drawLine(Color.White, Offset(0f, size.height * 2 / 3), Offset(size.width, size.height * 2 / 3), gridStroke)
    }
}