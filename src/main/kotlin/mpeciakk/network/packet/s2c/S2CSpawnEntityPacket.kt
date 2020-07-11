package mpeciakk.network.packet.s2c

import mpeciakk.math.Vector3d
import mpeciakk.network.packet.PacketByteBuf
import java.util.*

class S2CSpawnEntityPacket(
    private val entityId: Int,
    private val uuid: UUID,
    private val type: Int,
    private val position: Vector3d,
    private val pitch: Float,
    private val yaw: Float,
    private val data: Int,
    private val velocityX: Int,
    private val velocityY: Int,
    private val velocityZ: Int
) : S2CPacket(0x00) {
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(entityId)
        buf.writeUUID(uuid)
        buf.writeVarInt(type)
        buf.writeDouble(position.x)
        buf.writeDouble(position.y)
        buf.writeDouble(position.z)
        buf.writeByte(1)
        buf.writeByte(1)
        buf.writeInt(data)
        buf.writeShort(velocityX)
        buf.writeShort(velocityY)
        buf.writeShort(velocityZ)
    }
}