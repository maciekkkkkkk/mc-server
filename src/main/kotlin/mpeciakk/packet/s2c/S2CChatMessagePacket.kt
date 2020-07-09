package mpeciakk.packet.s2c

import mpeciakk.packet.PacketByteBuf
import java.util.*

class S2CChatMessagePacket(private val component: String, private val position: Int) : S2CPacket(0x0E) {
    override fun write(buf: PacketByteBuf) {
        buf.writeString(component)
        buf.writeByte(position)
        buf.writeUUID(UUID.randomUUID())
    }
}