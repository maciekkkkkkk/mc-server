package mpeciakk.network.packet.s2c

import mpeciakk.network.packet.PacketByteBuf

abstract class S2CPacket(private val id: Int) {
    protected abstract fun write(buf: PacketByteBuf)

    fun get(): ByteArray {
        val finalBuf = PacketByteBuf()

        val buf = PacketByteBuf()
        write(buf)

        finalBuf.writeVarInt(buf.readableBytes() + 1)
        finalBuf.writeVarInt(id)
        finalBuf.writeBytes(buf)

        return finalBuf.array()
    }

    fun getIad(): Int {
        return id
    }
}