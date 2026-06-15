package com.sil.morphlect

import androidx.compose.ui.geometry.Offset
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sil.morphlect.command.StudioCommand
import com.sil.morphlect.command.StudioCommandManager
import com.sil.morphlect.command.impl.ContrastCommand
import com.sil.morphlect.enums.Filter
import com.sil.morphlect.exception.CommandException
import com.sil.morphlect.extension.extend
import com.sil.morphlect.layerwork.LayerManager
import com.sil.morphlect.layerwork.StudioLayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size

class MockCommandManager : StudioCommandManager {
    override var undoStack: List<StudioCommand> = listOf()
    override var redoStack: List<StudioCommand> = listOf()
    override fun runCommand(command: StudioCommand) = Unit
    override fun undoLastCommand() = Unit
    override fun redoLastCommand() = Unit
}

class MockLayerManager {
    val manager = LayerManager()
}

@RunWith(AndroidJUnit4::class)
class MorphlectEssentialTestSuite {
    companion object {
        @BeforeClass @JvmStatic
        fun loadOpenCv() {
            check(OpenCVLoader.initLocal()) { "OpenCV native libs failed to load" }
        }
    }
    
    @Test
    fun properlyFormattedFactoryParamsOutputACommand() {
        val command = StudioCommand.of(Filter.Contrast, 50.0)
        assert(command.javaClass == ContrastCommand::class.java)
    }

    @Test
    fun zeroFactorArgsFactoryCallThrows() {
        assertThrows(CommandException::class.java) {
            val invalidComm = StudioCommand.of(Filter.Contrast)
        }
    }

    @Test
    fun noChangeFromDeleteOnCommandManagerEmptyStacks() {
        val manager = MockCommandManager()
        assert(manager.undoStack.isEmpty() && manager.redoStack.isEmpty())
        manager.undoStack -= StudioCommand.of(Filter.Contrast, 0.0)
        manager.redoStack -= StudioCommand.of(Filter.Contrast, 0.0)
        assert(manager.undoStack.isEmpty() && manager.redoStack.isEmpty())
    }

    @Test
    fun commandManagerAllowsArbitraryBoundedStackIndex() {
        val mockComms = listOf(
            StudioCommand.of(Filter.Contrast, 10.0),
            StudioCommand.of(Filter.Brightness, 20.0),
            StudioCommand.of(Filter.Blur, 30.0),
        )

        val manager = MockCommandManager().apply {
            undoStack = mockComms.toList()
            redoStack = mockComms.toList()
        }

        assert(manager.undoStack[1] != manager.undoStack[2] && manager.redoStack[2] != manager.redoStack[0])
    }

    @Test
    fun noChangeFromDeleteOnCommandManagerInvalidStackIndex() {
        val mockComms = listOf(
            StudioCommand.of(Filter.Contrast, 10.0),
            StudioCommand.of(Filter.Brightness, 20.0),
            StudioCommand.of(Filter.Blur, 30.0),
        )

        val manager = MockCommandManager().apply {
            undoStack = mockComms.toList()
            redoStack = mockComms.toList()
        }

        assertThrows(IndexOutOfBoundsException::class.java) {
            manager.undoStack -= manager.undoStack[6_954_876]
        }
        assertThrows(IndexOutOfBoundsException::class.java) {
            manager.redoStack -= manager.redoStack[-1]
        }
        assert(manager.undoStack.size == 3 && manager.redoStack.size == 3)
    }

    @Test
    fun matExtendMethodExtendsToGivenSize() {
        val mat = Mat(300, 300, CvType.CV_8UC4)
        val extended = mat.extend(Size(800.0, 800.0))
        assert(extended.width() == 800 && extended.height() == 800)
    }

    @Test
    fun noChangeFromDeleteOnEmptyLayerStack() {
        val wrapper = MockLayerManager()
        assert(wrapper.manager.layers.isEmpty())

        wrapper.manager.removeLayer(0)
        assert(wrapper.manager.layers.isEmpty())
        wrapper.manager.close()
    }

    @Test
    fun layerManagerAllowsArbitraryBoundedLayerStackIndex() {
        val wrapper = MockLayerManager()
        wrapper.manager.apply {
            addLayer(StudioLayer(Mat(100, 200, CvType.CV_8UC4)))
            addLayer(StudioLayer(Mat(300, 400, CvType.CV_8UC4)))
            addLayer(StudioLayer(Mat(500, 600, CvType.CV_8UC4)))
        }
        assert(wrapper.manager.layers[2].javaClass == StudioLayer::class.java)
        wrapper.manager.close()
    }

    @Test
    fun mergingLayersReturnsProperlySizedLayer() {
        val wrapper = MockLayerManager()
        wrapper.manager.apply {
            addLayer(StudioLayer(Mat(200, 100, CvType.CV_8UC4)))
            addLayer(StudioLayer(Mat(100, 200, CvType.CV_8UC4)))
        }
        wrapper.manager.mergeLayerWithAbove(1)
        val mat = wrapper.manager.layers[0].mat
        assert(mat.width() == 200 && mat.height() == 200)
        wrapper.manager.close()
    }

    @Test
    fun cloningLayersReturnsANewInstance() {
        val layer = StudioLayer.empty()
        val clone = layer.clone()
        clone.visible = false
        assert(layer != clone)
    }

    @Test
    fun properCropping() {
        val layer = StudioLayer(Mat(Size(1000.0, 1000.0), CvType.CV_8UC4))
        val cropped = layer.cropped(
            upCorner = Offset(0f, 0f),
            downCorner = Offset(500f, 500f),
            containerSize = androidx.compose.ui.geometry.Size(1000f, 1000f),
            outer = false
        )
        assert(cropped.mat.width() == 500 && cropped.mat.height() == 500)
        layer.close()
        cropped.close()
    }

    @Test
    fun properUniformDownscaling() {
        val bigLayer = StudioLayer(Mat(Size(1000.0, 1000.0), CvType.CV_8UC4))
        val uniformDownscaled = bigLayer.downscaledUniformly()
        assert(uniformDownscaled.mat.width() == 1000 || uniformDownscaled.mat.height() == 1000)
        bigLayer.close()
        uniformDownscaled.close()
    }

    @Test
    fun addingLayersScalesAllLayersAccordingly() {
        val layers = listOf(
            StudioLayer(Mat(Size(300.0, 400.0), CvType.CV_8UC4)),
            StudioLayer(Mat(Size(100.0, 200.0), CvType.CV_8UC4)),
            StudioLayer(Mat(Size(600.0, 150.0), CvType.CV_8UC4))
        )
        val manager = LayerManager()
        layers.forEach { manager.addLayer(it) }
        assert(manager.layers.size == 3)
        assert(
            (layers[0].width == 600 && layers[0].height == 400) &&
            (layers[1].width == 600 && layers[1].height == 400) &&
            (layers[2].width == 600 && layers[2].height == 400)
        )
    }

    @Test
    fun layersInterchangeAccordingly() {
        val layers = listOf(
            StudioLayer(Mat(Size(300.0, 400.0), CvType.CV_8UC4), "A", ),
            StudioLayer(Mat(Size(100.0, 200.0), CvType.CV_8UC4), "B" ),
            StudioLayer(Mat(Size(600.0, 150.0), CvType.CV_8UC4), "C",)
        )
        val manager = LayerManager()
        layers.forEach { manager.addLayer(it) }
        assert(manager.layers.size == 3)
        manager.interchangeLayers(0, 1)
        assert(
            (manager.layers[0].tag == "B") &&
            (manager.layers[1].tag == "A")
        )
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.sil.morphlect", appContext.packageName)
    }
}