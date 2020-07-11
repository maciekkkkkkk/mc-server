package mpeciakk.network.packet.s2c

import mpeciakk.network.packet.PacketByteBuf
import mpeciakk.world.Chunk

class S2CChunkDataPacket(private val chunk: Chunk) : S2CPacket(0x21) {
    override fun write(buf: PacketByteBuf) {
        Chunk.write(chunk, buf)
    }
}