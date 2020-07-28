package mpeciakk.network.packet.s2c

import mpeciakk.network.packet.PacketByteBuf

class S2CEntityRotationPacket(
    private val entityId: Int,
    private val yaw: Float,
    private val pitch: Float,
    private val onGround: Boolean
) : S2CPacket(0x2A) {
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(entityId)
        buf.writeAngle(yaw)
        buf.writeAngle(pitch)
        buf.writeBoolean(onGround)
    }
}