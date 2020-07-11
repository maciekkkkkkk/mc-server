package mpeciakk.network.packet.s2c

import mpeciakk.network.packet.PacketByteBuf
import java.util.*

class S2CLoginSuccess(private val uuid: UUID, private val username: String) : S2CPacket(0x02) {
    override fun write(buf: PacketByteBuf) {
        buf.writeUUID(uuid)
        buf.writeString(username)
    }
}