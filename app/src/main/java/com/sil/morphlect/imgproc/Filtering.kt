package com.sil.morphlect.imgproc

import android.util.Log
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.pow

object Filtering {
    fun contrast(src: Mat, gamma: Double): Mat {
        val correctedGamma = 1.0 + gamma // so 0 -> no change, positive -> more contrast

        val lut = Mat(1, 256, CvType.CV_8U)
        val lutData = ByteArray(256) { i ->
            (255.0 * (i / 255.0).pow(correctedGamma))
                .coerceIn(0.0, 255.0)
                .toInt()
                .toByte()
        }
        lut.put(0, 0, lutData)

        val dst = Mat()
        Core.LUT(src, lut, dst)
        lut.release()
        return dst
    }

    fun brightness(src: Mat, brightness: Double): Mat {
        Log.i("Filtering", "apply $brightness")
        val dst = Mat()
        src.convertTo(dst, -1, 1.0, brightness * 100)
        return dst
    }

    // TODO
    fun blur(src: Mat, xStrength: Double, yStrength: Double): Mat {
        // values in range (0, 1) must be converted to arbitrary odd kernel sizes
        // can be multiplied by a larger number for higher blur, but it will run very poorly
        val hKernel = (xStrength * 100).toInt().let {
            if (it <= 1) return src.clone()
            if (it % 2 == 0) it + 1 else it
        }
        val vKernel = (yStrength * 100).toInt().let {
            if (it <= 1) return src.clone()
            if (it % 2 == 0) it + 1 else it
        }

        val dst = Mat()
        val kSize = Size(hKernel.toDouble(), vKernel.toDouble())
        Imgproc.GaussianBlur(src, dst, kSize, 0.0)
        return dst
    }

    fun sharpen(src: Mat, sharpening: Double): Mat {
        if (sharpening == 0.0) return src

        // blur the source
        val blurred = Mat()
        Imgproc.GaussianBlur(src, blurred, Size(0.0, 0.0), 5.0)

        // unsharp mask: dst = src * (1 + amount) - blurred * amount
        val dst = Mat()
        Core.addWeighted(src, 1.0 + sharpening, blurred, -sharpening, 0.0, dst)

        blurred.release()
        return dst
    }

    fun lightBalance(src: Mat, lb: Double): Mat {
        if (lb == 0.0) {
            return src.clone()
        }
        val dst = Mat()
        val channels = mutableListOf<Mat>()
        Core.split(src, channels)
        val redShift = -lb * 25
        val blueShift = lb * 25
        
        if (channels.size >= 3) {
            channels[0].convertTo(channels[0], -1, 1.0, redShift)
            channels[2].convertTo(channels[2], -1, 1.0, blueShift)
        } else if (channels.isNotEmpty()) {
            channels[0].convertTo(channels[0], -1, 1.0, redShift)
        }

        Core.merge(channels, dst)
        channels.forEach {
            it.release()
        }
        return dst
    }

    fun hueShift(src: Mat, shift: Double): Mat {
        if (shift == 0.0) {
            return src.clone()
        }
        val dst = Mat()
        val hsv = Mat()
        
        val rgb = Mat()
        val hasAlpha = src.channels() == 4
        if (hasAlpha) {
            Imgproc.cvtColor(src, rgb, Imgproc.COLOR_RGBA2RGB)
        } else {
            src.copyTo(rgb)
        }
        
        Imgproc.cvtColor(rgb, hsv, Imgproc.COLOR_RGB2HSV)

        val channels = mutableListOf<Mat>()
        Core.split(hsv, channels)

        val shiftAngle = shift * 180
        channels[0].convertTo(channels[0], -1, 1.0, shiftAngle)

        Core.normalize(channels[0], channels[0], 0.0, 180.0, Core.NORM_MINMAX)

        Core.merge(channels, hsv)
        val rgbProcessed = Mat()
        Imgproc.cvtColor(hsv, rgbProcessed, Imgproc.COLOR_HSV2RGB)

        if (hasAlpha) {
            val srcChannels = mutableListOf<Mat>()
            Core.split(src, srcChannels)
            val alpha = srcChannels[3]
            
            val processedChannels = mutableListOf<Mat>()
            Core.split(rgbProcessed, processedChannels)
            processedChannels.add(alpha)
            
            Core.merge(processedChannels, dst)
            
            for (i in 0..2) {
                srcChannels[i].release()
                processedChannels[i].release()
            }
            alpha.release()
        } else {
            rgbProcessed.copyTo(dst)
        }

        channels.forEach { it.release() }
        rgb.release()
        hsv.release()
        rgbProcessed.release()
        return dst
    }

    fun saturation(src: Mat, factor: Double): Mat {
        if (factor == 0.0) return src.clone()

        val dst = Mat()
        val hsv = Mat()
        
        val rgb = Mat()
        val hasAlpha = src.channels() == 4
        if (hasAlpha) {
            Imgproc.cvtColor(src, rgb, Imgproc.COLOR_RGBA2RGB)
        } else {
            src.copyTo(rgb)
        }
        
        Imgproc.cvtColor(rgb, hsv, Imgproc.COLOR_RGB2HSV)

        val channels = mutableListOf<Mat>()
        Core.split(hsv, channels)

        // Scale saturation
        val alphaFactor = 1.0 + factor
        Core.multiply(channels[1], Scalar(alphaFactor), channels[1])

        // Clamp to [0, 255]
        val temp = Mat()
        Imgproc.threshold(channels[1], temp, 255.0, 255.0, Imgproc.THRESH_TRUNC)
        Imgproc.threshold(temp, channels[1], 0.0, 0.0, Imgproc.THRESH_TOZERO)
        temp.release()

        Core.merge(channels, hsv)
        val rgbProcessed = Mat()
        Imgproc.cvtColor(hsv, rgbProcessed, Imgproc.COLOR_HSV2RGB)

        if (hasAlpha) {
            val srcChannels = mutableListOf<Mat>()
            Core.split(src, srcChannels)
            val alpha = srcChannels[3]
            
            val processedChannels = mutableListOf<Mat>()
            Core.split(rgbProcessed, processedChannels)
            processedChannels.add(alpha)
            
            Core.merge(processedChannels, dst)
            
            for (i in 0..2) {
                srcChannels[i].release()
                processedChannels[i].release()
            }
            alpha.release()
        } else {
            rgbProcessed.copyTo(dst)
        }

        channels.forEach { it.release() }
        rgb.release()
        hsv.release()
        rgbProcessed.release()
        return dst
    }

    // uniformly downscales CV mats. the bigger the resolution -> the bigger the downscale
    fun uniformDownscale(src: Mat, maxDimension: Int = 1000, smoothing: Boolean = true): Mat {
        val largest = maxOf(src.rows(), src.cols())
        if (largest <= maxDimension) return src.clone()

        val scale = maxDimension / largest.toDouble()
        val dst = Mat()
        Imgproc.resize(src, dst, Size(), scale, scale, Imgproc.INTER_CUBIC)
        return if (!smoothing) dst else blur(dst, .02, .02) // apply very minute smoothing
    }
}