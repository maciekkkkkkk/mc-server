package mpeciakk.network.packet.s2c

import mpeciakk.network.packet.PacketByteBuf

class S2CServerListPacket : S2CPacket(0x00) {
    override fun write(buf: PacketByteBuf) {
        buf.writeString(
            "{\n" +
                    "    \"version\": {\n" +
                    "        \"name\": \"nie wiem\",\n" +
                    "        \"protocol\": 736\n" +
                    "    },\n" +
                    "    \"players\": {\n" +
                    "        \"max\": 412,\n" +
                    "        \"online\": 56,\n" +
                    "        \"sample\": [\n" +
                    "            {\n" +
                    "                \"name\": \"brrr\",\n" +
                    "                \"id\": \"4566e69f-c907-48ee-8d71-d7ba5aa00d20\"\n" +
                    "            }\n" +
                    "        ]\n" +
                    "    },\t\n" +
                    "    \"description\": {\n" +
                    "        \"text\": \"jeszcze jak\"\n" +
                    "    },\n" +
                    "}"
        )
    }
}