package mpeciakk.world.gen.alpha

import mpeciakk.math.Vector3i
import mpeciakk.world.Chunk
import mpeciakk.world.ChunkGenerator
import java.util.*


class AlphaChunkGenerator : ChunkGenerator() {
    var rand = Random()
    private val random = Random()

    private val noise1: OctaveAlpha11NoiseSampler =
        OctaveAlpha11NoiseSampler(rand, 16)
    private val noise2: OctaveAlpha11NoiseSampler =
        OctaveAlpha11NoiseSampler(rand, 16)
    private val noise3: OctaveAlpha11NoiseSampler =
        OctaveAlpha11NoiseSampler(rand, 8)
    private val noise6: OctaveAlpha11NoiseSampler =
        OctaveAlpha11NoiseSampler(rand, 10)
    private val noise7: OctaveAlpha11NoiseSampler =
        OctaveAlpha11NoiseSampler(rand, 16)
    private val beachNoise = OctaveAlpha11NoiseSampler(rand, 4);
    private val surfaceNoise = OctaveAlpha11NoiseSampler(rand, 4);

    private var noiseArray1: DoubleArray? = null
    private var noiseArray2: DoubleArray? = null
    private var noiseArray3: DoubleArray? = null
    private var noiseArray4: DoubleArray? = null
    private var noiseArray5: DoubleArray? = null
    private var heightNoise: DoubleArray? = null
    private var sandSample = DoubleArray(256)
    private var gravelSample = DoubleArray(256)
    private var stoneNoise = DoubleArray(256)

    override fun generateChunk(chunk: Chunk) {
        val chunkX: Int = chunk.x
        val chunkZ: Int = chunk.z
        val oneEighth = 0.125
        val oneQuarter = 0.25
        heightNoise = generateOctaves(heightNoise, chunkX * 4, 0, chunkZ * 4, 5, 17, 5)

        val posMutable = Vector3i()

        for (xSubChunk in 0..3) {
            for (zSubChunk in 0..3) {
                for (ySubChunk in 0..15) {
                    var sampleNWLow = this.heightNoise!![(xSubChunk * 5 + zSubChunk) * 17 + ySubChunk]
                    var sampleSWLow =
                        this.heightNoise!![(xSubChunk * 5 + zSubChunk + 1) * 17 + ySubChunk]
                    var sampleNELow =
                        this.heightNoise!![((xSubChunk + 1) * 5 + zSubChunk) * 17 + ySubChunk]
                    var sampleSELow =
                        this.heightNoise!![((xSubChunk + 1) * 5 + zSubChunk + 1) * 17 + ySubChunk]
                    val sampleNWHigh =
                        (this.heightNoise!![(xSubChunk * 5 + zSubChunk) * 17 + ySubChunk + 1] - sampleNWLow) * oneEighth
                    val sampleSWHigh =
                        (this.heightNoise!![(xSubChunk * 5 + zSubChunk + 1) * 17 + ySubChunk + 1] - sampleSWLow) * oneEighth
                    val sampleNEHigh =
                        (this.heightNoise!![((xSubChunk + 1) * 5 + zSubChunk) * 17 + ySubChunk + 1] - sampleNELow) * oneEighth
                    val sampleSEHigh =
                        (this.heightNoise!![((xSubChunk + 1) * 5 + zSubChunk + 1) * 17 + ySubChunk + 1] - sampleSELow) * oneEighth
                    for (localY in 0..7) {
                        val y = ySubChunk * 8 + localY
                        posMutable.y = y
                        var sampleNWInitial = sampleNWLow
                        var sampleSWInitial = sampleSWLow
                        val sampleNAverage = (sampleNELow - sampleNWLow) * oneQuarter
                        val sampleSAverage = (sampleSELow - sampleSWLow) * oneQuarter
                        for (localX in 0..3) {
                            posMutable.x = localX + xSubChunk * 4
                            var sample = sampleNWInitial
                            val someOffsetThing = (sampleSWInitial - sampleNWInitial) * oneQuarter
                            for (localZ in 0..3) {
                                posMutable.z = zSubChunk * 4 + localZ
                                var toSet = 0
                                if (sample > 0.0) {
                                    toSet = 1
                                }
                                chunk[posMutable.x, posMutable.y, posMutable.z] = toSet
                                sample += someOffsetThing
                            }
                            sampleNWInitial += sampleNAverage
                            sampleSWInitial += sampleSAverage
                        }
                        sampleNWLow += sampleNWHigh
                        sampleSWLow += sampleSWHigh
                        sampleNELow += sampleNEHigh
                        sampleSELow += sampleSEHigh
                    }
                }
            }
        }
    }

