package mpeciakk.nbt

import java.io.DataOutput

class ShortTag(private val value: Short) : NumberTag<Short> {

    override fun write(output: DataOutput) {
        output.writeShort(value.toInt())
    }

    override fun getType(): Byte {
        return 2
    }

    override fun get(): Short {
        return value
    }

    override fun toString(): String {
        return "ShortTag(value=$value)"
    }


}