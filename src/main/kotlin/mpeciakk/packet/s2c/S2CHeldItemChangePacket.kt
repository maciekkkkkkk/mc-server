package mpeciakk.packet.s2c

import mpeciakk.packet.PacketByteBuf

class S2CHeldItemChangePacket : S2CPacket(0x3F) {
    override fun write(buf: PacketByteBuf) {
        buf.writeByte(0)
    }
}