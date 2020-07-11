package mpeciakk.network.packet.c2s

import mpeciakk.network.Connection
import mpeciakk.network.packet.PacketByteBuf

class C2SPlayerAbilitiesPacket : C2SPacket(0x1A) {
    override fun read(connection: Connection, buf: PacketByteBuf) {

    }
}