package mpeciakk.world

import java.util.function.IntFunction

class FlexibleStorage(
    private val bitsPerEntry: Int
) {
    var data = LongArray((4096 + 64 / bitsPerEntry - 1) / (64 / bitsPerEntry))
    val size = data.size * 64 / bitsPerEntry

    operator fun set(index: Int, value: Long) {
        if (index < 0 || index > size) {
            throw IndexOutOfBoundsException()
        }

        val cap = index * bitsPerEntry / 64
        val cabp = index * bitsPerEntry % 64
        val overlap = 64 - cabp

        data[cap] = data[cap] or ((value and makeMask(bitsPerEntry)) shl cabp)
        if (cabp + bitsPerEntry > 64) {
            data[cap + 1] = data[cap + 1] or (value shr overlap)
        }
    }

    private fun makeMask(bitsPerEntry: Int): Long {
        return (0.inv() shr (64 - bitsPerEntry)).toLong()
    }

    operator fun get(index: Int): Int {
        if (index < 0 || index > size) {
            return 0
        }

        val cap = index * bitsPerEntry / 64
        val cabp = index * bitsPerEntry % 64
        val overlap = 64 - cabp

        if (cap >= data.size - 1) {
            return 0
        }

        var read = (data[cap] and (makeMask(bitsPerEntry) shl cabp)) shr cabp

        if (cap + 1 > data.size) {
            read = read or (data[cap + 1] and ((0.inv() shr cabp).toLong())) shl overlap
        }

        return read.toInt()
    }

    fun transferData(newBitsPerEntry: Int, valueGetter: IntFunction<Int>): FlexibleStorage {
        val newStorage = FlexibleStorage(newBitsPerEntry)
        for (i in 0 until 4096) {
            newStorage[i] = valueGetter.apply(i).toLong()
        }
        return newStorage
    }
}