package com.sil.morphlect.logic

import kotlin.math.sqrt

/**
 * helper class that packs `obj` alongside its center given by (`x`, `y`)
 */
data class CenterWise<T>(val x: Int, val y: Int, val obj: T)

/**
 * the type of clustering applied to the algorithm - either `K-means` or `DBSCAN`
 */
enum class ClusteringType { Kmeans, DBSCAN }

// approach 1 - using DBSCAN -> O(n^2) complexity
// may run slower on lots of boxes but clusters the best
fun<T> neighborsOfObject(p: CenterWise<T>, points: List<CenterWise<T>>, eps: Float) = points.filter { q->
    val dx = p.x - q.x
    val dy = p.y - q.y
    sqrt((dx * dx + dy * dy).toDouble()) <= eps
}

fun<T> dbscan(
    obj: List<T>,
    eps: Float,
    minPoints: Int = 2,
    centerWiseObjTransform: (T) -> CenterWise<T>
): Map<Int, List<T>> {
    val points = obj.map(centerWiseObjTransform)
    val labels = mutableMapOf<CenterWise<T>, Int>() // -1 = noise, 0+ = cluster id
    var clusterId = 0

    for (point in points) {
        if (point in labels) continue

        val n = neighborsOfObject(point, points, eps)
        if (n.size < minPoints) {
            labels[point] = -1 // noise
            continue
        }

        val queue = ArrayDeque(n)
        labels[point] = clusterId

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (labels[current] == -1) labels[current] = clusterId
            if (current in labels) continue
            labels[current] = clusterId
            val currentNeighbors = neighborsOfObject(current, points, eps)
            if (currentNeighbors.size >= minPoints) queue.addAll(currentNeighbors)
        }

        clusterId++
    }

    return labels
        .filter { it.value >= 0 } // discard noise
        .entries
        .groupBy({ it.value }, { it.key.obj })
}

// approach 2 -> using k-means with initial clusters set as segments of the image -> O(n*9k) ~ O(n)
// runs faster on a lot of elements but clustering is not as great
fun<T> imageSegmentKmeans(
    objs: List<T>,
    imageWidth: Int,
    imageHeight: Int,
    cols: Int = 3,
    rows: Int = 3,
    iterations: Int = 10,
    centerSelector: (T) -> Pair<Int, Int>
): Map<Int, List<T>> {
    if (objs.isEmpty()) return emptyMap()

    val cellW = imageWidth / cols.toFloat()
    val cellH = imageHeight / rows.toFloat()

    // initial centroids = center of each grid cell
    val centroids = (0 until rows).flatMap { row ->
        (0 until cols).map { col ->
            floatArrayOf(
                col * cellW + cellW / 2,
                row * cellH + cellH / 2
            )
        }
    }.toMutableList()

    val assignments = IntArray(objs.size)

    for (_i in  0..iterations) {
        // assign each box to nearest centroid
        objs.forEachIndexed { i, obj ->
            val (cx, cy) = centerSelector(obj)
            assignments[i] = centroids.indices.minByOrNull { j ->
                val dx = cx - centroids[j][0]
                val dy = cy - centroids[j][1]
                dx * dx + dy * dy
            } ?: 0
        }

        // update centroids to mean of assigned boxes
        centroids.indices.forEach { j ->
            val cluster = objs.filterIndexed { i, _ -> assignments[i] == j }
            if (cluster.isNotEmpty()) {
                centroids[j][0] = cluster.map { centerSelector(it).first }.average().toFloat()
                centroids[j][1] = cluster.map { centerSelector(it).second }.average().toFloat()
            }
        }
    }

    return objs.indices.groupBy({ assignments[it] }, { objs[it] })
}