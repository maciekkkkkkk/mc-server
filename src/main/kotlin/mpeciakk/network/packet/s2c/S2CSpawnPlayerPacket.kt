package mpeciakk.network.packet.s2c

import mpeciakk.math.Vector3d
import mpeciakk.network.packet.PacketByteBuf
import java.util.*

class S2CSpawnPlayerPacket(
    private val id: Int,
    private val uuid: UUID,
    private val position: Vector3d,
    private val yaw: Float,
    private val pitch: Float
) : S2CPacket(0x04) {
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(id)
        buf.writeUUID(uuid)
        buf.writeDouble(position.x)
        buf.writeDouble(position.y)
        buf.writeDouble(position.z)

        buf.writeAngle(yaw)
        buf.writeAngle(pitch)
    }
}