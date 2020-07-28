package mpeciakk.math

import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt


class Vector3i(var x: Int = 0, var y: Int = 0, var z: Int = 0) {
    constructor(vec: Vector3i) : this(vec.x, vec.y, vec.z)

    fun distance(other: Vector3i): Double {
        return sqrt((x - other.x).toDouble().pow(2) + (y - other.y).toDouble().pow(2) + (z - other.z).toDouble().pow(2));
    }

    override fun toString(): String {
        return "Vector3i(x=$x, y=$y, z=$z)"
    }
}