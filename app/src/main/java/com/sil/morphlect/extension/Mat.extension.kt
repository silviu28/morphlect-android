package com.sil.morphlect.extension

import org.opencv.core.Mat
import org.opencv.core.Size

/**
 * returns a new `Mat` padded to fit given `size`.
 */
fun Mat.extend(size: Size): Mat {
    if (size == size()) return this

    val dst = Mat.zeros(size, type())

    val xOffset = ((size.width - cols()) / 2).toInt()
    val yOffset = ((size.height - rows()) / 2).toInt()

    val region = dst.submat(yOffset, yOffset + rows(), xOffset, xOffset + cols())
    copyTo(region)
    region.release()

    return dst
}