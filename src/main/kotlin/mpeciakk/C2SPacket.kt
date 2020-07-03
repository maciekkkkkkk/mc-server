package mpeciakk

abstract class C2SPacket {

    abstract fun handle(connection: Connection, buf: PacketByteBuf)
}