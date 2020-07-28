package mpeciakk.network.packet.s2c

import mpeciakk.math.Vector3d
import mpeciakk.network.packet.PacketByteBuf

class S2CPlayerPositionAndLookPacket(
    private val position: Vector3d,
    private val yaw: Float,
    private val pitch: Float,
    private val teleportId: Int
) : S2CPacket(0x35) {
    override fun write(buf: PacketByteBuf) {
        buf.writeDouble(position.x)
        buf.writeDouble(position.y)
        buf.writeDouble(position.z)
        buf.writeFloat(yaw)
        buf.writeFloat(pitch)
        buf.writeByte(0)
        buf.writeVarInt(teleportId)
    }
}