package mpeciakk.packet.c2s

import mpeciakk.net.Connection
import mpeciakk.packet.PacketByteBuf

abstract class C2SPacket(val id: Int) {
    abstract fun read(connection: Connection, buf: PacketByteBuf)
}