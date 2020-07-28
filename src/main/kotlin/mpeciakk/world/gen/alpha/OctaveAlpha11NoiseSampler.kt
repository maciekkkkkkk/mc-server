package mpeciakk.world.gen.alpha

import java.util.*

class OctaveAlpha11NoiseSampler(rand: Random?, private val octaves: Int) {
    private val generators: Array<Alpha11NoiseSampler?> = arrayOfNulls<Alpha11NoiseSampler>(octaves)
    fun sample(x: Double, y: Double): Double {
        var double6 = 0.0
        var double8 = 1.0
        for (i in 0 until octaves) {
            double6 += generators[i]!!.sample(x * double8, y * double8) / double8
            double8 /= 2.0
        }
        return double6
    }

    fun sample(
        arrayToReuse: DoubleArray?,
        double3: Double,
        double5: Double,
        double7: Double,
        integer9: Int,
        integer10: Int,
        integer11: Int,
        double12: Double,
        double14: Double,
        double16: Double
    ): DoubleArray {
        var arrayToReuse = arrayToReuse
        if (arrayToReuse == null) {
            arrayToReuse = DoubleArray(integer9 * integer10 * integer11)
        } else {
            for (i in arrayToReuse.indices) {
                arrayToReuse[i] = 0.0
            }
        }
        var double18 = 1.0
        for (j in 0 until octaves) {
            generators[j]!!.sample(
                arrayToReuse,
                double3,
                double5,
                double7,
                integer9,
                integer10,
                integer11,
                double12 * double18,
                double14 * double18,
                double16 * double18,
                double18
            )
            double18 /= 2.0
        }
        return arrayToReuse
    }

    init {
        for (i in 0 until octaves) {
            generators[i] = Alpha11NoiseSampler(rand!!)
        }
    }
}