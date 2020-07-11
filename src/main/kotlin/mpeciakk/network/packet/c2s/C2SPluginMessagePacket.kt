package mpeciakk.network.packet.c2s

import mpeciakk.network.Connection
import mpeciakk.network.packet.PacketByteBuf

class C2SPluginMessagePacket : C2SPacket(0x0B) {
    override fun read(connection: Connection, buf: PacketByteBuf) {
        // Discard
    }
}