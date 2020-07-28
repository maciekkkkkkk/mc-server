package mpeciakk.math

class Vector3d(var x: Double = 0.0, var y: Double = 0.0, var z: Double = 0.0) {
    override fun equals(other: Any?): Boolean {
        if (other !is Vector3d) {
            return false
        }

        return other.x == x && other.y == y && other.z == z
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        return result
    }

    override fun toString(): String {
        return "Vector3d(x=$x, y=$y, z=$z)"
    }
}