    override fun buildSurface(chunk: Chunk) {
        val pos = Vector3i()

        val chunkX: Int = chunk.x
        val chunkZ: Int = chunk.z

        val seaLevel: Int = 64
        val oneSixteenth = 0.03125

        this.sandSample = this.beachNoise.sample(
            this.sandSample,
            (chunkX * 16).toDouble(),
            (chunkZ * 16).toDouble(),
            0.0,
            16,
            16,
            1,
            oneSixteenth,
            oneSixteenth,
            1.0
        )
        this.gravelSample = this.beachNoise.sample(
            this.gravelSample,
            (chunkZ * 16).toDouble(),
            109.0134,
            (chunkX * 16).toDouble(),
            16,
            1,
            16,
            oneSixteenth,
            1.0,
            oneSixteenth
        )
        this.stoneNoise = this.surfaceNoise.sample(
            this.stoneNoise,
            (chunkX * 16).toDouble(),
            (chunkZ * 16).toDouble(),
            0.0,
            16,
            16,
            1,
            oneSixteenth * 2.0,
            oneSixteenth * 2.0,
            oneSixteenth * 2.0
        )

        for (x in 0..15) {
            pos.x = (x)
            for (z in 0..15) {
                pos.z = (z)
                val sandSampleAtPos: Boolean = this.sandSample.get(x + z * 16) + random.nextDouble() * 0.2 > 0.0
                val gravelSampleAtPos: Boolean = this.gravelSample.get(x + z * 16) + random.nextDouble() * 0.2 > 3.0
                val stoneSampleAtPos =
                    (this.stoneNoise[x + z * 16] / 3.0 + 3.0 + rand.nextDouble() * 0.25)
                var run = (-1).toDouble()
                var topState = 9
                var underState = 10
                for (y in 255 downTo 128) {
                    pos.y = (y)
                    chunk[pos] = 0
                }
                for (y in 127 downTo 0) {
                    pos.y = (y)
                    if (y <= random.nextInt(6) - 1) {
                        chunk[x, y, z] = 18
                    } else {
                        val currentBlock = chunk[pos]
                        if (currentBlock === 0) {
                            run = (-1).toDouble()
                        } else if (currentBlock === 1) {
                            if (run == (-1).toDouble()) {
                                if (stoneSampleAtPos <= 0) {
                                    topState = 0
                                    underState = 1
                                } else if (y >= seaLevel - 4 && y <= seaLevel + 1) {
                                    topState = 9
                                    underState = 10
                                    if (gravelSampleAtPos) {
                                        topState = 0
                                        underState = 14
                                    }
                                    if (sandSampleAtPos) {
                                        topState = 15
                                        underState = 15
                                    }
                                }
                                run = stoneSampleAtPos
                                if (y >= seaLevel - 1) {
                                    chunk[x, y, z] = topState
                                } else {
                                    chunk[x, y, z] = underState
                                }
                            } else if (run > 0) {
                                run--
                                chunk[x, y, z] = underState
                            }
                        }
                    }
                }
            }
        }
    }

