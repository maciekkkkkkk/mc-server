package mpeciakk.entity

import mpeciakk.math.Vector3d
import java.util.*

open class Entity {
    val position: Vector3d = Vector3d()
    var yaw: Float = 0.0f
    var pitch: Float = 0.0f
    var onGround: Boolean = false

    val uuid: UUID = UUID.randomUUID()
}