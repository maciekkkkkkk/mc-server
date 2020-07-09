package mpeciakk.packet.c2s

import mpeciakk.net.Connection
import mpeciakk.packet.PacketByteBuf

class C2SKeepAlivePacket : C2SPacket(0x10) {
    override fun read(connection: Connection, buf: PacketByteBuf) {
        // TODO: find out what's this
        val id = buf.readLong()
    }
}