package mpeciakk.world

abstract class ChunkGenerator {
    abstract fun generateChunk(chunk: Chunk)
    abstract fun buildSurface(chunk: Chunk)
}