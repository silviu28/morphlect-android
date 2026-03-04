package com.sil.morphlect.extension

import org.opencv.core.Mat
import org.opencv.core.Size

/**
 * returns a new `Mat` padded to fit given `size`.
 */
fun Mat.extend(size: Size): Mat {
    if (size == size()) return this

    var dst = Mat.zeros(size, type())
    val region = dst.submat(0, rows(), 0, cols())
    copyTo(region)
    region.release()

    return dst
}