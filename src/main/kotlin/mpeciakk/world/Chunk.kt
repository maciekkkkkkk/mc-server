package mpeciakk.world

import mpeciakk.math.Vector3i
import mpeciakk.nbt.CompoundTag
import mpeciakk.network.packet.PacketByteBuf
import kotlin.math.abs

class Chunk(val x: Int, val z: Int, val full: Boolean, val ignoreOldData: Boolean) {
    val sections = arrayOfNulls<ChunkSection?>(16)

    fun getSection(y: Int): ChunkSection {
        if (y >= sections.size) {
            val section = ChunkSection()
            sections[y] = section

            return section
        }

        var section = sections[y]

        if (section == null) {
            section = ChunkSection()
            sections[y] = section
        }

        return section
    }

    operator fun set(x: Int, y: Int, z: Int, id: Int) {
        val section = getSection(y / 16)

        section[abs(x), y % 16, abs(z)] = id
    }

    operator fun set(pos: Vector3i, id: Int) {
        set(pos.x, pos.y, pos.z, id)
    }

    operator fun get(x: Int, y: Int, z: Int): Int {
        val section = getSection(y / 16)

        return section[x, y, z]
    }

    operator fun get(pos: Vector3i): Int {
        return get(pos.x, pos.y, pos.z)
    }

    companion object {
        fun write(chunk: Chunk, buf: PacketByteBuf) {
            buf.writeInt(chunk.x)
            buf.writeInt(chunk.z)
            buf.writeBoolean(chunk.full)
            buf.writeBoolean(chunk.ignoreOldData)

            val sectionsBuf = PacketByteBuf()
            var mask = 0
            for (index in chunk.sections.indices) {
                val section = chunk.sections[index]
                if (section != null && !section.isEmpty()) {
                    mask = mask or (1 shl index)
                    ChunkSection.write(section, sectionsBuf)
                }
            }

            buf.writeVarInt(mask)

            buf.writeTag(CompoundTag())

            for (i in 0 until 1024) {
                buf.writeInt(127)
            }

            buf.writeVarInt(sectionsBuf.readableBytes())
            buf.writeBytes(sectionsBuf)

            buf.writeVarInt(0)
        }
    }
}