package mpeciakk.world.gen.flat

import mpeciakk.world.Chunk
import mpeciakk.world.ChunkGenerator

class FlatChunkGenerator : ChunkGenerator() {
    override fun generateChunk(chunk: Chunk) {
        for (x in 0 until 16) {
            for (y in 0 until 16) {
                for (z in 0 until 16) {
                    chunk[x, y, z] = 1
                }
            }
        }
    }

    override fun buildSurface(chunk: Chunk) {

    }
}