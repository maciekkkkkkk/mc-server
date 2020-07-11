package mpeciakk.network.packet.s2c

import mpeciakk.math.Vector3i
import mpeciakk.network.packet.PacketByteBuf

class S2CBlockChangePacket(private val position: Vector3i, private val id: Int) : S2CPacket(0x0B) {
    override fun write(buf: PacketByteBuf) {
        buf.writePosition(position)
        buf.writeVarInt(id)
    }
}