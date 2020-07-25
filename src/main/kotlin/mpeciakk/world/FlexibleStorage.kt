package mpeciakk.world

import java.util.*
import java.util.function.IntFunction

class FlexibleStorage @JvmOverloads constructor(
    private val bitsPerEntry: Int,
    data: LongArray = LongArray((4096 + 64 / bitsPerEntry - 1) / (64 / bitsPerEntry))
) {
    val data: LongArray
    val size: Int
    private val maxEntryValue: Long
    private val valuesPerLong: Char
    private val magicIndex: Int
    private val divideMultiply: Long
    private val divideAdd: Long
    private val divideShift: Long
    operator fun get(index: Int): Int {
        if (index < 0 || index > size - 1) {
            throw IndexOutOfBoundsException()
        }
        val cellIndex = (index * divideMultiply + divideAdd shr 32L shr divideShift.toInt()).toInt()
        val bitIndex = (index - cellIndex * valuesPerLong.toInt()) * bitsPerEntry
        return (data[cellIndex] shr bitIndex and maxEntryValue).toInt()
    }

    operator fun set(index: Int, value: Int) {
        if (index < 0 || index > size - 1) {
            throw IndexOutOfBoundsException()
        }
        require(!(value < 0 || value > maxEntryValue)) { "Value cannot be outside of accepted range." }
        val cellIndex = (index * divideMultiply + divideAdd shr 32L shr divideShift.toInt()).toInt()
        val bitIndex = (index - cellIndex * valuesPerLong.toInt()) * bitsPerEntry
        data[cellIndex] =
            data[cellIndex] and (maxEntryValue shl bitIndex).inv() or (value.toLong() and maxEntryValue) shl bitIndex
    }

    fun transferData(newBitsPerEntry: Int, valueGetter: IntFunction<Int>): FlexibleStorage {
        val newStorage = FlexibleStorage(newBitsPerEntry)
        for (i in 0..4095) {
            newStorage[i] = valueGetter.apply(i)
        }
        return newStorage
    }

    companion object {
        private val MAGIC_VALUES = intArrayOf(
            -1, -1, 0, Int.MIN_VALUE, 0, 0, 1431655765, 1431655765, 0, Int.MIN_VALUE,
            0, 1, 858993459, 858993459, 0, 715827882, 715827882, 0, 613566756, 613566756,
            0, Int.MIN_VALUE, 0, 2, 477218588, 477218588, 0, 429496729, 429496729, 0,
            390451572, 390451572, 0, 357913941, 357913941, 0, 330382099, 330382099, 0, 306783378,
            306783378, 0, 286331153, 286331153, 0, Int.MIN_VALUE, 0, 3, 252645135, 252645135,
            0, 238609294, 238609294, 0, 226050910, 226050910, 0, 214748364, 214748364, 0,
            204522252, 204522252, 0, 195225786, 195225786, 0, 186737708, 186737708, 0, 178956970,
            178956970, 0, 171798691, 171798691, 0, 165191049, 165191049, 0, 159072862, 159072862,
            0, 153391689, 153391689, 0, 148102320, 148102320, 0, 143165576, 143165576, 0,
            138547332, 138547332, 0, Int.MIN_VALUE, 0, 4, 130150524, 130150524, 0, 126322567,
            126322567, 0, 122713351, 122713351, 0, 119304647, 119304647, 0, 116080197, 116080197,
            0, 113025455, 113025455, 0, 110127366, 110127366, 0, 107374182, 107374182, 0,
            104755299, 104755299, 0, 102261126, 102261126, 0, 99882960, 99882960, 0, 97612893,
            97612893, 0, 95443717, 95443717, 0, 93368854, 93368854, 0, 91382282, 91382282,
            0, 89478485, 89478485, 0, 87652393, 87652393, 0, 85899345, 85899345, 0,
            84215045, 84215045, 0, 82595524, 82595524, 0, 81037118, 81037118, 0, 79536431,
            79536431, 0, 78090314, 78090314, 0, 76695844, 76695844, 0, 75350303, 75350303,
            0, 74051160, 74051160, 0, 72796055, 72796055, 0, 71582788, 71582788, 0,
            70409299, 70409299, 0, 69273666, 69273666, 0, 68174084, 68174084, 0, Int.MIN_VALUE,
            0, 5
        )
    }

    init {
        this.data = Arrays.copyOf(data, data.size)
        size = data.size * 64 / bitsPerEntry
        maxEntryValue = (1L shl bitsPerEntry) - 1
        valuesPerLong = (64 / bitsPerEntry).toChar()
        val expectedLength = (4096 + valuesPerLong.toInt() - 1) / valuesPerLong.toInt()
        require(data.size == expectedLength) { "Expected " + expectedLength + " longs but got " + data.size + " longs" }
        magicIndex = 3 * (valuesPerLong.toInt() - 1)
        divideMultiply = Integer.toUnsignedLong(MAGIC_VALUES[magicIndex])
        divideAdd = Integer.toUnsignedLong(MAGIC_VALUES[magicIndex + 1])
        divideShift = MAGIC_VALUES[magicIndex + 2].toLong()
    }
}