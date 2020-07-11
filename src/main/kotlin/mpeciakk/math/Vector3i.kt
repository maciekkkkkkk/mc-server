package mpeciakk.math

class Vector3i(var x: Int = 0, var y: Int = 0, var z: Int = 0) {
    constructor(vec: Vector3i) : this(vec.x, vec.y, vec.z)

    override fun toString(): String {
        return "Vector3i(x=$x, y=$y, z=$z)"
    }
}