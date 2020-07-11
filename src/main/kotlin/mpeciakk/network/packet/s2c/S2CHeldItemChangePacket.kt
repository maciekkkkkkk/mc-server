package mpeciakk.network.packet.s2c

import mpeciakk.network.packet.PacketByteBuf

class S2CHeldItemChangePacket : S2CPacket(0x3F) {
    override fun write(buf: PacketByteBuf) {
        buf.writeByte(0)
    }
}