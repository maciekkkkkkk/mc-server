package mpeciakk.world

class OpenSimplexNoise {
    private var perm: ShortArray
    private var permGrad2: Array<Grad2?>
    private var permGrad3: Array<Grad3?>
    private var permGrad4: Array<Grad4?>

    constructor(perm: ShortArray) {
        this.perm = perm
        permGrad2 = arrayOfNulls(PSIZE)
        permGrad3 = arrayOfNulls(PSIZE)
        permGrad4 = arrayOfNulls(PSIZE)
        for (i in 0 until PSIZE) {
            permGrad2[i] = GRADIENTS_2D[perm[i].toInt()]
            permGrad3[i] = GRADIENTS_3D[perm[i].toInt()]
            permGrad4[i] = GRADIENTS_4D[perm[i].toInt()]
        }
    }

    @JvmOverloads
    constructor(seed: Long = DEFAULT_SEED) {
        var seed = seed
        perm = ShortArray(PSIZE)
        permGrad2 = arrayOfNulls(PSIZE)
        permGrad3 = arrayOfNulls(PSIZE)
        permGrad4 = arrayOfNulls(PSIZE)
        val source = ShortArray(PSIZE)
        for (i in 0 until PSIZE) source[i] = i.toShort()
        for (i in PSIZE - 1 downTo 0) {
            seed = seed * 6364136223846793005L + 1442695040888963407L
            var r = ((seed + 31) % (i + 1)).toInt()
            if (r < 0) r += i + 1
            perm[i] = source[r]
            permGrad2[i] = GRADIENTS_2D[perm[i].toInt()]
            permGrad3[i] = GRADIENTS_3D[perm[i].toInt()]
            permGrad4[i] = GRADIENTS_4D[perm[i].toInt()]
            source[r] = source[i]
        }
    }

    // 2D OpenSimplex Noise.
    fun eval(x: Double, y: Double): Double {

        // Place input coordinates onto grid.
        val stretchOffset = (x + y) * STRETCH_CONSTANT_2D
        val xs = x + stretchOffset
        val ys = y + stretchOffset

        // Floor to get grid coordinates of rhombus (stretched square) super-cell origin.
        var xsb = fastFloor(xs)
        var ysb = fastFloor(ys)

        // Compute grid coordinates relative to rhombus origin.
        val xins = xs - xsb
        val yins = ys - ysb

        // Sum those together to get a value that determines which region we're in.
        val inSum = xins + yins

        // Positions relative to origin point.
        val squishOffsetIns = inSum * SQUISH_CONSTANT_2D
        var dx0 = xins + squishOffsetIns
        var dy0 = yins + squishOffsetIns

        // We'll be defining these inside the next block and using them afterwards.
        val dx_ext: Double
        val dy_ext: Double
        val xsv_ext: Int
        val ysv_ext: Int
        var value = 0.0

        // Contribution (1,0)
        val dx1 = dx0 - 1 - SQUISH_CONSTANT_2D
        val dy1 = dy0 - 0 - SQUISH_CONSTANT_2D
        var attn1 = 2 - dx1 * dx1 - dy1 * dy1
        if (attn1 > 0) {
            attn1 *= attn1
            value += attn1 * attn1 * extrapolate(xsb + 1, ysb + 0, dx1, dy1)
        }

        // Contribution (0,1)
        val dx2 = dx0 - 0 - SQUISH_CONSTANT_2D
        val dy2 = dy0 - 1 - SQUISH_CONSTANT_2D
        var attn2 = 2 - dx2 * dx2 - dy2 * dy2
        if (attn2 > 0) {
            attn2 *= attn2
            value += attn2 * attn2 * extrapolate(xsb + 0, ysb + 1, dx2, dy2)
        }
        if (inSum <= 1) { // We're inside the triangle (2-Simplex) at (0,0)
            val zins = 1 - inSum
            if (zins > xins || zins > yins) { // (0,0) is one of the closest two triangular vertices
                if (xins > yins) {
                    xsv_ext = xsb + 1
                    ysv_ext = ysb - 1
                    dx_ext = dx0 - 1
                    dy_ext = dy0 + 1
                } else {
                    xsv_ext = xsb - 1
                    ysv_ext = ysb + 1
                    dx_ext = dx0 + 1
                    dy_ext = dy0 - 1
                }
            } else { // (1,0) and (0,1) are the closest two vertices.
                xsv_ext = xsb + 1
                ysv_ext = ysb + 1
                dx_ext = dx0 - 1 - 2 * SQUISH_CONSTANT_2D
                dy_ext = dy0 - 1 - 2 * SQUISH_CONSTANT_2D
            }
        } else { // We're inside the triangle (2-Simplex) at (1,1)
            val zins = 2 - inSum
            if (zins < xins || zins < yins) { // (0,0) is one of the closest two triangular vertices
                if (xins > yins) {
                    xsv_ext = xsb + 2
                    ysv_ext = ysb + 0
                    dx_ext = dx0 - 2 - 2 * SQUISH_CONSTANT_2D
                    dy_ext = dy0 + 0 - 2 * SQUISH_CONSTANT_2D
                } else {
                    xsv_ext = xsb + 0
                    ysv_ext = ysb + 2
                    dx_ext = dx0 + 0 - 2 * SQUISH_CONSTANT_2D
                    dy_ext = dy0 - 2 - 2 * SQUISH_CONSTANT_2D
                }
            } else { // (1,0) and (0,1) are the closest two vertices.
                dx_ext = dx0
                dy_ext = dy0
                xsv_ext = xsb
                ysv_ext = ysb
            }
            xsb += 1
            ysb += 1
            dx0 = dx0 - 1 - 2 * SQUISH_CONSTANT_2D
            dy0 = dy0 - 1 - 2 * SQUISH_CONSTANT_2D
        }

        // Contribution (0,0) or (1,1)
        var attn0 = 2 - dx0 * dx0 - dy0 * dy0
        if (attn0 > 0) {
            attn0 *= attn0
            value += attn0 * attn0 * extrapolate(xsb, ysb, dx0, dy0)
        }

        // Extra Vertex
        var attn_ext = 2 - dx_ext * dx_ext - dy_ext * dy_ext
        if (attn_ext > 0) {
            attn_ext *= attn_ext
            value += attn_ext * attn_ext * extrapolate(xsv_ext, ysv_ext, dx_ext, dy_ext)
        }
        return value
    }

    // 3D OpenSimplex Noise.
    fun eval(x: Double, y: Double, z: Double): Double {

        // Place input coordinates on simplectic honeycomb.
        val stretchOffset = (x + y + z) * STRETCH_CONSTANT_3D
        val xs = x + stretchOffset
        val ys = y + stretchOffset
        val zs = z + stretchOffset
        return eval3_Base(xs, ys, zs)
    }

    // Not as good as in SuperSimplex/OpenSimplex2S, since there are more visible differences between different slices.
    // The Z coordinate should always be the "different" coordinate in your use case.
    fun eval3_XYBeforeZ(x: Double, y: Double, z: Double): Double {
        // Combine rotation with skew transform.
        val xy = x + y
        val s2 = xy * 0.211324865405187
        val zz = z * 0.288675134594813
        val xs = s2 - x + zz
        val ys = s2 - y + zz
        val zs = xy * 0.577350269189626 + zz
        return eval3_Base(xs, ys, zs)
    }

    // Similar to the above, except the Y coordinate should always be the "different" coordinate in your use case.
    fun eval3_XZBeforeY(x: Double, y: Double, z: Double): Double {
        // Combine rotation with skew transform.
        val xz = x + z
        val s2 = xz * 0.211324865405187
        val yy = y * 0.288675134594813
        val xs = s2 - x + yy
        val zs = s2 - z + yy
        val ys = xz * 0.577350269189626 + yy
        return eval3_Base(xs, ys, zs)
    }

