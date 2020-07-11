package mpeciakk.network.packet.s2c

import mpeciakk.network.packet.PacketByteBuf

class S2CPlayerPositionAndLookPacket : S2CPacket(0x35) {
    override fun write(buf: PacketByteBuf) {
        buf.writeDouble(1.0)
        buf.writeDouble(20.0)
        buf.writeDouble(1.0)
        buf.writeFloat(1.0f)
        buf.writeFloat(1.0f)
        buf.writeByte(0)
        buf.writeVarInt(1)
    }
}