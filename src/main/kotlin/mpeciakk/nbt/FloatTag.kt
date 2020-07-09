package mpeciakk.nbt

import java.io.DataOutput

class FloatTag(private val value: Float) : NumberTag<Float> {

    override fun write(output: DataOutput) {
        output.writeFloat(value)
    }

    override fun getType(): Byte {
        return 5
    }

    override fun get(): Float {
        return value
    }

    override fun toString(): String {
        return "FloatTag(value=$value)"
    }
}