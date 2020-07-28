package mpeciakk.network.packet.c2s

import mpeciakk.network.Connection
import mpeciakk.network.packet.PacketByteBuf

class C2SUseItemPacket : C2SPacket(0x2E) {
    override fun read(connection: Connection, buf: PacketByteBuf) {
        /*
            Hand
            0 - main hand
            1 - off hand
         */
        val hand = buf.readVarInt()
    }
}