package mpeciakk.nbt

import java.io.DataOutput

class StringTag(private val value: String) : Tag {

    override fun write(output: DataOutput) {
        output.writeUTF(value)
    }

    override fun getType(): Byte {
        return 8
    }

    fun getValue(): String {
        return value
    }

    override fun toString(): String {
        return "StringTag(value='$value')"
    }


}