package com.sil.morphlect.data

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.datastore.core.Closeable
import com.sil.morphlect.logic.FormatConverters
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size

class EditorLayer(var name: String, private val mat: Mat) : Closeable {
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
}

fun Mat.extend(src: Mat, size: Size): Mat {
    var dst = Mat.zeros(size, src.type())
    val region = dst.submat(0, src.rows(), 0, src.cols())
    src.copyTo(region)
    region.release()

    return dst
}
