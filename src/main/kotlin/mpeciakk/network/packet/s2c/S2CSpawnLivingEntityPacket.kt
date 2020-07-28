package mpeciakk.network.packet.s2c

import mpeciakk.math.Vector3d
import mpeciakk.network.packet.PacketByteBuf
import java.util.*

class S2CSpawnLivingEntityPacket(
    private val entityId: Int,
    private val entityUUID: UUID,
    private val type: Int,
    private val position: Vector3d,
    private val yaw: Float,
    private val pitch: Float,
    private val headPitch: Float,
    private val velocityX: Short,
    private val velocityY: Short,
    private val velocityZ: Short
) : S2CPacket(0x02) {
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(entityId)
        buf.writeUUID(entityUUID)
        buf.writeVarInt(type)
        buf.writeDouble(position.x)
        buf.writeDouble(position.y)
        buf.writeDouble(position.z)

        buf.writeAngle(pitch)
        buf.writeAngle(yaw)
        buf.writeAngle(headPitch)

        buf.writeShort(velocityX.toInt())
        buf.writeShort(velocityY.toInt())
        buf.writeShort(velocityZ.toInt())
    }
}