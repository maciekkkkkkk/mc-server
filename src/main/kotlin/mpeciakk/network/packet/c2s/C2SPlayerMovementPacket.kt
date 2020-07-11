package mpeciakk.network.packet.c2s

import mpeciakk.network.Connection
import mpeciakk.network.packet.PacketByteBuf

class C2SPlayerMovementPacket : C2SPacket(0x15) {
    override fun read(connection: Connection, buf: PacketByteBuf) {
        // Is player on ground
        val onGround = buf.readBoolean()

        connection.player.onGround = onGround
    }
}