package mpeciakk.nbt

import java.io.DataOutput

class ByteTag(private val value: Byte) : NumberTag<Byte> {

    override fun write(output: DataOutput) {
        output.writeByte(value.toInt())
    }

    override fun getType(): Byte {
        return 1
    }

    override fun get(): Byte {
        return value
    }

    override fun toString(): String {
        return "ByteTag(value=$value)"
    }
}