package mpeciakk.network.packet.c2s

import mpeciakk.network.Connection
import mpeciakk.network.packet.PacketByteBuf

class C2SKeepAlivePacket : C2SPacket(0x10) {
    override fun read(connection: Connection, buf: PacketByteBuf) {
        // Same as in S2CKeepAlivePacket
        val id = buf.readLong()
    }
}