    // 3D OpenSimplex Noise (base which takes skewed coordinates directly).
    private fun eval3_Base(xs: Double, ys: Double, zs: Double): Double {

        // Floor to get simplectic honeycomb coordinates of rhombohedron (stretched cube) super-cell origin.
        val xsb = fastFloor(xs)
        val ysb = fastFloor(ys)
        val zsb = fastFloor(zs)

        // Compute simplectic honeycomb coordinates relative to rhombohedral origin.
        val xins = xs - xsb
        val yins = ys - ysb
        val zins = zs - zsb

        // Sum those together to get a value that determines which region we're in.
        val inSum = xins + yins + zins

        // Positions relative to origin point.
        val squishOffsetIns = inSum * SQUISH_CONSTANT_3D
        var dx0 = xins + squishOffsetIns
        var dy0 = yins + squishOffsetIns
        var dz0 = zins + squishOffsetIns

        // We'll be defining these inside the next block and using them afterwards.
        val dx_ext0: Double
        var dy_ext0: Double
        val dz_ext0: Double
        var dx_ext1: Double
        var dy_ext1: Double
        var dz_ext1: Double
        val xsv_ext0: Int
        var ysv_ext0: Int
        val zsv_ext0: Int
        var xsv_ext1: Int
        var ysv_ext1: Int
        var zsv_ext1: Int
        var value = 0.0
        if (inSum <= 1) { // We're inside the tetrahedron (3-Simplex) at (0,0,0)

            // Determine which two of (0,0,1), (0,1,0), (1,0,0) are closest.
            var aPoint: Byte = 0x01
            var aScore = xins
            var bPoint: Byte = 0x02
            var bScore = yins
            if (aScore >= bScore && zins > bScore) {
                bScore = zins
                bPoint = 0x04
            } else if (aScore < bScore && zins > aScore) {
                aScore = zins
                aPoint = 0x04
            }

            // Now we determine the two lattice points not part of the tetrahedron that may contribute.
            // This depends on the closest two tetrahedral vertices, including (0,0,0)
            val wins = 1 - inSum
            if (wins > aScore || wins > bScore) { // (0,0,0) is one of the closest two tetrahedral vertices.
                val c =
                    if (bScore > aScore) bPoint else aPoint // Our other closest vertex is the closest out of a and b.
                if (c and 0x01 == 0) {
                    xsv_ext0 = xsb - 1
                    xsv_ext1 = xsb
                    dx_ext0 = dx0 + 1
                    dx_ext1 = dx0
                } else {
                    xsv_ext1 = xsb + 1
                    xsv_ext0 = xsv_ext1
                    dx_ext1 = dx0 - 1
                    dx_ext0 = dx_ext1
                }
                if (c and 0x02 == 0) {
                    ysv_ext1 = ysb
                    ysv_ext0 = ysv_ext1
                    dy_ext1 = dy0
                    dy_ext0 = dy_ext1
                    if (c and 0x01 == 0) {
                        ysv_ext1 -= 1
                        dy_ext1 += 1.0
                    } else {
                        ysv_ext0 -= 1
                        dy_ext0 += 1.0
                    }
                } else {
                    ysv_ext1 = ysb + 1
                    ysv_ext0 = ysv_ext1
                    dy_ext1 = dy0 - 1
                    dy_ext0 = dy_ext1
                }
                if (c and 0x04 == 0) {
                    zsv_ext0 = zsb
                    zsv_ext1 = zsb - 1
                    dz_ext0 = dz0
                    dz_ext1 = dz0 + 1
                } else {
                    zsv_ext1 = zsb + 1
                    zsv_ext0 = zsv_ext1
                    dz_ext1 = dz0 - 1
                    dz_ext0 = dz_ext1
                }
            } else { // (0,0,0) is not one of the closest two tetrahedral vertices.
                val c =
                    (aPoint or bPoint) as Byte // Our two extra vertices are determined by the closest two.
                if (c and 0x01 == 0) {
                    xsv_ext0 = xsb
                    xsv_ext1 = xsb - 1
                    dx_ext0 = dx0 - 2 * SQUISH_CONSTANT_3D
                    dx_ext1 = dx0 + 1 - SQUISH_CONSTANT_3D
                } else {
                    xsv_ext1 = xsb + 1
                    xsv_ext0 = xsv_ext1
                    dx_ext0 = dx0 - 1 - 2 * SQUISH_CONSTANT_3D
                    dx_ext1 = dx0 - 1 - SQUISH_CONSTANT_3D
                }
                if (c and 0x02 == 0) {
                    ysv_ext0 = ysb
                    ysv_ext1 = ysb - 1
                    dy_ext0 = dy0 - 2 * SQUISH_CONSTANT_3D
                    dy_ext1 = dy0 + 1 - SQUISH_CONSTANT_3D
                } else {
                    ysv_ext1 = ysb + 1
                    ysv_ext0 = ysv_ext1
                    dy_ext0 = dy0 - 1 - 2 * SQUISH_CONSTANT_3D
                    dy_ext1 = dy0 - 1 - SQUISH_CONSTANT_3D
                }
                if (c and 0x04 == 0) {
                    zsv_ext0 = zsb
                    zsv_ext1 = zsb - 1
                    dz_ext0 = dz0 - 2 * SQUISH_CONSTANT_3D
                    dz_ext1 = dz0 + 1 - SQUISH_CONSTANT_3D
                } else {
                    zsv_ext1 = zsb + 1
                    zsv_ext0 = zsv_ext1
                    dz_ext0 = dz0 - 1 - 2 * SQUISH_CONSTANT_3D
                    dz_ext1 = dz0 - 1 - SQUISH_CONSTANT_3D
                }
            }

            // Contribution (0,0,0)
            var attn0 = 2 - dx0 * dx0 - dy0 * dy0 - dz0 * dz0
            if (attn0 > 0) {
                attn0 *= attn0
                value += attn0 * attn0 * extrapolate(xsb + 0, ysb + 0, zsb + 0, dx0, dy0, dz0)
            }

            // Contribution (1,0,0)
            val dx1 = dx0 - 1 - SQUISH_CONSTANT_3D
            val dy1 = dy0 - 0 - SQUISH_CONSTANT_3D
            val dz1 = dz0 - 0 - SQUISH_CONSTANT_3D
            var attn1 = 2 - dx1 * dx1 - dy1 * dy1 - dz1 * dz1
            if (attn1 > 0) {
                attn1 *= attn1
                value += attn1 * attn1 * extrapolate(xsb + 1, ysb + 0, zsb + 0, dx1, dy1, dz1)
            }

            // Contribution (0,1,0)
            val dx2 = dx0 - 0 - SQUISH_CONSTANT_3D
            val dy2 = dy0 - 1 - SQUISH_CONSTANT_3D
            var attn2 = 2 - dx2 * dx2 - dy2 * dy2 - dz1 * dz1
            if (attn2 > 0) {
                attn2 *= attn2
                value += attn2 * attn2 * extrapolate(xsb + 0, ysb + 1, zsb + 0, dx2, dy2, dz1)
            }

            // Contribution (0,0,1)
            val dz3 = dz0 - 1 - SQUISH_CONSTANT_3D
            var attn3 = 2 - dx2 * dx2 - dy1 * dy1 - dz3 * dz3
            if (attn3 > 0) {
                attn3 *= attn3
                value += attn3 * attn3 * extrapolate(xsb + 0, ysb + 0, zsb + 1, dx2, dy1, dz3)
            }
        } else if (inSum >= 2) { // We're inside the tetrahedron (3-Simplex) at (1,1,1)

            // Determine which two tetrahedral vertices are the closest, out of (1,1,0), (1,0,1), (0,1,1) but not (1,1,1).
            var aPoint: Byte = 0x06
            var aScore = xins
            var bPoint: Byte = 0x05
            var bScore = yins
            if (aScore <= bScore && zins < bScore) {
                bScore = zins
                bPoint = 0x03
            } else if (aScore > bScore && zins < aScore) {
                aScore = zins
                aPoint = 0x03
            }

            // Now we determine the two lattice points not part of the tetrahedron that may contribute.
            // This depends on the closest two tetrahedral vertices, including (1,1,1)
            val wins = 3 - inSum
            if (wins < aScore || wins < bScore) { // (1,1,1) is one of the closest two tetrahedral vertices.
                val c =
                    if (bScore < aScore) bPoint else aPoint // Our other closest vertex is the closest out of a and b.
                if (c and 0x01 != 0) {
                    xsv_ext0 = xsb + 2
                    xsv_ext1 = xsb + 1
                    dx_ext0 = dx0 - 2 - 3 * SQUISH_CONSTANT_3D
                    dx_ext1 = dx0 - 1 - 3 * SQUISH_CONSTANT_3D
                } else {
                    xsv_ext1 = xsb
                    xsv_ext0 = xsv_ext1
                    dx_ext1 = dx0 - 3 * SQUISH_CONSTANT_3D
                    dx_ext0 = dx_ext1
                }
                if (c and 0x02 != 0) {
                    ysv_ext1 = ysb + 1
                    ysv_ext0 = ysv_ext1
                    dy_ext1 = dy0 - 1 - 3 * SQUISH_CONSTANT_3D
                    dy_ext0 = dy_ext1
                    if (c and 0x01 != 0) {
                        ysv_ext1 += 1
                        dy_ext1 -= 1.0
                    } else {
                        ysv_ext0 += 1
                        dy_ext0 -= 1.0
                    }
                } else {
                    ysv_ext1 = ysb
                    ysv_ext0 = ysv_ext1
                    dy_ext1 = dy0 - 3 * SQUISH_CONSTANT_3D
                    dy_ext0 = dy_ext1
                }
                if (c and 0x04 != 0) {
                    zsv_ext0 = zsb + 1
                    zsv_ext1 = zsb + 2
                    dz_ext0 = dz0 - 1 - 3 * SQUISH_CONSTANT_3D
                    dz_ext1 = dz0 - 2 - 3 * SQUISH_CONSTANT_3D
                } else {
                    zsv_ext1 = zsb
                    zsv_ext0 = zsv_ext1
                    dz_ext1 = dz0 - 3 * SQUISH_CONSTANT_3D
                    dz_ext0 = dz_ext1
                }
            } else { // (1,1,1) is not one of the closest two tetrahedral vertices.
                val c =
                    (aPoint and bPoint) as Byte // Our two extra vertices are determined by the closest two.
                if (c and 0x01 != 0) {
                    xsv_ext0 = xsb + 1
                    xsv_ext1 = xsb + 2
                    dx_ext0 = dx0 - 1 - SQUISH_CONSTANT_3D
                    dx_ext1 = dx0 - 2 - 2 * SQUISH_CONSTANT_3D
                } else {
                    xsv_ext1 = xsb
                    xsv_ext0 = xsv_ext1
                    dx_ext0 = dx0 - SQUISH_CONSTANT_3D
                    dx_ext1 = dx0 - 2 * SQUISH_CONSTANT_3D
                }
                if (c and 0x02 != 0) {
                    ysv_ext0 = ysb + 1
                    ysv_ext1 = ysb + 2
                    dy_ext0 = dy0 - 1 - SQUISH_CONSTANT_3D
                    dy_ext1 = dy0 - 2 - 2 * SQUISH_CONSTANT_3D
                } else {
                    ysv_ext1 = ysb
                    ysv_ext0 = ysv_ext1
                    dy_ext0 = dy0 - SQUISH_CONSTANT_3D
                    dy_ext1 = dy0 - 2 * SQUISH_CONSTANT_3D
                }
                if (c and 0x04 != 0) {
                    zsv_ext0 = zsb + 1
                    zsv_ext1 = zsb + 2
                    dz_ext0 = dz0 - 1 - SQUISH_CONSTANT_3D
                    dz_ext1 = dz0 - 2 - 2 * SQUISH_CONSTANT_3D
                } else {
                    zsv_ext1 = zsb
                    zsv_ext0 = zsv_ext1
                    dz_ext0 = dz0 - SQUISH_CONSTANT_3D
                    dz_ext1 = dz0 - 2 * SQUISH_CONSTANT_3D
                }
            }

            // Contribution (1,1,0)
            val dx3 = dx0 - 1 - 2 * SQUISH_CONSTANT_3D
            val dy3 = dy0 - 1 - 2 * SQUISH_CONSTANT_3D
            val dz3 = dz0 - 0 - 2 * SQUISH_CONSTANT_3D
            var attn3 = 2 - dx3 * dx3 - dy3 * dy3 - dz3 * dz3
            if (attn3 > 0) {
                attn3 *= attn3
                value += attn3 * attn3 * extrapolate(xsb + 1, ysb + 1, zsb + 0, dx3, dy3, dz3)
            }

            // Contribution (1,0,1)
            val dy2 = dy0 - 0 - 2 * SQUISH_CONSTANT_3D
            val dz2 = dz0 - 1 - 2 * SQUISH_CONSTANT_3D
            var attn2 = 2 - dx3 * dx3 - dy2 * dy2 - dz2 * dz2
            if (attn2 > 0) {
                attn2 *= attn2
                value += attn2 * attn2 * extrapolate(xsb + 1, ysb + 0, zsb + 1, dx3, dy2, dz2)
            }

            // Contribution (0,1,1)
            val dx1 = dx0 - 0 - 2 * SQUISH_CONSTANT_3D
            var attn1 = 2 - dx1 * dx1 - dy3 * dy3 - dz2 * dz2
            if (attn1 > 0) {
                attn1 *= attn1
                value += attn1 * attn1 * extrapolate(xsb + 0, ysb + 1, zsb + 1, dx1, dy3, dz2)
            }

            // Contribution (1,1,1)
            dx0 = dx0 - 1 - 3 * SQUISH_CONSTANT_3D
            dy0 = dy0 - 1 - 3 * SQUISH_CONSTANT_3D
            dz0 = dz0 - 1 - 3 * SQUISH_CONSTANT_3D
            var attn0 = 2 - dx0 * dx0 - dy0 * dy0 - dz0 * dz0
            if (attn0 > 0) {
                attn0 *= attn0
                value += attn0 * attn0 * extrapolate(xsb + 1, ysb + 1, zsb + 1, dx0, dy0, dz0)
            }
        } else { // We're inside the octahedron (Rectified 3-Simplex) in between.
            var aScore: Double
            var aPoint: Byte
            var aIsFurtherSide: Boolean
            var bScore: Double
            var bPoint: Byte
            var bIsFurtherSide: Boolean

            // Decide between point (0,0,1) and (1,1,0) as closest
            val p1 = xins + yins
            if (p1 > 1) {
                aScore = p1 - 1
                aPoint = 0x03
                aIsFurtherSide = true
            } else {
                aScore = 1 - p1
                aPoint = 0x04
                aIsFurtherSide = false
            }

            // Decide between point (0,1,0) and (1,0,1) as closest
            val p2 = xins + zins
            if (p2 > 1) {
                bScore = p2 - 1
                bPoint = 0x05
                bIsFurtherSide = true
            } else {
                bScore = 1 - p2
                bPoint = 0x02
                bIsFurtherSide = false
            }

            // The closest out of the two (1,0,0) and (0,1,1) will replace the furthest out of the two decided above, if closer.
            val p3 = yins + zins
            if (p3 > 1) {
                val score = p3 - 1
                if (aScore <= bScore && aScore < score) {
                    aScore = score
                    aPoint = 0x06
                    aIsFurtherSide = true
                } else if (aScore > bScore && bScore < score) {
                    bScore = score
                    bPoint = 0x06
                    bIsFurtherSide = true
                }
            } else {
                val score = 1 - p3
                if (aScore <= bScore && aScore < score) {
                    aScore = score
                    aPoint = 0x01
                    aIsFurtherSide = false
                } else if (aScore > bScore && bScore < score) {
                    bScore = score
                    bPoint = 0x01
                    bIsFurtherSide = false
                }
            }

            // Where each of the two closest points are determines how the extra two vertices are calculated.
            if (aIsFurtherSide == bIsFurtherSide) {
                if (aIsFurtherSide) { // Both closest points on (1,1,1) side

                    // One of the two extra points is (1,1,1)
                    dx_ext0 = dx0 - 1 - 3 * SQUISH_CONSTANT_3D
                    dy_ext0 = dy0 - 1 - 3 * SQUISH_CONSTANT_3D
                    dz_ext0 = dz0 - 1 - 3 * SQUISH_CONSTANT_3D
                    xsv_ext0 = xsb + 1
                    ysv_ext0 = ysb + 1
                    zsv_ext0 = zsb + 1

                    // Other extra point is based on the shared axis.
                    val c = (aPoint and bPoint) as Byte
                    if (c and 0x01 != 0) {
                        dx_ext1 = dx0 - 2 - 2 * SQUISH_CONSTANT_3D
                        dy_ext1 = dy0 - 2 * SQUISH_CONSTANT_3D
                        dz_ext1 = dz0 - 2 * SQUISH_CONSTANT_3D
                        xsv_ext1 = xsb + 2
                        ysv_ext1 = ysb
                        zsv_ext1 = zsb
                    } else if (c and 0x02 != 0) {
                        dx_ext1 = dx0 - 2 * SQUISH_CONSTANT_3D
                        dy_ext1 = dy0 - 2 - 2 * SQUISH_CONSTANT_3D
                        dz_ext1 = dz0 - 2 * SQUISH_CONSTANT_3D
                        xsv_ext1 = xsb
                        ysv_ext1 = ysb + 2
                        zsv_ext1 = zsb
                    } else {
                        dx_ext1 = dx0 - 2 * SQUISH_CONSTANT_3D
                        dy_ext1 = dy0 - 2 * SQUISH_CONSTANT_3D
                        dz_ext1 = dz0 - 2 - 2 * SQUISH_CONSTANT_3D
                        xsv_ext1 = xsb
                        ysv_ext1 = ysb
                        zsv_ext1 = zsb + 2
                    }
                } else { // Both closest points on (0,0,0) side

                    // One of the two extra points is (0,0,0)
                    dx_ext0 = dx0
                    dy_ext0 = dy0
                    dz_ext0 = dz0
                    xsv_ext0 = xsb
                    ysv_ext0 = ysb
                    zsv_ext0 = zsb

                    // Other extra point is based on the omitted axis.
                    val c = (aPoint or bPoint) as Byte
                    if (c and 0x01 == 0) {
                        dx_ext1 = dx0 + 1 - SQUISH_CONSTANT_3D
                        dy_ext1 = dy0 - 1 - SQUISH_CONSTANT_3D
                        dz_ext1 = dz0 - 1 - SQUISH_CONSTANT_3D
                        xsv_ext1 = xsb - 1
                        ysv_ext1 = ysb + 1
                        zsv_ext1 = zsb + 1
                    } else if (c and 0x02 == 0) {
                        dx_ext1 = dx0 - 1 - SQUISH_CONSTANT_3D
                        dy_ext1 = dy0 + 1 - SQUISH_CONSTANT_3D
                        dz_ext1 = dz0 - 1 - SQUISH_CONSTANT_3D
                        xsv_ext1 = xsb + 1
                        ysv_ext1 = ysb - 1
                        zsv_ext1 = zsb + 1
                    } else {
                        dx_ext1 = dx0 - 1 - SQUISH_CONSTANT_3D
                        dy_ext1 = dy0 - 1 - SQUISH_CONSTANT_3D
                        dz_ext1 = dz0 + 1 - SQUISH_CONSTANT_3D
                        xsv_ext1 = xsb + 1
                        ysv_ext1 = ysb + 1
                        zsv_ext1 = zsb - 1
                    }
                }
            } else { // One point on (0,0,0) side, one point on (1,1,1) side
                val c1: Byte
                val c2: Byte
                if (aIsFurtherSide) {
                    c1 = aPoint
                    c2 = bPoint
                } else {
                    c1 = bPoint
                    c2 = aPoint
                }

                // One contribution is a permutation of (1,1,-1)
                if (c1 and 0x01 == 0) {
                    dx_ext0 = dx0 + 1 - SQUISH_CONSTANT_3D
                    dy_ext0 = dy0 - 1 - SQUISH_CONSTANT_3D
                    dz_ext0 = dz0 - 1 - SQUISH_CONSTANT_3D
                    xsv_ext0 = xsb - 1
                    ysv_ext0 = ysb + 1
                    zsv_ext0 = zsb + 1
                } else if (c1 and 0x02 == 0) {
                    dx_ext0 = dx0 - 1 - SQUISH_CONSTANT_3D
                    dy_ext0 = dy0 + 1 - SQUISH_CONSTANT_3D
                    dz_ext0 = dz0 - 1 - SQUISH_CONSTANT_3D
                    xsv_ext0 = xsb + 1
                    ysv_ext0 = ysb - 1
                    zsv_ext0 = zsb + 1
                } else {
                    dx_ext0 = dx0 - 1 - SQUISH_CONSTANT_3D
                    dy_ext0 = dy0 - 1 - SQUISH_CONSTANT_3D
                    dz_ext0 = dz0 + 1 - SQUISH_CONSTANT_3D
                    xsv_ext0 = xsb + 1
                    ysv_ext0 = ysb + 1
                    zsv_ext0 = zsb - 1
                }

                // One contribution is a permutation of (0,0,2)
                dx_ext1 = dx0 - 2 * SQUISH_CONSTANT_3D
                dy_ext1 = dy0 - 2 * SQUISH_CONSTANT_3D
                dz_ext1 = dz0 - 2 * SQUISH_CONSTANT_3D
                xsv_ext1 = xsb
                ysv_ext1 = ysb
                zsv_ext1 = zsb
                if (c2 and 0x01 != 0) {
                    dx_ext1 -= 2.0
                    xsv_ext1 += 2
                } else if (c2 and 0x02 != 0) {
                    dy_ext1 -= 2.0
                    ysv_ext1 += 2
                } else {
                    dz_ext1 -= 2.0
                    zsv_ext1 += 2
                }
            }

            // Contribution (1,0,0)
            val dx1 = dx0 - 1 - SQUISH_CONSTANT_3D
            val dy1 = dy0 - 0 - SQUISH_CONSTANT_3D
            val dz1 = dz0 - 0 - SQUISH_CONSTANT_3D
            var attn1 = 2 - dx1 * dx1 - dy1 * dy1 - dz1 * dz1
            if (attn1 > 0) {
                attn1 *= attn1
                value += attn1 * attn1 * extrapolate(xsb + 1, ysb + 0, zsb + 0, dx1, dy1, dz1)
            }

            // Contribution (0,1,0)
            val dx2 = dx0 - 0 - SQUISH_CONSTANT_3D
            val dy2 = dy0 - 1 - SQUISH_CONSTANT_3D
            var attn2 = 2 - dx2 * dx2 - dy2 * dy2 - dz1 * dz1
            if (attn2 > 0) {
                attn2 *= attn2
                value += attn2 * attn2 * extrapolate(xsb + 0, ysb + 1, zsb + 0, dx2, dy2, dz1)
            }

            // Contribution (0,0,1)
            val dz3 = dz0 - 1 - SQUISH_CONSTANT_3D
            var attn3 = 2 - dx2 * dx2 - dy1 * dy1 - dz3 * dz3
            if (attn3 > 0) {
                attn3 *= attn3
                value += attn3 * attn3 * extrapolate(xsb + 0, ysb + 0, zsb + 1, dx2, dy1, dz3)
            }

            // Contribution (1,1,0)
            val dx4 = dx0 - 1 - 2 * SQUISH_CONSTANT_3D
            val dy4 = dy0 - 1 - 2 * SQUISH_CONSTANT_3D
            val dz4 = dz0 - 0 - 2 * SQUISH_CONSTANT_3D
            var attn4 = 2 - dx4 * dx4 - dy4 * dy4 - dz4 * dz4
            if (attn4 > 0) {
                attn4 *= attn4
                value += attn4 * attn4 * extrapolate(xsb + 1, ysb + 1, zsb + 0, dx4, dy4, dz4)
            }

            // Contribution (1,0,1)
            val dy5 = dy0 - 0 - 2 * SQUISH_CONSTANT_3D
            val dz5 = dz0 - 1 - 2 * SQUISH_CONSTANT_3D
            var attn5 = 2 - dx4 * dx4 - dy5 * dy5 - dz5 * dz5
            if (attn5 > 0) {
                attn5 *= attn5
                value += attn5 * attn5 * extrapolate(xsb + 1, ysb + 0, zsb + 1, dx4, dy5, dz5)
            }

            // Contribution (0,1,1)
            val dx6 = dx0 - 0 - 2 * SQUISH_CONSTANT_3D
            var attn6 = 2 - dx6 * dx6 - dy4 * dy4 - dz5 * dz5
            if (attn6 > 0) {
                attn6 *= attn6
                value += attn6 * attn6 * extrapolate(xsb + 0, ysb + 1, zsb + 1, dx6, dy4, dz5)
            }
        }

        // First extra vertex
        var attn_ext0 = 2 - dx_ext0 * dx_ext0 - dy_ext0 * dy_ext0 - dz_ext0 * dz_ext0
        if (attn_ext0 > 0) {
            attn_ext0 *= attn_ext0
            value += attn_ext0 * attn_ext0 * extrapolate(xsv_ext0, ysv_ext0, zsv_ext0, dx_ext0, dy_ext0, dz_ext0)
        }

        // Second extra vertex
        var attn_ext1 = 2 - dx_ext1 * dx_ext1 - dy_ext1 * dy_ext1 - dz_ext1 * dz_ext1
        if (attn_ext1 > 0) {
            attn_ext1 *= attn_ext1
            value += attn_ext1 * attn_ext1 * extrapolate(xsv_ext1, ysv_ext1, zsv_ext1, dx_ext1, dy_ext1, dz_ext1)
        }
        return value
    }

