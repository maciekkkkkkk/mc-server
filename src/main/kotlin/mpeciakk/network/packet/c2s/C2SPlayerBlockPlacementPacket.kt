package mpeciakk.network.packet.c2s

import mpeciakk.math.Vector3i
import mpeciakk.network.Connection
import mpeciakk.network.packet.PacketByteBuf
import mpeciakk.network.packet.s2c.S2CBlockChangePacket
import mpeciakk.network.packet.s2c.S2CSpawnLivingEntityPacket
import java.util.*

class C2SPlayerBlockPlacementPacket : C2SPacket(0x2D) {
    override fun read(connection: Connection, buf: PacketByteBuf) {
        /*
            Hand
            0 - main hand
            1 - off hand
         */
        val hand = buf.readVarInt()

        // Block location
        val location = buf.readPosition()

        /*
            Face
            0 - bottom
            1 - top
            2 - north
            3 - south
            4 - west
            5 - east
         */
        val face = buf.readVarInt()

        // Cursor position x
        val cursorX = buf.readFloat()

        // Cursor position y
        val cursorY = buf.readFloat()

        // Cursor position z
        val cursorZ = buf.readFloat()

        // Is player head inside of block
        val insideBlock = buf.readBoolean()

        connection.player.inventory.selectedItem?.let {
            when (face) {
                0 -> {
                    location.y -= 1
                }
                1 -> {
                    location.y += 1
                }
                2 -> {
                    location.z -= 1
                }
                3 -> {
                    location.z += 1
                }
                4 -> {
                    location.x -= 1
                }
                5 -> {
                    location.x += 1
                }
            }

            Connection.sendToAll(S2CBlockChangePacket(location, it))
        }
    }
}