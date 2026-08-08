package com.sil.morphlect

import com.sil.morphlect.logic.CenterWise
import com.sil.morphlect.logic.dbscan
import com.sil.morphlect.logic.imageSegmentKmeans
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

private data class Point2D(val x: Int, val y: Int)

private fun pointToCenterWise(point: Point2D): CenterWise<Point2D> {
    return CenterWise(
        obj = point,
        x = point.x,
        y = point.y
    )
}

@RunWith(JUnit4::class)
class MorphlectUnitTests {
    @Test
    fun addition_isCorrect() {
        Assert.assertEquals(4, 2 + 2)
    }

    @Test
    fun dbscan_emptyListReturnsEmptyMap() {
        val result = dbscan(
            obj = emptyList(),
            eps = 1.0f,
            minPoints = 2,
            centerWiseObjTransform = ::pointToCenterWise
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun dbscan_noisePointsAreExcludedFromResult() {
        val cluster = listOf(
            Point2D(0, 0),
            Point2D(5, 5),
            Point2D(2, 8)
        )
        val noise = Point2D(500, 500)
        val points = cluster + noise

        val result = dbscan(points, eps = 10f, minPoints = 2, ::pointToCenterWise)

        assertEquals(1, result.size)
        assertEquals(3, result[0]?.size)
        assertFalse(result[0]?.contains(noise) == true)
    }

    @Test
    fun dbscan_minPointsParameterAffectsClustering() {
        val points = listOf(
            Point2D(0, 0),
            Point2D(1, 1),
            Point2D(2, 2)
        )

        val result2 = dbscan(points, eps = 1.5f, minPoints = 2, ::pointToCenterWise)
        assertEquals(1, result2.size)

        val result4 = dbscan(points, eps = 1.5f, minPoints = 4, ::pointToCenterWise)
        assertEquals(0, result4.size)
    }

    @Test
    fun dbScan_epsParameterAffectsClusterSize() {
        val points = listOf(
            Point2D(0, 0),
            Point2D(5, 5),
            Point2D(10, 10)
        )

        val resultSmall = dbscan(points, eps = 1f, minPoints = 2, ::pointToCenterWise)
        assertEquals(0, resultSmall.size)

        val resultLarge = dbscan(points, eps = 8f, minPoints = 2, ::pointToCenterWise)
        assertEquals(1, resultLarge.size)
        assertEquals(3, resultLarge[0]?.size)
    }

    data class TestBox(val id: Int, val x: Int, val y: Int, val width: Int, val height: Int) {
        val centerX: Int get() = x + width / 2
        val centerY: Int get() = y + height / 2
    }

    @Test
    fun kMeans_emptyListReturnsEmptyMap() {
        val result = imageSegmentKmeans(
            objs = emptyList<TestBox>(),
            imageWidth = 100,
            imageHeight = 100,
            centerSelector = { it.centerX to it.centerY }
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun kMeans_singleElementAssignedToCluster() {
        val box = TestBox(1, 10, 10, 20, 20)
        val result = imageSegmentKmeans(
            objs = listOf(box),
            imageWidth = 100,
            imageHeight = 100,
            cols = 3,
            rows = 3,
            centerSelector = { it.centerX to it.centerY }
        )

        assertEquals(1, result.size)
        assertEquals(1, result.values.flatten().size)
        assertTrue(result.values.flatten().contains(box))
    }

    @Test
    fun kMeans_noElementsLostDuringClustering() {
        val boxes = (0 until 50).map {
            TestBox(it, (0..100).random(), (0..100).random(), 10, 10)
        }

        val result = imageSegmentKmeans(
            objs = boxes,
            imageWidth = 100,
            imageHeight = 100,
            cols = 3,
            rows = 3,
            iterations = 15,
            centerSelector = { it.centerX to it.centerY }
        )

        val allClusteredElements = result.values.flatten()
        assertEquals(boxes.size, allClusteredElements.size)
        assertTrue(allClusteredElements.containsAll(boxes))
    }

    @Test
    fun kMeans_imageDimensionsAffectInitialCentroids() {
        val boxes = (0 until 20).map {
            TestBox(it, (0..50).random(), (0..50).random(), 5, 5)
        }

        val resultSmall = imageSegmentKmeans(
            objs = boxes,
            imageWidth = 50,
            imageHeight = 50,
            cols = 2,
            rows = 2,
            centerSelector = { it.centerX to it.centerY }
        )

        val resultLarge = imageSegmentKmeans(
            objs = boxes,
            imageWidth = 500,
            imageHeight = 500,
            cols = 2,
            rows = 2,
            centerSelector = { it.centerX to it.centerY }
        )

        assertEquals(boxes.size, resultSmall.values.flatten().size)
        assertEquals(boxes.size, resultLarge.values.flatten().size)
    }
}