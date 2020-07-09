package mpeciakk.packet.c2s

import mpeciakk.net.Connection
import mpeciakk.packet.PacketByteBuf

class C2SPlayerPositionPacket : C2SPacket(0x12) {
    override fun read(connection: Connection, buf: PacketByteBuf) {
        // Player x
        val x = buf.readDouble()

        // Player y
        val y = buf.readDouble()

        // Player z
        val z = buf.readDouble()

        // Is player on ground
        val onGround = buf.readBoolean()

        connection.player.position.x = x
        connection.player.position.y = y
        connection.player.position.z = z
        connection.player.onGround = onGround
    }
}