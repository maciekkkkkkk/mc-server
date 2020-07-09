package mpeciakk.packet.c2s

import mpeciakk.minecraftServer
import mpeciakk.net.Connection
import mpeciakk.net.SocketState
import mpeciakk.packet.PacketByteBuf
import mpeciakk.packet.s2c.*
import java.util.*


class C2SHandshakePacket : C2SPacket(0x00) {
    override fun read(connection: Connection, buf: PacketByteBuf) {
        if (connection.state == SocketState.HANDSHAKE) {
            // Protocol used by client
            val protocol = buf.readVarInt()

            // Client's ip
            val ip = buf.readString(32767)

            // Client's port
            val port = buf.readShort()

            /*
                Client's connection state
                1 - status
                2 - login
             */
            val state = buf.readVarInt()

            if (state == 1) {
                connection.state = SocketState.STATUS
            } else {
                connection.state = SocketState.LOGIN
            }
        } else if (connection.state == SocketState.STATUS) {
            connection.send(S2CServerListPacket())
        } else if (connection.state == SocketState.LOGIN) {
            // Player's nick name with max length 16
            val nickname = buf.readString(16)

            minecraftServer.playerManager.connectPlayer(connection, nickname)
        } else if (connection.state == SocketState.PLAY) {
            // TODO: identify this packet
        }
    }
}