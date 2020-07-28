package mpeciakk.world.gen.alpha

import java.util.*

class Alpha11NoiseSampler @JvmOverloads constructor(rand: Random = Random()) {
    private val p: IntArray = IntArray(512)
    var offsetX: Double = rand.nextDouble() * 256.0
    var offsetY: Double = rand.nextDouble() * 256.0
    var offsetZ: Double = rand.nextDouble() * 256.0

    @JvmOverloads
    fun sample(x: Double, y: Double, z: Double = 0.0): Double {
        var sampleX = x + offsetX
        var sampleY = y + offsetY
        var sampleZ = z + offsetZ
        var floorX = sampleX.toInt()
        var floorY = sampleY.toInt()
        var floorZ = sampleZ.toInt()
        if (sampleX < floorX) {
            --floorX
        }
        if (sampleY < floorY) {
            --floorY
        }
        if (sampleZ < floorZ) {
            --floorZ
        }
        val integer17 = floorX and 0xFF
        val integer18 = floorY and 0xFF
        val integer19 = floorZ and 0xFF
        sampleX -= floorX.toDouble()
        sampleY -= floorY.toDouble()
        sampleZ -= floorZ.toDouble()
        val fadeX = sampleX * sampleX * sampleX * (sampleX * (sampleX * 6.0 - 15.0) + 10.0)
        val fadeY = sampleY * sampleY * sampleY * (sampleY * (sampleY * 6.0 - 15.0) + 10.0)
        val fadeZ = sampleZ * sampleZ * sampleZ * (sampleZ * (sampleZ * 6.0 - 15.0) + 10.0)
        val integer26 = p[integer17] + integer18
        val integer27 = p[integer26] + integer19
        val integer28 = p[integer26 + 1] + integer19
        val integer29 = p[integer17 + 1] + integer18
        val integer30 = p[integer29] + integer19
        val integer31 = p[integer29 + 1] + integer19
        return lerp(
            fadeZ,
            lerp(
                fadeY,
                lerp(
                    fadeX,
                    gradient(p[integer27], sampleX, sampleY, sampleZ),
                    gradient(p[integer30], sampleX - 1.0, sampleY, sampleZ)
                ),
                lerp(
                    fadeX,
                    gradient(p[integer28], sampleX, sampleY - 1.0, sampleZ),
                    gradient(p[integer31], sampleX - 1.0, sampleY - 1.0, sampleZ)
                )
            ),
            lerp(
                fadeY,
                lerp(
                    fadeX,
                    gradient(p[integer27 + 1], sampleX, sampleY, sampleZ - 1.0),
                    gradient(p[integer30 + 1], sampleX - 1.0, sampleY, sampleZ - 1.0)
                ),
                lerp(
                    fadeX,
                    gradient(p[integer28 + 1], sampleX, sampleY - 1.0, sampleZ - 1.0),
                    gradient(p[integer31 + 1], sampleX - 1.0, sampleY - 1.0, sampleZ - 1.0)
                )
            )
        )
    }

    fun lerp(progress: Double, low: Double, high: Double): Double {
        return low + progress * (high - low)
    }

    fun gradient(integer: Int, double3: Double, double5: Double, double7: Double): Double {
        val integer9 = integer and 0xF
        val double10 = if (integer9 < 8) double3 else double5
        val double12 =
            if (integer9 < 4) double5 else if (integer9 == 12 || integer9 == 14) double3 else double7
        return (if (integer9 and 0x1 == 0x0) double10 else -double10) + if (integer9 and 0x2 == 0x0) double12 else -double12
    }

    fun sample(
        arrayToReuse: DoubleArray,
        double3: Double,
        double5: Double,
        double7: Double,
        integer9: Int,
        integer10: Int,
        integer11: Int,
        double12: Double,
        double14: Double,
        double16: Double,
        double18: Double
    ) {
        var integer20 = 0
        val double21 = 1.0 / double18
        var integer23 = -1
        var double30 = 0.0
        var double32 = 0.0
        var double34 = 0.0
        var double36 = 0.0
        for (i in 0 until integer9) {
            var double39 = (double3 + i) * double12 + offsetX
            var integer41 = double39.toInt()
            if (double39 < integer41) {
                --integer41
            }
            val integer42 = integer41 and 0xFF
            double39 -= integer41.toDouble()
            val double43 = double39 * double39 * double39 * (double39 * (double39 * 6.0 - 15.0) + 10.0)
            for (j in 0 until integer11) {
                var double46 = (double7 + j) * double16 + offsetZ
                var integer48 = double46.toInt()
                if (double46 < integer48) {
                    --integer48
                }
                val integer49 = integer48 and 0xFF
                double46 -= integer48.toDouble()
                val double50 =
                    double46 * double46 * double46 * (double46 * (double46 * 6.0 - 15.0) + 10.0)
                for (k in 0 until integer10) {
                    var double53 = (double5 + k) * double14 + offsetY
                    var integer55 = double53.toInt()
                    if (double53 < integer55) {
                        --integer55
                    }
                    val integer56 = integer55 and 0xFF
                    double53 -= integer55.toDouble()
                    val double57 =
                        double53 * double53 * double53 * (double53 * (double53 * 6.0 - 15.0) + 10.0)
                    if (k == 0 || integer56 != integer23) {
                        integer23 = integer56
                        val integer24 = p[integer42] + integer56
                        val integer25 = p[integer24] + integer49
                        val integer26 = p[integer24 + 1] + integer49
                        val integer27 = p[integer42 + 1] + integer56
                        val integer28 = p[integer27] + integer49
                        val integer29 = p[integer27 + 1] + integer49
                        double30 = lerp(
                            double43,
                            gradient(p[integer25], double39, double53, double46),
                            gradient(p[integer28], double39 - 1.0, double53, double46)
                        )
                        double32 = lerp(
                            double43,
                            gradient(p[integer26], double39, double53 - 1.0, double46),
                            gradient(p[integer29], double39 - 1.0, double53 - 1.0, double46)
                        )
                        double34 = lerp(
                            double43,
                            gradient(p[integer25 + 1], double39, double53, double46 - 1.0),
                            gradient(p[integer28 + 1], double39 - 1.0, double53, double46 - 1.0)
                        )
                        double36 = lerp(
                            double43,
                            gradient(p[integer26 + 1], double39, double53 - 1.0, double46 - 1.0),
                            gradient(p[integer29 + 1], double39 - 1.0, double53 - 1.0, double46 - 1.0)
                        )
                    }
                    val double63 = lerp(
                        double50,
                        lerp(double57, double30, double32),
                        lerp(double57, double34, double36)
                    )
                    val n21 = integer20++
                    arrayToReuse[n21] += double63 * double21
                }
            }
        }
    }

    init {
        for (index in 0..255) {
            p[index] = index
        }
        for (index in 0..255) {
            val newIndex: Int = rand.nextInt(256 - index) + index
            val value = p[index]
            p[index] = p[newIndex]
            p[newIndex] = value
            p[index + 256] = p[index]
        }
    }
}