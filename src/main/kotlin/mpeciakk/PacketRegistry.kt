package mpeciakk

object PacketRegistry {

    private val packets: MutableMap<Int, C2SPacket> = mutableMapOf();

    fun register(id: Int, packet: C2SPacket) {
        packets[id] = packet
    }

    fun get(id: Int): C2SPacket? {
        return packets[id]
    }
}