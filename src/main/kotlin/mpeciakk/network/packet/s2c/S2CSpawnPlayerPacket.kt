package mpeciakk.network.packet.s2c

import mpeciakk.math.Vector3d
import mpeciakk.network.packet.PacketByteBuf
import java.util.*

class S2CSpawnPlayerPacket(
    private val id: Int,
    private val uuid: UUID,
    private val x: Double,
    private val y: Double,
    private val z: Double,
    private val yaw: Float,
    private val pitch: Float
) : S2CPacket(0x04) {
    constructor(
        id: Int,
        uuid: UUID,
        position: Vector3d,
        yaw: Float,
        pitch: Float
    ) : this(id, uuid, position.x, position.y, position.z, yaw, pitch)

    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(id)
        buf.writeUUID(uuid)
        buf.writeDouble(x)
        buf.writeDouble(y)
        buf.writeDouble(z)

        // TODO: change this to angles
        buf.writeByte(yaw.toInt())
        buf.writeByte(pitch.toInt())
    }
}