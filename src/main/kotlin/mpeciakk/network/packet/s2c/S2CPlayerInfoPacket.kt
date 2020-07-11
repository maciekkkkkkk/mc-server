package mpeciakk.network.packet.s2c

import mpeciakk.network.packet.PacketByteBuf
import java.util.*

// TODO: make this be able to send multiple player information
class S2CPlayerInfoPacket(private val uuid: UUID, private val name: String) : S2CPacket(0x33) {
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(0)
        buf.writeVarInt(1)
        buf.writeUUID(uuid)
        buf.writeString(name)
        buf.writeVarInt(0)
        buf.writeVarInt(1)
        buf.writeVarInt(1)
        buf.writeBoolean(false)
    }
}