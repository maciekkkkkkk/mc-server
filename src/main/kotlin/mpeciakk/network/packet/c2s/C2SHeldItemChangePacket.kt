package mpeciakk.network.packet.c2s

import mpeciakk.network.Connection
import mpeciakk.network.packet.PacketByteBuf

class C2SHeldItemChangePacket : C2SPacket(0x24) {
    override fun read(connection: Connection, buf: PacketByteBuf) {
        // New slot
        val slot = buf.readShort()

        connection.player.inventory.selectedSlot = (slot + 36).toShort()
    }
}