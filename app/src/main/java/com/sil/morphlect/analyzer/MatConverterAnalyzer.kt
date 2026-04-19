package com.sil.morphlect.analyzer

import android.graphics.Bitmap
import android.graphics.ImageFormat
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.sil.morphlect.extension.yuvToRgba
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import androidx.core.graphics.createBitmap
import com.sil.morphlect.logic.FormatConverters

/**
 * attaches to a camera feed and processes frames into OpenCV Mat format.
 */
class MatConverterAnalyzer(
    private val listener: (String, Mat) -> Unit
) : ImageAnalysis.Analyzer {
    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        image.image?.let {
            if (it.format == ImageFormat.YUV_420_888 && it.planes.size == 3) {
                val rgbMat = it.yuvToRgba()
//                Imgproc.cvtColor(rgbMat, buf, Imgproc.COLOR_RGBA2GRAY)
//                val bmp = createBitmap(buf.cols(), buf.rows())
                val message = "You can pass in additional metadata here"

//                Utils.matToBitmap(buf, bmp)

                listener(message, rgbMat)
            }
        }

        image.close()
    }
}