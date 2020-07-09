package mpeciakk.nbt

import java.io.DataOutput

class DoubleTag(private val value: Double) : NumberTag<Double> {

    override fun write(output: DataOutput) {
        output.writeDouble(value)
    }

    override fun getType(): Byte {
        return 6
    }

    override fun get(): Double {
        return value
    }

    override fun toString(): String {
        return "DoubleTag(value=$value)"
    }
}