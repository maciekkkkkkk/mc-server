package mpeciakk

import io.netty.channel.ChannelHandlerContext

class HandshakePacket : C2SPacket() {
    override fun handle(connection: Connection, buf: PacketByteBuf) {
        val protocol = buf.readVarInt()
        val ip = buf.readString(32767)
        val port = buf.readShort()
        val state = buf.readVarInt()

        if (state == 1) {
            connection.write(ServerListPacket())
        }
    }
}