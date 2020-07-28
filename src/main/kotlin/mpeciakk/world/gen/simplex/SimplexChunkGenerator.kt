package mpeciakk.world.gen.simplex

import mpeciakk.world.Chunk
import mpeciakk.world.ChunkGenerator

class SimplexChunkGenerator : ChunkGenerator() {
    private val noise = OpenSimplexNoise()

    override fun generateChunk(chunk: Chunk) {
        for (x in 0 until 16) {
            for (y in 0 until 16) {
                for (z in 0 until 16) {
                    chunk[
                            x, ((noise.eval(
                                chunk.x + x.toDouble() / 128,
                                chunk.z + z.toDouble() / 128
                            ) + 1) * 64).toInt(), z
                    ] = 1
                }
            }
        }
    }

    override fun buildSurface(chunk: Chunk) {

    }
}