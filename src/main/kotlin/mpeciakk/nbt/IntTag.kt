package mpeciakk.nbt

import java.io.DataOutput

class IntTag(private val value: Int) : NumberTag<Int> {

    override fun write(output: DataOutput) {
        output.writeInt(value)
    }

    override fun getType(): Byte {
        return 3
    }

    override fun get(): Int {
        return value
    }

    override fun toString(): String {
        return "IntTag(value=$value)"
    }
}