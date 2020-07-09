package mpeciakk.packet.c2s

import mpeciakk.net.Connection
import mpeciakk.packet.PacketByteBuf

class C2SPluginMessagePacket : C2SPacket(0x0B) {
    override fun read(connection: Connection, buf: PacketByteBuf) {
        // Discard
    }
}