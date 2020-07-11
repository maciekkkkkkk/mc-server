package mpeciakk.entity

import mpeciakk.network.packet.s2c.S2CPacket
import mpeciakk.network.packet.s2c.S2CSpawnEntityPacket

class ArrowEntity(private val shooterId: Int) : Entity() {
    override fun getSpawnPacket(): S2CPacket {
        return S2CSpawnEntityPacket(id, uuid, 2, position, 0.0f, 0.0f, shooterId + 1, 0, 0, 0)
    }
}