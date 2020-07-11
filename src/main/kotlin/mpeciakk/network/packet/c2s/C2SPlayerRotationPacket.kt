package mpeciakk.network.packet.c2s

import mpeciakk.network.Connection
import mpeciakk.network.packet.PacketByteBuf

class C2SPlayerRotationPacket : C2SPacket(0x14) {
    override fun read(connection: Connection, buf: PacketByteBuf) {
        // Player yaw
        val yaw = buf.readFloat()

        // Player pitch
        val pitch = buf.readFloat()

        // Is player on ground
        val onGround = buf.readBoolean()

        connection.player.yaw = yaw
        connection.player.pitch = pitch
        connection.player.onGround = onGround
    }
}