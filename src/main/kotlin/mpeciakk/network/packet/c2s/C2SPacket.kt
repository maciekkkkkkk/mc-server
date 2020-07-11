package mpeciakk.network.packet.c2s

import mpeciakk.network.Connection
import mpeciakk.network.packet.PacketByteBuf

abstract class C2SPacket(val id: Int) {
    abstract fun read(connection: Connection, buf: PacketByteBuf)
}