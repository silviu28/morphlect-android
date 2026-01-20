package com.sil.morphlect.logic

import android.util.Log
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

object Filtering {
    fun contrast(src: Mat, contrast: Double): Mat {
        val dst = Mat()
        val alpha = 1.0 + contrast
        src.convertTo(dst, -1, alpha, 0.0)
        return dst
    }

    fun brightness(src: Mat, brightness: Double): Mat {
        Log.i("Filtering", "apply $brightness")
        val dst = Mat()
        src.convertTo(dst, -1, 1.0, brightness * 100)
        return dst
    }

    // TODO
    fun blur(src: Mat, horizontalKernelDim: Double, verticalKernelDim: Double): Mat {
        val hKernel = horizontalKernelDim.toInt().let {
            if (it <= 1) return src
            if (it % 2 == 0) it + 1 else it
        }
        val vKernel = verticalKernelDim.toInt().let {
            if (it <= 1) return src
            if (it % 2 == 0) it + 1 else it
        }

        val dst = Mat()
        val kSize = Size(hKernel.toDouble(), vKernel.toDouble())
        Imgproc.blur(src, dst, kSize)
        return dst
    }

    fun lightBalance(src: Mat, lb: Double): Mat {
        if (lb == 0.0) {
            return src
        }
        val channels = mutableListOf<Mat>()
        Core.split(src, channels)
        val redShift = -lb * 50
        val blueShift = lb * 50
        channels[2].convertTo(channels[2], -1, 1.0, redShift)
        channels[0].convertTo(channels[0], -1, 1.0, blueShift)

        Core.merge(channels, src)
        channels.forEach {
            it.release()
        }
        return src
    }

    fun hueShift(src: Mat, shift: Double): Mat {
        if (shift == 0.0) {
            return src
        }

        val hsv = Mat()
        Imgproc.cvtColor(src, hsv, Imgproc.COLOR_BGR2HSV)

        val channels = mutableListOf<Mat>()
        Core.split(hsv, channels)

        val shiftAngle = shift * 180
        channels[0].convertTo(channels[0], -1, 1.0, shiftAngle)

        Core.normalize(channels[0], channels[0], 0.0, 180.0, Core.NORM_MINMAX)

        Core.merge(channels, hsv)
        Imgproc.cvtColor(hsv, src, Imgproc.COLOR_HSV2BGR)

        channels.forEach { it.release() }
        return src
    }
}