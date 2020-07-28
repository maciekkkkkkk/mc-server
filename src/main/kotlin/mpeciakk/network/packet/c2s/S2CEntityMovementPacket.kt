package mpeciakk.network.packet.c2s

import mpeciakk.network.packet.PacketByteBuf
import mpeciakk.network.packet.s2c.S2CPacket

class S2CEntityMovementPacket(private val entityId: Int) : S2CPacket(0x2B) {
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(entityId)
    }
}