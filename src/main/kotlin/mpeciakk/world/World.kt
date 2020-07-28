package mpeciakk.world

import mpeciakk.world.gen.alpha.AlphaChunkGenerator
import mpeciakk.world.gen.flat.FlatChunkGenerator

class World {
    val chunks = mutableListOf<Chunk>()

    private val generator = FlatChunkGenerator()

    init {
        for (x in 0..8) {
            for (z in 0..8) {
                chunks.add(Chunk(x, z, full = true, ignoreOldData = false))
            }
        }

        for (chunk in chunks) {
            generator.generateChunk(chunk)
            generator.buildSurface(chunk)
        }

        println("world generated")
    }

    fun setBlock(x: Int, y: Int, z: Int, block: Int) {
        val chunk = getChunk(x, z)

        var blockX = x % 16
        var blockZ = z % 16

        if (x < 0) {
            blockX += 16
        }

        if (z < 0) {
            blockZ += 16
        }

        chunk[blockX, y, blockZ] = block
    }

    fun getChunk(x: Int, z: Int): Chunk {
        val chunkX = x shr 4
        val chunkZ = z shr 4

        for (chunk in chunks) {
            if (chunk.x == chunkX && chunk.z == chunkZ) {
                return chunk
            }
        }

        val chunk = Chunk(chunkX, chunkZ, full = true, ignoreOldData = false)
        chunks.add(chunk)

        return chunk
    }
}