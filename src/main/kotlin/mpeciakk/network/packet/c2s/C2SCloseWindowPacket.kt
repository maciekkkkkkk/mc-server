package mpeciakk.network.packet.c2s

import mpeciakk.network.Connection
import mpeciakk.network.packet.PacketByteBuf

class C2SCloseWindowPacket : C2SPacket(0x0A) {
    override fun read(connection: Connection, buf: PacketByteBuf) {
        // Closed window id
        val id = buf.readByte()
    }
}