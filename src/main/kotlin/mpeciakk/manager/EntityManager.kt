package mpeciakk.manager

import mpeciakk.entity.Entity
import mpeciakk.network.Connection

class EntityManager {
    private var currentId = 0
    val nextId: Int
        get() {
            return currentId++
        }

    fun spawn(entity: Entity) {
        Connection.sendToAll(entity.getSpawnPacket())
    }
}