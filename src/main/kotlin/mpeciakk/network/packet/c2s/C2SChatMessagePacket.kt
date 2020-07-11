package mpeciakk.network.packet.c2s

import mpeciakk.minecraftServer
import mpeciakk.network.Connection
import mpeciakk.network.packet.PacketByteBuf

class C2SChatMessagePacket : C2SPacket(0x03) {
    override fun read(connection: Connection, buf: PacketByteBuf) {
        // Text message with max length 256
        val text = buf.readString(256)

        minecraftServer.playerManager.chatMessage("Player with nick ${connection.player.nick} (id ${connection.player.id}): $text")
    }
}