package mpeciakk.network.packet.c2s

import mpeciakk.network.Connection
import mpeciakk.network.packet.PacketByteBuf

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

        connection.player.prevPosition.x = connection.player.position.x
        connection.player.prevPosition.y = connection.player.position.y
        connection.player.prevPosition.z = connection.player.position.z
        connection.player.position.x = x
        connection.player.position.y = y
        connection.player.position.z = z
        connection.player.onGround = onGround
    }
}