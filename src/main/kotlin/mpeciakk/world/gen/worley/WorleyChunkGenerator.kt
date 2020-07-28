package mpeciakk.world.gen.worley

import mpeciakk.math.Vector3i
import mpeciakk.world.Chunk
import mpeciakk.world.ChunkGenerator
import java.util.*

class WorleyChunkGenerator : ChunkGenerator() {
    private val points = arrayOfNulls<Vector3i>(500)
    private val random = Random()

    init {
        for (i in points.indices) {
            points[i] = Vector3i(random.nextInt(256), random.nextInt(256), random.nextInt(256))
        }
    }

    override fun generateChunk(chunk: Chunk) {
        val distances = FloatArray(points.size)

        for (x in 0 until 16) {
            for (y in 0 until 16) {
                for (z in 0 until 16) {
                    for (i in points.indices) {
                        distances[i] = points[i]!!.distance(Vector3i(x, y, z)).toFloat()
                    }

                    val n = 0
                    distances.sort()
                    val noise = distances[n]

                    if (noise > 15) {
                        chunk[x, y, z] = 1
                    }
                }
            }
        }
    }

    override fun buildSurface(chunk: Chunk) {

    }
}