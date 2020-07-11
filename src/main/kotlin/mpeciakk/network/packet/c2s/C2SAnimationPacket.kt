package mpeciakk.network.packet.c2s

import mpeciakk.network.Connection
import mpeciakk.network.packet.PacketByteBuf

class C2SAnimationPacket : C2SPacket(0x2B) {
    override fun read(connection: Connection, buf: PacketByteBuf) {
        // Used hand
        val hand = buf.readVarInt()
    }
}