package com.sil.morphlect.view.custom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CircleOutlineButton(
    onClick: () -> Unit,
    content: @Composable (() -> Unit)
) {
    OutlinedButton(
        onClick,
        shape = CircleShape,
        border = BorderStroke(1.dp, Color(0xFFFF6600)),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF2C2C2C)),
        contentPadding = PaddingValues(15.dp),
        modifier = Modifier.padding(2.dp)
    ) {
        content()
    }
}