package mpeciakk

import io.netty.buffer.Unpooled

abstract class S2CPacket(private val id: Int) {

    protected abstract fun write(buf: PacketByteBuf)

    fun get(): ByteArray {
        val finalBuf = PacketByteBuf()

        val buf = PacketByteBuf()
        write(buf)

        finalBuf.writeVarInt(buf.capacity())
        finalBuf.writeVarInt(id)
        finalBuf.writeBytes(buf)

        return finalBuf.array()
    }
}