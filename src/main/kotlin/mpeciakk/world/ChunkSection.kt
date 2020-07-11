package mpeciakk.world

import mpeciakk.network.packet.PacketByteBuf
import java.util.*
import java.util.function.IntFunction

/** Yeah it's bad */
class ChunkSection {
    var blockCount: Short = 0
    var bitsPerBlock: Int = 4
    var storage = FlexibleStorage(4)

    private val palette = mutableListOf(0)

    operator fun set(x: Int, y: Int, z: Int, state: Int) {
        var id = if (this.bitsPerBlock <= 8) this.palette.indexOf(state) else state
        if (id == -1) {
            this.palette.add(state)
            if (this.palette.size > 1 shl this.bitsPerBlock) {
                this.bitsPerBlock++
                val oldStates: List<Int> =
                    if (this.bitsPerBlock > 8) ArrayList<Int>(this.palette) else this.palette
                if (this.bitsPerBlock > 8) {
                    this.palette.clear()
                    this.bitsPerBlock = 14
                }
                val oldStorage = storage
                storage = oldStorage.transferData(
                    this.bitsPerBlock,
                    IntFunction { index -> if (this.bitsPerBlock <= 8) oldStorage[index] else oldStates[index] }
                )
            }
            id = if (this.bitsPerBlock <= 8) this.palette.indexOf(state) else state
        }

        val ind = index(x, y, z)
        val curr = storage[ind]
        if (state != 0 && curr == 0) {
            blockCount++
        } else if (state == 0 && curr != 0) {
            blockCount--
        }

        storage[ind] = id
    }

    fun isEmpty(): Boolean {
        for (index in 0 until storage.size) {
            if (storage[index] != 0) {
                return false
            }
        }
        return true
    }

    companion object {
        fun index(x: Int, y: Int, z: Int): Int {
            return y shl 8 or (z shl 4) or x
        }

        fun write(section: ChunkSection, buf: PacketByteBuf) {
            buf.writeShort(section.blockCount.toInt())
            buf.writeByte(section.bitsPerBlock)

            if (section.bitsPerBlock <= 8) {
                buf.writeVarInt(section.palette.size)
                for (state in section.palette) {
                    buf.writeVarInt(state)
                }
            }

            val data = section.storage.data
            buf.writeVarInt(data.size)
            buf.writeLongs(data)
        }
    }
}