package mpeciakk.packet.c2s

import mpeciakk.net.Connection
import mpeciakk.packet.PacketByteBuf

class C2SPlayerMovementPacket : C2SPacket(0x15) {
    override fun read(connection: Connection, buf: PacketByteBuf) {
        // Is player on ground
        val onGround = buf.readBoolean()

        connection.player.onGround = onGround
    }
}