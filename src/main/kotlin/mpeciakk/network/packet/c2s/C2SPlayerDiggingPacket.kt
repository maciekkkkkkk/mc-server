package mpeciakk.network.packet.c2s

import mpeciakk.entity.ArrowEntity
import mpeciakk.minecraftServer
import mpeciakk.network.Connection
import mpeciakk.network.packet.PacketByteBuf
import mpeciakk.network.packet.s2c.S2CBlockChangePacket

class C2SPlayerDiggingPacket : C2SPacket(0x1B) {
    override fun read(connection: Connection, buf: PacketByteBuf) {
        /*
            Action
            0 - started digging
            1 - cancelled digging
            2 - finished digging
            3 - drop item stack
            4 - drop item
            5 - shoot arrow/finish eating
            6 - swap item in hand
         */
        val action = buf.readVarInt()

        // Location
        val position = buf.readPosition()

        /*
            Face
            0 - bottom
            1 - top
            2 - north
            3 - south
            4 - west
            5 - east
         */
        val face = buf.readByte()

        if (action == 0) {
            Connection.sendToAll(S2CBlockChangePacket(position, 0))
        }

        if (action == 5) {
            val entity = ArrowEntity(connection.player.id)
            entity.position.x = connection.player.position.x
            entity.position.y = connection.player.position.y
            entity.position.z = connection.player.position.z
            minecraftServer.entityManager.spawn(entity)
        }
    }
}