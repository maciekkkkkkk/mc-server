package mpeciakk.packet.c2s

import mpeciakk.net.Connection
import mpeciakk.packet.PacketByteBuf

class C2SClientSettingsPacket : C2SPacket(0x05) {
    override fun read(connection: Connection, buf: PacketByteBuf) {
        // Client's language
        val locale = buf.readString(16)

        // Client's view distance
        val viewDistance = buf.readByte()

        /*
            Client's chat mode
            0 - all messages
            1 - only commands feedback
            2 - hidden
         */
        val chatMode = buf.readVarInt()

        // Are colors allowed
        val chatColors = buf.readBoolean()

        /*
            Displayed skin parts
            TODO: find out what's this
            Bit 0 (0x01): Cape enabled
            Bit 1 (0x02): Jacket enabled
            Bit 2 (0x04): Left Sleeve enabled
            Bit 3 (0x08): Right Sleeve enabled
            Bit 4 (0x10): Left Pants Leg enabled
            Bit 5 (0x20): Right Pants Leg enabled
            Bit 6 (0x40): Hat enabled
            Bit 7 (0x80): Unused
         */
        val displayedSkinParts = buf.readByte()

        /*
            Player's main hand
            0 - left
            1 - right
         */
        val hand = buf.readVarInt()
    }
}