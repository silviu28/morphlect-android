package com.sil.morphlect.layerwork

import android.graphics.Typeface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.Closeable
import com.sil.morphlect.enums.Filter
import com.sil.morphlect.extension.extend
import com.sil.morphlect.imgproc.Filtering
import com.sil.morphlect.imgproc.FormatConverters
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Size
import kotlin.math.abs
import androidx.core.graphics.createBitmap

class StudioLayer(val mat: Mat) : Closeable {
    companion object {
        /**
         * creates a new empty StudioLayer.
         */
        fun empty(): StudioLayer {
            return StudioLayer(Mat.zeros(300, 300, CvType.CV_8UC4))
        }

        /**
         * creates a transparent StudioLayer containing given `text`.
         */
        fun withText(
            canvasWidth: Int,
            canvasHeight: Int,
            text: String,
            textSizeSp: Float = 24f,
            color: Color = Color.White,
            typeface: Typeface = Typeface.DEFAULT,
            position: LayerPosition = LayerPosition.Center,
            antialiased: Boolean = true,
        ): StudioLayer {
            val paint = android.graphics.Paint().apply {
                this.color = color.toArgb()
                this.typeface = typeface
                isAntiAlias = antialiased
                textSize = textSizeSp
            }

            val bounds = android.graphics.Rect()
            paint.getTextBounds(text, 0, text.length, bounds)

            val padding = 16
            val width = bounds.width() + padding * 2
            val height = bounds.height() + padding * 2

            val bitmap = createBitmap(width, height)
            val canvas = android.graphics.Canvas(bitmap)

            canvas.drawText(
                text,
                padding.toFloat() - bounds.left,
                padding.toFloat() - bounds.top,
                paint
            )

            val mat = FormatConverters.bitmapToMat(bitmap)
            bitmap.recycle()

            val offset = position.toOffset(canvasWidth, canvasHeight, width, height)

            val canvasMat = Mat.zeros(canvasHeight, canvasWidth, CvType.CV_8UC4)

            val roi = Rect(offset.x, offset.y, mat.cols(), mat.rows())
            val targetRegion = canvasMat.submat(roi)

            mat.copyTo(targetRegion)
            mat.release()

            return StudioLayer(canvasMat)
        }
    }

    /**
     * lazy loaded visual representation of the layer.
    */
    val visual by lazy { FormatConverters.matToBitmap(mat).asImageBitmap() }

    var visible by mutableStateOf(true)

    val width = mat.width()
    val height = mat.height()

    /**
     * returns the resulting layer created from the merging of another given layer.
     */
    fun mergeWith(other: StudioLayer): StudioLayer {
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

        return StudioLayer(extended)
    }

    /**
     * CALL THIS ONLY AFTER REMOVAL to ensure safe memory free.
     */
    override fun close() {
        mat.release()
    }

    fun clone(): StudioLayer {
        val matClone = mat.clone()
        return StudioLayer(matClone)
    }

    fun cropped(
        upCorner: Offset,
        downCorner: Offset,
        containerSize: androidx.compose.ui.geometry.Size,
        outer: Boolean
    ): StudioLayer {
        val matWidth = mat.width().toFloat()
        val matHeight = mat.height().toFloat()

        val containerAspect = containerSize.width / containerSize.height
        val matAspect = matWidth / matHeight

        var displayedSize: androidx.compose.ui.geometry.Size
        var displayOffset: Offset

        if (matAspect > containerAspect) {
            val displayedWidth = containerSize.width
            val displayedHeight = displayedWidth / matAspect
            displayedSize = androidx.compose.ui.geometry.Size(displayedWidth, displayedHeight)
            displayOffset = Offset(0f, (containerSize.height - displayedHeight) / 2f)
        } else {
            val displayedHeight = containerSize.height
            val displayedWidth = displayedHeight * matAspect
            displayedSize = androidx.compose.ui.geometry.Size(displayedWidth, displayedHeight)
            displayOffset = Offset((containerSize.width - displayedWidth) / 2f, 0f)
        }

        val scaleX = matWidth / displayedSize.width
        val scaleY = matHeight / displayedSize.height

        val matUpCorner = Offset(
            (upCorner.x - displayOffset.x) * scaleX,
            (upCorner.y - displayOffset.y) * scaleY
        )
        val matDownCorner = Offset(
            (downCorner.x - displayOffset.x) * scaleX,
            (downCorner.y - displayOffset.y) * scaleY
        )

        val x = minOf(matUpCorner.x, matDownCorner.x).toInt()
        val y = minOf(matUpCorner.y, matDownCorner.y).toInt()
        val width = abs(matUpCorner.x - matDownCorner.x).toInt()
        val height = abs(matUpCorner.y - matDownCorner.y).toInt()

        val safeX = x.coerceIn(0, mat.width() - 1)
        val safeY = y.coerceIn(0, mat.height() - 1)
        val safeWidth = width.coerceIn(1, mat.width() - safeX)
        val safeHeight = height.coerceIn(1, mat.height() - safeY)

        val roi = Rect(safeX, safeY, safeWidth, safeHeight)

        if (outer) {
            val result = mat.clone()
            val transparent = Mat.zeros(roi.height, roi.width, result.type())
            transparent.copyTo(result.submat(roi))

            return StudioLayer(result)
        }
        return StudioLayer(mat.clone().submat(roi))
    }

    fun downscaledUniformly(maxDimension: Int = 1000) : StudioLayer {
        return StudioLayer(Filtering.uniformDownscale(mat, maxDimension))
            .also { it.visible = this.visible }
    }

    fun toCvMat(): Mat = mat

    fun applyFilterMap(filters: Map<Filter, Double>): StudioLayer {
        return StudioLayer(mat.applyFilterMap(filters))
    }
}

fun Mat.applyFilterMap(filters: Map<Filter, Double>): Mat {
    var newMat = clone()
    filters.forEach { (filter, factor) ->
        newMat = when (filter) {
            Filter.Contrast -> Filtering.contrast(newMat, factor * 10)
            Filter.Brightness -> Filtering.brightness(newMat, factor * 10)
            Filter.Blur -> Filtering.blur(newMat, factor * 10, factor * 10)
            Filter.LightBalance -> Filtering.lightBalance(newMat, factor * 10)
            Filter.Hue -> Filtering.saturation(newMat, factor * 10)
            Filter.Sharpness -> Filtering.sharpen(newMat, factor * 10)
        }
    }
    return newMat
}