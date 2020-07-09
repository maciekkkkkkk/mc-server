package mpeciakk.packet.c2s

import mpeciakk.net.Connection
import mpeciakk.net.SocketState
import mpeciakk.packet.PacketByteBuf
import mpeciakk.packet.s2c.S2CPingPacket

class C2SPingPacket : C2SPacket(0x01) {
    override fun read(connection: Connection, buf: PacketByteBuf) {
        // Some payload, don't matter
        val payload = buf.readLong()
        connection.send(S2CPingPacket(payload))
    }
}