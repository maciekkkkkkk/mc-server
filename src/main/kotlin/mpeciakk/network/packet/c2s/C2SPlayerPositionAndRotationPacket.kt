package mpeciakk.network.packet.c2s

import mpeciakk.network.Connection
import mpeciakk.network.packet.PacketByteBuf

class C2SPlayerPositionAndRotationPacket : C2SPacket(0x13) {
    override fun read(connection: Connection, buf: PacketByteBuf) {
        // Player x
        val x = buf.readDouble()

        // Player y
        val y = buf.readDouble()

        // Player z
        val z = buf.readDouble()

        // Player yaw
        val yaw = buf.readFloat()

        // Player pitch
        val pitch = buf.readFloat()

        // Is player on ground
        val onGround = buf.readBoolean()

        connection.player.prevPosition.x = connection.player.position.x
        connection.player.prevPosition.y = connection.player.position.y
        connection.player.prevPosition.z = connection.player.position.z
        connection.player.position.x = x
        connection.player.position.y = y
        connection.player.position.z = z
        connection.player.yaw = yaw
        connection.player.pitch = pitch
        connection.player.onGround = onGround

        connection.player.update()
    }
}