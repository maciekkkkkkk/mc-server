package mpeciakk

class PingPacket : S2CPacket(0x01) {
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(0)
    }
}