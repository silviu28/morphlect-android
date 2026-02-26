package com.sil.morphlect.data

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

    /**
     * returns the resulting layer created from the merging of another given layer.
     */
    fun mergeWith(other: EditorLayer): EditorLayer {
        TODO("below has to be tested.")
//        val extended = other.mat.extend(other.mat, mat.size())
//        var res = mat.clone()
//        val channels = ArrayList<Mat>()
//        Core.split(extended, channels)
//        val opacityMask = channels[3]
//        extended.copyTo(res, opacityMask)
//        extended.release()
//
//        return EditorLayer("$name + ${other.name}", res)
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

fun Mat.extend(src: Mat, size: Size): Mat {
    var dst = Mat.zeros(size, src.type())
    val region = dst.submat(0, src.rows(), 0, src.cols())
    src.copyTo(region)
    region.release()

    return dst
}
