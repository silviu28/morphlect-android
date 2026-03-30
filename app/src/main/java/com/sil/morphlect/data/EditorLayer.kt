package com.sil.morphlect.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.datastore.core.Closeable
import com.sil.morphlect.logic.FormatConverters
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Size
import kotlin.math.abs
import org.opencv.core.Core
import com.sil.morphlect.extension.extend
import com.sil.morphlect.extension.toCvScalar
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class EditorLayer(val mat: Mat) : Closeable {
    companion object {
        /**
         * creates a new empty EditorLayer.
         */
        fun empty(): EditorLayer {
            return EditorLayer(Mat.zeros(300, 300, CvType.CV_8UC4))
        }

        /**
         * creates a transparent EditorLayer containing given `text`.
         */
        fun withText(
            text: String,
            size: TextUnit = .5.sp,
            color: Color = Color.White,
            thickness: Int = 2,
            position: IntOffset = IntOffset.Zero,
            antialiased: Boolean = true,
        ): EditorLayer {
            // TODO this is okay, but can't use custom fonts, and Bitmap is just simple raster..
            val mat = Mat.zeros(300, 300, CvType.CV_8UC4)
            Imgproc.putText(
                mat,
                text,
                Point(150.0 + position.x, 150.0 + position.y),
                Imgproc.FONT_HERSHEY_SIMPLEX,
                size.value.toDouble(),
                color.toCvScalar(),
                thickness,
                if (antialiased) Imgproc.LINE_AA else Imgproc.LINE_8,
            )
            return EditorLayer(mat)
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

        return EditorLayer(extended)
    }

    /**
     * CALL THIS ONLY AFTER REMOVAL to ensure safe memory free.
     */
    override fun close() {
        mat.release()
    }

    fun clone(): EditorLayer {
        val matClone = mat.clone()
        return EditorLayer(matClone)
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
        return EditorLayer(mat.clone().submat(roi))
    }
}


