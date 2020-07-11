package mpeciakk.network.packet.s2c

import mpeciakk.network.packet.PacketByteBuf

class S2CEntityPositionPacket(
    private val id: Int,
    private val deltaX: Int,
    private val deltaY: Int,
    private val deltaZ: Int,
    private val onGround: Boolean
) : S2CPacket(0x28) {
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(id)
        buf.writeShort(deltaX)
        buf.writeShort(deltaY)
        buf.writeShort(deltaZ)
        buf.writeBoolean(onGround)
    }
}