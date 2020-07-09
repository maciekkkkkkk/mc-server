package mpeciakk.packet.s2c

import mpeciakk.packet.PacketByteBuf

class S2CPingPacket(private val data: Long) : S2CPacket(0x01) {
    override fun write(buf: PacketByteBuf) {
        buf.writeLong(data)
    }
}