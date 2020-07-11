package mpeciakk.entity

import mpeciakk.network.packet.s2c.S2CPacket
import mpeciakk.network.packet.s2c.S2CSpawnLivingEntityPacket

class CowEntity : Entity() {
    override fun getSpawnPacket(): S2CPacket {
        return S2CSpawnLivingEntityPacket(id, uuid, 11, position, yaw, pitch, 0.0f, 0, 0, 0)
    }
}