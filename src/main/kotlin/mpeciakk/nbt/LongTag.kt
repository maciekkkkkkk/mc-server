package mpeciakk.nbt

import java.io.DataOutput

class LongTag(private val value: Long) : NumberTag<Long> {

    override fun write(output: DataOutput) {
        output.writeLong(value)
    }

    override fun getType(): Byte {
        return 4
    }

    override fun get(): Long {
        return value
    }

    override fun toString(): String {
        return "LongTag(value=$value)"
    }


}