    fun eval(x: Double, y: Double, z: Double, w: Double): Double {

        // Get points for A4 lattice
        val s = -0.138196601125011 * (x + y + z + w)
        val xs = x + s
        val ys = y + s
        val zs = z + s
        val ws = w + s
        return eval4_Base(xs, ys, zs, ws)
    }

    fun eval4_XYBeforeZW(x: Double, y: Double, z: Double, w: Double): Double {
        val s2 = (x + y) * -0.178275657951399372 + (z + w) * 0.215623393288842828
        val t2 = (z + w) * -0.403949762580207112 + (x + y) * -0.375199083010075342
        val xs = x + s2
        val ys = y + s2
        val zs = z + t2
        val ws = w + t2
        return eval4_Base(xs, ys, zs, ws)
    }

    fun eval4_XZBeforeYW(x: Double, y: Double, z: Double, w: Double): Double {
        val s2 = (x + z) * -0.178275657951399372 + (y + w) * 0.215623393288842828
        val t2 = (y + w) * -0.403949762580207112 + (x + z) * -0.375199083010075342
        val xs = x + s2
        val ys = y + t2
        val zs = z + s2
        val ws = w + t2
        return eval4_Base(xs, ys, zs, ws)
    }

    fun eval4_XYZBeforeW(x: Double, y: Double, z: Double, w: Double): Double {
        val xyz = x + y + z
        val ww = w * 0.2236067977499788
        val s2 = xyz * -0.16666666666666666 + ww
        val xs = x + s2
        val ys = y + s2
        val zs = z + s2
        val ws = -0.5 * xyz + ww
        return eval4_Base(xs, ys, zs, ws)
    }

