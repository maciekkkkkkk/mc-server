package mpeciakk.network.packet.s2c

import mpeciakk.network.packet.PacketByteBuf

class S2CEntityStatusPacket : S2CPacket(0x1B) {
    override fun write(buf: PacketByteBuf) {
        buf.writeInt(0)
        buf.writeByte(23)
    }
}