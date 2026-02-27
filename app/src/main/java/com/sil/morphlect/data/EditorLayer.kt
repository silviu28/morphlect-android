package com.sil.morphlect.data

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.datastore.core.Closeable
import com.sil.morphlect.logic.FormatConverters
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Size
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import androidx.core.graphics.createBitmap
import org.opencv.core.Core

class EditorLayer(var name: String, val mat: Mat) : Closeable {
    companion object {
        fun emptyNamed(name: String) : EditorLayer {
            return EditorLayer(name, Mat.zeros(300, 300, CvType.CV_8UC4))
        }
    }

    /**
     * lazy loaded visual representation of the layer.
    */
    val visual by lazy { FormatConverters.matToBitmap(mat).asImageBitmap() }

    var visible by mutableStateOf(true)

    /**
     * returns the resulting layer created from the merging of another given layer.
     */
    fun mergeWith(other: EditorLayer): EditorLayer {
        // merged layer should fit in size both layers
        val resultSize = Size(
            maxOf(mat.cols(), other.mat.cols()).toDouble(),
            maxOf(mat.rows(), other.mat.rows()).toDouble()
        )

        // span both images across the required size
        val extended = mat.extend(resultSize)
        val extendedOther = other.mat.extend(resultSize)

        // overlap extended other on this layer with respect to the alpha channel
        val channels = ArrayList<Mat>()
        Core.split(extendedOther, channels)
        val opacityMask = channels[3]

        extendedOther.copyTo(extended, opacityMask)

        // dispose JNI resources
        extendedOther.release()
        channels.forEach { it.release() }

        return EditorLayer("$name + ${other.name}", extended)
    }

    /**
     * CALL THIS ONLY AFTER REMOVAL to ensure safe memory free.
     */
    override fun close() {
        mat.release()
    }

    fun clone(): EditorLayer {
        val matClone = mat.clone()
        return EditorLayer(name, matClone)
    }

    fun crop(upCorner: Offset, downCorner: Offset, size: androidx.compose.ui.geometry.Size): EditorLayer {
        // scale factors between display space and mat space
        val scaleX = mat.width().toFloat() / size.width
        val scaleY = mat.height().toFloat() / size.height

        // convert offsets from display space to mat space
        val matUpCorner = Offset(upCorner.x * scaleX, upCorner.y * scaleY)
        val matDownCorner = Offset(downCorner.x * scaleX, downCorner.y * scaleY)

        val x = minOf(matUpCorner.x, matDownCorner.x).toInt()
        val y = minOf(matUpCorner.y, matDownCorner.y).toInt()
        val width = abs(matUpCorner.x - matDownCorner.x).toInt()
        val height = abs(matUpCorner.y - matDownCorner.y).toInt()

        // clamp to mat bounds just in case
        val safeX = x.coerceIn(0, mat.width() - 1)
        val safeY = y.coerceIn(0, mat.height() - 1)
        val safeWidth = width.coerceIn(1, mat.width() - safeX)
        val safeHeight = height.coerceIn(1, mat.height() - safeY)

        val roi = Rect(safeX, safeY, safeWidth, safeHeight)
        return EditorLayer(name, mat.clone().submat(roi))
    }
}

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