    // 4D OpenSimplex Noise.
    private fun eval4_Base(xs: Double, ys: Double, zs: Double, ws: Double): Double {

        // Floor to get simplectic honeycomb coordinates of rhombo-hypercube super-cell origin.
        val xsb = fastFloor(xs)
        val ysb = fastFloor(ys)
        val zsb = fastFloor(zs)
        val wsb = fastFloor(ws)

        // Compute simplectic honeycomb coordinates relative to rhombo-hypercube origin.
        val xins = xs - xsb
        val yins = ys - ysb
        val zins = zs - zsb
        val wins = ws - wsb

        // Sum those together to get a value that determines which region we're in.
        val inSum = xins + yins + zins + wins

        // Positions relative to origin point.
        val squishOffsetIns = inSum * SQUISH_CONSTANT_4D
        var dx0 = xins + squishOffsetIns
        var dy0 = yins + squishOffsetIns
        var dz0 = zins + squishOffsetIns
        var dw0 = wins + squishOffsetIns

        // We'll be defining these inside the next block and using them afterwards.
        var dx_ext0: Double
        var dy_ext0: Double
        var dz_ext0: Double
        var dw_ext0: Double
        var dx_ext1: Double
        var dy_ext1: Double
        var dz_ext1: Double
        var dw_ext1: Double
        var dx_ext2: Double
        var dy_ext2: Double
        var dz_ext2: Double
        var dw_ext2: Double
        var xsv_ext0: Int
        var ysv_ext0: Int
        var zsv_ext0: Int
        var wsv_ext0: Int
        var xsv_ext1: Int
        var ysv_ext1: Int
        var zsv_ext1: Int
        var wsv_ext1: Int
        var xsv_ext2: Int
        var ysv_ext2: Int
        var zsv_ext2: Int
        var wsv_ext2: Int
        var value = 0.0
        if (inSum <= 1) { // We're inside the pentachoron (4-Simplex) at (0,0,0,0)

            // Determine which two of (0,0,0,1), (0,0,1,0), (0,1,0,0), (1,0,0,0) are closest.
            var aPoint: Byte = 0x01
            var aScore = xins
            var bPoint: Byte = 0x02
            var bScore = yins
            if (aScore >= bScore && zins > bScore) {
                bScore = zins
                bPoint = 0x04
            } else if (aScore < bScore && zins > aScore) {
                aScore = zins
                aPoint = 0x04
            }
            if (aScore >= bScore && wins > bScore) {
                bScore = wins
                bPoint = 0x08
            } else if (aScore < bScore && wins > aScore) {
                aScore = wins
                aPoint = 0x08
            }

            // Now we determine the three lattice points not part of the pentachoron that may contribute.
            // This depends on the closest two pentachoron vertices, including (0,0,0,0)
            val uins = 1 - inSum
            if (uins > aScore || uins > bScore) { // (0,0,0,0) is one of the closest two pentachoron vertices.
                val c =
                    if (bScore > aScore) bPoint else aPoint // Our other closest vertex is the closest out of a and b.
                if (c and 0x01 == 0) {
                    xsv_ext0 = xsb - 1
                    xsv_ext2 = xsb
                    xsv_ext1 = xsv_ext2
                    dx_ext0 = dx0 + 1
                    dx_ext2 = dx0
                    dx_ext1 = dx_ext2
                } else {
                    xsv_ext2 = xsb + 1
                    xsv_ext1 = xsv_ext2
                    xsv_ext0 = xsv_ext1
                    dx_ext2 = dx0 - 1
                    dx_ext1 = dx_ext2
                    dx_ext0 = dx_ext1
                }
                if (c and 0x02 == 0) {
                    ysv_ext2 = ysb
                    ysv_ext1 = ysv_ext2
                    ysv_ext0 = ysv_ext1
                    dy_ext2 = dy0
                    dy_ext1 = dy_ext2
                    dy_ext0 = dy_ext1
                    if (c and 0x01 == 0x01) {
                        ysv_ext0 -= 1
                        dy_ext0 += 1.0
                    } else {
                        ysv_ext1 -= 1
                        dy_ext1 += 1.0
                    }
                } else {
                    ysv_ext2 = ysb + 1
                    ysv_ext1 = ysv_ext2
                    ysv_ext0 = ysv_ext1
                    dy_ext2 = dy0 - 1
                    dy_ext1 = dy_ext2
                    dy_ext0 = dy_ext1
                }
                if (c and 0x04 == 0) {
                    zsv_ext2 = zsb
                    zsv_ext1 = zsv_ext2
                    zsv_ext0 = zsv_ext1
                    dz_ext2 = dz0
                    dz_ext1 = dz_ext2
                    dz_ext0 = dz_ext1
                    if (c and 0x03 != 0) {
                        if (c and 0x03 == 0x03) {
                            zsv_ext0 -= 1
                            dz_ext0 += 1.0
                        } else {
                            zsv_ext1 -= 1
                            dz_ext1 += 1.0
                        }
                    } else {
                        zsv_ext2 -= 1
                        dz_ext2 += 1.0
                    }
                } else {
                    zsv_ext2 = zsb + 1
                    zsv_ext1 = zsv_ext2
                    zsv_ext0 = zsv_ext1
                    dz_ext2 = dz0 - 1
                    dz_ext1 = dz_ext2
                    dz_ext0 = dz_ext1
                }
                if (c and 0x08 == 0) {
                    wsv_ext1 = wsb
                    wsv_ext0 = wsv_ext1
                    wsv_ext2 = wsb - 1
                    dw_ext1 = dw0
                    dw_ext0 = dw_ext1
                    dw_ext2 = dw0 + 1
                } else {
                    wsv_ext2 = wsb + 1
                    wsv_ext1 = wsv_ext2
                    wsv_ext0 = wsv_ext1
                    dw_ext2 = dw0 - 1
                    dw_ext1 = dw_ext2
                    dw_ext0 = dw_ext1
                }
            } else { // (0,0,0,0) is not one of the closest two pentachoron vertices.
                val c =
                    (aPoint or bPoint) as Byte // Our three extra vertices are determined by the closest two.
                if (c and 0x01 == 0) {
                    xsv_ext2 = xsb
                    xsv_ext0 = xsv_ext2
                    xsv_ext1 = xsb - 1
                    dx_ext0 = dx0 - 2 * SQUISH_CONSTANT_4D
                    dx_ext1 = dx0 + 1 - SQUISH_CONSTANT_4D
                    dx_ext2 = dx0 - SQUISH_CONSTANT_4D
                } else {
                    xsv_ext2 = xsb + 1
                    xsv_ext1 = xsv_ext2
                    xsv_ext0 = xsv_ext1
                    dx_ext0 = dx0 - 1 - 2 * SQUISH_CONSTANT_4D
                    dx_ext2 = dx0 - 1 - SQUISH_CONSTANT_4D
                    dx_ext1 = dx_ext2
                }
                if (c and 0x02 == 0) {
                    ysv_ext2 = ysb
                    ysv_ext1 = ysv_ext2
                    ysv_ext0 = ysv_ext1
                    dy_ext0 = dy0 - 2 * SQUISH_CONSTANT_4D
                    dy_ext2 = dy0 - SQUISH_CONSTANT_4D
                    dy_ext1 = dy_ext2
                    if (c and 0x01 == 0x01) {
                        ysv_ext1 -= 1
                        dy_ext1 += 1.0
                    } else {
                        ysv_ext2 -= 1
                        dy_ext2 += 1.0
                    }
                } else {
                    ysv_ext2 = ysb + 1
                    ysv_ext1 = ysv_ext2
                    ysv_ext0 = ysv_ext1
                    dy_ext0 = dy0 - 1 - 2 * SQUISH_CONSTANT_4D
                    dy_ext2 = dy0 - 1 - SQUISH_CONSTANT_4D
                    dy_ext1 = dy_ext2
                }
                if (c and 0x04 == 0) {
                    zsv_ext2 = zsb
                    zsv_ext1 = zsv_ext2
                    zsv_ext0 = zsv_ext1
                    dz_ext0 = dz0 - 2 * SQUISH_CONSTANT_4D
                    dz_ext2 = dz0 - SQUISH_CONSTANT_4D
                    dz_ext1 = dz_ext2
                    if (c and 0x03 == 0x03) {
                        zsv_ext1 -= 1
                        dz_ext1 += 1.0
                    } else {
                        zsv_ext2 -= 1
                        dz_ext2 += 1.0
                    }
                } else {
                    zsv_ext2 = zsb + 1
                    zsv_ext1 = zsv_ext2
                    zsv_ext0 = zsv_ext1
                    dz_ext0 = dz0 - 1 - 2 * SQUISH_CONSTANT_4D
                    dz_ext2 = dz0 - 1 - SQUISH_CONSTANT_4D
                    dz_ext1 = dz_ext2
                }
                if (c and 0x08 == 0) {
                    wsv_ext1 = wsb
                    wsv_ext0 = wsv_ext1
                    wsv_ext2 = wsb - 1
                    dw_ext0 = dw0 - 2 * SQUISH_CONSTANT_4D
                    dw_ext1 = dw0 - SQUISH_CONSTANT_4D
                    dw_ext2 = dw0 + 1 - SQUISH_CONSTANT_4D
                } else {
                    wsv_ext2 = wsb + 1
                    wsv_ext1 = wsv_ext2
                    wsv_ext0 = wsv_ext1
                    dw_ext0 = dw0 - 1 - 2 * SQUISH_CONSTANT_4D
                    dw_ext2 = dw0 - 1 - SQUISH_CONSTANT_4D
                    dw_ext1 = dw_ext2
                }
            }

            // Contribution (0,0,0,0)
            var attn0 = 2 - dx0 * dx0 - dy0 * dy0 - dz0 * dz0 - dw0 * dw0
            if (attn0 > 0) {
                attn0 *= attn0
                value += attn0 * attn0 * extrapolate(xsb + 0, ysb + 0, zsb + 0, wsb + 0, dx0, dy0, dz0, dw0)
            }

            // Contribution (1,0,0,0)
            val dx1 = dx0 - 1 - SQUISH_CONSTANT_4D
            val dy1 = dy0 - 0 - SQUISH_CONSTANT_4D
            val dz1 = dz0 - 0 - SQUISH_CONSTANT_4D
            val dw1 = dw0 - 0 - SQUISH_CONSTANT_4D
            var attn1 = 2 - dx1 * dx1 - dy1 * dy1 - dz1 * dz1 - dw1 * dw1
            if (attn1 > 0) {
                attn1 *= attn1
                value += attn1 * attn1 * extrapolate(xsb + 1, ysb + 0, zsb + 0, wsb + 0, dx1, dy1, dz1, dw1)
            }

            // Contribution (0,1,0,0)
            val dx2 = dx0 - 0 - SQUISH_CONSTANT_4D
            val dy2 = dy0 - 1 - SQUISH_CONSTANT_4D
            var attn2 = 2 - dx2 * dx2 - dy2 * dy2 - dz1 * dz1 - dw1 * dw1
            if (attn2 > 0) {
                attn2 *= attn2
                value += attn2 * attn2 * extrapolate(xsb + 0, ysb + 1, zsb + 0, wsb + 0, dx2, dy2, dz1, dw1)
            }

            // Contribution (0,0,1,0)
            val dz3 = dz0 - 1 - SQUISH_CONSTANT_4D
            var attn3 = 2 - dx2 * dx2 - dy1 * dy1 - dz3 * dz3 - dw1 * dw1
            if (attn3 > 0) {
                attn3 *= attn3
                value += attn3 * attn3 * extrapolate(xsb + 0, ysb + 0, zsb + 1, wsb + 0, dx2, dy1, dz3, dw1)
            }

            // Contribution (0,0,0,1)
            val dw4 = dw0 - 1 - SQUISH_CONSTANT_4D
            var attn4 = 2 - dx2 * dx2 - dy1 * dy1 - dz1 * dz1 - dw4 * dw4
            if (attn4 > 0) {
                attn4 *= attn4
                value += attn4 * attn4 * extrapolate(xsb + 0, ysb + 0, zsb + 0, wsb + 1, dx2, dy1, dz1, dw4)
            }
        } else if (inSum >= 3) { // We're inside the pentachoron (4-Simplex) at (1,1,1,1)
            // Determine which two of (1,1,1,0), (1,1,0,1), (1,0,1,1), (0,1,1,1) are closest.
            var aPoint: Byte = 0x0E
            var aScore = xins
            var bPoint: Byte = 0x0D
            var bScore = yins
            if (aScore <= bScore && zins < bScore) {
                bScore = zins
                bPoint = 0x0B
            } else if (aScore > bScore && zins < aScore) {
                aScore = zins
                aPoint = 0x0B
            }
            if (aScore <= bScore && wins < bScore) {
                bScore = wins
                bPoint = 0x07
            } else if (aScore > bScore && wins < aScore) {
                aScore = wins
                aPoint = 0x07
            }

            // Now we determine the three lattice points not part of the pentachoron that may contribute.
            // This depends on the closest two pentachoron vertices, including (0,0,0,0)
            val uins = 4 - inSum
            if (uins < aScore || uins < bScore) { // (1,1,1,1) is one of the closest two pentachoron vertices.
                val c =
                    if (bScore < aScore) bPoint else aPoint // Our other closest vertex is the closest out of a and b.
                if (c and 0x01 != 0) {
                    xsv_ext0 = xsb + 2
                    xsv_ext2 = xsb + 1
                    xsv_ext1 = xsv_ext2
                    dx_ext0 = dx0 - 2 - 4 * SQUISH_CONSTANT_4D
                    dx_ext2 = dx0 - 1 - 4 * SQUISH_CONSTANT_4D
                    dx_ext1 = dx_ext2
                } else {
                    xsv_ext2 = xsb
                    xsv_ext1 = xsv_ext2
                    xsv_ext0 = xsv_ext1
                    dx_ext2 = dx0 - 4 * SQUISH_CONSTANT_4D
                    dx_ext1 = dx_ext2
                    dx_ext0 = dx_ext1
                }
                if (c and 0x02 != 0) {
                    ysv_ext2 = ysb + 1
                    ysv_ext1 = ysv_ext2
                    ysv_ext0 = ysv_ext1
                    dy_ext2 = dy0 - 1 - 4 * SQUISH_CONSTANT_4D
                    dy_ext1 = dy_ext2
                    dy_ext0 = dy_ext1
                    if (c and 0x01 != 0) {
                        ysv_ext1 += 1
                        dy_ext1 -= 1.0
                    } else {
                        ysv_ext0 += 1
                        dy_ext0 -= 1.0
                    }
                } else {
                    ysv_ext2 = ysb
                    ysv_ext1 = ysv_ext2
                    ysv_ext0 = ysv_ext1
                    dy_ext2 = dy0 - 4 * SQUISH_CONSTANT_4D
                    dy_ext1 = dy_ext2
                    dy_ext0 = dy_ext1
                }
                if (c and 0x04 != 0) {
                    zsv_ext2 = zsb + 1
                    zsv_ext1 = zsv_ext2
                    zsv_ext0 = zsv_ext1
                    dz_ext2 = dz0 - 1 - 4 * SQUISH_CONSTANT_4D
                    dz_ext1 = dz_ext2
                    dz_ext0 = dz_ext1
                    if (c and 0x03 != 0x03) {
                        if (c and 0x03 == 0) {
                            zsv_ext0 += 1
                            dz_ext0 -= 1.0
                        } else {
                            zsv_ext1 += 1
                            dz_ext1 -= 1.0
                        }
                    } else {
                        zsv_ext2 += 1
                        dz_ext2 -= 1.0
                    }
                } else {
                    zsv_ext2 = zsb
                    zsv_ext1 = zsv_ext2
                    zsv_ext0 = zsv_ext1
                    dz_ext2 = dz0 - 4 * SQUISH_CONSTANT_4D
                    dz_ext1 = dz_ext2
                    dz_ext0 = dz_ext1
                }
                if (c and 0x08 != 0) {
                    wsv_ext1 = wsb + 1
                    wsv_ext0 = wsv_ext1
                    wsv_ext2 = wsb + 2
                    dw_ext1 = dw0 - 1 - 4 * SQUISH_CONSTANT_4D
                    dw_ext0 = dw_ext1
                    dw_ext2 = dw0 - 2 - 4 * SQUISH_CONSTANT_4D
                } else {
                    wsv_ext2 = wsb
                    wsv_ext1 = wsv_ext2
                    wsv_ext0 = wsv_ext1
                    dw_ext2 = dw0 - 4 * SQUISH_CONSTANT_4D
                    dw_ext1 = dw_ext2
                    dw_ext0 = dw_ext1
                }
            } else { // (1,1,1,1) is not one of the closest two pentachoron vertices.
                val c =
                    (aPoint and bPoint) as Byte // Our three extra vertices are determined by the closest two.
                if (c and 0x01 != 0) {
                    xsv_ext2 = xsb + 1
                    xsv_ext0 = xsv_ext2
                    xsv_ext1 = xsb + 2
                    dx_ext0 = dx0 - 1 - 2 * SQUISH_CONSTANT_4D
                    dx_ext1 = dx0 - 2 - 3 * SQUISH_CONSTANT_4D
                    dx_ext2 = dx0 - 1 - 3 * SQUISH_CONSTANT_4D
                } else {
                    xsv_ext2 = xsb
                    xsv_ext1 = xsv_ext2
                    xsv_ext0 = xsv_ext1
                    dx_ext0 = dx0 - 2 * SQUISH_CONSTANT_4D
                    dx_ext2 = dx0 - 3 * SQUISH_CONSTANT_4D
                    dx_ext1 = dx_ext2
                }
                if (c and 0x02 != 0) {
                    ysv_ext2 = ysb + 1
                    ysv_ext1 = ysv_ext2
                    ysv_ext0 = ysv_ext1
                    dy_ext0 = dy0 - 1 - 2 * SQUISH_CONSTANT_4D
                    dy_ext2 = dy0 - 1 - 3 * SQUISH_CONSTANT_4D
                    dy_ext1 = dy_ext2
                    if (c and 0x01 != 0) {
                        ysv_ext2 += 1
                        dy_ext2 -= 1.0
                    } else {
                        ysv_ext1 += 1
                        dy_ext1 -= 1.0
                    }
                } else {
                    ysv_ext2 = ysb
                    ysv_ext1 = ysv_ext2
                    ysv_ext0 = ysv_ext1
                    dy_ext0 = dy0 - 2 * SQUISH_CONSTANT_4D
                    dy_ext2 = dy0 - 3 * SQUISH_CONSTANT_4D
                    dy_ext1 = dy_ext2
                }
                if (c and 0x04 != 0) {
                    zsv_ext2 = zsb + 1
                    zsv_ext1 = zsv_ext2
                    zsv_ext0 = zsv_ext1
                    dz_ext0 = dz0 - 1 - 2 * SQUISH_CONSTANT_4D
                    dz_ext2 = dz0 - 1 - 3 * SQUISH_CONSTANT_4D
                    dz_ext1 = dz_ext2
                    if (c and 0x03 != 0) {
                        zsv_ext2 += 1
                        dz_ext2 -= 1.0
                    } else {
                        zsv_ext1 += 1
                        dz_ext1 -= 1.0
                    }
                } else {
                    zsv_ext2 = zsb
                    zsv_ext1 = zsv_ext2
                    zsv_ext0 = zsv_ext1
                    dz_ext0 = dz0 - 2 * SQUISH_CONSTANT_4D
                    dz_ext2 = dz0 - 3 * SQUISH_CONSTANT_4D
                    dz_ext1 = dz_ext2
                }
                if (c and 0x08 != 0) {
                    wsv_ext1 = wsb + 1
                    wsv_ext0 = wsv_ext1
                    wsv_ext2 = wsb + 2
                    dw_ext0 = dw0 - 1 - 2 * SQUISH_CONSTANT_4D
                    dw_ext1 = dw0 - 1 - 3 * SQUISH_CONSTANT_4D
                    dw_ext2 = dw0 - 2 - 3 * SQUISH_CONSTANT_4D
                } else {
                    wsv_ext2 = wsb
                    wsv_ext1 = wsv_ext2
                    wsv_ext0 = wsv_ext1
                    dw_ext0 = dw0 - 2 * SQUISH_CONSTANT_4D
                    dw_ext2 = dw0 - 3 * SQUISH_CONSTANT_4D
                    dw_ext1 = dw_ext2
                }
            }

            // Contribution (1,1,1,0)
            val dx4 = dx0 - 1 - 3 * SQUISH_CONSTANT_4D
            val dy4 = dy0 - 1 - 3 * SQUISH_CONSTANT_4D
            val dz4 = dz0 - 1 - 3 * SQUISH_CONSTANT_4D
            val dw4 = dw0 - 3 * SQUISH_CONSTANT_4D
            var attn4 = 2 - dx4 * dx4 - dy4 * dy4 - dz4 * dz4 - dw4 * dw4
            if (attn4 > 0) {
                attn4 *= attn4
                value += attn4 * attn4 * extrapolate(xsb + 1, ysb + 1, zsb + 1, wsb + 0, dx4, dy4, dz4, dw4)
            }

            // Contribution (1,1,0,1)
            val dz3 = dz0 - 3 * SQUISH_CONSTANT_4D
            val dw3 = dw0 - 1 - 3 * SQUISH_CONSTANT_4D
            var attn3 = 2 - dx4 * dx4 - dy4 * dy4 - dz3 * dz3 - dw3 * dw3
            if (attn3 > 0) {
                attn3 *= attn3
                value += attn3 * attn3 * extrapolate(xsb + 1, ysb + 1, zsb + 0, wsb + 1, dx4, dy4, dz3, dw3)
            }

            // Contribution (1,0,1,1)
            val dy2 = dy0 - 3 * SQUISH_CONSTANT_4D
            var attn2 = 2 - dx4 * dx4 - dy2 * dy2 - dz4 * dz4 - dw3 * dw3
            if (attn2 > 0) {
                attn2 *= attn2
                value += attn2 * attn2 * extrapolate(xsb + 1, ysb + 0, zsb + 1, wsb + 1, dx4, dy2, dz4, dw3)
            }

            // Contribution (0,1,1,1)
            val dx1 = dx0 - 3 * SQUISH_CONSTANT_4D
            var attn1 = 2 - dx1 * dx1 - dy4 * dy4 - dz4 * dz4 - dw3 * dw3
            if (attn1 > 0) {
                attn1 *= attn1
                value += attn1 * attn1 * extrapolate(xsb + 0, ysb + 1, zsb + 1, wsb + 1, dx1, dy4, dz4, dw3)
            }

            // Contribution (1,1,1,1)
            dx0 = dx0 - 1 - 4 * SQUISH_CONSTANT_4D
            dy0 = dy0 - 1 - 4 * SQUISH_CONSTANT_4D
            dz0 = dz0 - 1 - 4 * SQUISH_CONSTANT_4D
            dw0 = dw0 - 1 - 4 * SQUISH_CONSTANT_4D
            var attn0 = 2 - dx0 * dx0 - dy0 * dy0 - dz0 * dz0 - dw0 * dw0
            if (attn0 > 0) {
                attn0 *= attn0
                value += attn0 * attn0 * extrapolate(xsb + 1, ysb + 1, zsb + 1, wsb + 1, dx0, dy0, dz0, dw0)
            }
        } else if (inSum <= 2) { // We're inside the first dispentachoron (Rectified 4-Simplex)
            var aScore: Double
            var aPoint: Byte
            var aIsBiggerSide = true
            var bScore: Double
            var bPoint: Byte
            var bIsBiggerSide = true

            // Decide between (1,1,0,0) and (0,0,1,1)
            if (xins + yins > zins + wins) {
                aScore = xins + yins
                aPoint = 0x03
            } else {
                aScore = zins + wins
                aPoint = 0x0C
            }

            // Decide between (1,0,1,0) and (0,1,0,1)
            if (xins + zins > yins + wins) {
                bScore = xins + zins
                bPoint = 0x05
            } else {
                bScore = yins + wins
                bPoint = 0x0A
            }

            // Closer between (1,0,0,1) and (0,1,1,0) will replace the further of a and b, if closer.
            if (xins + wins > yins + zins) {
                val score = xins + wins
                if (aScore >= bScore && score > bScore) {
                    bScore = score
                    bPoint = 0x09
                } else if (aScore < bScore && score > aScore) {
                    aScore = score
                    aPoint = 0x09
                }
            } else {
                val score = yins + zins
                if (aScore >= bScore && score > bScore) {
                    bScore = score
                    bPoint = 0x06
                } else if (aScore < bScore && score > aScore) {
                    aScore = score
                    aPoint = 0x06
                }
            }

            // Decide if (1,0,0,0) is closer.
            val p1 = 2 - inSum + xins
            if (aScore >= bScore && p1 > bScore) {
                bScore = p1
                bPoint = 0x01
                bIsBiggerSide = false
            } else if (aScore < bScore && p1 > aScore) {
                aScore = p1
                aPoint = 0x01
                aIsBiggerSide = false
            }

            // Decide if (0,1,0,0) is closer.
            val p2 = 2 - inSum + yins
            if (aScore >= bScore && p2 > bScore) {
                bScore = p2
                bPoint = 0x02
                bIsBiggerSide = false
            } else if (aScore < bScore && p2 > aScore) {
                aScore = p2
                aPoint = 0x02
                aIsBiggerSide = false
            }

            // Decide if (0,0,1,0) is closer.
            val p3 = 2 - inSum + zins
            if (aScore >= bScore && p3 > bScore) {
                bScore = p3
                bPoint = 0x04
                bIsBiggerSide = false
            } else if (aScore < bScore && p3 > aScore) {
                aScore = p3
                aPoint = 0x04
                aIsBiggerSide = false
            }

            // Decide if (0,0,0,1) is closer.
            val p4 = 2 - inSum + wins
            if (aScore >= bScore && p4 > bScore) {
                bScore = p4
                bPoint = 0x08
                bIsBiggerSide = false
            } else if (aScore < bScore && p4 > aScore) {
                aScore = p4
                aPoint = 0x08
                aIsBiggerSide = false
            }

            // Where each of the two closest points are determines how the extra three vertices are calculated.
            if (aIsBiggerSide == bIsBiggerSide) {
                if (aIsBiggerSide) { // Both closest points on the bigger side
                    val c1 = (aPoint or bPoint) as Byte
                    val c2 = (aPoint and bPoint) as Byte
                    if (c1 and 0x01 == 0) {
                        xsv_ext0 = xsb
                        xsv_ext1 = xsb - 1
                        dx_ext0 = dx0 - 3 * SQUISH_CONSTANT_4D
                        dx_ext1 = dx0 + 1 - 2 * SQUISH_CONSTANT_4D
                    } else {
                        xsv_ext1 = xsb + 1
                        xsv_ext0 = xsv_ext1
                        dx_ext0 = dx0 - 1 - 3 * SQUISH_CONSTANT_4D
                        dx_ext1 = dx0 - 1 - 2 * SQUISH_CONSTANT_4D
                    }
                    if (c1 and 0x02 == 0) {
                        ysv_ext0 = ysb
                        ysv_ext1 = ysb - 1
                        dy_ext0 = dy0 - 3 * SQUISH_CONSTANT_4D
                        dy_ext1 = dy0 + 1 - 2 * SQUISH_CONSTANT_4D
                    } else {
                        ysv_ext1 = ysb + 1
                        ysv_ext0 = ysv_ext1
                        dy_ext0 = dy0 - 1 - 3 * SQUISH_CONSTANT_4D
                        dy_ext1 = dy0 - 1 - 2 * SQUISH_CONSTANT_4D
                    }
                    if (c1 and 0x04 == 0) {
                        zsv_ext0 = zsb
                        zsv_ext1 = zsb - 1
                        dz_ext0 = dz0 - 3 * SQUISH_CONSTANT_4D
                        dz_ext1 = dz0 + 1 - 2 * SQUISH_CONSTANT_4D
                    } else {
                        zsv_ext1 = zsb + 1
                        zsv_ext0 = zsv_ext1
                        dz_ext0 = dz0 - 1 - 3 * SQUISH_CONSTANT_4D
                        dz_ext1 = dz0 - 1 - 2 * SQUISH_CONSTANT_4D
                    }
                    if (c1 and 0x08 == 0) {
                        wsv_ext0 = wsb
                        wsv_ext1 = wsb - 1
                        dw_ext0 = dw0 - 3 * SQUISH_CONSTANT_4D
                        dw_ext1 = dw0 + 1 - 2 * SQUISH_CONSTANT_4D
                    } else {
                        wsv_ext1 = wsb + 1
                        wsv_ext0 = wsv_ext1
                        dw_ext0 = dw0 - 1 - 3 * SQUISH_CONSTANT_4D
                        dw_ext1 = dw0 - 1 - 2 * SQUISH_CONSTANT_4D
                    }

                    // One combination is a permutation of (0,0,0,2) based on c2
                    xsv_ext2 = xsb
                    ysv_ext2 = ysb
                    zsv_ext2 = zsb
                    wsv_ext2 = wsb
                    dx_ext2 = dx0 - 2 * SQUISH_CONSTANT_4D
                    dy_ext2 = dy0 - 2 * SQUISH_CONSTANT_4D
                    dz_ext2 = dz0 - 2 * SQUISH_CONSTANT_4D
                    dw_ext2 = dw0 - 2 * SQUISH_CONSTANT_4D
                    if (c2 and 0x01 != 0) {
                        xsv_ext2 += 2
                        dx_ext2 -= 2.0
                    } else if (c2 and 0x02 != 0) {
                        ysv_ext2 += 2
                        dy_ext2 -= 2.0
                    } else if (c2 and 0x04 != 0) {
                        zsv_ext2 += 2
                        dz_ext2 -= 2.0
                    } else {
                        wsv_ext2 += 2
                        dw_ext2 -= 2.0
                    }
                } else { // Both closest points on the smaller side
                    // One of the two extra points is (0,0,0,0)
                    xsv_ext2 = xsb
                    ysv_ext2 = ysb
                    zsv_ext2 = zsb
                    wsv_ext2 = wsb
                    dx_ext2 = dx0
                    dy_ext2 = dy0
                    dz_ext2 = dz0
                    dw_ext2 = dw0

                    // Other two points are based on the omitted axes.
                    val c = (aPoint or bPoint) as Byte
                    if (c and 0x01 == 0) {
                        xsv_ext0 = xsb - 1
                        xsv_ext1 = xsb
                        dx_ext0 = dx0 + 1 - SQUISH_CONSTANT_4D
                        dx_ext1 = dx0 - SQUISH_CONSTANT_4D
                    } else {
                        xsv_ext1 = xsb + 1
                        xsv_ext0 = xsv_ext1
                        dx_ext1 = dx0 - 1 - SQUISH_CONSTANT_4D
                        dx_ext0 = dx_ext1
                    }
                    if (c and 0x02 == 0) {
                        ysv_ext1 = ysb
                        ysv_ext0 = ysv_ext1
                        dy_ext1 = dy0 - SQUISH_CONSTANT_4D
                        dy_ext0 = dy_ext1
                        if (c and 0x01 == 0x01) {
                            ysv_ext0 -= 1
                            dy_ext0 += 1.0
                        } else {
                            ysv_ext1 -= 1
                            dy_ext1 += 1.0
                        }
                    } else {
                        ysv_ext1 = ysb + 1
                        ysv_ext0 = ysv_ext1
                        dy_ext1 = dy0 - 1 - SQUISH_CONSTANT_4D
                        dy_ext0 = dy_ext1
                    }
                    if (c and 0x04 == 0) {
                        zsv_ext1 = zsb
                        zsv_ext0 = zsv_ext1
                        dz_ext1 = dz0 - SQUISH_CONSTANT_4D
                        dz_ext0 = dz_ext1
                        if (c and 0x03 == 0x03) {
                            zsv_ext0 -= 1
                            dz_ext0 += 1.0
                        } else {
                            zsv_ext1 -= 1
                            dz_ext1 += 1.0
                        }
                    } else {
                        zsv_ext1 = zsb + 1
                        zsv_ext0 = zsv_ext1
                        dz_ext1 = dz0 - 1 - SQUISH_CONSTANT_4D
                        dz_ext0 = dz_ext1
                    }
                    if (c and 0x08 == 0) {
                        wsv_ext0 = wsb
                        wsv_ext1 = wsb - 1
                        dw_ext0 = dw0 - SQUISH_CONSTANT_4D
                        dw_ext1 = dw0 + 1 - SQUISH_CONSTANT_4D
                    } else {
                        wsv_ext1 = wsb + 1
                        wsv_ext0 = wsv_ext1
                        dw_ext1 = dw0 - 1 - SQUISH_CONSTANT_4D
                        dw_ext0 = dw_ext1
                    }
                }
            } else { // One point on each "side"
                val c1: Byte
                val c2: Byte
                if (aIsBiggerSide) {
                    c1 = aPoint
                    c2 = bPoint
                } else {
                    c1 = bPoint
                    c2 = aPoint
                }

                // Two contributions are the bigger-sided point with each 0 replaced with -1.
                if (c1 and 0x01 == 0) {
                    xsv_ext0 = xsb - 1
                    xsv_ext1 = xsb
                    dx_ext0 = dx0 + 1 - SQUISH_CONSTANT_4D
                    dx_ext1 = dx0 - SQUISH_CONSTANT_4D
                } else {
                    xsv_ext1 = xsb + 1
                    xsv_ext0 = xsv_ext1
                    dx_ext1 = dx0 - 1 - SQUISH_CONSTANT_4D
                    dx_ext0 = dx_ext1
                }
                if (c1 and 0x02 == 0) {
                    ysv_ext1 = ysb
                    ysv_ext0 = ysv_ext1
                    dy_ext1 = dy0 - SQUISH_CONSTANT_4D
                    dy_ext0 = dy_ext1
                    if (c1 and 0x01 == 0x01) {
                        ysv_ext0 -= 1
                        dy_ext0 += 1.0
                    } else {
                        ysv_ext1 -= 1
                        dy_ext1 += 1.0
                    }
                } else {
                    ysv_ext1 = ysb + 1
                    ysv_ext0 = ysv_ext1
                    dy_ext1 = dy0 - 1 - SQUISH_CONSTANT_4D
                    dy_ext0 = dy_ext1
                }
                if (c1 and 0x04 == 0) {
                    zsv_ext1 = zsb
                    zsv_ext0 = zsv_ext1
                    dz_ext1 = dz0 - SQUISH_CONSTANT_4D
                    dz_ext0 = dz_ext1
                    if (c1 and 0x03 == 0x03) {
                        zsv_ext0 -= 1
                        dz_ext0 += 1.0
                    } else {
                        zsv_ext1 -= 1
                        dz_ext1 += 1.0
                    }
                } else {
                    zsv_ext1 = zsb + 1
                    zsv_ext0 = zsv_ext1
                    dz_ext1 = dz0 - 1 - SQUISH_CONSTANT_4D
                    dz_ext0 = dz_ext1
                }
                if (c1 and 0x08 == 0) {
                    wsv_ext0 = wsb
                    wsv_ext1 = wsb - 1
                    dw_ext0 = dw0 - SQUISH_CONSTANT_4D
                    dw_ext1 = dw0 + 1 - SQUISH_CONSTANT_4D
                } else {
                    wsv_ext1 = wsb + 1
                    wsv_ext0 = wsv_ext1
                    dw_ext1 = dw0 - 1 - SQUISH_CONSTANT_4D
                    dw_ext0 = dw_ext1
                }

                // One contribution is a permutation of (0,0,0,2) based on the smaller-sided point
                xsv_ext2 = xsb
                ysv_ext2 = ysb
                zsv_ext2 = zsb
                wsv_ext2 = wsb
                dx_ext2 = dx0 - 2 * SQUISH_CONSTANT_4D
                dy_ext2 = dy0 - 2 * SQUISH_CONSTANT_4D
                dz_ext2 = dz0 - 2 * SQUISH_CONSTANT_4D
                dw_ext2 = dw0 - 2 * SQUISH_CONSTANT_4D
                if (c2 and 0x01 != 0) {
                    xsv_ext2 += 2
                    dx_ext2 -= 2.0
                } else if (c2 and 0x02 != 0) {
                    ysv_ext2 += 2
                    dy_ext2 -= 2.0
                } else if (c2 and 0x04 != 0) {
                    zsv_ext2 += 2
                    dz_ext2 -= 2.0
                } else {
                    wsv_ext2 += 2
                    dw_ext2 -= 2.0
                }
            }

            // Contribution (1,0,0,0)
            val dx1 = dx0 - 1 - SQUISH_CONSTANT_4D
            val dy1 = dy0 - 0 - SQUISH_CONSTANT_4D
            val dz1 = dz0 - 0 - SQUISH_CONSTANT_4D
            val dw1 = dw0 - 0 - SQUISH_CONSTANT_4D
            var attn1 = 2 - dx1 * dx1 - dy1 * dy1 - dz1 * dz1 - dw1 * dw1
            if (attn1 > 0) {
                attn1 *= attn1
                value += attn1 * attn1 * extrapolate(xsb + 1, ysb + 0, zsb + 0, wsb + 0, dx1, dy1, dz1, dw1)
            }

            // Contribution (0,1,0,0)
            val dx2 = dx0 - 0 - SQUISH_CONSTANT_4D
            val dy2 = dy0 - 1 - SQUISH_CONSTANT_4D
            var attn2 = 2 - dx2 * dx2 - dy2 * dy2 - dz1 * dz1 - dw1 * dw1
            if (attn2 > 0) {
                attn2 *= attn2
                value += attn2 * attn2 * extrapolate(xsb + 0, ysb + 1, zsb + 0, wsb + 0, dx2, dy2, dz1, dw1)
            }

            // Contribution (0,0,1,0)
            val dz3 = dz0 - 1 - SQUISH_CONSTANT_4D
            var attn3 = 2 - dx2 * dx2 - dy1 * dy1 - dz3 * dz3 - dw1 * dw1
            if (attn3 > 0) {
                attn3 *= attn3
                value += attn3 * attn3 * extrapolate(xsb + 0, ysb + 0, zsb + 1, wsb + 0, dx2, dy1, dz3, dw1)
            }

            // Contribution (0,0,0,1)
            val dw4 = dw0 - 1 - SQUISH_CONSTANT_4D
            var attn4 = 2 - dx2 * dx2 - dy1 * dy1 - dz1 * dz1 - dw4 * dw4
            if (attn4 > 0) {
                attn4 *= attn4
                value += attn4 * attn4 * extrapolate(xsb + 0, ysb + 0, zsb + 0, wsb + 1, dx2, dy1, dz1, dw4)
            }

            // Contribution (1,1,0,0)
            val dx5 = dx0 - 1 - 2 * SQUISH_CONSTANT_4D
            val dy5 = dy0 - 1 - 2 * SQUISH_CONSTANT_4D
            val dz5 = dz0 - 0 - 2 * SQUISH_CONSTANT_4D
            val dw5 = dw0 - 0 - 2 * SQUISH_CONSTANT_4D
            var attn5 = 2 - dx5 * dx5 - dy5 * dy5 - dz5 * dz5 - dw5 * dw5
            if (attn5 > 0) {
                attn5 *= attn5
                value += attn5 * attn5 * extrapolate(xsb + 1, ysb + 1, zsb + 0, wsb + 0, dx5, dy5, dz5, dw5)
            }

            // Contribution (1,0,1,0)
            val dx6 = dx0 - 1 - 2 * SQUISH_CONSTANT_4D
            val dy6 = dy0 - 0 - 2 * SQUISH_CONSTANT_4D
            val dz6 = dz0 - 1 - 2 * SQUISH_CONSTANT_4D
            val dw6 = dw0 - 0 - 2 * SQUISH_CONSTANT_4D
            var attn6 = 2 - dx6 * dx6 - dy6 * dy6 - dz6 * dz6 - dw6 * dw6
            if (attn6 > 0) {
                attn6 *= attn6
                value += attn6 * attn6 * extrapolate(xsb + 1, ysb + 0, zsb + 1, wsb + 0, dx6, dy6, dz6, dw6)
            }

            // Contribution (1,0,0,1)
            val dx7 = dx0 - 1 - 2 * SQUISH_CONSTANT_4D
            val dy7 = dy0 - 0 - 2 * SQUISH_CONSTANT_4D
            val dz7 = dz0 - 0 - 2 * SQUISH_CONSTANT_4D
            val dw7 = dw0 - 1 - 2 * SQUISH_CONSTANT_4D
            var attn7 = 2 - dx7 * dx7 - dy7 * dy7 - dz7 * dz7 - dw7 * dw7
            if (attn7 > 0) {
                attn7 *= attn7
                value += attn7 * attn7 * extrapolate(xsb + 1, ysb + 0, zsb + 0, wsb + 1, dx7, dy7, dz7, dw7)
            }

            // Contribution (0,1,1,0)
            val dx8 = dx0 - 0 - 2 * SQUISH_CONSTANT_4D
            val dy8 = dy0 - 1 - 2 * SQUISH_CONSTANT_4D
            val dz8 = dz0 - 1 - 2 * SQUISH_CONSTANT_4D
            val dw8 = dw0 - 0 - 2 * SQUISH_CONSTANT_4D
            var attn8 = 2 - dx8 * dx8 - dy8 * dy8 - dz8 * dz8 - dw8 * dw8
            if (attn8 > 0) {
                attn8 *= attn8
                value += attn8 * attn8 * extrapolate(xsb + 0, ysb + 1, zsb + 1, wsb + 0, dx8, dy8, dz8, dw8)
            }

            // Contribution (0,1,0,1)
            val dx9 = dx0 - 0 - 2 * SQUISH_CONSTANT_4D
            val dy9 = dy0 - 1 - 2 * SQUISH_CONSTANT_4D
            val dz9 = dz0 - 0 - 2 * SQUISH_CONSTANT_4D
            val dw9 = dw0 - 1 - 2 * SQUISH_CONSTANT_4D
            var attn9 = 2 - dx9 * dx9 - dy9 * dy9 - dz9 * dz9 - dw9 * dw9
            if (attn9 > 0) {
                attn9 *= attn9
                value += attn9 * attn9 * extrapolate(xsb + 0, ysb + 1, zsb + 0, wsb + 1, dx9, dy9, dz9, dw9)
            }

            // Contribution (0,0,1,1)
            val dx10 = dx0 - 0 - 2 * SQUISH_CONSTANT_4D
            val dy10 = dy0 - 0 - 2 * SQUISH_CONSTANT_4D
            val dz10 = dz0 - 1 - 2 * SQUISH_CONSTANT_4D
            val dw10 = dw0 - 1 - 2 * SQUISH_CONSTANT_4D
            var attn10 = 2 - dx10 * dx10 - dy10 * dy10 - dz10 * dz10 - dw10 * dw10
            if (attn10 > 0) {
                attn10 *= attn10
                value += attn10 * attn10 * extrapolate(xsb + 0, ysb + 0, zsb + 1, wsb + 1, dx10, dy10, dz10, dw10)
            }
        } else { // We're inside the second dispentachoron (Rectified 4-Simplex)
            var aScore: Double
            var aPoint: Byte
            var aIsBiggerSide = true
            var bScore: Double
            var bPoint: Byte
            var bIsBiggerSide = true

            // Decide between (0,0,1,1) and (1,1,0,0)
            if (xins + yins < zins + wins) {
                aScore = xins + yins
                aPoint = 0x0C
            } else {
                aScore = zins + wins
                aPoint = 0x03
            }

            // Decide between (0,1,0,1) and (1,0,1,0)
            if (xins + zins < yins + wins) {
                bScore = xins + zins
                bPoint = 0x0A
            } else {
                bScore = yins + wins
                bPoint = 0x05
            }

            // Closer between (0,1,1,0) and (1,0,0,1) will replace the further of a and b, if closer.
            if (xins + wins < yins + zins) {
                val score = xins + wins
                if (aScore <= bScore && score < bScore) {
                    bScore = score
                    bPoint = 0x06
                } else if (aScore > bScore && score < aScore) {
                    aScore = score
                    aPoint = 0x06
                }
            } else {
                val score = yins + zins
                if (aScore <= bScore && score < bScore) {
                    bScore = score
                    bPoint = 0x09
                } else if (aScore > bScore && score < aScore) {
                    aScore = score
                    aPoint = 0x09
                }
            }

            // Decide if (0,1,1,1) is closer.
            val p1 = 3 - inSum + xins
            if (aScore <= bScore && p1 < bScore) {
                bScore = p1
                bPoint = 0x0E
                bIsBiggerSide = false
            } else if (aScore > bScore && p1 < aScore) {
                aScore = p1
                aPoint = 0x0E
                aIsBiggerSide = false
            }

            // Decide if (1,0,1,1) is closer.
            val p2 = 3 - inSum + yins
            if (aScore <= bScore && p2 < bScore) {
                bScore = p2
                bPoint = 0x0D
                bIsBiggerSide = false
            } else if (aScore > bScore && p2 < aScore) {
                aScore = p2
                aPoint = 0x0D
                aIsBiggerSide = false
            }

            // Decide if (1,1,0,1) is closer.
            val p3 = 3 - inSum + zins
            if (aScore <= bScore && p3 < bScore) {
                bScore = p3
                bPoint = 0x0B
                bIsBiggerSide = false
            } else if (aScore > bScore && p3 < aScore) {
                aScore = p3
                aPoint = 0x0B
                aIsBiggerSide = false
            }

            // Decide if (1,1,1,0) is closer.
            val p4 = 3 - inSum + wins
            if (aScore <= bScore && p4 < bScore) {
                bScore = p4
                bPoint = 0x07
                bIsBiggerSide = false
            } else if (aScore > bScore && p4 < aScore) {
                aScore = p4
                aPoint = 0x07
                aIsBiggerSide = false
            }

            // Where each of the two closest points are determines how the extra three vertices are calculated.
            if (aIsBiggerSide == bIsBiggerSide) {
                if (aIsBiggerSide) { // Both closest points on the bigger side
                    val c1 = (aPoint and bPoint) as Byte
                    val c2 = (aPoint or bPoint) as Byte

                    // Two contributions are permutations of (0,0,0,1) and (0,0,0,2) based on c1
                    xsv_ext1 = xsb
                    xsv_ext0 = xsv_ext1
                    ysv_ext1 = ysb
                    ysv_ext0 = ysv_ext1
                    zsv_ext1 = zsb
                    zsv_ext0 = zsv_ext1
                    wsv_ext1 = wsb
                    wsv_ext0 = wsv_ext1
                    dx_ext0 = dx0 - SQUISH_CONSTANT_4D
                    dy_ext0 = dy0 - SQUISH_CONSTANT_4D
                    dz_ext0 = dz0 - SQUISH_CONSTANT_4D
                    dw_ext0 = dw0 - SQUISH_CONSTANT_4D
                    dx_ext1 = dx0 - 2 * SQUISH_CONSTANT_4D
                    dy_ext1 = dy0 - 2 * SQUISH_CONSTANT_4D
                    dz_ext1 = dz0 - 2 * SQUISH_CONSTANT_4D
                    dw_ext1 = dw0 - 2 * SQUISH_CONSTANT_4D
                    if (c1 and 0x01 != 0) {
                        xsv_ext0 += 1
                        dx_ext0 -= 1.0
                        xsv_ext1 += 2
                        dx_ext1 -= 2.0
                    } else if (c1 and 0x02 != 0) {
                        ysv_ext0 += 1
                        dy_ext0 -= 1.0
                        ysv_ext1 += 2
                        dy_ext1 -= 2.0
                    } else if (c1 and 0x04 != 0) {
                        zsv_ext0 += 1
                        dz_ext0 -= 1.0
                        zsv_ext1 += 2
                        dz_ext1 -= 2.0
                    } else {
                        wsv_ext0 += 1
                        dw_ext0 -= 1.0
                        wsv_ext1 += 2
                        dw_ext1 -= 2.0
                    }

                    // One contribution is a permutation of (1,1,1,-1) based on c2
                    xsv_ext2 = xsb + 1
                    ysv_ext2 = ysb + 1
                    zsv_ext2 = zsb + 1
                    wsv_ext2 = wsb + 1
                    dx_ext2 = dx0 - 1 - 2 * SQUISH_CONSTANT_4D
                    dy_ext2 = dy0 - 1 - 2 * SQUISH_CONSTANT_4D
                    dz_ext2 = dz0 - 1 - 2 * SQUISH_CONSTANT_4D
                    dw_ext2 = dw0 - 1 - 2 * SQUISH_CONSTANT_4D
                    if (c2 and 0x01 == 0) {
                        xsv_ext2 -= 2
                        dx_ext2 += 2.0
                    } else if (c2 and 0x02 == 0) {
                        ysv_ext2 -= 2
                        dy_ext2 += 2.0
                    } else if (c2 and 0x04 == 0) {
                        zsv_ext2 -= 2
                        dz_ext2 += 2.0
                    } else {
                        wsv_ext2 -= 2
                        dw_ext2 += 2.0
                    }
                } else { // Both closest points on the smaller side
                    // One of the two extra points is (1,1,1,1)
                    xsv_ext2 = xsb + 1
                    ysv_ext2 = ysb + 1
                    zsv_ext2 = zsb + 1
                    wsv_ext2 = wsb + 1
                    dx_ext2 = dx0 - 1 - 4 * SQUISH_CONSTANT_4D
                    dy_ext2 = dy0 - 1 - 4 * SQUISH_CONSTANT_4D
                    dz_ext2 = dz0 - 1 - 4 * SQUISH_CONSTANT_4D
                    dw_ext2 = dw0 - 1 - 4 * SQUISH_CONSTANT_4D

                    // Other two points are based on the shared axes.
                    val c = (aPoint and bPoint) as Byte
                    if (c and 0x01 != 0) {
                        xsv_ext0 = xsb + 2
                        xsv_ext1 = xsb + 1
                        dx_ext0 = dx0 - 2 - 3 * SQUISH_CONSTANT_4D
                        dx_ext1 = dx0 - 1 - 3 * SQUISH_CONSTANT_4D
                    } else {
                        xsv_ext1 = xsb
                        xsv_ext0 = xsv_ext1
                        dx_ext1 = dx0 - 3 * SQUISH_CONSTANT_4D
                        dx_ext0 = dx_ext1
                    }
                    if (c and 0x02 != 0) {
                        ysv_ext1 = ysb + 1
                        ysv_ext0 = ysv_ext1
                        dy_ext1 = dy0 - 1 - 3 * SQUISH_CONSTANT_4D
                        dy_ext0 = dy_ext1
                        if (c and 0x01 == 0) {
                            ysv_ext0 += 1
                            dy_ext0 -= 1.0
                        } else {
                            ysv_ext1 += 1
                            dy_ext1 -= 1.0
                        }
                    } else {
                        ysv_ext1 = ysb
                        ysv_ext0 = ysv_ext1
                        dy_ext1 = dy0 - 3 * SQUISH_CONSTANT_4D
                        dy_ext0 = dy_ext1
                    }
                    if (c and 0x04 != 0) {
                        zsv_ext1 = zsb + 1
                        zsv_ext0 = zsv_ext1
                        dz_ext1 = dz0 - 1 - 3 * SQUISH_CONSTANT_4D
                        dz_ext0 = dz_ext1
                        if (c and 0x03 == 0) {
                            zsv_ext0 += 1
                            dz_ext0 -= 1.0
                        } else {
                            zsv_ext1 += 1
                            dz_ext1 -= 1.0
                        }
                    } else {
                        zsv_ext1 = zsb
                        zsv_ext0 = zsv_ext1
                        dz_ext1 = dz0 - 3 * SQUISH_CONSTANT_4D
                        dz_ext0 = dz_ext1
                    }
                    if (c and 0x08 != 0) {
                        wsv_ext0 = wsb + 1
                        wsv_ext1 = wsb + 2
                        dw_ext0 = dw0 - 1 - 3 * SQUISH_CONSTANT_4D
                        dw_ext1 = dw0 - 2 - 3 * SQUISH_CONSTANT_4D
                    } else {
                        wsv_ext1 = wsb
                        wsv_ext0 = wsv_ext1
                        dw_ext1 = dw0 - 3 * SQUISH_CONSTANT_4D
                        dw_ext0 = dw_ext1
                    }
                }
            } else { // One point on each "side"
                val c1: Byte
                val c2: Byte
                if (aIsBiggerSide) {
                    c1 = aPoint
                    c2 = bPoint
                } else {
                    c1 = bPoint
                    c2 = aPoint
                }

                // Two contributions are the bigger-sided point with each 1 replaced with 2.
                if (c1 and 0x01 != 0) {
                    xsv_ext0 = xsb + 2
                    xsv_ext1 = xsb + 1
                    dx_ext0 = dx0 - 2 - 3 * SQUISH_CONSTANT_4D
                    dx_ext1 = dx0 - 1 - 3 * SQUISH_CONSTANT_4D
                } else {
                    xsv_ext1 = xsb
                    xsv_ext0 = xsv_ext1
                    dx_ext1 = dx0 - 3 * SQUISH_CONSTANT_4D
                    dx_ext0 = dx_ext1
                }
                if (c1 and 0x02 != 0) {
                    ysv_ext1 = ysb + 1
                    ysv_ext0 = ysv_ext1
                    dy_ext1 = dy0 - 1 - 3 * SQUISH_CONSTANT_4D
                    dy_ext0 = dy_ext1
                    if (c1 and 0x01 == 0) {
                        ysv_ext0 += 1
                        dy_ext0 -= 1.0
                    } else {
                        ysv_ext1 += 1
                        dy_ext1 -= 1.0
                    }
                } else {
                    ysv_ext1 = ysb
                    ysv_ext0 = ysv_ext1
                    dy_ext1 = dy0 - 3 * SQUISH_CONSTANT_4D
                    dy_ext0 = dy_ext1
                }
                if (c1 and 0x04 != 0) {
                    zsv_ext1 = zsb + 1
                    zsv_ext0 = zsv_ext1
                    dz_ext1 = dz0 - 1 - 3 * SQUISH_CONSTANT_4D
                    dz_ext0 = dz_ext1
                    if (c1 and 0x03 == 0) {
                        zsv_ext0 += 1
                        dz_ext0 -= 1.0
                    } else {
                        zsv_ext1 += 1
                        dz_ext1 -= 1.0
                    }
                } else {
                    zsv_ext1 = zsb
                    zsv_ext0 = zsv_ext1
                    dz_ext1 = dz0 - 3 * SQUISH_CONSTANT_4D
                    dz_ext0 = dz_ext1
                }
                if (c1 and 0x08 != 0) {
                    wsv_ext0 = wsb + 1
                    wsv_ext1 = wsb + 2
                    dw_ext0 = dw0 - 1 - 3 * SQUISH_CONSTANT_4D
                    dw_ext1 = dw0 - 2 - 3 * SQUISH_CONSTANT_4D
                } else {
                    wsv_ext1 = wsb
                    wsv_ext0 = wsv_ext1
                    dw_ext1 = dw0 - 3 * SQUISH_CONSTANT_4D
                    dw_ext0 = dw_ext1
                }

                // One contribution is a permutation of (1,1,1,-1) based on the smaller-sided point
                xsv_ext2 = xsb + 1
                ysv_ext2 = ysb + 1
                zsv_ext2 = zsb + 1
                wsv_ext2 = wsb + 1
                dx_ext2 = dx0 - 1 - 2 * SQUISH_CONSTANT_4D
                dy_ext2 = dy0 - 1 - 2 * SQUISH_CONSTANT_4D
                dz_ext2 = dz0 - 1 - 2 * SQUISH_CONSTANT_4D
                dw_ext2 = dw0 - 1 - 2 * SQUISH_CONSTANT_4D
                if (c2 and 0x01 == 0) {
                    xsv_ext2 -= 2
                    dx_ext2 += 2.0
                } else if (c2 and 0x02 == 0) {
                    ysv_ext2 -= 2
                    dy_ext2 += 2.0
                } else if (c2 and 0x04 == 0) {
                    zsv_ext2 -= 2
                    dz_ext2 += 2.0
                } else {
                    wsv_ext2 -= 2
                    dw_ext2 += 2.0
                }
            }

            // Contribution (1,1,1,0)
            val dx4 = dx0 - 1 - 3 * SQUISH_CONSTANT_4D
            val dy4 = dy0 - 1 - 3 * SQUISH_CONSTANT_4D
            val dz4 = dz0 - 1 - 3 * SQUISH_CONSTANT_4D
            val dw4 = dw0 - 3 * SQUISH_CONSTANT_4D
            var attn4 = 2 - dx4 * dx4 - dy4 * dy4 - dz4 * dz4 - dw4 * dw4
            if (attn4 > 0) {
                attn4 *= attn4
                value += attn4 * attn4 * extrapolate(xsb + 1, ysb + 1, zsb + 1, wsb + 0, dx4, dy4, dz4, dw4)
            }

            // Contribution (1,1,0,1)
            val dz3 = dz0 - 3 * SQUISH_CONSTANT_4D
            val dw3 = dw0 - 1 - 3 * SQUISH_CONSTANT_4D
            var attn3 = 2 - dx4 * dx4 - dy4 * dy4 - dz3 * dz3 - dw3 * dw3
            if (attn3 > 0) {
                attn3 *= attn3
                value += attn3 * attn3 * extrapolate(xsb + 1, ysb + 1, zsb + 0, wsb + 1, dx4, dy4, dz3, dw3)
            }

            // Contribution (1,0,1,1)
            val dy2 = dy0 - 3 * SQUISH_CONSTANT_4D
            var attn2 = 2 - dx4 * dx4 - dy2 * dy2 - dz4 * dz4 - dw3 * dw3
            if (attn2 > 0) {
                attn2 *= attn2
                value += attn2 * attn2 * extrapolate(xsb + 1, ysb + 0, zsb + 1, wsb + 1, dx4, dy2, dz4, dw3)
            }

            // Contribution (0,1,1,1)
            val dx1 = dx0 - 3 * SQUISH_CONSTANT_4D
            var attn1 = 2 - dx1 * dx1 - dy4 * dy4 - dz4 * dz4 - dw3 * dw3
            if (attn1 > 0) {
                attn1 *= attn1
                value += attn1 * attn1 * extrapolate(xsb + 0, ysb + 1, zsb + 1, wsb + 1, dx1, dy4, dz4, dw3)
            }

            // Contribution (1,1,0,0)
            val dx5 = dx0 - 1 - 2 * SQUISH_CONSTANT_4D
            val dy5 = dy0 - 1 - 2 * SQUISH_CONSTANT_4D
            val dz5 = dz0 - 0 - 2 * SQUISH_CONSTANT_4D
            val dw5 = dw0 - 0 - 2 * SQUISH_CONSTANT_4D
            var attn5 = 2 - dx5 * dx5 - dy5 * dy5 - dz5 * dz5 - dw5 * dw5
            if (attn5 > 0) {
                attn5 *= attn5
                value += attn5 * attn5 * extrapolate(xsb + 1, ysb + 1, zsb + 0, wsb + 0, dx5, dy5, dz5, dw5)
            }

            // Contribution (1,0,1,0)
            val dx6 = dx0 - 1 - 2 * SQUISH_CONSTANT_4D
            val dy6 = dy0 - 0 - 2 * SQUISH_CONSTANT_4D
            val dz6 = dz0 - 1 - 2 * SQUISH_CONSTANT_4D
            val dw6 = dw0 - 0 - 2 * SQUISH_CONSTANT_4D
            var attn6 = 2 - dx6 * dx6 - dy6 * dy6 - dz6 * dz6 - dw6 * dw6
            if (attn6 > 0) {
                attn6 *= attn6
                value += attn6 * attn6 * extrapolate(xsb + 1, ysb + 0, zsb + 1, wsb + 0, dx6, dy6, dz6, dw6)
            }

            // Contribution (1,0,0,1)
            val dx7 = dx0 - 1 - 2 * SQUISH_CONSTANT_4D
            val dy7 = dy0 - 0 - 2 * SQUISH_CONSTANT_4D
            val dz7 = dz0 - 0 - 2 * SQUISH_CONSTANT_4D
            val dw7 = dw0 - 1 - 2 * SQUISH_CONSTANT_4D
            var attn7 = 2 - dx7 * dx7 - dy7 * dy7 - dz7 * dz7 - dw7 * dw7
            if (attn7 > 0) {
                attn7 *= attn7
                value += attn7 * attn7 * extrapolate(xsb + 1, ysb + 0, zsb + 0, wsb + 1, dx7, dy7, dz7, dw7)
            }

            // Contribution (0,1,1,0)
            val dx8 = dx0 - 0 - 2 * SQUISH_CONSTANT_4D
            val dy8 = dy0 - 1 - 2 * SQUISH_CONSTANT_4D
            val dz8 = dz0 - 1 - 2 * SQUISH_CONSTANT_4D
            val dw8 = dw0 - 0 - 2 * SQUISH_CONSTANT_4D
            var attn8 = 2 - dx8 * dx8 - dy8 * dy8 - dz8 * dz8 - dw8 * dw8
            if (attn8 > 0) {
                attn8 *= attn8
                value += attn8 * attn8 * extrapolate(xsb + 0, ysb + 1, zsb + 1, wsb + 0, dx8, dy8, dz8, dw8)
            }

            // Contribution (0,1,0,1)
            val dx9 = dx0 - 0 - 2 * SQUISH_CONSTANT_4D
            val dy9 = dy0 - 1 - 2 * SQUISH_CONSTANT_4D
            val dz9 = dz0 - 0 - 2 * SQUISH_CONSTANT_4D
            val dw9 = dw0 - 1 - 2 * SQUISH_CONSTANT_4D
            var attn9 = 2 - dx9 * dx9 - dy9 * dy9 - dz9 * dz9 - dw9 * dw9
            if (attn9 > 0) {
                attn9 *= attn9
                value += attn9 * attn9 * extrapolate(xsb + 0, ysb + 1, zsb + 0, wsb + 1, dx9, dy9, dz9, dw9)
            }

            // Contribution (0,0,1,1)
            val dx10 = dx0 - 0 - 2 * SQUISH_CONSTANT_4D
            val dy10 = dy0 - 0 - 2 * SQUISH_CONSTANT_4D
            val dz10 = dz0 - 1 - 2 * SQUISH_CONSTANT_4D
            val dw10 = dw0 - 1 - 2 * SQUISH_CONSTANT_4D
            var attn10 = 2 - dx10 * dx10 - dy10 * dy10 - dz10 * dz10 - dw10 * dw10
            if (attn10 > 0) {
                attn10 *= attn10
                value += attn10 * attn10 * extrapolate(xsb + 0, ysb + 0, zsb + 1, wsb + 1, dx10, dy10, dz10, dw10)
            }
        }

        // First extra vertex
        var attn_ext0 =
            2 - dx_ext0 * dx_ext0 - dy_ext0 * dy_ext0 - dz_ext0 * dz_ext0 - dw_ext0 * dw_ext0
        if (attn_ext0 > 0) {
            attn_ext0 *= attn_ext0
            value += attn_ext0 * attn_ext0 * extrapolate(
                xsv_ext0,
                ysv_ext0,
                zsv_ext0,
                wsv_ext0,
                dx_ext0,
                dy_ext0,
                dz_ext0,
                dw_ext0
            )
        }

        // Second extra vertex
        var attn_ext1 =
            2 - dx_ext1 * dx_ext1 - dy_ext1 * dy_ext1 - dz_ext1 * dz_ext1 - dw_ext1 * dw_ext1
        if (attn_ext1 > 0) {
            attn_ext1 *= attn_ext1
            value += attn_ext1 * attn_ext1 * extrapolate(
                xsv_ext1,
                ysv_ext1,
                zsv_ext1,
                wsv_ext1,
                dx_ext1,
                dy_ext1,
                dz_ext1,
                dw_ext1
            )
        }

        // Third extra vertex
        var attn_ext2 =
            2 - dx_ext2 * dx_ext2 - dy_ext2 * dy_ext2 - dz_ext2 * dz_ext2 - dw_ext2 * dw_ext2
        if (attn_ext2 > 0) {
            attn_ext2 *= attn_ext2
            value += attn_ext2 * attn_ext2 * extrapolate(
                xsv_ext2,
                ysv_ext2,
                zsv_ext2,
                wsv_ext2,
                dx_ext2,
                dy_ext2,
                dz_ext2,
                dw_ext2
            )
        }
        return value
    }

