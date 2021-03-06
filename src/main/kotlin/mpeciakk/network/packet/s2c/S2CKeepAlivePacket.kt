package mpeciakk.network.packet.s2c

import mpeciakk.network.packet.PacketByteBuf

class S2CKeepAlivePacket(private val data: Long) : S2CPacket(0x20) {
    override fun write(buf: PacketByteBuf) {
        buf.writeLong(data)
    }
}