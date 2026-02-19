package com.sil.morphlect.data

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.datastore.core.Closeable
import com.sil.morphlect.logic.FormatConverters
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size

class EditorLayer(var name: String, private val mat: Mat) : Closeable {
    companion object {
        fun emptyNamed(name: String) : EditorLayer {
            return EditorLayer(name, Mat(300, 300, CvType.CV_8UC3))
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
        return TODO()
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
