package com.sil.morphlect.logic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import org.opencv.android.Utils
import org.opencv.core.Mat

object FormatConverters {
    /**
     * pass the URI of an image and return a bitmap object representing the image at the specified URI.
     */
    @RequiresApi(Build.VERSION_CODES.P)
    fun uriToBitmap(context: Context, uri: Uri): Bitmap {
        val src = ImageDecoder.createSource(context.contentResolver, uri)
        return ImageDecoder
            .decodeBitmap(src)
            .copy(Bitmap.Config.ARGB_8888, true)
    }

    /**
     * convert a bitmap to an OpenCV Mat object.
     */
    fun bitmapToMat(bitmap: Bitmap): Mat {
        Mat().also {
            Utils.bitmapToMat(bitmap, it)
            return it
        }
    }

    /**
     * convert an OpenCV Mat object to a Bitmap
     */
    fun matToBitmap(mat: Mat): Bitmap {
        val bitmap = Bitmap.createBitmap(
            mat.cols(),
            mat.rows(),
            Bitmap.Config.ARGB_8888
        )
        Utils.matToBitmap(mat, bitmap)
        return bitmap
    }
}