    private fun generateOctaves(
        oldArray: DoubleArray?,
        x: Int,
        y: Int,
        z: Int,
        xSize: Int,
        ySize: Int,
        zSize: Int
    ): DoubleArray? {
        var oldArray: DoubleArray? = oldArray
        if (oldArray == null) {
            oldArray = DoubleArray(xSize * ySize * zSize)
        }
        val const1 = 684.412
        val const2 = 684.412
        noiseArray4 = noise6.sample(
            noiseArray4,
            x.toDouble(),
            y.toDouble(),
            z.toDouble(),
            xSize,
            1,
            zSize,
            1.0,
            0.0,
            1.0
        )
        noiseArray5 = noise7.sample(
            noiseArray5,
            x.toDouble(),
            y.toDouble(),
            z.toDouble(),
            xSize,
            1,
            zSize,
            100.0,
            0.0,
            100.0
        )
        noiseArray3 = noise3.sample(
            noiseArray3,
            x.toDouble(),
            y.toDouble(),
            z.toDouble(),
            xSize,
            ySize,
            zSize,
            const1 / 80.0,
            const2 / 160.0,
            const1 / 80.0
        )
        noiseArray1 = noise1.sample(
            noiseArray1,
            x.toDouble(),
            y.toDouble(),
            z.toDouble(),
            xSize,
            ySize,
            zSize,
            const1,
            const2,
            const1
        )
        noiseArray2 = noise2.sample(
            noiseArray2,
            x.toDouble(),
            y.toDouble(),
            z.toDouble(),
            xSize,
            ySize,
            zSize,
            const1,
            const2,
            const1
        )
        var index0 = 0
        var index1 = 0
        for (localX in 0 until xSize) {
            for (localZ in 0 until zSize) {
                var double0 = (noiseArray4!![index1] + 256.0) / 512.0
                if (double0 > 1.0) {
                    double0 = 1.0
                }
                val double2 = 0.0
                var double3 = noiseArray5!![index1] / 8000.0
                if (double3 < 0.0) {
                    double3 = -double3
                }
                double3 = double3 * 3.0 - 3.0
                if (double3 < 0.0) {
                    double3 /= 2.0
                    if (double3 < -1.0) {
                        double3 = -1.0
                    }
                    double3 /= 1.4
                    double3 /= 2.0
                    double0 = 0.0
                } else {
                    if (double3 > 1.0) {
                        double3 = 1.0
                    }
                    double3 /= 6.0
                }
                double0 += 0.5
                double3 = double3 * ySize.toDouble() / 16.0
                val double4 = ySize.toDouble() / 2.0 + double3 * 4.0
                ++index1
                for (localY in 0 until ySize) {
                    var double1 = 0.0
                    var double5 = (localY.toDouble() - double4) * 12.0 / double0
                    if (double5 < 0.0) {
                        double5 *= 4.0
                    }
                    val sample0 = noiseArray1!![index0] / 512.0
                    val sample1 = noiseArray2!![index0] / 512.0
                    val sample2 = (noiseArray3!![index0] / 10.0 + 1.0) / 2.0
                    double1 = if (sample2 < 0.0) {
                        sample0
                    } else if (sample2 > 1.0) {
                        sample1
                    } else {
                        sample0 + (sample1 - sample0) * sample2
                    }
                    double1 -= double5
                    if (localY > ySize - 4) {
                        val double6 = ((localY - (ySize - 4)).toFloat() / 3.0f).toDouble()
                        double1 = double1 * (1.0 - double6) + -10.0 * double6
                    }
                    if (localY < double2) {
                        var double7 = (double2 - localY.toDouble()) / 4.0
                        if (double7 < 0.0) {
                            double7 = 0.0
                        }
                        if (double7 > 1.0) {
                            double7 = 1.0
                        }
                        double1 = double1 * (1.0 - double7) + -10.0 * double7
                    }
                    oldArray[index0] = double1
                    ++index0
                }
            }
        }
        return oldArray
    }
}