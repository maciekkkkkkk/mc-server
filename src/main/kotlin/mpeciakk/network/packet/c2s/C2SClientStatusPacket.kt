package mpeciakk.network.packet.c2s

import mpeciakk.network.Connection
import mpeciakk.network.packet.PacketByteBuf

class C2SClientStatusPacket : C2SPacket(0x04) {
    override fun read(connection: Connection, buf: PacketByteBuf) {
        /*
            Client action
            0 - perform respawn
            1 - request stats
         */
        val action = buf.readVarInt()
    }
}