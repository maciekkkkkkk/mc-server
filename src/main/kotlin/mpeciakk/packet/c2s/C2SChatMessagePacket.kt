package mpeciakk.packet.c2s

import mpeciakk.minecraftServer
import mpeciakk.net.Connection
import mpeciakk.packet.PacketByteBuf
import mpeciakk.text.TextComponent

class C2SChatMessagePacket : C2SPacket(0x03) {
    override fun read(connection: Connection, buf: PacketByteBuf) {
        minecraftServer.playerManager.players.forEach { (_, player) ->
            val component = TextComponent()
            // Text message with max length 256
            component.text = buf.readString(256)

            player.sendMessage(component)
        }
    }
}