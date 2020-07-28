package mpeciakk.entity

import mpeciakk.math.Vector3d
import mpeciakk.minecraftServer
import mpeciakk.network.packet.s2c.S2CPacket
import java.util.*

abstract class Entity {
    val position = Vector3d()
    val prevPosition = Vector3d()
    var yaw = 0.0f
    var pitch = 0.0f
    var onGround = false
    var id = minecraftServer.entityManager.nextId

    val uuid = UUID.randomUUID()!!

    abstract fun getSpawnPacket(): S2CPacket

    open fun tick() {}
}