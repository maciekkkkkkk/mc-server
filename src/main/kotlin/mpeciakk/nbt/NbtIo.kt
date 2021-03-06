package mpeciakk.nbt

import java.io.DataOutput

object NbtIo {

    fun write(tag: Tag, output: DataOutput) {
        output.writeByte(tag.getType().toInt())
        if (tag.getType().toInt() != 0) {
            output.writeUTF("")
            tag.write(output)
        }
    }
}