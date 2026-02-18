package com.sil.morphlect.data

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.sil.morphlect.logic.FormatConverters
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size

class EditorLayer(private val mat: Mat, var name: String) {
    companion object {
        fun emptyNamed(name: String) : EditorLayer {
            return EditorLayer(Mat(300, 300, CvType.CV_8UC3), name)
        }
    }

    /**
     * lazy loaded visual representation of the layer.
    */
    val visual by lazy { FormatConverters.matToBitmap(mat).asImageBitmap() }

    fun mergeWith(other: EditorLayer): EditorLayer {
        return TODO()
    }
}