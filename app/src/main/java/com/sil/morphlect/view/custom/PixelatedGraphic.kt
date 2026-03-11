package com.sil.morphlect.view.custom

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sil.morphlect.R

/**
 * draws an `ImageBitmap` or `ImageVector` using a pixelation mask
*/
@Composable
fun PixelatedGraphic(
    imageBitmap: ImageBitmap? = null,
    imageVector: ImageVector? = null,
    size: Dp = 200.dp,
) {
    Box(
        Modifier
            .size(size)
            .padding(10.dp)
    ) {
        imageBitmap?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )
        }
        imageVector?.let {
            Image(
                imageVector = it,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Image(
            painter = painterResource(R.drawable.pixel_mask),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
    }
}