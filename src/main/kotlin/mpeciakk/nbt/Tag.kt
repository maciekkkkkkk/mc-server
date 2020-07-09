package mpeciakk.nbt

import java.io.DataOutput


interface Tag {
    fun write(output: DataOutput)
    fun getType(): Byte
}