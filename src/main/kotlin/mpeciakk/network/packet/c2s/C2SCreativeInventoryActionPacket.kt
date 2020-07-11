package mpeciakk.network.packet.c2s

import mpeciakk.network.Connection
import mpeciakk.network.packet.PacketByteBuf

class C2SCreativeInventoryActionPacket : C2SPacket(0x27) {
    override fun read(connection: Connection, buf: PacketByteBuf) {
        // Clicked slot id
        val slot = buf.readShort()

        // Clicked item
        val item = buf.readItemData()

        if (item.present) {
            connection.player.inventory[slot] = item.id!!
        } else {
            connection.player.inventory[slot] = 0
        }
    }
}