    private fun extrapolate(xsb: Int, ysb: Int, dx: Double, dy: Double): Double {
        val grad =
            permGrad2[perm[xsb and PMASK] xor (ysb and PMASK)]
        return grad!!.dx * dx + grad.dy * dy
    }

    private fun extrapolate(
        xsb: Int,
        ysb: Int,
        zsb: Int,
        dx: Double,
        dy: Double,
        dz: Double
    ): Double {
        val grad =
            permGrad3[perm[perm[xsb and PMASK] xor (ysb and PMASK)] xor (zsb and PMASK)]
        return grad!!.dx * dx + grad.dy * dy + grad.dz * dz
    }

    private fun extrapolate(
        xsb: Int,
        ysb: Int,
        zsb: Int,
        wsb: Int,
        dx: Double,
        dy: Double,
        dz: Double,
        dw: Double
    ): Double {
        val grad =
            permGrad4[perm[perm[perm[xsb and PMASK] xor (ysb and PMASK)] xor (zsb and PMASK)] xor (wsb and PMASK)]
        return grad!!.dx * dx + grad.dy * dy + grad.dz * dz + grad.dw * dw
    }

    class Grad2(var dx: Double, var dy: Double)

    class Grad3(var dx: Double, var dy: Double, var dz: Double)

    class Grad4(var dx: Double, var dy: Double, var dz: Double, var dw: Double)

