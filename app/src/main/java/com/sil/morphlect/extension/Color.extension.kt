package com.sil.morphlect.extension

import androidx.compose.ui.graphics.Color
import org.opencv.core.Scalar

fun Color.toCvScalar(): Scalar {
    return Scalar(red * 255.0, green * 255.0, blue * 255.0)
}