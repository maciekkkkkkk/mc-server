package mpeciakk.network.packet.c2s

import mpeciakk.network.Connection
import mpeciakk.network.packet.PacketByteBuf

class C2SEntityActionPacket : C2SPacket(0x1C) {
    override fun read(connection: Connection, buf: PacketByteBuf) {
        // Player id
        val id = buf.readVarInt()

        /*
            Action id

            0 - Start sneaking
            1 - Stop sneaking
            2 - Leave bed (only when gui button clicked)
            3 - Start sprinting
            4 - Stop sprinting
            5 - Start jump with horse
            6 - Stop jump with horse
            7 - Open horse inventory
            8 - Start flying with elytra
         */
        val action = buf.readVarInt()

        // Jump boost, when action = 5 then 0-100 otherwise 0
        val boost = buf.readVarInt()

        println(action)
    }
}