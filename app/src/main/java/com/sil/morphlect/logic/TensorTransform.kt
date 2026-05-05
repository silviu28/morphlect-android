package com.sil.morphlect.logic

import com.sil.morphlect.ml.impl.Tensor4D
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

// todo might have to be suspending
fun depthToMat(depthTensor: Tensor4D): Mat {
    val depthMat = Mat(256, 256, CvType.CV_32F)
    val data = depthTensor.data[0]

    for (i in 0 until 256) {
        val row = FloatArray(256) { j -> data[i][j][0] }
        depthMat.put(i, 0, row)
    }

    val normalized = Mat()
    Core.normalize(depthMat, normalized, 0.0, 255.0, Core.NORM_MINMAX)
    normalized.convertTo(normalized, CvType.CV_8U)

    val result = Mat()
    Imgproc.cvtColor(normalized, result, Imgproc.COLOR_GRAY2RGBA)
    return result
}