    companion object {
        private const val STRETCH_CONSTANT_2D = -0.211324865405187 // (1/Math.sqrt(2+1)-1)/2;
        private const val SQUISH_CONSTANT_2D = 0.366025403784439 // (Math.sqrt(2+1)-1)/2;
        private const val STRETCH_CONSTANT_3D = -1.0 / 6 // (1/Math.sqrt(3+1)-1)/3;
        private const val SQUISH_CONSTANT_3D = 1.0 / 3 // (Math.sqrt(3+1)-1)/3;
        private const val STRETCH_CONSTANT_4D = -0.138196601125011 // (1/Math.sqrt(4+1)-1)/4;
        private const val SQUISH_CONSTANT_4D = 0.309016994374947 // (Math.sqrt(4+1)-1)/4;
        private const val DEFAULT_SEED: Long = 0
        private const val PSIZE = 2048
        private const val PMASK = 2047
        private fun fastFloor(x: Double): Int {
            val xi = x.toInt()
            return if (x < xi) xi - 1 else xi
        }

        private const val N2 = 7.69084574549313
        private const val N3 = 26.92263139946168
        private const val N4 = 8.881759591352166
        private val GRADIENTS_2D = arrayOfNulls<Grad2>(PSIZE)
        private val GRADIENTS_3D = arrayOfNulls<Grad3>(PSIZE)
        private val GRADIENTS_4D = arrayOfNulls<Grad4>(PSIZE)

        init {
            val grad2 = arrayOf(
                Grad2(0.130526192220052, 0.99144486137381),
                Grad2(0.38268343236509, 0.923879532511287),
                Grad2(0.608761429008721, 0.793353340291235),
                Grad2(0.793353340291235, 0.608761429008721),
                Grad2(0.923879532511287, 0.38268343236509),
                Grad2(0.99144486137381, 0.130526192220051),
                Grad2(0.99144486137381, -0.130526192220051),
                Grad2(0.923879532511287, -0.38268343236509),
                Grad2(0.793353340291235, -0.60876142900872),
                Grad2(0.608761429008721, -0.793353340291235),
                Grad2(0.38268343236509, -0.923879532511287),
                Grad2(0.130526192220052, -0.99144486137381),
                Grad2(-0.130526192220052, -0.99144486137381),
                Grad2(-0.38268343236509, -0.923879532511287),
                Grad2(-0.608761429008721, -0.793353340291235),
                Grad2(-0.793353340291235, -0.608761429008721),
                Grad2(-0.923879532511287, -0.38268343236509),
                Grad2(-0.99144486137381, -0.130526192220052),
                Grad2(-0.99144486137381, 0.130526192220051),
                Grad2(-0.923879532511287, 0.38268343236509),
                Grad2(-0.793353340291235, 0.608761429008721),
                Grad2(-0.608761429008721, 0.793353340291235),
                Grad2(-0.38268343236509, 0.923879532511287),
                Grad2(-0.130526192220052, 0.99144486137381)
            )
            for (i in mpeciakk.world.grad2.indices) {
                mpeciakk.world.grad2.get(i).dx /= N2
                mpeciakk.world.grad2.get(i).dy /= N2
            }
            for (i in 0 until PSIZE) {
                GRADIENTS_2D[i] = mpeciakk.world.grad2.get(i % mpeciakk.world.grad2.size)
            }
            val grad3 = arrayOf(
                Grad3(-1.4082482904633333, -1.4082482904633333, -2.6329931618533333),
                Grad3(-0.07491495712999985, -0.07491495712999985, -3.29965982852),
                Grad3(0.24732126143473554, -1.6667938651159684, -2.838945207362466),
                Grad3(-1.6667938651159684, 0.24732126143473554, -2.838945207362466),
                Grad3(-1.4082482904633333, -2.6329931618533333, -1.4082482904633333),
                Grad3(-0.07491495712999985, -3.29965982852, -0.07491495712999985),
                Grad3(-1.6667938651159684, -2.838945207362466, 0.24732126143473554),
                Grad3(0.24732126143473554, -2.838945207362466, -1.6667938651159684),
                Grad3(1.5580782047233335, 0.33333333333333337, -2.8914115380566665),
                Grad3(2.8914115380566665, -0.33333333333333337, -1.5580782047233335),
                Grad3(1.8101897177633992, -1.2760767510338025, -2.4482280932803),
                Grad3(2.4482280932803, 1.2760767510338025, -1.8101897177633992),
                Grad3(1.5580782047233335, -2.8914115380566665, 0.33333333333333337),
                Grad3(2.8914115380566665, -1.5580782047233335, -0.33333333333333337),
                Grad3(2.4482280932803, -1.8101897177633992, 1.2760767510338025),
                Grad3(1.8101897177633992, -2.4482280932803, -1.2760767510338025),
                Grad3(-2.6329931618533333, -1.4082482904633333, -1.4082482904633333),
                Grad3(-3.29965982852, -0.07491495712999985, -0.07491495712999985),
                Grad3(-2.838945207362466, 0.24732126143473554, -1.6667938651159684),
                Grad3(-2.838945207362466, -1.6667938651159684, 0.24732126143473554),
                Grad3(0.33333333333333337, 1.5580782047233335, -2.8914115380566665),
                Grad3(-0.33333333333333337, 2.8914115380566665, -1.5580782047233335),
                Grad3(1.2760767510338025, 2.4482280932803, -1.8101897177633992),
                Grad3(-1.2760767510338025, 1.8101897177633992, -2.4482280932803),
                Grad3(0.33333333333333337, -2.8914115380566665, 1.5580782047233335),
                Grad3(-0.33333333333333337, -1.5580782047233335, 2.8914115380566665),
                Grad3(-1.2760767510338025, -2.4482280932803, 1.8101897177633992),
                Grad3(1.2760767510338025, -1.8101897177633992, 2.4482280932803),
                Grad3(3.29965982852, 0.07491495712999985, 0.07491495712999985),
                Grad3(2.6329931618533333, 1.4082482904633333, 1.4082482904633333),
                Grad3(2.838945207362466, -0.24732126143473554, 1.6667938651159684),
                Grad3(2.838945207362466, 1.6667938651159684, -0.24732126143473554),
                Grad3(-2.8914115380566665, 1.5580782047233335, 0.33333333333333337),
                Grad3(-1.5580782047233335, 2.8914115380566665, -0.33333333333333337),
                Grad3(-2.4482280932803, 1.8101897177633992, -1.2760767510338025),
                Grad3(-1.8101897177633992, 2.4482280932803, 1.2760767510338025),
                Grad3(-2.8914115380566665, 0.33333333333333337, 1.5580782047233335),
                Grad3(-1.5580782047233335, -0.33333333333333337, 2.8914115380566665),
                Grad3(-1.8101897177633992, 1.2760767510338025, 2.4482280932803),
                Grad3(-2.4482280932803, -1.2760767510338025, 1.8101897177633992),
                Grad3(0.07491495712999985, 3.29965982852, 0.07491495712999985),
                Grad3(1.4082482904633333, 2.6329931618533333, 1.4082482904633333),
                Grad3(1.6667938651159684, 2.838945207362466, -0.24732126143473554),
                Grad3(-0.24732126143473554, 2.838945207362466, 1.6667938651159684),
                Grad3(0.07491495712999985, 0.07491495712999985, 3.29965982852),
                Grad3(1.4082482904633333, 1.4082482904633333, 2.6329931618533333),
                Grad3(-0.24732126143473554, 1.6667938651159684, 2.838945207362466),
                Grad3(1.6667938651159684, -0.24732126143473554, 2.838945207362466)
            )
            for (i in mpeciakk.world.grad3.indices) {
                mpeciakk.world.grad3.get(i).dx /= N3
                mpeciakk.world.grad3.get(i).dy /= N3
                mpeciakk.world.grad3.get(i).dz /= N3
            }
            for (i in 0 until PSIZE) {
                GRADIENTS_3D[i] = mpeciakk.world.grad3.get(i % mpeciakk.world.grad3.size)
            }
            val grad4 = arrayOf(
                Grad4(-0.753341017856078, -0.37968289875261624, -0.37968289875261624, -0.37968289875261624),
                Grad4(-0.7821684431180708, -0.4321472685365301, -0.4321472685365301, 0.12128480194602098),
                Grad4(-0.7821684431180708, -0.4321472685365301, 0.12128480194602098, -0.4321472685365301),
                Grad4(-0.7821684431180708, 0.12128480194602098, -0.4321472685365301, -0.4321472685365301),
                Grad4(-0.8586508742123365, -0.508629699630796, 0.044802370851755174, 0.044802370851755174),
                Grad4(-0.8586508742123365, 0.044802370851755174, -0.508629699630796, 0.044802370851755174),
                Grad4(-0.8586508742123365, 0.044802370851755174, 0.044802370851755174, -0.508629699630796),
                Grad4(-0.9982828964265062, -0.03381941603233842, -0.03381941603233842, -0.03381941603233842),
                Grad4(-0.37968289875261624, -0.753341017856078, -0.37968289875261624, -0.37968289875261624),
                Grad4(-0.4321472685365301, -0.7821684431180708, -0.4321472685365301, 0.12128480194602098),
                Grad4(-0.4321472685365301, -0.7821684431180708, 0.12128480194602098, -0.4321472685365301),
                Grad4(0.12128480194602098, -0.7821684431180708, -0.4321472685365301, -0.4321472685365301),
                Grad4(-0.508629699630796, -0.8586508742123365, 0.044802370851755174, 0.044802370851755174),
                Grad4(0.044802370851755174, -0.8586508742123365, -0.508629699630796, 0.044802370851755174),
                Grad4(0.044802370851755174, -0.8586508742123365, 0.044802370851755174, -0.508629699630796),
                Grad4(-0.03381941603233842, -0.9982828964265062, -0.03381941603233842, -0.03381941603233842),
                Grad4(-0.37968289875261624, -0.37968289875261624, -0.753341017856078, -0.37968289875261624),
                Grad4(-0.4321472685365301, -0.4321472685365301, -0.7821684431180708, 0.12128480194602098),
                Grad4(-0.4321472685365301, 0.12128480194602098, -0.7821684431180708, -0.4321472685365301),
                Grad4(0.12128480194602098, -0.4321472685365301, -0.7821684431180708, -0.4321472685365301),
                Grad4(-0.508629699630796, 0.044802370851755174, -0.8586508742123365, 0.044802370851755174),
                Grad4(0.044802370851755174, -0.508629699630796, -0.8586508742123365, 0.044802370851755174),
                Grad4(0.044802370851755174, 0.044802370851755174, -0.8586508742123365, -0.508629699630796),
                Grad4(-0.03381941603233842, -0.03381941603233842, -0.9982828964265062, -0.03381941603233842),
                Grad4(-0.37968289875261624, -0.37968289875261624, -0.37968289875261624, -0.753341017856078),
                Grad4(-0.4321472685365301, -0.4321472685365301, 0.12128480194602098, -0.7821684431180708),
                Grad4(-0.4321472685365301, 0.12128480194602098, -0.4321472685365301, -0.7821684431180708),
                Grad4(0.12128480194602098, -0.4321472685365301, -0.4321472685365301, -0.7821684431180708),
                Grad4(-0.508629699630796, 0.044802370851755174, 0.044802370851755174, -0.8586508742123365),
                Grad4(0.044802370851755174, -0.508629699630796, 0.044802370851755174, -0.8586508742123365),
                Grad4(0.044802370851755174, 0.044802370851755174, -0.508629699630796, -0.8586508742123365),
                Grad4(-0.03381941603233842, -0.03381941603233842, -0.03381941603233842, -0.9982828964265062),
                Grad4(-0.6740059517812944, -0.3239847771997537, -0.3239847771997537, 0.5794684678643381),
                Grad4(-0.7504883828755602, -0.4004672082940195, 0.15296486218853164, 0.5029860367700724),
                Grad4(-0.7504883828755602, 0.15296486218853164, -0.4004672082940195, 0.5029860367700724),
                Grad4(-0.8828161875373585, 0.08164729285680945, 0.08164729285680945, 0.4553054119602712),
                Grad4(-0.4553054119602712, -0.08164729285680945, -0.08164729285680945, 0.8828161875373585),
                Grad4(-0.5029860367700724, -0.15296486218853164, 0.4004672082940195, 0.7504883828755602),
                Grad4(-0.5029860367700724, 0.4004672082940195, -0.15296486218853164, 0.7504883828755602),
                Grad4(-0.5794684678643381, 0.3239847771997537, 0.3239847771997537, 0.6740059517812944),
                Grad4(-0.3239847771997537, -0.6740059517812944, -0.3239847771997537, 0.5794684678643381),
                Grad4(-0.4004672082940195, -0.7504883828755602, 0.15296486218853164, 0.5029860367700724),
                Grad4(0.15296486218853164, -0.7504883828755602, -0.4004672082940195, 0.5029860367700724),
                Grad4(0.08164729285680945, -0.8828161875373585, 0.08164729285680945, 0.4553054119602712),
                Grad4(-0.08164729285680945, -0.4553054119602712, -0.08164729285680945, 0.8828161875373585),
                Grad4(-0.15296486218853164, -0.5029860367700724, 0.4004672082940195, 0.7504883828755602),
                Grad4(0.4004672082940195, -0.5029860367700724, -0.15296486218853164, 0.7504883828755602),
                Grad4(0.3239847771997537, -0.5794684678643381, 0.3239847771997537, 0.6740059517812944),
                Grad4(-0.3239847771997537, -0.3239847771997537, -0.6740059517812944, 0.5794684678643381),
                Grad4(-0.4004672082940195, 0.15296486218853164, -0.7504883828755602, 0.5029860367700724),
                Grad4(0.15296486218853164, -0.4004672082940195, -0.7504883828755602, 0.5029860367700724),
                Grad4(0.08164729285680945, 0.08164729285680945, -0.8828161875373585, 0.4553054119602712),
                Grad4(-0.08164729285680945, -0.08164729285680945, -0.4553054119602712, 0.8828161875373585),
                Grad4(-0.15296486218853164, 0.4004672082940195, -0.5029860367700724, 0.7504883828755602),
                Grad4(0.4004672082940195, -0.15296486218853164, -0.5029860367700724, 0.7504883828755602),
                Grad4(0.3239847771997537, 0.3239847771997537, -0.5794684678643381, 0.6740059517812944),
                Grad4(-0.6740059517812944, -0.3239847771997537, 0.5794684678643381, -0.3239847771997537),
                Grad4(-0.7504883828755602, -0.4004672082940195, 0.5029860367700724, 0.15296486218853164),
                Grad4(-0.7504883828755602, 0.15296486218853164, 0.5029860367700724, -0.4004672082940195),
                Grad4(-0.8828161875373585, 0.08164729285680945, 0.4553054119602712, 0.08164729285680945),
                Grad4(-0.4553054119602712, -0.08164729285680945, 0.8828161875373585, -0.08164729285680945),
                Grad4(-0.5029860367700724, -0.15296486218853164, 0.7504883828755602, 0.4004672082940195),
                Grad4(-0.5029860367700724, 0.4004672082940195, 0.7504883828755602, -0.15296486218853164),
                Grad4(-0.5794684678643381, 0.3239847771997537, 0.6740059517812944, 0.3239847771997537),
                Grad4(-0.3239847771997537, -0.6740059517812944, 0.5794684678643381, -0.3239847771997537),
                Grad4(-0.4004672082940195, -0.7504883828755602, 0.5029860367700724, 0.15296486218853164),
                Grad4(0.15296486218853164, -0.7504883828755602, 0.5029860367700724, -0.4004672082940195),
                Grad4(0.08164729285680945, -0.8828161875373585, 0.4553054119602712, 0.08164729285680945),
                Grad4(-0.08164729285680945, -0.4553054119602712, 0.8828161875373585, -0.08164729285680945),
                Grad4(-0.15296486218853164, -0.5029860367700724, 0.7504883828755602, 0.4004672082940195),
                Grad4(0.4004672082940195, -0.5029860367700724, 0.7504883828755602, -0.15296486218853164),
                Grad4(0.3239847771997537, -0.5794684678643381, 0.6740059517812944, 0.3239847771997537),
                Grad4(-0.3239847771997537, -0.3239847771997537, 0.5794684678643381, -0.6740059517812944),
                Grad4(-0.4004672082940195, 0.15296486218853164, 0.5029860367700724, -0.7504883828755602),
                Grad4(0.15296486218853164, -0.4004672082940195, 0.5029860367700724, -0.7504883828755602),
                Grad4(0.08164729285680945, 0.08164729285680945, 0.4553054119602712, -0.8828161875373585),
                Grad4(-0.08164729285680945, -0.08164729285680945, 0.8828161875373585, -0.4553054119602712),
                Grad4(-0.15296486218853164, 0.4004672082940195, 0.7504883828755602, -0.5029860367700724),
                Grad4(0.4004672082940195, -0.15296486218853164, 0.7504883828755602, -0.5029860367700724),
                Grad4(0.3239847771997537, 0.3239847771997537, 0.6740059517812944, -0.5794684678643381),
                Grad4(-0.6740059517812944, 0.5794684678643381, -0.3239847771997537, -0.3239847771997537),
                Grad4(-0.7504883828755602, 0.5029860367700724, -0.4004672082940195, 0.15296486218853164),
                Grad4(-0.7504883828755602, 0.5029860367700724, 0.15296486218853164, -0.4004672082940195),
                Grad4(-0.8828161875373585, 0.4553054119602712, 0.08164729285680945, 0.08164729285680945),
                Grad4(-0.4553054119602712, 0.8828161875373585, -0.08164729285680945, -0.08164729285680945),
                Grad4(-0.5029860367700724, 0.7504883828755602, -0.15296486218853164, 0.4004672082940195),
                Grad4(-0.5029860367700724, 0.7504883828755602, 0.4004672082940195, -0.15296486218853164),
                Grad4(-0.5794684678643381, 0.6740059517812944, 0.3239847771997537, 0.3239847771997537),
                Grad4(-0.3239847771997537, 0.5794684678643381, -0.6740059517812944, -0.3239847771997537),
                Grad4(-0.4004672082940195, 0.5029860367700724, -0.7504883828755602, 0.15296486218853164),
                Grad4(0.15296486218853164, 0.5029860367700724, -0.7504883828755602, -0.4004672082940195),
                Grad4(0.08164729285680945, 0.4553054119602712, -0.8828161875373585, 0.08164729285680945),
                Grad4(-0.08164729285680945, 0.8828161875373585, -0.4553054119602712, -0.08164729285680945),
                Grad4(-0.15296486218853164, 0.7504883828755602, -0.5029860367700724, 0.4004672082940195),
                Grad4(0.4004672082940195, 0.7504883828755602, -0.5029860367700724, -0.15296486218853164),
                Grad4(0.3239847771997537, 0.6740059517812944, -0.5794684678643381, 0.3239847771997537),
                Grad4(-0.3239847771997537, 0.5794684678643381, -0.3239847771997537, -0.6740059517812944),
                Grad4(-0.4004672082940195, 0.5029860367700724, 0.15296486218853164, -0.7504883828755602),
                Grad4(0.15296486218853164, 0.5029860367700724, -0.4004672082940195, -0.7504883828755602),
                Grad4(0.08164729285680945, 0.4553054119602712, 0.08164729285680945, -0.8828161875373585),
                Grad4(-0.08164729285680945, 0.8828161875373585, -0.08164729285680945, -0.4553054119602712),
                Grad4(-0.15296486218853164, 0.7504883828755602, 0.4004672082940195, -0.5029860367700724),
                Grad4(0.4004672082940195, 0.7504883828755602, -0.15296486218853164, -0.5029860367700724),
                Grad4(0.3239847771997537, 0.6740059517812944, 0.3239847771997537, -0.5794684678643381),
                Grad4(0.5794684678643381, -0.6740059517812944, -0.3239847771997537, -0.3239847771997537),
                Grad4(0.5029860367700724, -0.7504883828755602, -0.4004672082940195, 0.15296486218853164),
                Grad4(0.5029860367700724, -0.7504883828755602, 0.15296486218853164, -0.4004672082940195),
                Grad4(0.4553054119602712, -0.8828161875373585, 0.08164729285680945, 0.08164729285680945),
                Grad4(0.8828161875373585, -0.4553054119602712, -0.08164729285680945, -0.08164729285680945),
                Grad4(0.7504883828755602, -0.5029860367700724, -0.15296486218853164, 0.4004672082940195),
                Grad4(0.7504883828755602, -0.5029860367700724, 0.4004672082940195, -0.15296486218853164),
                Grad4(0.6740059517812944, -0.5794684678643381, 0.3239847771997537, 0.3239847771997537),
                Grad4(0.5794684678643381, -0.3239847771997537, -0.6740059517812944, -0.3239847771997537),
                Grad4(0.5029860367700724, -0.4004672082940195, -0.7504883828755602, 0.15296486218853164),
                Grad4(0.5029860367700724, 0.15296486218853164, -0.7504883828755602, -0.4004672082940195),
                Grad4(0.4553054119602712, 0.08164729285680945, -0.8828161875373585, 0.08164729285680945),
                Grad4(0.8828161875373585, -0.08164729285680945, -0.4553054119602712, -0.08164729285680945),
                Grad4(0.7504883828755602, -0.15296486218853164, -0.5029860367700724, 0.4004672082940195),
                Grad4(0.7504883828755602, 0.4004672082940195, -0.5029860367700724, -0.15296486218853164),
                Grad4(0.6740059517812944, 0.3239847771997537, -0.5794684678643381, 0.3239847771997537),
                Grad4(0.5794684678643381, -0.3239847771997537, -0.3239847771997537, -0.6740059517812944),
                Grad4(0.5029860367700724, -0.4004672082940195, 0.15296486218853164, -0.7504883828755602),
                Grad4(0.5029860367700724, 0.15296486218853164, -0.4004672082940195, -0.7504883828755602),
                Grad4(0.4553054119602712, 0.08164729285680945, 0.08164729285680945, -0.8828161875373585),
                Grad4(0.8828161875373585, -0.08164729285680945, -0.08164729285680945, -0.4553054119602712),
                Grad4(0.7504883828755602, -0.15296486218853164, 0.4004672082940195, -0.5029860367700724),
                Grad4(0.7504883828755602, 0.4004672082940195, -0.15296486218853164, -0.5029860367700724),
                Grad4(0.6740059517812944, 0.3239847771997537, 0.3239847771997537, -0.5794684678643381),
                Grad4(0.03381941603233842, 0.03381941603233842, 0.03381941603233842, 0.9982828964265062),
                Grad4(-0.044802370851755174, -0.044802370851755174, 0.508629699630796, 0.8586508742123365),
                Grad4(-0.044802370851755174, 0.508629699630796, -0.044802370851755174, 0.8586508742123365),
                Grad4(-0.12128480194602098, 0.4321472685365301, 0.4321472685365301, 0.7821684431180708),
                Grad4(0.508629699630796, -0.044802370851755174, -0.044802370851755174, 0.8586508742123365),
                Grad4(0.4321472685365301, -0.12128480194602098, 0.4321472685365301, 0.7821684431180708),
                Grad4(0.4321472685365301, 0.4321472685365301, -0.12128480194602098, 0.7821684431180708),
                Grad4(0.37968289875261624, 0.37968289875261624, 0.37968289875261624, 0.753341017856078),
                Grad4(0.03381941603233842, 0.03381941603233842, 0.9982828964265062, 0.03381941603233842),
                Grad4(-0.044802370851755174, 0.044802370851755174, 0.8586508742123365, 0.508629699630796),
                Grad4(-0.044802370851755174, 0.508629699630796, 0.8586508742123365, -0.044802370851755174),
                Grad4(-0.12128480194602098, 0.4321472685365301, 0.7821684431180708, 0.4321472685365301),
                Grad4(0.508629699630796, -0.044802370851755174, 0.8586508742123365, -0.044802370851755174),
                Grad4(0.4321472685365301, -0.12128480194602098, 0.7821684431180708, 0.4321472685365301),
                Grad4(0.4321472685365301, 0.4321472685365301, 0.7821684431180708, -0.12128480194602098),
                Grad4(0.37968289875261624, 0.37968289875261624, 0.753341017856078, 0.37968289875261624),
                Grad4(0.03381941603233842, 0.9982828964265062, 0.03381941603233842, 0.03381941603233842),
                Grad4(-0.044802370851755174, 0.8586508742123365, -0.044802370851755174, 0.508629699630796),
                Grad4(-0.044802370851755174, 0.8586508742123365, 0.508629699630796, -0.044802370851755174),
                Grad4(-0.12128480194602098, 0.7821684431180708, 0.4321472685365301, 0.4321472685365301),
                Grad4(0.508629699630796, 0.8586508742123365, -0.044802370851755174, -0.044802370851755174),
                Grad4(0.4321472685365301, 0.7821684431180708, -0.12128480194602098, 0.4321472685365301),
                Grad4(0.4321472685365301, 0.7821684431180708, 0.4321472685365301, -0.12128480194602098),
                Grad4(0.37968289875261624, 0.753341017856078, 0.37968289875261624, 0.37968289875261624),
                Grad4(0.9982828964265062, 0.03381941603233842, 0.03381941603233842, 0.03381941603233842),
                Grad4(0.8586508742123365, -0.044802370851755174, -0.044802370851755174, 0.508629699630796),
                Grad4(0.8586508742123365, -0.044802370851755174, 0.508629699630796, -0.044802370851755174),
                Grad4(0.7821684431180708, -0.12128480194602098, 0.4321472685365301, 0.4321472685365301),
                Grad4(0.8586508742123365, 0.508629699630796, -0.044802370851755174, -0.044802370851755174),
                Grad4(0.7821684431180708, 0.4321472685365301, -0.12128480194602098, 0.4321472685365301),
                Grad4(0.7821684431180708, 0.4321472685365301, 0.4321472685365301, -0.12128480194602098),
                Grad4(0.753341017856078, 0.37968289875261624, 0.37968289875261624, 0.37968289875261624)
            )
            for (i in mpeciakk.world.grad4.indices) {
                mpeciakk.world.grad4.get(i).dx /= N4
                mpeciakk.world.grad4.get(i).dy /= N4
                mpeciakk.world.grad4.get(i).dz /= N4
                mpeciakk.world.grad4.get(i).dw /= N4
            }
            for (i in 0 until PSIZE) {
                GRADIENTS_4D[i] = mpeciakk.world.grad4.get(i % mpeciakk.world.grad4.size)
            }
        }
    }
}