package mpeciakk.world

import mpeciakk.nbt.CompoundTag
import mpeciakk.network.packet.PacketByteBuf

class Chunk(val x: Int, val z: Int, val full: Boolean, val ignoreOldData: Boolean) {
    val sections = mutableListOf<ChunkSection?>()

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
                if(section != null && !section.isEmpty()) {
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