package mpeciakk.network.packet

import mpeciakk.network.packet.c2s.C2SPacket

object PacketRegistry {
    private val packets: MutableMap<Int, C2SPacket> = mutableMapOf();

    fun register(packet: C2SPacket) {
        packets[packet.id] = packet
    }

    fun get(id: Int): C2SPacket? {
        return packets[id]
    }
}