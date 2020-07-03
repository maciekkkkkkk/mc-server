package mpeciakk

class ServerListPacket : S2CPacket(0x00) {
    override fun write(buf: PacketByteBuf) {
        buf.writeString(
            "{\n" +
                    "    \"version\": {\n" +
                    "        \"name\": \"1.16.1\",\n" +
                    "        \"protocol\": 736\n" +
                    "    },\n" +
                    "    \"players\": {\n" +
                    "        \"max\": 100,\n" +
                    "        \"online\": 5,\n" +
                    "        \"sample\": [\n" +
                    "            {\n" +
                    "                \"name\": \"thinkofdeath\",\n" +
                    "                \"id\": \"4566e69f-c907-48ee-8d71-d7ba5aa00d20\"\n" +
                    "            }\n" +
                    "        ]\n" +
                    "    },\t\n" +
                    "    \"description\": {\n" +
                    "        \"text\": \"Hello world\"\n" +
                    "    },\n" +
                    "    \"favicon\": \"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAOEAAADhCAMAAAAJbSJIAAAAA1BMVEUAAACnej3aAAAASElEQVR4nO3BgQAAAADDoPlTX+AIVQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADwDcaiAAFXD1ujAAAAAElFTkSuQmCC\"\n" +
                    "}"
        )
    }
}